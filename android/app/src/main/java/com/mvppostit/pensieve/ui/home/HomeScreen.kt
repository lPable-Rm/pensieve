package com.mvppostit.pensieve.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.data.local.ReminderEntity
import com.mvppostit.pensieve.ui.home.components.EmptyReminderState
import com.mvppostit.pensieve.ui.home.components.HomeActions
import com.mvppostit.pensieve.ui.home.components.ManualReminderInput
import com.mvppostit.pensieve.ui.home.components.NolvidaHeader
import com.mvppostit.pensieve.ui.home.components.ReminderCard
import com.mvppostit.pensieve.ui.home.components.VoiceInputBar
import com.mvppostit.pensieve.ui.theme.PensieveTheme
import java.text.DateFormat
import java.util.Date

/**
 * Única pantalla principal de Nolvida.
 *
 * Recibe un estado inmutable y callbacks desde HomeRoute. De este modo no
 * conoce Room ni modifica directamente la fuente persistente de recordatorios.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    modifier: Modifier = Modifier,
    onNewNoteClick: () -> Unit = {},
    onVoiceNoteClick: () -> Unit = {},
    onManualReminderTextChange: (String) -> Unit = {},
    onCreateManualReminder: () -> Unit = {},
    onCancelManualReminder: () -> Unit = {},
    onCompleteReminderClick: (Long) -> Unit = {},
    onStopVoiceRecording: () -> Unit = {},
    onVoiceReminderTextChange: (String) -> Unit = {},
    onCreateVoiceReminder: () -> Unit = {},
    onCancelVoiceInput: () -> Unit = {},
    onRetryVoiceInput: () -> Unit = {},
    onOpenMicrophoneSettings: () -> Unit = {},
    onAppearanceClick: () -> Unit = {},
) {
    val isVoiceInputActive = uiState.voiceInputState !is VoiceInputState.Hidden
    val showEmptyState =
        uiState.reminders.isEmpty() &&
            !uiState.isManualInputVisible &&
            !isVoiceInputActive
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            // Una sola protección sirve tanto para la entrada manual como
            // para la revisión editable de voz.
            .imePadding()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        NolvidaHeader(onAppearanceClick = onAppearanceClick)

        Spacer(modifier = Modifier.height(28.dp))

        // La voz mantiene su propia posición fija sobre la lista para que la
        // persona vea la transcripción y pueda detenerla sin abrir otra pantalla.
        if (isVoiceInputActive) {
            VoiceInputBar(
                state = uiState.voiceInputState,
                onStopRecording = onStopVoiceRecording,
                onTextChange = onVoiceReminderTextChange,
                onSave = onCreateVoiceReminder,
                onCancel = onCancelVoiceInput,
                onRetry = onRetryVoiceInput,
                onOpenSettings = onOpenMicrophoneSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }

        if (showEmptyState) {
            // La disposición centrada se conserva con altura normal. Cuando
            // falta espacio, LazyColumn permite alcanzar la acción por scroll.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                item {
                    EmptyReminderState(
                        onVoiceClick = onVoiceNoteClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            // weight hace que la lista ocupe el espacio libre, dejando los botones
            // inferiores siempre accesibles aunque haya muchos recordatorios.
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // La clave estable permite a Compose identificar cada tarjeta al eliminarla
                // o reordenarla, en lugar de confundirla con otra posición de la lista.
                items(
                    items = uiState.reminders,
                    key = { reminder -> reminder.id },
                ) { reminder ->
                    AnimatedVisibility(
                        visible = reminder.id !in uiState.completingReminderIds,
                        exit = fadeOut(
                            animationSpec = tween(CompletionAnimationDurationMillis),
                        ) + shrinkVertically(
                            animationSpec = tween(CompletionAnimationDurationMillis),
                        ),
                    ) {
                        ReminderCard(
                            text = reminder.text,
                            createdAtLabel = formatCreatedAtLabel(
                                createdAtMillis = reminder.createdAtMillis,
                            ),
                            completeDescription = stringResource(
                                R.string.complete_reminder,
                                reminder.text,
                            ),
                            onCompleteClick = { onCompleteReminderClick(reminder.id) },
                        )
                    }
                }
            }
        }

        if (uiState.isManualInputVisible) {
            ManualReminderInput(
                text = uiState.manualReminderText,
                isSaving = uiState.isCreatingReminder,
                onTextChange = onManualReminderTextChange,
                onSave = onCreateManualReminder,
                onCancel = onCancelManualReminder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }

        HomeActions(
            onNewNoteClick = onNewNoteClick,
            onVoiceNoteClick = onVoiceNoteClick,
            isManualInputVisible = uiState.isManualInputVisible,
            isVoiceInputActive = isVoiceInputActive,
            isSavingReminder = uiState.isCreatingReminder,
        )
    }
}

/** Convierte la fecha almacenada en texto de interfaz sin modificar el dato original. */
private fun formatCreatedAtLabel(createdAtMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(createdAtMillis))

// Datos exclusivos de los previews: la aplicación real siempre recibe Room.
private val previewReminders = listOf(
    ReminderEntity(
        id = 1,
        text = "Comprar tornillos, cinta aislante y una bombilla para arreglar la lámpara del pasillo",
        createdAtMillis = System.currentTimeMillis(),
    ),
    ReminderEntity(
        id = 2,
        text = "Recoger a los niños a las cinco",
        createdAtMillis = System.currentTimeMillis() - 3_600_000,
    ),
)

// Los previews permiten comprobar la composición en Android Studio sin iniciar el emulador.
@Preview(
    name = "Pantalla principal",
    showBackground = true,
    backgroundColor = 0xFFFAF9FF,
    heightDp = 800,
)
@Composable
private fun HomeScreenPreview() {
    PensieveTheme {
        HomeScreen(uiState = HomeUiState(reminders = previewReminders))
    }
}

@Preview(
    name = "Pantalla principal vacía",
    showBackground = true,
    backgroundColor = 0xFFFAF9FF,
    heightDp = 800,
)
@Composable
private fun HomeScreenEmptyPreview() {
    PensieveTheme {
        HomeScreen(uiState = HomeUiState())
    }
}

// Esta variante sirve para detectar problemas de espacio con el tamaño de letra del sistema.
@Preview(
    name = "Pantalla principal con texto grande",
    showBackground = true,
    backgroundColor = 0xFFFAF9FF,
    fontScale = 1.8f,
    heightDp = 900,
)
@Composable
private fun HomeScreenLargeTextPreview() {
    PensieveTheme {
        HomeScreen(uiState = HomeUiState(reminders = previewReminders))
    }
}

@Preview(
    name = "Estado vacío, texto grande y poco alto",
    showBackground = true,
    backgroundColor = 0xFFFAF9FF,
    widthDp = 360,
    heightDp = 480,
    fontScale = 1.8f,
)
@Composable
private fun HomeScreenEmptyLargeTextPreview() {
    PensieveTheme {
        HomeScreen(uiState = HomeUiState())
    }
}

@Preview(
    name = "Estado vacío horizontal",
    showBackground = true,
    backgroundColor = 0xFFFAF9FF,
    widthDp = 800,
    heightDp = 360,
)
@Composable
private fun HomeScreenEmptyLandscapePreview() {
    PensieveTheme {
        HomeScreen(uiState = HomeUiState())
    }
}
