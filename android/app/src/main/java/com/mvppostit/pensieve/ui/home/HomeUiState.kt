package com.mvppostit.pensieve.ui.home

import com.mvppostit.pensieve.data.local.ReminderEntity

/** Duración compartida entre la salida visual y la operación coordinada. */
internal const val CompletionAnimationDurationMillis = 180

/** Operación local que no pudo terminar y necesita informar a la interfaz. */
internal enum class ReminderOperation {
    Load,
    Create,
    Complete,
    Restore,
}

/**
 * Cada error recibe un id para que dos fallos consecutivos de la misma
 * operación se muestren como mensajes diferentes.
 */
internal data class HomeOperationError(
    val id: Long,
    val operation: ReminderOperation,
)

/**
 * Estados exclusivos de la entrada de voz dentro de la pantalla principal.
 *
 * Un estado cerrado evita, por ejemplo, mostrar a la vez una revisión editable
 * y una captura activa. El texto solo llega a Room cuando pasa por Review.
 */
sealed interface VoiceInputState {
    data object Hidden : VoiceInputState

    data class Listening(
        val partialText: String = "",
        val startedAtMillis: Long,
    ) : VoiceInputState

    data object Processing : VoiceInputState

    data class Review(val text: String) : VoiceInputState

    data class Saving(val text: String) : VoiceInputState

    data class Error(val error: VoiceInputError) : VoiceInputState
}

/** Mensajes de voz reducidos a situaciones que la persona puede resolver. */
sealed interface VoiceInputError {
    data object NoSpeech : VoiceInputError
    data object LanguageUnavailable : VoiceInputError
    data object RecognizerUnavailable : VoiceInputError
    data class PermissionDenied(val canOpenSettings: Boolean) : VoiceInputError
    data object TemporaryFailure : VoiceInputError
}

/**
 * Datos que HomeScreen necesita para dibujar la pantalla en un instante dado.
 *
 * Es inmutable para que cada actualización describa un estado completo y fácil
 * de previsualizar o probar sin depender de Android ni de un ViewModel.
 */
data class HomeUiState(
    val reminders: List<ReminderEntity> = emptyList(),
    val completingReminderIds: Set<Long> = emptySet(),
    val isManualInputVisible: Boolean = false,
    val manualReminderText: String = "",
    val isCreatingReminder: Boolean = false,
    val voiceInputState: VoiceInputState = VoiceInputState.Hidden,
)
