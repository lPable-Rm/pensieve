package com.mvppostit.pensieve.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Indica si Android exige y ha concedido el permiso de notificaciones. */
internal fun Context.hasReminderNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Comprueba la capacidad real de mostrar los recordatorios de Pensieve.
 *
 * Además del permiso, respeta si la persona ha bloqueado las notificaciones
 * de la aplicación o el canal concreto desde los ajustes de Android.
 */
internal fun Context.canPostReminderNotifications(): Boolean {
    if (!hasReminderNotificationPermission()) return false

    val notificationManager = getSystemService(NotificationManager::class.java)
    if (!notificationManager.areNotificationsEnabled()) return false

    val channel = notificationManager.getNotificationChannel(
        ReminderNotificationChannel.CHANNEL_ID,
    ) ?: return false

    return channel.importance != NotificationManager.IMPORTANCE_NONE
}
