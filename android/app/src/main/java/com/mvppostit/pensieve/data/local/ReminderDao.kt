package com.mvppostit.pensieve.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Declara las operaciones SQL de la tabla de recordatorios.
 *
 * Room genera la implementación de esta interfaz en compilación. El [Flow]
 * permite que las capas superiores reciban automáticamente una lista nueva
 * cada vez que cambien los datos almacenados.
 */
@Dao
interface ReminderDao {

    /**
     * Observa las notas activas, desde la más nueva a la más antigua.
     *
     * El segundo criterio evita que el orden cambie si dos notas se crean en
     * el mismo milisegundo.
     */
    @Query(
        """
        SELECT * FROM reminders
        ORDER BY createdAtMillis DESC, id DESC
        """,
    )
    fun observeAll(): Flow<List<ReminderEntity>>

    /** Inserta una nota nueva y devuelve el identificador creado por Room. */
    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    /**
     * Restaura una nota conservando su identificador original.
     *
     * Solo se usará al deshacer una finalización reciente. REPLACE hace que
     * esta restauración siga siendo segura aunque la fila ya existiera.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(reminder: ReminderEntity)

    /** Eliminar una nota es equivalente a marcarla como completada en el MVP. */
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteById(reminderId: Long): Int

    /** Busca una nota concreta dentro de las operaciones coordinadas del DAO. */
    @Query("SELECT * FROM reminders WHERE id = :reminderId LIMIT 1")
    suspend fun findById(reminderId: Long): ReminderEntity?

    /**
     * Devuelve la fila solo si esta llamada ha conseguido eliminarla.
     *
     * La transacción evita que dos orígenes —por ejemplo, la pantalla y una
     * notificación— completen la misma nota y ofrezcan dos veces Deshacer.
     */
    @Transaction
    suspend fun completeById(reminderId: Long): ReminderEntity? {
        val reminder = findById(reminderId) ?: return null
        return reminder.takeIf { deleteById(reminderId) == 1 }
    }
}
