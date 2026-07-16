package com.mvppostit.pensieve.reminders

import com.mvppostit.pensieve.data.local.ReminderDao
import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.data.repository.ReminderRepository
import com.mvppostit.pensieve.notifications.ReminderNotifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ReminderManagerTest {

    @Test
    fun createReminder_persistsBeforePublishingTheAssignedId() = runBlocking {
        val fixture = createFixture()

        val reminder = fixture.manager.createReminder("Comprar pan")

        assertTrue(reminder.id > 0L)
        assertEquals(listOf("room_insert", "notification_publish"), fixture.events)
        assertEquals(listOf(reminder), fixture.notifier.publishedReminders)
    }

    @Test
    fun completeReminder_deletesBeforeCancelling() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Llamar al taller")
        fixture.events.clear()

        val completedReminder = fixture.manager.completeReminder(reminder.id)

        assertEquals(reminder, completedReminder)
        assertEquals(
            listOf("room_find", "room_delete", "notification_cancel"),
            fixture.events,
        )
        assertEquals(listOf(reminder.id), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun completeMissingReminder_stillCancelsAnOrphanNotification() = runBlocking {
        val fixture = createFixture()
        val missingReminderId = 42L

        val completedReminder = fixture.manager.completeReminder(missingReminderId)

        assertNull(completedReminder)
        assertEquals(listOf("room_find", "notification_cancel"), fixture.events)
        assertEquals(listOf(missingReminderId), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun databaseFailure_doesNotCancelTheNotification() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Conservar")
        fixture.events.clear()
        fixture.dao.failOnDelete = true

        try {
            fixture.manager.completeReminder(reminder.id)
            fail("La operación debía propagar el fallo de Room")
        } catch (_: IllegalStateException) {
            // El gestor no debe ocultar un fallo de la fuente de verdad.
        }

        assertEquals(listOf("room_find", "room_delete"), fixture.events)
        assertEquals(emptyList<Long>(), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun restoreReminder_persistsBeforePublishingAgain() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Recoger un paquete")
        val completedReminder = requireNotNull(fixture.manager.completeReminder(reminder.id))
        fixture.events.clear()
        fixture.notifier.publishedReminders.clear()

        fixture.manager.restoreReminder(completedReminder)

        assertEquals(listOf("room_restore", "notification_publish"), fixture.events)
        assertEquals(listOf(completedReminder), fixture.notifier.publishedReminders)
    }

    private fun createFixture(): Fixture {
        val events = mutableListOf<String>()
        val dao = FakeReminderDao(events)
        val notifier = RecordingReminderNotifier(events)
        val manager = ReminderManager(
            repository = ReminderRepository(dao),
            notificationPublisher = notifier,
        )
        return Fixture(events, dao, notifier, manager)
    }

    private data class Fixture(
        val events: MutableList<String>,
        val dao: FakeReminderDao,
        val notifier: RecordingReminderNotifier,
        val manager: ReminderManager,
    )

    private class FakeReminderDao(
        private val events: MutableList<String>,
    ) : ReminderDao {
        private val reminders = MutableStateFlow<List<ReminderEntity>>(emptyList())
        var failOnDelete = false

        override fun observeAll(): Flow<List<ReminderEntity>> = reminders

        override suspend fun insert(reminder: ReminderEntity): Long {
            events += "room_insert"
            val reminderId = (reminders.value.maxOfOrNull(ReminderEntity::id) ?: 0L) + 1L
            reminders.value = reminders.value + reminder.copy(id = reminderId)
            return reminderId
        }

        override suspend fun insertOrReplace(reminder: ReminderEntity) {
            events += "room_restore"
            reminders.value = reminders.value.filterNot { it.id == reminder.id } + reminder
        }

        override suspend fun deleteById(reminderId: Long): Int {
            events += "room_delete"
            if (failOnDelete) throw IllegalStateException("Fallo de Room simulado")

            val previousSize = reminders.value.size
            reminders.value = reminders.value.filterNot { it.id == reminderId }
            return previousSize - reminders.value.size
        }

        override suspend fun findById(reminderId: Long): ReminderEntity? {
            events += "room_find"
            return reminders.value.firstOrNull { it.id == reminderId }
        }
    }

    private class RecordingReminderNotifier(
        private val events: MutableList<String>,
    ) : ReminderNotifier {
        val publishedReminders = mutableListOf<ReminderEntity>()
        val cancelledReminderIds = mutableListOf<Long>()

        override fun publish(reminder: ReminderEntity) {
            events += "notification_publish"
            publishedReminders += reminder
        }

        override fun cancel(reminderId: Long) {
            events += "notification_cancel"
            cancelledReminderIds += reminderId
        }
    }
}
