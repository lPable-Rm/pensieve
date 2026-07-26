package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import com.mvppostit.pensieve.ui.theme.NolvidaRecording
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
    val stopDescription = stringResource(R.string.voice_stop_recording)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VoiceListeningIndicator()
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.voice_listening),
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

        // El botón conserva un tamaño cómodo para tocarlo, aunque el icono sea pequeño.
        IconButton(
            onClick = onStopRecording,
            modifier = Modifier.semantics {
                contentDescription = stopDescription
            },
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_widget_stop),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = NolvidaRecording,
                    )
                }
            }
        }
    }
}

/**
 * Indicador fijo de escucha.
 *
 * El punto rojo dice "el micrófono está activo". La forma de barras solo
 * decora la superficie: no pretende medir el volumen real del micrófono.
 */
@Composable
private fun VoiceListeningIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = NolvidaRecording.copy(alpha = 0.12f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(NolvidaRecording),
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(R.drawable.ic_widget_waveform),
            contentDescription = null,
            modifier = Modifier
                .width(48.dp)
                .height(20.dp),
            tint = NolvidaRecording,
        )
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
            text = stringResource(R.string.voice_processing),
            modifier = Modifier
                .weight(1f)
                .voiceStatusLiveRegion(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(
            onClick = onCancel,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text(text = stringResource(R.string.voice_cancel))
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

    Column(
        modifier = Modifier
            // En horizontal el IME puede dejar una ventana muy baja. Solo la
            // revisión editable necesita scroll para mantener sus acciones.
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = stringResource(R.string.voice_review_placeholder))
            },
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (canSave) onSave()
                },
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_voice_edit),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(text = stringResource(R.string.voice_cancel))
            }

            FilledTonalButton(
                onClick = onSave,
                enabled = canSave,
            ) {
                Text(text = stringResource(R.string.voice_save_reminder))
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
        VoiceSavingIndicator()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.voice_saving),
                modifier = Modifier.voiceStatusLiveRegion(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.voice_saving_support),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** El check convierte el mensaje de guardado en una confirmación fácil de reconocer. */
@Composable
private fun VoiceSavingIndicator() {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_voice_check),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
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
            text = stringResource(message),
            modifier = Modifier.voiceStatusLiveRegion(),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(text = stringResource(R.string.voice_close))
            }

            when {
                error is VoiceInputError.PermissionDenied && error.canOpenSettings -> {
                    FilledTonalButton(onClick = onOpenSettings) {
                        Text(text = stringResource(R.string.voice_open_settings))
                    }
                }

                canRetry -> {
                    FilledTonalButton(onClick = onRetry) {
                        Text(text = stringResource(R.string.voice_retry))
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
        color = MaterialTheme.colorScheme.onSurface,
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
