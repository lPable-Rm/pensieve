package com.mvppostit.pensieve.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una fila de la tabla local de recordatorios.
 *
 * Room usa esta clase para guardar y recuperar los datos. Mantenemos solo la
 * información necesaria para el MVP: el contenido de la nota y el momento en
 * que se creó. Ese instante permitirá mostrar las notas más recientes primero.
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    // Room asigna este identificador al insertar un recordatorio nuevo.
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val createdAtMillis: Long,
)
