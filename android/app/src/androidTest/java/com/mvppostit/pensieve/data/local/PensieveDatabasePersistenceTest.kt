package com.mvppostit.pensieve.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PensieveDatabasePersistenceTest {

    @Test
    fun reminder_isAvailableAfterClosingAndReopeningTheDatabase() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "pensieve-persistence-test.db"
        context.deleteDatabase(databaseName)

        try {
            val firstDatabase = Room.databaseBuilder(
                context,
                PensieveDatabase::class.java,
                databaseName,
            )
                .allowMainThreadQueries()
                .build()

            val reminderId = try {
                firstDatabase.reminderDao().insert(
                    ReminderEntity(text = "Persistente", createdAtMillis = 100),
                )
            } finally {
                firstDatabase.close()
            }

            val reopenedDatabase = Room.databaseBuilder(
                context,
                PensieveDatabase::class.java,
                databaseName,
            )
                .allowMainThreadQueries()
                .build()

            try {
                val storedReminder = reopenedDatabase.reminderDao().observeAll().first().single()
                assertEquals(reminderId, storedReminder.id)
                assertEquals("Persistente", storedReminder.text)
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }
}
