package com.mvppostit.pensieve.data.repository

import com.mvppostit.pensieve.data.local.ReminderDao
import com.mvppostit.pensieve.data.local.ReminderEntity
import kotlinx.coroutines.flow.Flow

/**
 * Punto de acceso a los recordatorios almacenados localmente.
 *
 * El repositorio protege al resto de la aplicación de los detalles de Room y
 * se limita a los datos. ReminderManager coordina después las notificaciones.
 */
class ReminderRepository(
    private val reminderDao: ReminderDao,
) {

    fun observeReminders(): Flow<List<ReminderEntity>> = reminderDao.observeAll()

    /** Crea una nota y devuelve la copia que ya incluye el id asignado por Room. */
    suspend fun createReminder(text: String): ReminderEntity {
        val normalizedText = text.trim()
        require(normalizedText.isNotEmpty()) { "El texto del recordatorio no puede estar vacío" }

        val reminder = ReminderEntity(
            text = normalizedText,
            createdAtMillis = System.currentTimeMillis(),
        )
        val reminderId = reminderDao.insert(reminder)

        return reminder.copy(id = reminderId)
    }

    suspend fun completeReminder(reminderId: Long): ReminderEntity? =
        reminderDao.completeById(reminderId)

    /** Restaura exactamente la misma fila al pulsar Deshacer. */
    suspend fun restoreReminder(reminder: ReminderEntity) {
        reminderDao.insertOrReplace(reminder)
    }
}
