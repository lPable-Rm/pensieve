package com.mvppostit.pensieve.widget

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.res.ColorStateList
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.mvppostit.pensieve.PensieveApplication
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.notifications.VoiceCaptureNotificationChannel
import com.mvppostit.pensieve.notifications.canPostReminderNotifications
import com.mvppostit.pensieve.voice.VoiceCaptureService
import com.mvppostit.pensieve.ui.theme.PaletteId
import com.mvppostit.pensieve.ui.theme.paletteColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Widget de acceso rápido para iniciar una captura de voz.
 *
 * El proveedor solo prepara la vista y el destino del clic. No reconoce voz,
 * no accede a Room y no guarda notas: esas responsabilidades pertenecen al
 * servicio foreground y al contenedor existente.
 */
class VoiceCaptureWidgetProvider : AppWidgetProvider() {

    /** Android llama aquí al instalar o actualizar una instancia del widget. */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        val applicationContext = context.applicationContext

        // DataStore se lee fuera del breve callback del BroadcastReceiver.
        // goAsync mantiene vivo el proceso hasta terminar esta actualización.
        widgetUpdateScope.launch {
            try {
                updateWidgets(
                    context = applicationContext,
                    appWidgetManager = appWidgetManager,
                    appWidgetIds = appWidgetIds,
                    paletteId = storedPalette(applicationContext),
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        /** Acción que el fallback entrega a MainActivity para iniciar la voz. */
        const val ACTION_PREPARE_WIDGET_VOICE =
            "com.mvppostit.pensieve.action.PREPARE_WIDGET_VOICE"

        // El alias no es una clase Kotlin: este nombre completo identifica el
        // componente privado que el PendingIntent del widget puede abrir.
        const val WIDGET_ENTRY_CLASS_NAME =
            "com.mvppostit.pensieve.WidgetVoiceEntry"

        /**
         * Actualiza todas las instancias instaladas, incluso si la lista está
         * vacía. Se vuelve a llamar cuando cambian permisos o canales.
         */
        fun updateAll(context: Context) {
            val applicationContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val provider = ComponentName(
                applicationContext,
                VoiceCaptureWidgetProvider::class.java,
            )
            val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)

            if (appWidgetIds.isEmpty()) return

            // El proveedor puede recibir una actualización fuera de MainActivity.
            // En ese caso leemos la misma preferencia del contenedor y después
            // reconstruimos todas las instancias con el color correcto.
            widgetUpdateScope.launch {
                updateWidgets(
                    applicationContext,
                    appWidgetManager,
                    appWidgetIds,
                    storedPalette(applicationContext),
                )
            }
        }

        /** Actualización inmediata después de elegir una paleta en la actividad. */
        fun updateAll(context: Context, paletteId: PaletteId) {
            val applicationContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val provider = ComponentName(
                applicationContext,
                VoiceCaptureWidgetProvider::class.java,
            )
            val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)
            updateWidgets(applicationContext, appWidgetManager, appWidgetIds, paletteId)
        }

        /**
         * Cambia el estado efímero que muestran todas las instancias instaladas.
         *
         * El estado no se persiste: si el proceso desaparece, el widget vuelve
         * correctamente a reposo y el servicio ya no puede estar escuchando.
         * La actualización parcial es síncrona y conserva la paleta aplicada.
         */
        fun setRecordingState(context: Context, recording: Boolean) {
            isRecording = recording
            val applicationContext = context.applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val provider = ComponentName(
                applicationContext,
                VoiceCaptureWidgetProvider::class.java,
            )
            val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)

            if (appWidgetIds.isEmpty()) return

            val remoteViews = RemoteViews(
                applicationContext.packageName,
                R.layout.widget_voice_capture,
            ).apply {
                applyInteractionState(applicationContext, recording)
            }
            appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, remoteViews)
        }

        /** Actualiza una o varias instancias sin crear estado propio del widget. */
        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
            paletteId: PaletteId,
        ) {
            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    createRemoteViews(context, paletteId),
                )
            }
        }

        /** Construye la vista y hace pulsable toda su superficie. */
        private fun createRemoteViews(
            context: Context,
            paletteId: PaletteId,
        ): RemoteViews {
            val palette = paletteColors(paletteId)
            val primaryContainer = palette.primaryContainer.toArgb()
            val onPrimaryContainer = palette.onPrimaryContainer.toArgb()

            return RemoteViews(
                context.packageName,
                R.layout.widget_voice_capture,
            ).apply {
                val recording = isRecording

                // El fondo permanece neutro. La paleta se concentra en el
                // control de voz, donde el par semántico sí garantiza contraste.
                setColorStateList(
                    R.id.widget_voice_capture_idle_mic_container,
                    "setBackgroundTintList",
                    ColorStateList.valueOf(primaryContainer),
                )
                setInt(
                    R.id.widget_voice_capture_idle_mic_icon,
                    "setColorFilter",
                    onPrimaryContainer,
                )
                setInt(
                    R.id.widget_voice_capture_idle_logo,
                    "setColorFilter",
                    onPrimaryContainer,
                )
                setInt(
                    R.id.widget_voice_capture_recording_logo,
                    "setColorFilter",
                    onPrimaryContainer,
                )

                applyInteractionState(context, recording)
            }
        }

        /** Aplica solo el estado que cambia al empezar o terminar la escucha. */
        private fun RemoteViews.applyInteractionState(
            context: Context,
            recording: Boolean,
        ) {
            setViewVisibility(
                R.id.widget_voice_capture_idle,
                if (recording) View.GONE else View.VISIBLE,
            )
            setViewVisibility(
                R.id.widget_voice_capture_recording,
                if (recording) View.VISIBLE else View.GONE,
            )
            setOnClickPendingIntent(
                R.id.widget_voice_capture_root,
                createClickPendingIntent(context, recording),
            )
            setContentDescription(
                R.id.widget_voice_capture_root,
                context.getString(
                    if (recording) {
                        R.string.widget_recording_description
                    } else {
                        R.string.widget_tap_to_speak
                    },
                ),
            )
        }

        /** Lee la paleta persistida desde el único contenedor de la aplicación. */
        private suspend fun storedPalette(context: Context): PaletteId =
            (context.applicationContext as PensieveApplication)
                .appContainer.appPreferences.colorPalette.first()

        /**
         * Elige el camino normal o el fallback según el estado actual.
         *
         * El PendingIntent puede quedar instalado durante un cambio de
         * permisos; el servicio vuelve a validar los requisitos al arrancar.
         */
        private fun createClickPendingIntent(
            context: Context,
            recording: Boolean,
        ): PendingIntent =
            if (recording) {
                PendingIntent.getService(
                    context,
                    CANCEL_REQUEST_CODE,
                    Intent(context, VoiceCaptureService::class.java).apply {
                        action = VoiceCaptureService.ACTION_CANCEL_CAPTURE
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            } else if (canStartCaptureDirectly(context)) {
                PendingIntent.getForegroundService(
                    context,
                    SERVICE_REQUEST_CODE,
                    Intent(context, VoiceCaptureService::class.java).apply {
                        action = VoiceCaptureService.ACTION_START_CAPTURE
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            } else {
                PendingIntent.getActivity(
                    context,
                    ACTIVITY_REQUEST_CODE,
                    Intent().apply {
                        component = ComponentName(
                            context.packageName,
                            WIDGET_ENTRY_CLASS_NAME,
                        )
                        action = ACTION_PREPARE_WIDGET_VOICE
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

        /** Comprueba solo los requisitos que el widget puede conocer sin escuchar. */
        private fun canStartCaptureDirectly(context: Context): Boolean =
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED &&
                context.canPostReminderNotifications() &&
                VoiceCaptureNotificationChannel.isAvailable(context)

        @Volatile
        private var isRecording = false

        private val widgetUpdateScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        private const val SERVICE_REQUEST_CODE = 3001
        private const val ACTIVITY_REQUEST_CODE = 3002
        private const val CANCEL_REQUEST_CODE = 3003
    }
}
