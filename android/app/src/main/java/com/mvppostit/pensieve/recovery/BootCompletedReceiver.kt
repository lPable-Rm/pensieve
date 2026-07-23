package com.mvppostit.pensieve.recovery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recibe el arranque normal del dispositivo y delega la reconciliación a
 * WorkManager, que puede ejecutar el trabajo fuera del tiempo limitado del receiver.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        // El receiver no consulta Room ni abre la interfaz: solo deja el trabajo
        // preparado y devuelve el control a Android inmediatamente.
        ReminderReconcileWorker.enqueueAfterBoot(context)
    }
}
