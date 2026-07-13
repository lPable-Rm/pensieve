package com.mvppostit.pensieve.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de datos privada de Pensieve.
 *
 * La versión empieza en 1. Cada cambio futuro en las tablas requerirá una
 * migración y un aumento de esta versión; los esquemas exportados nos ayudarán
 * a comprobar que esa evolución conserva los datos de las personas usuarias.
 */
@Database(
    entities = [ReminderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PensieveDatabase : RoomDatabase() {

    /** Room proporciona la implementación de este acceso al DAO en compilación. */
    abstract fun reminderDao(): ReminderDao
}
