package com.mvppostit.pensieve.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.notifications.canPostReminderNotifications
import com.mvppostit.pensieve.notifications.hasReminderNotificationPermission
import com.mvppostit.pensieve.reminders.ReminderManager
import kotlinx.coroutines.launch

/**
 * Conecta la pantalla visual con Android y con HomeViewModel.
 *
 * HomeScreen recibe solo estado y funciones. Por eso sus previews no necesitan
 * una base de datos ni un ViewModel, mientras esta ruta concentra la lógica de
 * ciclo de vida y del snackbar.
 */
@Composable
fun HomeRoute(
    reminderManager: ReminderManager,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val viewModel: HomeViewModel = viewModel(
        factory = remember(reminderManager) { HomeViewModelFactory(reminderManager) },
    )
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingUndoReminder by viewModel.pendingUndoReminder.collectAsStateWithLifecycle()
    val operationError by viewModel.operationError.collectAsStateWithLifecycle()
    val undoInProgress by viewModel.undoInProgress.collectAsStateWithLifecycle()
    val completedMessage = stringResource(R.string.reminder_completed)
    val undoLabel = stringResource(R.string.undo)
    val loadFailedMessage = stringResource(R.string.reminder_load_failed)
    val createFailedMessage = stringResource(R.string.reminder_create_failed)
    val completeFailedMessage = stringResource(R.string.reminder_complete_failed)
    val restoreFailedMessage = stringResource(R.string.reminder_restore_failed)
    val notificationPermissionDeniedMessage =
        stringResource(R.string.notification_permission_denied)

    fun showNotificationUnavailableMessage() {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = notificationPermissionDeniedMessage,
                duration = SnackbarDuration.Long,
            )
        }
    }

    // El launcher debe recordarse entre recomposiciones. Tras conceder el
    // permiso retomamos la acción original de guardar la nota.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { permissionGranted ->
        if (permissionGranted && context.canPostReminderNotifications()) {
            viewModel.createManualReminder()
        } else {
            // El borrador se conserva en HomeViewModel para que la persona no
            // pierda lo que ha escrito y pueda decidir qué hacer después.
            showNotificationUnavailableMessage()
        }
    }

    // Un error tiene prioridad, pero no elimina el Deshacer pendiente. Cuando
    // termina su snackbar, el efecto se reinicia y vuelve a ofrecer esa acción.
    LaunchedEffect(
        operationError,
        pendingUndoReminder,
        undoInProgress,
        snackbarHostState,
        completedMessage,
        undoLabel,
        loadFailedMessage,
        createFailedMessage,
        completeFailedMessage,
        restoreFailedMessage,
    ) {
        operationError?.let { error ->
            val message = when (error.operation) {
                ReminderOperation.Load -> loadFailedMessage
                ReminderOperation.Create -> createFailedMessage
                ReminderOperation.Complete -> completeFailedMessage
                ReminderOperation.Restore -> restoreFailedMessage
            }

            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
            viewModel.consumeOperationError(error.id)
            return@LaunchedEffect
        }

        // Mientras Room restaura la fila no mostramos otra acción que pueda
        // lanzar un segundo intento sobre el mismo recordatorio.
        if (undoInProgress) return@LaunchedEffect

        val reminder = pendingUndoReminder ?: return@LaunchedEffect

        when (
            snackbarHostState.showSnackbar(
                message = completedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short,
            )
        ) {
            SnackbarResult.ActionPerformed -> viewModel.undoCompletion(reminder)
            SnackbarResult.Dismissed -> viewModel.discardCompletion(reminder.id)
        }
    }

    HomeScreen(
        uiState = uiState,
        modifier = modifier,
        onNewNoteClick = viewModel::showManualInput,
        onManualReminderTextChange = viewModel::updateManualReminderText,
        onCreateManualReminder = {
            // Pedimos el permiso solo cuando una nota ya está lista para
            // guardarse, no al abrir Pensieve ni al mostrar el campo de texto.
            when {
                context.canPostReminderNotifications() -> viewModel.createManualReminder()
                !context.hasReminderNotificationPermission() -> {
                    notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS,
                    )
                }
                else -> showNotificationUnavailableMessage()
            }
        },
        onCancelManualReminder = viewModel::cancelManualReminder,
        onCompleteReminderClick = viewModel::completeReminder,
    )
}
