package com.mvppostit.pensieve.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mvppostit.pensieve.PensieveApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Recibe la acción Hecho de una notificación sin abrir la interfaz.
 *
 * Un BroadcastReceiver debe terminar pronto. [goAsync] permite completar la
 * operación de Room en segundo plano antes de informar a Android de que la
 * recepción ha finalizado.
 */
class CompleteReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COMPLETE_REMINDER) return

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, INVALID_REMINDER_ID)
        if (reminderId == INVALID_REMINDER_ID) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val application = context.applicationContext as PensieveApplication
                application.appContainer.reminderManager.completeReminder(reminderId)
            } finally {
                // Android puede liberar el receptor cuando termina esta llamada.
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE_REMINDER =
            "com.mvppostit.pensieve.action.COMPLETE_REMINDER"
        const val EXTRA_REMINDER_ID = "reminder_id"

        private const val INVALID_REMINDER_ID = -1L
    }
}
