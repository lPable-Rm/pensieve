package com.mvppostit.pensieve.voice

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.mvppostit.pensieve.PensieveApplication
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.notifications.VoiceCaptureNotificationChannel
import com.mvppostit.pensieve.notifications.canPostReminderNotifications
import com.mvppostit.pensieve.widget.VoiceCaptureWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Ejecuta una captura de voz visible cuando el widget inicia el servicio.
 *
 * El servicio no tiene una copia de Room ni de la lógica de notificaciones.
 * Reutiliza [OnDeviceVoiceRecognizer] y el [ReminderManager] compartido de
 * [PensieveApplication], manteniendo una única fuente de verdad para las notas.
 */
class VoiceCaptureService : Service() {

    // Estas operaciones siempre se ejecutan en el hilo principal de Android.
    private val mainHandler = Handler(Looper.getMainLooper())

    // El scope permite esperar a Room sin dejar una corrutina huérfana al salir.
    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate,
    )

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private var voiceRecognizer: OnDeviceVoiceRecognizer? = null
    private var timeoutJob: Job? = null
    private var saveJob: Job? = null

    // Impide iniciar una segunda sesión o procesar callbacks después de limpiar.
    private var foregroundStarted = false
    private var cleanupStarted = false
    private var saveStarted = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_CAPTURE -> startCaptureIfNeeded()
            ACTION_CANCEL_CAPTURE -> cancelCaptureIfPossible()
            else -> finishCapture()
        }

        // Android no debe reconstruir una captura que el usuario ya terminó.
        return START_NOT_STICKY
    }

    /**
     * Cancela solo la parte de escucha.
     *
     * Cuando Room ya está guardando, una orden Cancelar antigua no puede
     * deshacer el resultado aceptado ni cancelar la corrutina de persistencia.
     */
    private fun cancelCaptureIfPossible() {
        if (saveStarted) return

        finishCapture()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        // El mismo método sirve para una cancelación normal y para una
        // destrucción externa: por eso la limpieza debe ser idempotente.
        finishCapture()
        mainHandler.removeCallbacksAndMessages(null)
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Promociona el servicio antes de tocar el micrófono.
     *
     * El widget puede enviar dos pulsaciones rápidas. [foregroundStarted] y
     * [cleanupStarted] hacen que la segunda orden no cree otra sesión.
     */
    private fun startCaptureIfNeeded() {
        if (foregroundStarted || cleanupStarted) return

        try {
            startForeground(
                CAPTURE_NOTIFICATION_ID,
                createForegroundNotification(R.string.voice_listening),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
            foregroundStarted = true
        } catch (_: SecurityException) {
            // Una intención antigua puede llegar después de que Android haya
            // revocado un permiso. Fallamos sin dejar el servicio activo.
            VoiceCaptureWidgetProvider.updateAll(applicationContext)
            showGenericFailureNotification()
            finishCapture()
            return
        }

        // La comprobación ocurre después de ser visible y antes de abrir el
        // micrófono, que es el orden exigido por el flujo del servicio.
        startRecognitionIfPossible()
    }

    /** Valida los requisitos actuales y arranca el reconocedor local. */
    private fun startRecognitionIfPossible() {
        if (cleanupStarted) return

        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            failAndStop(refreshWidget = true)
            return
        }

        // Si las notificaciones normales están bloqueadas, una nota guardada
        // desde el widget terminaría siendo invisible para la persona.
        if (!canPostReminderNotifications()) {
            failAndStop(refreshWidget = true)
            return
        }

        // El PendingIntent puede haberse creado antes de que se bloqueara el
        // canal temporal. No escuchamos si la sesión no puede ser visible.
        if (!VoiceCaptureNotificationChannel.isAvailable(this)) {
            failAndStop(refreshWidget = true)
            return
        }

        val recognizer = OnDeviceVoiceRecognizer(
            context = applicationContext,
            onEvent = ::handleRecognitionEvent,
        )

        if (!recognizer.isAvailable()) {
            recognizer.destroy()
            failAndStop()
            return
        }

        voiceRecognizer = recognizer

        // El timeout no utiliza parciales: si no llega un resultado final en
        // un minuto, la sesión termina sin crear ninguna nota.
        timeoutJob = serviceScope.launch {
            delay(CAPTURE_TIMEOUT_MILLIS)
            failAndStop()
        }

        // OnDeviceVoiceRecognizer exige ejecutarse en el hilo principal; este
        // método se llama desde el ciclo de vida principal del servicio.
        recognizer.start()
    }

    /**
     * Traduce los eventos del reconocedor a las únicas decisiones del servicio.
     * Los parciales nunca se guardan y tampoco se incluyen en notificaciones.
     */
    private fun handleRecognitionEvent(event: VoiceRecognitionEvent) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // SpeechRecognizer normalmente ya entrega aquí sus callbacks, pero
            // esta defensa conserva el contrato del reconocedor si Android
            // cambia el hilo de una implementación concreta.
            mainHandler.post { handleRecognitionEvent(event) }
            return
        }

        if (cleanupStarted) return

        when (event) {
            VoiceRecognitionEvent.ListeningStarted,
            VoiceRecognitionEvent.Processing,
            is VoiceRecognitionEvent.PartialResult,
            -> Unit

            is VoiceRecognitionEvent.FinalResult -> saveFinalResult(event.text)
            is VoiceRecognitionEvent.Failure -> failAndStop()
        }
    }

    /** Acepta como máximo un resultado final y lo guarda mediante Room. */
    private fun saveFinalResult(text: String) {
        if (cleanupStarted || saveStarted) return

        val normalizedText = text.trim()
        if (normalizedText.isEmpty()) {
            failAndStop()
            return
        }

        // Este bloqueo se activa antes de lanzar la corrutina. Así, dos
        // callbacks finales consecutivos no pueden insertar dos recordatorios.
        saveStarted = true
        // El resultado ya fue aceptado: liberar escucha y timeout antes de
        // esperar a Room evita mantener el micrófono abierto durante el guardado.
        stopListeningResources()
        updateForegroundNotification(
            textResId = R.string.voice_saving,
            showCancelAction = false,
        )

        saveJob = serviceScope.launch {
            try {
                // Todas las pantallas y servicios usan el mismo ReminderManager.
                // Room se actualiza antes de publicar la notificación normal.
                (application as PensieveApplication)
                    .appContainer
                    .reminderManager
                    .createReminder(normalizedText)
            } catch (_: Exception) {
                // No mostramos la excepción ni el texto capturado. Solo damos
                // una indicación genérica si Android permite notificarla.
                if (currentCoroutineContext().isActive) {
                    showGenericFailureNotification()
                }
            } finally {
                // Aquí ya no hay otro guardado pendiente: retiramos la sesión
                // temporal aunque Room haya respondido con un error.
                finishCapture(cancelSaveJob = false)
                saveJob = null
            }
        }
    }

    /** Termina una sesión que no puede continuar y evita guardar datos. */
    private fun failAndStop(refreshWidget: Boolean = false) {
        if (cleanupStarted || saveStarted) return

        if (refreshWidget) {
            // El siguiente toque debe volver a elegir entre servicio y
            // fallback usando el estado actual de Android.
            VoiceCaptureWidgetProvider.updateAll(applicationContext)
        }
        showGenericFailureNotification()
        finishCapture()
    }

    /** Libera exclusivamente los recursos usados mientras se escucha. */
    private fun stopListeningResources() {
        timeoutJob?.cancel()
        timeoutJob = null

        // destroy() cancela la sesión activa y libera el micrófono. El servicio
        // continúa vivo porque todavía puede estar esperando a Room.
        voiceRecognizer?.destroy()
        voiceRecognizer = null
    }

    /**
     * Único punto de salida del servicio.
     *
     * El orden es deliberado: primero se invalida la sesión, después se libera
     * el micrófono y finalmente se retira el foreground y se detiene Android.
     * [cancelSaveJob] permite que el propio guardado complete su limpieza sin
     * cancelarse a sí mismo en el bloque [finally].
     */
    private fun finishCapture(cancelSaveJob: Boolean = true) {
        if (cleanupStarted) return
        cleanupStarted = true

        stopListeningResources()

        if (cancelSaveJob) {
            saveJob?.cancel()
            saveJob = null
        }

        if (foregroundStarted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        } else {
            // También limpiamos una posible notificación si la promoción
            // quedó a medias por una excepción del sistema.
            notificationManager.cancel(CAPTURE_NOTIFICATION_ID)
        }

        stopSelf()
    }

    /** Actualiza el texto visible sin crear otra notificación foreground. */
    private fun updateForegroundNotification(
        textResId: Int,
        showCancelAction: Boolean = true,
    ) {
        if (!foregroundStarted || cleanupStarted) return

        notificationManager.notify(
            CAPTURE_NOTIFICATION_ID,
            createForegroundNotification(textResId, showCancelAction),
        )
    }

    /** Construye la notificación temporal sin incluir transcripciones. */
    private fun createForegroundNotification(
        textResId: Int,
        showCancelAction: Boolean = true,
    ): Notification {
        val builder = Notification.Builder(this, VoiceCaptureNotificationChannel.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(textResId))
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            // La captura es una sesión activa: Android debe mostrar su
            // notificación foreground sin esperar a una actualización posterior.
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)

        // Cancelar solo pertenece a la fase de escucha. Durante Guardando…
        // eliminamos la acción aunque Android conserve momentáneamente una
        // notificación anterior mientras procesa esta actualización.
        if (showCancelAction) {
            builder.addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_reminder_notification),
                    getString(R.string.cancel),
                    createCancelPendingIntent(),
                ).build(),
            )
        }

        return builder.build()
    }

    /** El botón de la notificación invoca directamente este mismo servicio. */
    private fun createCancelPendingIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            CANCEL_REQUEST_CODE,
            Intent(this, VoiceCaptureService::class.java).apply {
                action = ACTION_CANCEL_CAPTURE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /**
     * Informa del fallo sin exponer notas, transcripciones ni excepciones.
     * Si las notificaciones están bloqueadas, Android simplemente no lo muestra.
     */
    private fun showGenericFailureNotification() {
        val notification = Notification.Builder(
            this,
            VoiceCaptureNotificationChannel.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.voice_temporary_failure))
            .setAutoCancel(true)
            .setTimeoutAfter(FAILURE_NOTIFICATION_TIMEOUT_MILLIS)
            .build()

        try {
            notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // La notificación es solo informativa; el fallo principal ya está
            // controlado y nunca debe provocar un cierre inesperado.
        }
    }

    companion object {
        const val ACTION_START_CAPTURE =
            "com.mvppostit.pensieve.action.START_CAPTURE"
        const val ACTION_CANCEL_CAPTURE =
            "com.mvppostit.pensieve.action.CANCEL_CAPTURE"

        // Los IDs negativos quedan reservados para notificaciones internas y
        // no pueden coincidir con los IDs positivos asignados por Room.
        private const val CAPTURE_NOTIFICATION_ID = -2001
        private const val ERROR_NOTIFICATION_ID = -2003
        private const val CANCEL_REQUEST_CODE = 2002
        private const val CAPTURE_TIMEOUT_MILLIS = 60_000L
        private const val FAILURE_NOTIFICATION_TIMEOUT_MILLIS = 5_000L
    }
}
