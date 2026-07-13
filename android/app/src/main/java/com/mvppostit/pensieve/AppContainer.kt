package com.mvppostit.pensieve

import android.content.Context
import androidx.room.Room
import com.mvppostit.pensieve.data.local.PensieveDatabase
import com.mvppostit.pensieve.data.repository.ReminderRepository
import com.mvppostit.pensieve.reminders.ReminderManager

/**
 * Construye y conserva las dependencias compartidas de toda la aplicación.
 *
 * Esta es inyección manual: es explícita y fácil de seguir mientras Pensieve
 * tiene pocas dependencias. Una sola instancia de [PensieveDatabase] evita
 * crear conexiones a la base de datos cada vez que se muestra la pantalla.
 */
class AppContainer(context: Context) {

    private val database = Room.databaseBuilder(
        context.applicationContext,
        PensieveDatabase::class.java,
        DATABASE_NAME,
    ).build()

    private val reminderRepository = ReminderRepository(database.reminderDao())

    /** Dependencia de alto nivel que podrán usar pantalla, voz o widget. */
    val reminderManager = ReminderManager(reminderRepository)

    private companion object {
        const val DATABASE_NAME = "pensieve.db"
    }
}
