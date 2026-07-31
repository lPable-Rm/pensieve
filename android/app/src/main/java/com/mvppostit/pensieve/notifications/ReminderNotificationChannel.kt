package com.mvppostit.pensieve.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.mvppostit.pensieve.R

/**
 * Define el canal que Android usará para los recordatorios activos.
 *
 * El identificador debe mantenerse estable: Android guarda en él las
 * preferencias que la persona elija desde Ajustes.
 */
object ReminderNotificationChannel {

    const val CHANNEL_ID = "active_reminders"

    /**
     * Crea el canal si todavía no existe.
     *
     * IMPORTANCE_DEFAULT permite que el recordatorio pueda aparecer en la
     * pantalla de bloqueo. El sonido y la vibración se desactivan de forma
     * explícita para mantener una notificación silenciosa.
     */
    fun create(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(
                R.string.reminder_notification_channel_description,
            )
            // El canal necesita una importancia visible, pero no debe interrumpir.
            setSound(null, null)
            enableVibration(false)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
