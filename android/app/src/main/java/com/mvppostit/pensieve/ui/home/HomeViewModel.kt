package com.mvppostit.pensieve.ui.home

import android.database.sqlite.SQLiteException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.reminders.ReminderManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Mantiene el estado de la pantalla y coordina sus acciones con los datos.
 *
 * No contiene clases de Compose: así sobrevive a una recreación de la actividad
 * y puede probarse sin renderizar la interfaz. Room sigue siendo la fuente de
 * verdad de la lista de notas.
 */
class HomeViewModel(
    private val reminderManager: ReminderManager,
) : ViewModel() {

    private val isManualInputVisible = MutableStateFlow(false)
    private val manualReminderText = MutableStateFlow("")
    private val isCreatingReminder = MutableStateFlow(false)
    private val completingReminderIds = MutableStateFlow<Set<Long>>(emptySet())
    private val isUndoInProgress = MutableStateFlow(false)

    private val _operationError = MutableStateFlow<HomeOperationError?>(null)
    internal val operationError: StateFlow<HomeOperationError?> = _operationError.asStateFlow()
    internal val undoInProgress: StateFlow<Boolean> = isUndoInProgress.asStateFlow()
    private var nextOperationErrorId = 0L
    private var lastLoadedReminders = emptyList<ReminderEntity>()

    private val observedReminders = reminderManager.observeReminders()
        .onEach { reminders ->
            lastLoadedReminders = reminders
        }
        .catch { error ->
            if (error !is SQLiteException) throw error

            emit(lastLoadedReminders)
            reportOperationError(ReminderOperation.Load)
        }

    // El último recordatorio completado permanece como estado hasta que el
    // snackbar recibe una respuesta. Así una recreación puede mostrarlo otra vez.
    private val _pendingUndoReminder = MutableStateFlow<ReminderEntity?>(null)
    val pendingUndoReminder: StateFlow<ReminderEntity?> = _pendingUndoReminder.asStateFlow()

    /** Estado continuo que HomeRoute observará respetando el ciclo de vida. */
    val uiState: StateFlow<HomeUiState> = combine(
        observedReminders,
        isManualInputVisible,
        manualReminderText,
        completingReminderIds,
        isCreatingReminder,
    ) { reminders, isInputVisible, inputText, completingIds, isCreating ->
        HomeUiState(
            reminders = reminders,
            completingReminderIds = completingIds,
            isManualInputVisible = isInputVisible,
            manualReminderText = inputText,
            isCreatingReminder = isCreating,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = HomeUiState(),
    )

    fun showManualInput() {
        isManualInputVisible.value = true
    }

    fun updateManualReminderText(text: String) {
        if (!isCreatingReminder.value) {
            manualReminderText.value = text
        }
    }

    fun cancelManualReminder() {
        if (isCreatingReminder.value) return

        manualReminderText.value = ""
        isManualInputVisible.value = false
    }

    fun createManualReminder() {
        val text = manualReminderText.value.trim()
        if (text.isEmpty() || isCreatingReminder.value) return
        isCreatingReminder.value = true

        viewModelScope.launch {
            try {
                reminderManager.createReminder(text)
                manualReminderText.value = ""
                isManualInputVisible.value = false
            } catch (_: SQLiteException) {
                reportOperationError(ReminderOperation.Create)
            } finally {
                isCreatingReminder.value = false
            }
        }
    }

    fun completeReminder(reminderId: Long) {
        if (isUndoInProgress.value || reminderId in completingReminderIds.value) return
        completingReminderIds.value = completingReminderIds.value + reminderId

        viewModelScope.launch {
            var keepHiddenUntilSnackbarResult = false

            try {
                // La espera vive en viewModelScope: una rotación no cancela la
                // acción después de que la persona ya haya pulsado Hecho.
                delay(CompletionAnimationDurationMillis.toLong())
                if (isUndoInProgress.value) return@launch

                val completedReminder = reminderManager.completeReminder(reminderId)
                    ?: return@launch

                // Solo la finalización más reciente mantiene Deshacer. Esto
                // evita acumular snackbars que aparecerían tarde.
                _pendingUndoReminder.value?.let { previousReminder ->
                    completingReminderIds.value =
                        completingReminderIds.value - previousReminder.id
                }
                _pendingUndoReminder.value = completedReminder
                keepHiddenUntilSnackbarResult = true
            } catch (_: SQLiteException) {
                reportOperationError(ReminderOperation.Complete)
            } finally {
                // Tras un DELETE correcto mantenemos el id oculto hasta que el
                // snackbar termine. Así no reaparece mientras llega el Flow de Room.
                if (!keepHiddenUntilSnackbarResult) {
                    completingReminderIds.value = completingReminderIds.value - reminderId
                }
            }
        }
    }

    fun undoCompletion(reminder: ReminderEntity) {
        if (_pendingUndoReminder.value?.id != reminder.id || isUndoInProgress.value) return
        isUndoInProgress.value = true

        viewModelScope.launch {
            var restored = false

            try {
                reminderManager.restoreReminder(reminder)
                restored = true
            } catch (_: SQLiteException) {
                reportOperationError(ReminderOperation.Restore)
            } finally {
                if (restored) {
                    if (_pendingUndoReminder.value?.id == reminder.id) {
                        _pendingUndoReminder.value = null
                    }
                    // La tarjeta vuelve a ser visible cuando Room ya ha terminado
                    // de restaurar la fila, evitando un parpadeo intermedio.
                    completingReminderIds.value =
                        completingReminderIds.value - reminder.id
                }

                // Liberamos el bloqueo al final para no exponer un instante con
                // Deshacer disponible cuando la restauración ya ha terminado.
                isUndoInProgress.value = false
            }
        }
    }

    fun discardCompletion(reminderId: Long) {
        if (_pendingUndoReminder.value?.id == reminderId) {
            _pendingUndoReminder.value = null
        }
        completingReminderIds.value = completingReminderIds.value - reminderId
    }

    fun consumeOperationError(errorId: Long) {
        if (_operationError.value?.id == errorId) {
            _operationError.value = null
        }
    }

    private fun reportOperationError(operation: ReminderOperation) {
        nextOperationErrorId += 1
        _operationError.value = HomeOperationError(
            id = nextOperationErrorId,
            operation = operation,
        )
    }

}

/**
 * Crea HomeViewModel con la dependencia manual que vive en AppContainer.
 *
 * La fábrica evita que el composable conozca cómo construir repositorios o la
 * base de datos; solo recibe el ReminderManager ya preparado.
 */
class HomeViewModelFactory(
    private val reminderManager: ReminderManager,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(reminderManager) as T
        }

        throw IllegalArgumentException("ViewModel no compatible: ${modelClass.name}")
    }
}
