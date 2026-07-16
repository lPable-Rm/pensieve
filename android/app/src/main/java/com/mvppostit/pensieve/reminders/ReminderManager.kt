package com.mvppostit.pensieve.reminders

import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.data.repository.ReminderRepository
import com.mvppostit.pensieve.notifications.ReminderNotificationPublisher
import kotlinx.coroutines.flow.Flow

/**
 * Coordina las acciones de alto nivel sobre un recordatorio.
 *
 * Room sigue siendo la fuente de verdad. Tras actualizar sus datos, este
 * gestor sincroniza la notificación asociada cuando la aplicación dispone de
 * un [ReminderNotificationPublisher].
 */
class ReminderManager(
    private val repository: ReminderRepository,
    private val notificationPublisher: ReminderNotificationPublisher? = null,
) {

    fun observeReminders(): Flow<List<ReminderEntity>> = repository.observeReminders()

    /**
     * Guarda primero en Room para obtener el id estable y después publica su
     * notificación asociada.
     */
    suspend fun createReminder(text: String): ReminderEntity {
        val reminder = repository.createReminder(text)
        notificationPublisher?.publish(reminder)
        return reminder
    }

    /**
     * Elimina la nota de Room y retira su notificación si existía.
     */
    suspend fun completeReminder(reminderId: Long): ReminderEntity? {
        val completedReminder = repository.completeReminder(reminderId) ?: return null
        notificationPublisher?.cancel(completedReminder.id)
        return completedReminder
    }

    /**
     * Restaura la fila original y vuelve a publicar su recordatorio.
     */
    suspend fun restoreReminder(reminder: ReminderEntity) {
        repository.restoreReminder(reminder)
        notificationPublisher?.publish(reminder)
    }
}
