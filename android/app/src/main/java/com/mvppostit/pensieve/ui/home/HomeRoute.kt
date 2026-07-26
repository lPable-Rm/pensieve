package com.mvppostit.pensieve.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.notifications.canPostReminderNotifications
import com.mvppostit.pensieve.notifications.hasReminderNotificationPermission
import com.mvppostit.pensieve.reminders.ReminderManager
import com.mvppostit.pensieve.voice.OnDeviceVoiceRecognizer
import com.mvppostit.pensieve.voice.VoiceRecognitionEvent
import com.mvppostit.pensieve.voice.VoiceRecognitionFailure
import com.mvppostit.pensieve.widget.VoiceCaptureWidgetProvider
import kotlinx.coroutines.launch

/** Indica qué borrador debe continuar cuando Android responde al permiso de notificaciones. */
private enum class ReminderCreation {
    Manual,
    Voice,
}

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
    widgetVoiceRequestToken: Int = 0,
    onWidgetVoiceRequestConsumed: (Int) -> Unit = {},
    onAppearanceClick: () -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel(
        factory = remember(reminderManager) { HomeViewModelFactory(reminderManager) },
    )
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
    // Activity Result puede entregar una respuesta después de recrear la
    // actividad. Conservamos estos dos marcadores pequeños para interpretar
    // correctamente el permiso sin guardar aquí el texto de ningún borrador.
    var pendingReminderCreation by rememberSaveable {
        mutableStateOf<ReminderCreation?>(null)
    }
    var hasRequestedMicrophonePermission by rememberSaveable {
        mutableStateOf(false)
    }
    var isRequestingMicrophonePermission by rememberSaveable {
        mutableStateOf(false)
    }

    // La instancia vive junto a la pantalla, nunca en el contenedor global ni
    // en el ViewModel. Así puede destruirse al abandonar esta composición.
    val voiceRecognizer = remember(context.applicationContext, viewModel) {
        OnDeviceVoiceRecognizer(context.applicationContext) { event ->
            when (event) {
                VoiceRecognitionEvent.ListeningStarted -> viewModel.onVoiceListeningStarted()
                VoiceRecognitionEvent.Processing -> viewModel.onVoiceProcessing()
                is VoiceRecognitionEvent.PartialResult -> viewModel.onVoicePartialResult(event.text)
                is VoiceRecognitionEvent.FinalResult -> viewModel.onVoiceFinalResult(event.text)
                is VoiceRecognitionEvent.Failure -> viewModel.onVoiceRecognitionFailure(event.reason)
            }
        }
    }

    fun showNotificationUnavailableMessage() {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = notificationPermissionDeniedMessage,
                duration = SnackbarDuration.Long,
            )
        }
    }

    fun continueReminderCreation(creation: ReminderCreation) {
        when (creation) {
            ReminderCreation.Manual -> viewModel.createManualReminder()
            ReminderCreation.Voice -> viewModel.createVoiceReminder()
        }
    }

    // El launcher debe recordarse entre recomposiciones. Tras conceder el
    // permiso retomamos la acción original de guardar la nota.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { permissionGranted ->
        // El resultado puede cambiar el camino del widget de fallback a
        // servicio directo, por eso reconstruimos sus PendingIntent.
        VoiceCaptureWidgetProvider.updateAll(context)
        val pendingCreation = pendingReminderCreation
        pendingReminderCreation = null

        if (permissionGranted && context.canPostReminderNotifications()) {
            if (pendingCreation != null) {
                continueReminderCreation(pendingCreation)
            }
        } else {
            // El borrador se conserva en HomeViewModel para que la persona no
            // pierda lo que ha escrito y pueda decidir qué hacer después.
            showNotificationUnavailableMessage()
        }
    }

    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { permissionGranted ->
        isRequestingMicrophonePermission = false
        // También actualizamos cuando el permiso de micrófono cambia, tanto si
        // se concede como si se rechaza.
        VoiceCaptureWidgetProvider.updateAll(context)
        if (!permissionGranted) {
            viewModel.onVoicePermissionDenied(
                canOpenSettings = context.isMicrophonePermissionPermanentlyDenied(
                    hasRequestedMicrophonePermission = hasRequestedMicrophonePermission,
                ),
            )
        } else if (viewModel.canStartVoiceInput()) {
            voiceRecognizer.start()
        }
    }

    fun createReminderWithNotificationPermission(creation: ReminderCreation) {
        // Pedimos este permiso solo cuando el texto ya está listo para llegar a Room.
        when {
            context.canPostReminderNotifications() -> continueReminderCreation(creation)
            !context.hasReminderNotificationPermission() -> {
                pendingReminderCreation = creation
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> showNotificationUnavailableMessage()
        }
    }

    fun startVoiceInput() {
        // Evita lanzar dos diálogos de Android si llegan pulsaciones repetidas.
        if (!viewModel.canStartVoiceInput() || isRequestingMicrophonePermission) return

        if (!voiceRecognizer.isAvailable()) {
            viewModel.onVoiceRecognitionFailure(
                VoiceRecognitionFailure.RecognizerUnavailable,
            )
            return
        }

        if (
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            voiceRecognizer.start()
        } else {
            isRequestingMicrophonePermission = true
            hasRequestedMicrophonePermission = true
            microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // El fallback del widget llega como una señal efímera. Se consume una vez
    // y reutiliza exactamente la misma función que el botón de la pantalla.
    LaunchedEffect(widgetVoiceRequestToken) {
        if (widgetVoiceRequestToken == 0) return@LaunchedEffect

        startVoiceInput()
        onWidgetVoiceRequestConsumed(widgetVoiceRequestToken)
    }

    fun cancelVoiceInput() {
        voiceRecognizer.cancel()
        viewModel.cancelVoiceInput()
    }

    fun retryVoiceInput() {
        // Error no es un estado apto para iniciar una nueva sesión. Lo cerramos
        // primero y reutilizamos el mismo camino de permiso y disponibilidad.
        cancelVoiceInput()
        startVoiceInput()
    }

    fun openApplicationSettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ),
        )
    }

    // La escucha no debe continuar sin una superficie visible. La revisión ya
    // recibida se conserva para que una rotación o ir a segundo plano no borre texto.
    DisposableEffect(lifecycleOwner, voiceRecognizer, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Permisos y canales pueden cambiar fuera de Pensieve; al
                    // volver actualizamos el destino del siguiente toque.
                    VoiceCaptureWidgetProvider.updateAll(context)
                }

                Lifecycle.Event.ON_STOP -> {
                    voiceRecognizer.cancel()
                    viewModel.cancelActiveVoiceCapture()
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceRecognizer.destroy()
            viewModel.cancelActiveVoiceCapture()
        }
    }

    val isVoiceInputDismissible = when (uiState.voiceInputState) {
        is VoiceInputState.Listening,
        VoiceInputState.Processing,
        is VoiceInputState.Review,
        is VoiceInputState.Error,
        -> true

        VoiceInputState.Hidden,
        is VoiceInputState.Saving,
        -> false
    }

    // Atrás tiene prioridad sobre cerrar la actividad mientras haya una captura
    // o una revisión activa; Room no se toca hasta la confirmación de Guardar.
    BackHandler(enabled = isVoiceInputDismissible) {
        voiceRecognizer.cancel()
        viewModel.cancelVoiceInput()
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
        onVoiceNoteClick = ::startVoiceInput,
        onManualReminderTextChange = viewModel::updateManualReminderText,
        onCreateManualReminder = {
            createReminderWithNotificationPermission(ReminderCreation.Manual)
        },
        onCancelManualReminder = viewModel::cancelManualReminder,
        onCompleteReminderClick = viewModel::completeReminder,
        onStopVoiceRecording = voiceRecognizer::stop,
        onVoiceReminderTextChange = viewModel::updateVoiceReminderText,
        onCreateVoiceReminder = {
            createReminderWithNotificationPermission(ReminderCreation.Voice)
        },
        onCancelVoiceInput = ::cancelVoiceInput,
        onRetryVoiceInput = ::retryVoiceInput,
        onOpenMicrophoneSettings = ::openApplicationSettings,
        onAppearanceClick = onAppearanceClick,
    )
}

/**
 * Distingue la denegación permanente de una denegación que Android aún puede
 * volver a preguntar. La marca local evita confundir el estado inicial con
 * "No volver a preguntar".
 */
private fun Context.isMicrophonePermissionPermanentlyDenied(
    hasRequestedMicrophonePermission: Boolean,
): Boolean {
    if (!hasRequestedMicrophonePermission) return false

    val activity = findActivity() ?: return false
    return !activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
}

/** LocalContext puede estar envuelto; recorremos los wrappers hasta la actividad real. */
private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
