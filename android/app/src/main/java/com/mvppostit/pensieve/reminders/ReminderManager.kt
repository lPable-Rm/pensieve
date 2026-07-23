package com.mvppostit.pensieve.reminders

import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.data.repository.ReminderRepository
import com.mvppostit.pensieve.notifications.ReminderNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordina las acciones de alto nivel sobre un recordatorio.
 *
 * Room sigue siendo la fuente de verdad. Tras actualizar sus datos, este
 * gestor sincroniza la notificación asociada mediante [ReminderNotifier].
 */
class ReminderManager(
    private val repository: ReminderRepository,
    private val notificationPublisher: ReminderNotifier,
) {

    private val mutationMutex = Mutex()

    fun observeReminders(): Flow<List<ReminderEntity>> = repository.observeReminders()

    /**
     * Guarda primero en Room para obtener el id estable y después publica su
     * notificación asociada.
     */
    suspend fun createReminder(text: String): ReminderEntity {
        return mutationMutex.withLock {
            val reminder = repository.createReminder(text)
            notificationPublisher.publish(reminder)
            reminder
        }
    }

    /**
     * Elimina la nota de Room y retira su notificación si existía.
     */
    suspend fun completeReminder(reminderId: Long): ReminderEntity? {
        return mutationMutex.withLock {
            val completedReminder = repository.completeReminder(reminderId)

            // Cancelar también cuando la fila ya no existe limpia una posible
            // notificación huérfana tras una interrupción entre Room y Android.
            notificationPublisher.cancel(reminderId)
            completedReminder
        }
    }

    /**
     * Restaura la fila original y vuelve a publicar su recordatorio.
     */
    suspend fun restoreReminder(reminder: ReminderEntity) {
        mutationMutex.withLock {
            repository.restoreReminder(reminder)
            notificationPublisher.publish(reminder)
        }
    }

    suspend fun reconcileNotifications() {
        mutationMutex.withLock {
            val reminders = repository.observeReminders().first()
            val activeReminderIds = notificationPublisher.activeReminderIds()
            val roomReminderIds = reminders.mapTo(mutableSetOf(), ReminderEntity::id)

            reminders
                .filterNot { reminder -> reminder.id in activeReminderIds }
                .forEach(notificationPublisher::publish)

            activeReminderIds
                .filterNot { reminderId -> reminderId in roomReminderIds }
                .forEach(notificationPublisher::cancel)
        }
    }
}
