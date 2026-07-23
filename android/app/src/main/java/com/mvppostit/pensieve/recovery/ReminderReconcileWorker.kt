package com.mvppostit.pensieve.recovery

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mvppostit.pensieve.PensieveApplication
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

/**
 * Reconciliador periódico de las notas de Room y sus notificaciones.
 *
 * El worker no contiene reglas de negocio: obtiene el mismo
 * [com.mvppostit.pensieve.reminders.ReminderManager] que utiliza la interfaz
 * y le pide una única reconciliación.
 */
class ReminderReconcileWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val application = applicationContext as PensieveApplication
            application.appContainer.reminderManager.reconcileNotifications()
            Result.success()
        } catch (exception: Exception) {
            // La cancelación pertenece al ciclo de vida de WorkManager y no
            // debe convertirse en un reintento artificial.
            if (exception is CancellationException) throw exception

            // Si Android o Room fallan temporalmente, WorkManager reintentará
            // la misma reconciliación según sus propias reglas.
            Result.retry()
        }
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "reminder_notification_reconciliation"
        private const val PERIODIC_INTERVAL_MINUTES = 15L

        /**
         * Registra una única tarea periódica sin restricciones adicionales.
         * KEEP evita reemplazar una programación ya existente al abrir la app.
         */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderReconcileWorker>(
                PERIODIC_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
            ).build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
        }

        /**
         * Encola una ejecución puntual después del arranque del dispositivo.
         * KEEP evita duplicar el trabajo si Android entrega el evento más de una vez.
         */
        fun enqueueAfterBoot(context: Context) {
            val request = OneTimeWorkRequestBuilder<ReminderReconcileWorker>().build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(
                    BOOT_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    request,
                )
        }

        private const val BOOT_WORK_NAME = "reminder_notification_reconciliation_after_boot"
    }
}
