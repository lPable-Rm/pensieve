package com.mvppostit.pensieve.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.mvppostit.pensieve.R

/** Canal separado para la sesión temporal de captura por voz. */
object VoiceCaptureNotificationChannel {

    const val CHANNEL_ID = "voice_capture"

    /**
     * La sesión debe ser visible, pero no necesita sonido ni vibración.
     * Android conserva las preferencias de este canal entre actualizaciones.
     */
    fun create(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.voice_capture_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(
                R.string.voice_capture_notification_channel_description,
            )
            setShowBadge(false)
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    /** Comprueba si el canal temporal sigue visible para la persona. */
    fun isAvailable(context: Context): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (!notificationManager.areNotificationsEnabled()) return false

        val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
        return channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
    }
}
