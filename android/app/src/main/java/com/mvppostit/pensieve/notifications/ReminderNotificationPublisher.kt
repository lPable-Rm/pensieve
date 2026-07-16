package com.mvppostit.pensieve.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.mvppostit.pensieve.MainActivity
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.data.local.ReminderEntity

/**
 * Publica en Android la notificación de un recordatorio activo.
 *
 * Esta clase no guarda ni elimina notas: Room continúa siendo la fuente de
 * verdad. Su única responsabilidad es transformar un [ReminderEntity] en una
 * notificación del sistema.
 */
class ReminderNotificationPublisher(
    private val context: Context,
) : ReminderNotifier {

    // NotificationManager es el servicio de Android que muestra y retira
    // notificaciones. Con minSdk 31 podemos usar su API nativa directamente.
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    /**
     * Muestra una notificación silenciosa para el recordatorio indicado.
     *
     * Si Android no permite mostrarla, Room conserva el recordatorio como
     * fuente de verdad y la interfaz mantiene el borrador cuando corresponde.
     */
    override fun publish(reminder: ReminderEntity) {
        if (!context.canPostReminderNotifications()) return

        val notification = Notification.Builder(
            context,
            ReminderNotificationChannel.CHANNEL_ID,
        )
            // Android exige un icono pequeño para cada notificación.
            .setSmallIcon(R.drawable.ic_reminder_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(reminder.text)
            // Abrir Pensieve no completa la nota: sigue activa hasta pulsar Hecho.
            .setContentIntent(createOpenAppPendingIntent())
            .setAutoCancel(false)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(context, R.drawable.ic_reminder_notification),
                    context.getString(R.string.complete_reminder_action),
                    createCompleteReminderPendingIntent(reminder.id),
                ).build(),
            )
            .build()

        // NotificationManager identifica las notificaciones con un Int.
        // Los ids de Room son Long, pero para el volumen del MVP esta
        // conversión nos permite mantener una notificación por recordatorio.
        notificationManager.notify(reminder.id.toInt(), notification)
    }

    /**
     * Retira la notificación asociada a un recordatorio completado.
     */
    override fun cancel(reminderId: Long) {
        notificationManager.cancel(reminderId.toInt())
    }

    /**
     * Crea la acción que abre la actividad principal al tocar la notificación.
     *
     * El Intent es explícito, por lo que solo puede abrir un componente de
     * Pensieve. También es inmutable para que ninguna otra aplicación cambie
     * la acción que Android ejecutará.
     */
    private fun createOpenAppPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE,
        )

    /**
     * Crea la acción Hecho que Android enviará al receptor interno.
     *
     * Cada recordatorio usa su id como código de petición, para que el
     * PendingIntent conserve el id correcto incluso con varias notas activas.
     */
    private fun createCompleteReminderPendingIntent(reminderId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            Intent(context, CompleteReminderReceiver::class.java).apply {
                action = CompleteReminderReceiver.ACTION_COMPLETE_REMINDER
                putExtra(CompleteReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private companion object {
        const val OPEN_APP_REQUEST_CODE = 0
    }
}

/** Contrato mínimo que permite coordinar y probar las notificaciones. */
interface ReminderNotifier {
    fun publish(reminder: ReminderEntity)

    fun cancel(reminderId: Long)
}
