package com.mvppostit.pensieve.reminders

import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

/**
 * Coordina las acciones de alto nivel sobre un recordatorio.
 *
 * En la fase actual delega únicamente en el repositorio. Cuando añadamos
 * notificaciones, este será el punto para ejecutar de forma coordinada la
 * operación de datos y la publicación o cancelación de la notificación.
 */
class ReminderManager(
    private val repository: ReminderRepository,
) {

    fun observeReminders(): Flow<List<ReminderEntity>> = repository.observeReminders()

    suspend fun createReminder(text: String): ReminderEntity = repository.createReminder(text)

    suspend fun completeReminder(reminderId: Long): ReminderEntity? =
        repository.completeReminder(reminderId)

    suspend fun restoreReminder(reminder: ReminderEntity) {
        repository.restoreReminder(reminder)
    }
}
