package com.mvppostit.pensieve.ui.home

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mvppostit.pensieve.data.local.ReminderDao
import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.data.repository.ReminderRepository
import com.mvppostit.pensieve.notifications.ReminderNotifier
import com.mvppostit.pensieve.reminders.ReminderManager
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelErrorTest {

    private val viewModelStores = mutableListOf<ViewModelStore>()

    @After
    fun clearViewModels() {
        val storesToClear = viewModelStores.toList()
        viewModelStores.clear()

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            storesToClear.forEach(ViewModelStore::clear)
        }
    }

    @Test
    fun loadFailure_preservesLastListAndReportsTheOperation() = runBlocking {
        val reminder = ReminderEntity(
            id = 3,
            text = "Conservar tras error",
            createdAtMillis = 100,
        )
        val reminderDao = FakeReminderDao(initialReminders = listOf(reminder)).apply {
            failOnObserve = true
        }
        val viewModel = createViewModel(reminderDao)
        val errorDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.operationError.filterNotNull().first()
        }
        val stateCollection = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.uiState.first { false }
        }

        try {
            val error = withTimeout(TestTimeoutMillis) { errorDeferred.await() }

            assertEquals(ReminderOperation.Load, error.operation)
            assertEquals(1, reminderDao.observeAttempts)
            assertEquals(listOf(reminder), viewModel.uiState.value.reminders)
        } finally {
            stateCollection.cancelAndJoin()
        }
    }

    @Test
    fun createFailure_keepsTheDraftAndReportsTheOperation() = runBlocking {
        val reminderDao = FakeReminderDao().apply {
            failOnInsert = true
        }
        val viewModel = createViewModel(reminderDao)
        val draft = "  Comprar pan  "

        viewModel.showManualInput()
        viewModel.updateManualReminderText(draft)
        val errorDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.operationError.filterNotNull().first()
        }

        viewModel.createManualReminder()

        val error = withTimeout(TestTimeoutMillis) { errorDeferred.await() }
        val recoveredState = withTimeout(TestTimeoutMillis) {
            viewModel.uiState.first { state ->
                state.isManualInputVisible &&
                    state.manualReminderText == draft &&
                    !state.isCreatingReminder
            }
        }

        assertEquals(ReminderOperation.Create, error.operation)
        assertEquals(1, reminderDao.insertAttempts)
        assertEquals(draft, recoveredState.manualReminderText)
        assertTrue(recoveredState.isManualInputVisible)
        assertFalse(recoveredState.isCreatingReminder)
    }

    @Test
    fun completeFailure_makesTheReminderVisibleAndDoesNotOfferUndo() = runBlocking {
        val reminder = ReminderEntity(
            id = 7,
            text = "Conservar",
            createdAtMillis = 100,
        )
        val reminderDao = FakeReminderDao(initialReminders = listOf(reminder)).apply {
            failOnDelete = true
        }
        val viewModel = createViewModel(reminderDao)
        val errorDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.operationError.filterNotNull().first()
        }

        viewModel.completeReminder(reminder.id)

        val error = withTimeout(TestTimeoutMillis) { errorDeferred.await() }
        val recoveredState = withTimeout(TestTimeoutMillis) {
            viewModel.uiState.first { state ->
                state.reminders.any { it.id == reminder.id } &&
                    reminder.id !in state.completingReminderIds
            }
        }

        assertEquals(ReminderOperation.Complete, error.operation)
        assertEquals(1, reminderDao.deleteAttempts)
        assertNull(viewModel.pendingUndoReminder.value)
        assertTrue(recoveredState.reminders.any { it.id == reminder.id })
    }

    @Test
    fun restoreFailure_keepsUndoAvailableAndAllowsRetry() = runBlocking {
        val reminder = ReminderEntity(
            id = 11,
            text = "Recuperar",
            createdAtMillis = 100,
        )
        val reminderDao = FakeReminderDao(initialReminders = listOf(reminder))
        val viewModel = createViewModel(reminderDao)
        val pendingUndoDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.pendingUndoReminder.filterNotNull().first()
        }

        viewModel.completeReminder(reminder.id)
        val completedReminder = withTimeout(TestTimeoutMillis) {
            pendingUndoDeferred.await()
        }
        reminderDao.failOnRestore = true
        val errorDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.operationError.filterNotNull().first()
        }

        viewModel.undoCompletion(completedReminder)

        val error = withTimeout(TestTimeoutMillis) { errorDeferred.await() }
        withTimeout(TestTimeoutMillis) {
            viewModel.undoInProgress.first { inProgress -> !inProgress }
        }
        val failedRestoreState = withTimeout(TestTimeoutMillis) {
            viewModel.uiState.first { state ->
                reminder.id in state.completingReminderIds
            }
        }

        assertEquals(ReminderOperation.Restore, error.operation)
        assertEquals(1, reminderDao.restoreAttempts)
        assertEquals(completedReminder, viewModel.pendingUndoReminder.value)
        assertTrue(reminder.id in failedRestoreState.completingReminderIds)

        reminderDao.failOnRestore = false
        viewModel.consumeOperationError(error.id)
        val restoredStateDeferred = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.uiState.first { state ->
                state.reminders.any { it.id == reminder.id } &&
                    reminder.id !in state.completingReminderIds
            }
        }

        viewModel.undoCompletion(completedReminder)

        val restoredState = withTimeout(TestTimeoutMillis) {
            restoredStateDeferred.await()
        }
        withTimeout(TestTimeoutMillis) {
            viewModel.undoInProgress.first { inProgress -> !inProgress }
        }
        assertEquals(2, reminderDao.restoreAttempts)
        assertFalse(viewModel.undoInProgress.value)
        assertNull(viewModel.pendingUndoReminder.value)
        assertTrue(restoredState.reminders.any { it.id == reminder.id })
    }

    private fun createViewModel(reminderDao: ReminderDao): HomeViewModel {
        val store = ViewModelStore()
        viewModelStores += store
        val manager = ReminderManager(
            repository = ReminderRepository(reminderDao),
            notificationPublisher = NoOpReminderNotifier,
        )

        return ViewModelProvider(
            store,
            HomeViewModelFactory(manager),
        )[HomeViewModel::class.java]
    }

    private object NoOpReminderNotifier : ReminderNotifier {
        override fun publish(reminder: ReminderEntity) = Unit

        override fun cancel(reminderId: Long) = Unit
    }

    private class FakeReminderDao(
        initialReminders: List<ReminderEntity> = emptyList(),
    ) : ReminderDao {

        private val reminders = MutableStateFlow(sortReminders(initialReminders))

        var failOnInsert = false
        var failOnDelete = false
        var failOnRestore = false
        var failOnObserve = false
        var observeAttempts = 0
            private set
        var insertAttempts = 0
            private set
        var deleteAttempts = 0
            private set
        var restoreAttempts = 0
            private set

        override fun observeAll(): Flow<List<ReminderEntity>> = flow {
            observeAttempts += 1

            if (failOnObserve) {
                emit(reminders.value)
                throw simulatedDatabaseError()
            }

            emitAll(reminders)
        }

        override suspend fun insert(reminder: ReminderEntity): Long {
            insertAttempts += 1
            if (failOnInsert) throw simulatedDatabaseError()

            val reminderId = reminder.id.takeIf { it != 0L }
                ?: ((reminders.value.maxOfOrNull(ReminderEntity::id) ?: 0L) + 1L)
            val storedReminder = reminder.copy(id = reminderId)
            reminders.value = sortReminders(reminders.value + storedReminder)
            return reminderId
        }

        override suspend fun insertOrReplace(reminder: ReminderEntity) {
            restoreAttempts += 1
            if (failOnRestore) throw simulatedDatabaseError()

            reminders.value = sortReminders(
                reminders.value.filterNot { it.id == reminder.id } + reminder,
            )
        }

        override suspend fun deleteById(reminderId: Long): Int {
            deleteAttempts += 1
            if (failOnDelete) throw simulatedDatabaseError()

            val previousSize = reminders.value.size
            reminders.value = reminders.value.filterNot { it.id == reminderId }
            return previousSize - reminders.value.size
        }

        override suspend fun findById(reminderId: Long): ReminderEntity? =
            reminders.value.firstOrNull { it.id == reminderId }

        private companion object {
            fun sortReminders(reminders: List<ReminderEntity>): List<ReminderEntity> =
                reminders.sortedWith(
                    compareByDescending<ReminderEntity> { it.createdAtMillis }
                        .thenByDescending(ReminderEntity::id),
                )

            fun simulatedDatabaseError(): SQLiteException =
                SQLiteException("Fallo de base de datos simulado")
        }
    }

    private companion object {
        const val TestTimeoutMillis = 5_000L
    }
}
