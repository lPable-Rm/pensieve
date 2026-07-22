package com.mvppostit.pensieve.widget

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.RemoteViews
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.notifications.VoiceCaptureNotificationChannel
import com.mvppostit.pensieve.notifications.canPostReminderNotifications
import com.mvppostit.pensieve.voice.VoiceCaptureService

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
        updateWidgets(context, appWidgetManager, appWidgetIds)
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

            updateWidgets(applicationContext, appWidgetManager, appWidgetIds)
        }

        /** Actualiza una o varias instancias sin crear estado propio del widget. */
        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
        ) {
            appWidgetIds.forEach { appWidgetId ->
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    createRemoteViews(context),
                )
            }
        }

        /** Construye la vista y hace pulsable toda su superficie. */
        private fun createRemoteViews(context: Context): RemoteViews =
            RemoteViews(
                context.packageName,
                R.layout.widget_voice_capture,
            ).apply {
                setOnClickPendingIntent(
                    R.id.widget_voice_capture_root,
                    createClickPendingIntent(context),
                )
            }

        /**
         * Elige el camino normal o el fallback según el estado actual.
         *
         * El PendingIntent puede quedar instalado durante un cambio de
         * permisos; el servicio vuelve a validar los requisitos al arrancar.
         */
        private fun createClickPendingIntent(context: Context): PendingIntent =
            if (canStartCaptureDirectly(context)) {
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

        private const val SERVICE_REQUEST_CODE = 3001
        private const val ACTIVITY_REQUEST_CODE = 3002
    }
}
