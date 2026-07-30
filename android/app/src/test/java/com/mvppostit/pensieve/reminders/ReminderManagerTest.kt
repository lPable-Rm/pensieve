package com.mvppostit.pensieve.reminders

import com.mvppostit.pensieve.data.local.ReminderDao
import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.data.repository.ReminderRepository
import com.mvppostit.pensieve.notifications.ReminderNotifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun reconcileNotifications_republishesReminderWithoutNotification() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Revisar el correo")
        fixture.events.clear()
        fixture.notifier.publishedReminders.clear()

        fixture.manager.reconcileNotifications()

        assertEquals(
            listOf("notification_active_ids", "notification_publish"),
            fixture.events,
        )
        assertEquals(listOf(reminder), fixture.notifier.publishedReminders)
        assertEquals(emptyList<Long>(), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun reconcileNotifications_cancelsNotificationWithoutReminder() = runBlocking {
        val fixture = createFixture()
        val orphanReminderId = 42L
        fixture.notifier.activeReminderIds += orphanReminderId

        fixture.manager.reconcileNotifications()

        assertEquals(
            listOf("notification_active_ids", "notification_cancel"),
            fixture.events,
        )
        assertEquals(emptyList<ReminderEntity>(), fixture.notifier.publishedReminders)
        assertEquals(listOf(orphanReminderId), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun reconcileNotifications_ignoresMatchingReminderAndNotification() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Pagar la factura")
        fixture.events.clear()
        fixture.notifier.publishedReminders.clear()
        fixture.notifier.activeReminderIds += reminder.id

        fixture.manager.reconcileNotifications()

        assertEquals(listOf("notification_active_ids"), fixture.events)
        assertEquals(emptyList<ReminderEntity>(), fixture.notifier.publishedReminders)
        assertEquals(emptyList<Long>(), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun reconcileNotifications_repairsSeveralDifferencesAtOnce() = runBlocking {
        val fixture = createFixture()
        val existingReminder = fixture.manager.createReminder("Nota conservada")
        val missingNotificationReminder = fixture.manager.createReminder("Nota a recuperar")
        val orphanReminderId = 99L
        fixture.events.clear()
        fixture.notifier.publishedReminders.clear()
        fixture.notifier.activeReminderIds += existingReminder.id
        fixture.notifier.activeReminderIds += orphanReminderId

        fixture.manager.reconcileNotifications()

        assertEquals(
            listOf("notification_active_ids", "notification_publish", "notification_cancel"),
            fixture.events,
        )
        assertEquals(
            listOf(missingNotificationReminder),
            fixture.notifier.publishedReminders,
        )
        assertEquals(listOf(orphanReminderId), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun completeAndReconcile_doNotEnterTheFakesSimultaneously() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Complete with reconciliation")
        fixture.events.clear()

        val deleteStarted = CompletableDeferred<Unit>()
        val releaseDelete = CompletableDeferred<Unit>()
        val reconcileStarted = CompletableDeferred<Unit>()
        fixture.dao.deleteStarted = deleteStarted
        fixture.dao.releaseDelete = releaseDelete
        fixture.notifier.activeIdsRequested = reconcileStarted

        val completeJob = async(start = CoroutineStart.UNDISPATCHED) {
            fixture.manager.completeReminder(reminder.id)
        }
        deleteStarted.await()

        val reconcileJob = launch(start = CoroutineStart.UNDISPATCHED) {
            fixture.manager.reconcileNotifications()
        }

        try {
            assertFalse(reconcileStarted.isCompleted)
        } finally {
            releaseDelete.complete(Unit)
        }

        assertEquals(reminder, completeJob.await())
        reconcileJob.join()
        assertEquals(
            listOf("room_find", "room_delete", "notification_cancel", "notification_active_ids"),
            fixture.events,
        )
        assertEquals(emptyList<ReminderEntity>(), fixture.dao.currentReminders())
        assertEquals(listOf(reminder.id), fixture.notifier.cancelledReminderIds)
    }

    @Test
    fun restoreAndReconcile_doNotEnterTheFakesSimultaneously() = runBlocking {
        val fixture = createFixture()
        val reminder = fixture.manager.createReminder("Restore with reconciliation")
        val completedReminder = requireNotNull(fixture.manager.completeReminder(reminder.id))
        fixture.events.clear()
        fixture.notifier.publishedReminders.clear()

        val restoreStarted = CompletableDeferred<Unit>()
        val releaseRestore = CompletableDeferred<Unit>()
        val reconcileStarted = CompletableDeferred<Unit>()
        fixture.dao.restoreStarted = restoreStarted
        fixture.dao.releaseRestore = releaseRestore
        fixture.notifier.activeIdsRequested = reconcileStarted

        val restoreJob = launch(start = CoroutineStart.UNDISPATCHED) {
            fixture.manager.restoreReminder(completedReminder)
        }
        restoreStarted.await()

        val reconcileJob = launch(start = CoroutineStart.UNDISPATCHED) {
            fixture.manager.reconcileNotifications()
        }

        try {
            assertFalse(reconcileStarted.isCompleted)
        } finally {
            releaseRestore.complete(Unit)
        }

        restoreJob.join()
        reconcileJob.join()
        assertEquals(
            listOf(
                "room_restore",
                "notification_publish",
                "notification_active_ids",
                "notification_publish",
            ),
            fixture.events,
        )
        assertEquals(listOf(completedReminder), fixture.dao.currentReminders())
        assertEquals(
            listOf(completedReminder, completedReminder),
            fixture.notifier.publishedReminders,
        )
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
        var deleteStarted: CompletableDeferred<Unit>? = null
        var releaseDelete: CompletableDeferred<Unit>? = null
        var restoreStarted: CompletableDeferred<Unit>? = null
        var releaseRestore: CompletableDeferred<Unit>? = null

        fun currentReminders(): List<ReminderEntity> = reminders.value

        override fun observeAll(): Flow<List<ReminderEntity>> = reminders

        override suspend fun insert(reminder: ReminderEntity): Long {
            events += "room_insert"
            val reminderId = (reminders.value.maxOfOrNull(ReminderEntity::id) ?: 0L) + 1L
            reminders.value = reminders.value + reminder.copy(id = reminderId)
            return reminderId
        }

        override suspend fun insertOrReplace(reminder: ReminderEntity) {
            events += "room_restore"
            restoreStarted?.complete(Unit)
            releaseRestore?.await()
            reminders.value = reminders.value.filterNot { it.id == reminder.id } + reminder
        }

        override suspend fun deleteById(reminderId: Long): Int {
            events += "room_delete"
            deleteStarted?.complete(Unit)
            releaseDelete?.await()
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
        val activeReminderIds = mutableSetOf<Long>()
        val cancelledReminderIds = mutableListOf<Long>()
        var activeIdsRequested: CompletableDeferred<Unit>? = null

        override fun publish(reminder: ReminderEntity) {
            events += "notification_publish"
            publishedReminders += reminder
        }

        override fun activeReminderIds(): Set<Long> {
            events += "notification_active_ids"
            activeIdsRequested?.complete(Unit)
            return activeReminderIds.toSet()
        }

        override fun cancel(reminderId: Long) {
            events += "notification_cancel"
            cancelledReminderIds += reminderId
        }
    }
}
