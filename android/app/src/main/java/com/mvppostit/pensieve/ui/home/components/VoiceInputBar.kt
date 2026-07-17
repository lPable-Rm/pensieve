package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.ui.home.VoiceInputError
import com.mvppostit.pensieve.ui.home.VoiceInputState
import kotlinx.coroutines.delay

/**
 * Barra compacta para todos los estados de entrada por voz.
 *
 * No conoce permisos, SpeechRecognizer ni Room: recibe un [VoiceInputState]
 * inmutable y devuelve acciones a HomeScreen. Por eso puede previsualizarse y
 * probarse sin depender del micrófono real.
 */
@Composable
fun VoiceInputBar(
    state: VoiceInputState,
    onStopRecording: () -> Unit,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is VoiceInputState.Hidden) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        when (state) {
            is VoiceInputState.Listening -> VoiceListeningContent(
                state = state,
                onStopRecording = onStopRecording,
            )

            VoiceInputState.Processing -> VoiceProcessingContent(onCancel = onCancel)

            is VoiceInputState.Review -> VoiceReviewContent(
                text = state.text,
                onTextChange = onTextChange,
                onSave = onSave,
                onCancel = onCancel,
            )

            is VoiceInputState.Saving -> VoiceSavingContent()

            is VoiceInputState.Error -> VoiceErrorContent(
                error = state.error,
                onRetry = onRetry,
                onCancel = onCancel,
                onOpenSettings = onOpenSettings,
            )

            VoiceInputState.Hidden -> Unit
        }
    }
}

@Composable
private fun VoiceListeningContent(
    state: VoiceInputState.Listening,
    onStopRecording: () -> Unit,
) {
    val elapsedSeconds = rememberElapsedSeconds(state.startedAtMillis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoiceProgressIndicator()
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.voice_listening),
                modifier = Modifier.voiceStatusLiveRegion(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            if (state.partialText.isNotBlank()) {
                // El resultado cambia con frecuencia. Lo mostramos visualmente,
                // pero evitamos que TalkBack lo anuncie en cada actualización.
                Text(
                    text = state.partialText,
                    modifier = Modifier.semantics { hideFromAccessibility() },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                )
            }

            VoiceElapsedTime(elapsedSeconds)
        }

        TextButton(onClick = onStopRecording) {
            Text(text = androidx.compose.ui.res.stringResource(R.string.voice_stop_recording))
        }
    }
}

@Composable
private fun VoiceProcessingContent(onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoiceProgressIndicator()
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.voice_processing),
            modifier = Modifier
                .weight(1f)
                .voiceStatusLiveRegion(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onCancel) {
            Text(text = androidx.compose.ui.res.stringResource(R.string.voice_cancel))
        }
    }
}

@Composable
private fun VoiceReviewContent(
    text: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val canSave = text.trim().isNotEmpty()

    Column(modifier = Modifier.padding(12.dp)) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = androidx.compose.ui.res.stringResource(R.string.voice_review_placeholder))
            },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                onDone = {
                    if (canSave) onSave()
                },
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel) {
                Text(text = androidx.compose.ui.res.stringResource(R.string.voice_cancel))
            }

            FilledTonalButton(
                onClick = onSave,
                enabled = canSave,
            ) {
                Text(text = androidx.compose.ui.res.stringResource(R.string.voice_save_reminder))
            }
        }
    }
}

@Composable
private fun VoiceSavingContent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoiceProgressIndicator()
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(R.string.voice_saving),
            modifier = Modifier.voiceStatusLiveRegion(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun VoiceErrorContent(
    error: VoiceInputError,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val message = when (error) {
        VoiceInputError.NoSpeech -> R.string.voice_no_speech
        VoiceInputError.LanguageUnavailable -> R.string.voice_language_unavailable
        VoiceInputError.RecognizerUnavailable -> R.string.voice_recognizer_unavailable
        is VoiceInputError.PermissionDenied -> R.string.voice_permission_denied
        VoiceInputError.TemporaryFailure -> R.string.voice_temporary_failure
    }
    val canRetry = when (error) {
        VoiceInputError.NoSpeech,
        VoiceInputError.TemporaryFailure,
        -> true

        is VoiceInputError.PermissionDenied -> !error.canOpenSettings
        VoiceInputError.LanguageUnavailable,
        VoiceInputError.RecognizerUnavailable,
        -> false
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = androidx.compose.ui.res.stringResource(message),
            modifier = Modifier.voiceStatusLiveRegion(),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCancel) {
                Text(text = androidx.compose.ui.res.stringResource(R.string.voice_close))
            }

            when {
                error is VoiceInputError.PermissionDenied && error.canOpenSettings -> {
                    FilledTonalButton(onClick = onOpenSettings) {
                        Text(text = androidx.compose.ui.res.stringResource(R.string.voice_open_settings))
                    }
                }

                canRetry -> {
                    FilledTonalButton(onClick = onRetry) {
                        Text(text = androidx.compose.ui.res.stringResource(R.string.voice_retry))
                    }
                }
            }
        }
    }
}

/** El indicador acompaña al texto; no representa niveles reales de audio. */
@Composable
private fun VoiceProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(28.dp)
            .clearAndSetSemantics { },
        strokeWidth = 3.dp,
    )
}

/** Anuncia solo los cambios de estado estables, nunca el parcial ni el contador. */
private fun Modifier.voiceStatusLiveRegion(): Modifier =
    semantics { liveRegion = LiveRegionMode.Polite }

@Composable
private fun VoiceElapsedTime(elapsedSeconds: Int) {
    val accessibilityLabel = pluralStringResource(
        R.plurals.voice_elapsed_seconds,
        elapsedSeconds,
        elapsedSeconds,
    )
    val visualLabel = "%d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)

    Text(
        text = visualLabel,
        modifier = Modifier.semantics { contentDescription = accessibilityLabel },
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
private fun rememberElapsedSeconds(startedAtMillis: Long): Int {
    var elapsedSeconds by remember(startedAtMillis) { mutableIntStateOf(0) }

    LaunchedEffect(startedAtMillis) {
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - startedAtMillis) / 1_000)
                .coerceAtLeast(0)
                .toInt()
            delay(1_000)
        }
    }

    return elapsedSeconds
}
