package com.mvppostit.pensieve.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import com.mvppostit.pensieve.reminders.ReminderManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Material solo ofrece duraciones predefinidas. Esta es la duración concreta
// acordada para que Deshacer esté disponible, pero no frene el flujo principal.
private const val UndoSnackbarDurationMillis = 2_000L

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

    // El launcher debe recordarse entre recomposiciones. Tras conceder el
    // permiso retomamos la acción original de guardar la nota.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { permissionGranted ->
        if (permissionGranted) {
            viewModel.createManualReminder()
        } else {
            // El borrador se conserva en HomeViewModel para que la persona no
            // pierda lo que ha escrito y pueda decidir qué hacer después.
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = notificationPermissionDeniedMessage,
                    duration = SnackbarDuration.Long,
                )
            }
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
            snackbarHostState.showUndoSnackbar(
                message = completedMessage,
                actionLabel = undoLabel,
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
            if (context.hasNotificationPermission()) {
                viewModel.createManualReminder()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onCancelManualReminder = viewModel::cancelManualReminder,
        onCompleteReminderClick = viewModel::completeReminder,
    )
}

/**
 * Desde Android 13 las notificaciones requieren consentimiento en tiempo de
 * ejecución. En Android 12L y anteriores se consideran concedidas al instalar.
 */
private fun Context.hasNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Muestra un snackbar de deshacer durante una duración exacta.
 *
 * Usamos [SnackbarDuration.Indefinite] para que Material no aplique sus
 * duraciones fijas. La corrutina hija lo descarta a los dos segundos; si la
 * persona pulsa Deshacer antes, showSnackbar termina y cancela esa corrutina.
 */
private suspend fun SnackbarHostState.showUndoSnackbar(
    message: String,
    actionLabel: String,
): SnackbarResult = coroutineScope {
    val dismissJob = launch {
        delay(UndoSnackbarDurationMillis)
        currentSnackbarData?.dismiss()
    }

    try {
        showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Indefinite,
        )
    } finally {
        dismissJob.cancel()
    }
}
