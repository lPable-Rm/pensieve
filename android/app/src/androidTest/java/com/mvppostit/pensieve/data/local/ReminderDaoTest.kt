package com.mvppostit.pensieve.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var database: PensieveDatabase
    private lateinit var reminderDao: ReminderDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            PensieveDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()
        reminderDao = database.reminderDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun observeAll_ordersNewestFirstAndUsesIdAsTieBreaker() = runBlocking {
        val oldestId = reminderDao.insert(
            ReminderEntity(text = "Primero", createdAtMillis = 100),
        )
        val firstRecentId = reminderDao.insert(
            ReminderEntity(text = "Segundo", createdAtMillis = 200),
        )
        val lastRecentId = reminderDao.insert(
            ReminderEntity(text = "Tercero", createdAtMillis = 200),
        )

        val storedIds = reminderDao.observeAll().first().map(ReminderEntity::id)

        assertEquals(listOf(lastRecentId, firstRecentId, oldestId), storedIds)
    }

    @Test
    fun completeById_returnsTheDeletedRowOnlyOnce() = runBlocking {
        val reminderId = reminderDao.insert(
            ReminderEntity(text = "Completar", createdAtMillis = 100),
        )

        val completedReminder = reminderDao.completeById(reminderId)
        val secondCompletion = reminderDao.completeById(reminderId)

        assertEquals(reminderId, completedReminder?.id)
        assertNull(secondCompletion)
        assertEquals(emptyList<ReminderEntity>(), reminderDao.observeAll().first())
    }

    @Test
    fun insertOrReplace_restoresTheSameRow() = runBlocking {
        val reminderId = reminderDao.insert(
            ReminderEntity(text = "Restaurar", createdAtMillis = 100),
        )
        val completedReminder = requireNotNull(reminderDao.completeById(reminderId))

        reminderDao.insertOrReplace(completedReminder)

        assertEquals(listOf(completedReminder), reminderDao.observeAll().first())
    }
}
