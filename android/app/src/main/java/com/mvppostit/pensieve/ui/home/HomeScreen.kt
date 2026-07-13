package com.mvppostit.pensieve.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.ui.theme.PensieveTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colores temporales de la primera versión visual. Las paletas configurables
// se incorporarán en la Fase 8, por eso todavía viven cerca de la pantalla.
private val BackgroundColor = Color(0xFFFAF9FF)
private val PrimaryColor = Color(0xFF7C3AED)
private val PrimaryLightColor = Color(0xFFEDE9FF)
private val TextColor = Color(0xFF172033)
private val SecondaryTextColor = Color(0xFF667085)

// Debe ser lo bastante breve para que completar siga siendo inmediato, pero visible.
private const val CompletionAnimationDurationMillis = 180

// Modelo visual mínimo para la Fase 2. Más adelante Room aportará los datos reales.
private data class SampleReminder(
    val id: Int,
    val text: String,
    val createdAtLabel: String,
)

// Datos de prueba ordenados de la nota más reciente a la más antigua.
// La persistencia real llegará en la Fase 3.
private val sampleReminders = listOf(
    SampleReminder(
        id = 4,
        text = "Comprar tornillos, cinta aislante y una bombilla para arreglar la lámpara del pasillo",
        createdAtLabel = "Hoy, 09:35",
    ),
    SampleReminder(3, "Recoger a los niños a las cinco", "Hoy, 09:20"),
    SampleReminder(2, "Llamar al dentista", "Ayer, 18:45"),
    SampleReminder(1, "Enviar el informe", "Ayer, 17:20"),
)

/**
 * Única pantalla principal de Pensieve.
 *
 * Durante la Fase 2 guarda sus recordatorios solo en memoria. Los callbacks
 * permiten que en fases posteriores la actividad o un ViewModel conecten las
 * acciones con la persistencia y las notificaciones, sin cambiar los componentes visuales.
 */
@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onNewNoteClick: () -> Unit = {},
    onVoiceNoteClick: () -> Unit = {},
    onCompleteReminderClick: (Int) -> Unit = {},
) {
    // remember conserva la misma lista durante las recomposiciones: por ejemplo,
    // cuando eliminamos una tarjeta. No sobrevive al cierre de la app ni a recrear
    // la actividad; Room sustituirá este estado temporal en la Fase 3.
    //
    // toMutableStateList crea una lista observable. Compose detecta add/remove
    // y actualiza automáticamente las partes de la interfaz que dependen de ella.
    val reminders = remember { sampleReminders.toMutableStateList() }

    // Las operaciones de completar combinan una espera corta de animación y un snackbar.
    // El scope se cancela automáticamente cuando HomeScreen sale de composición.
    val screenScope = rememberCoroutineScope()
    val completedMessage = stringResource(R.string.reminder_completed)
    val undoLabel = stringResource(R.string.undo)
    val justNowLabel = stringResource(R.string.created_just_now)

    // El campo de texto solo existe mientras el usuario crea una nota. En el estado
    // normal no ocupamos espacio con una entrada vacía, como requiere el flujo del MVP.
    var isManualInputVisible by remember { mutableStateOf(false) }
    var manualReminderText by remember { mutableStateOf("") }
    var nextReminderId by remember {
        mutableStateOf(sampleReminders.maxOf { reminder -> reminder.id } + 1)
    }
    val completingReminderIds = remember { mutableStateListOf<Int>() }

    fun saveManualReminder() {
        val reminderText = manualReminderText.trim()

        // El botón se desactiva para texto vacío, pero esta comprobación también protege
        // la acción "Hecho" del teclado.
        if (reminderText.isEmpty()) return

        // Insertar en el índice 0 conserva el orden de más reciente a más antigua.
        reminders.add(
            index = 0,
            element = SampleReminder(
                id = nextReminderId,
                text = reminderText,
                createdAtLabel = justNowLabel,
            ),
        )
        nextReminderId += 1
        manualReminderText = ""
        isManualInputVisible = false
    }

    fun completeReminder(reminder: SampleReminder) {
        // Evita programar dos eliminaciones si el usuario toca dos veces el mismo círculo.
        if (reminder.id in completingReminderIds) return

        // AnimatedVisibility observa este estado. Primero reproduce la salida y,
        // cuando termina, eliminamos el dato que alimenta a LazyColumn.
        completingReminderIds.add(reminder.id)

        screenScope.launch {
            delay(CompletionAnimationDurationMillis.toLong())

            val removedIndex = reminders.indexOf(reminder)
            if (removedIndex == -1) {
                completingReminderIds.remove(reminder.id)
                return@launch
            }

            reminders.removeAt(removedIndex)
            completingReminderIds.remove(reminder.id)

            // En esta fase no tiene efecto; después eliminará la nota de Room
            // y retirará su notificación persistente.
            onCompleteReminderClick(reminder.id)

            val snackbarResult = snackbarHostState.showSnackbar(
                message = completedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Long,
            )

            if (snackbarResult == SnackbarResult.ActionPerformed) {
                // Si se completaron más notas mientras el snackbar estaba visible,
                // la lista puede ser más corta. Ajustamos el índice para restaurar
                // sin salir de sus límites.
                val restoreIndex = removedIndex.coerceAtMost(reminders.size)
                reminders.add(restoreIndex, reminder)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        PensieveHeader()

        Spacer(modifier = Modifier.height(28.dp))

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
                items = reminders,
                key = { reminder -> reminder.id },
            ) { reminder ->
                AnimatedVisibility(
                    visible = reminder.id !in completingReminderIds,
                    exit = fadeOut(
                        animationSpec = tween(CompletionAnimationDurationMillis),
                    ) + shrinkVertically(
                        animationSpec = tween(CompletionAnimationDurationMillis),
                    ),
                ) {
                    ReminderCard(
                        text = reminder.text,
                        createdAtLabel = reminder.createdAtLabel,
                        onCompleteClick = { completeReminder(reminder) },
                    )
                }
            }
        }

        if (isManualInputVisible) {
            ManualReminderInput(
                text = manualReminderText,
                onTextChange = { newText -> manualReminderText = newText },
                onSave = ::saveManualReminder,
                onCancel = {
                    manualReminderText = ""
                    isManualInputVisible = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(top = 16.dp),
            )
        }

        HomeActions(
            onNewNoteClick = {
                isManualInputVisible = true
                onNewNoteClick()
            },
            onVoiceNoteClick = onVoiceNoteClick,
        )
    }
}

/**
 * Entrada temporal para una nota manual.
 *
 * El componente recibe el texto y las acciones desde HomeScreen: así no crea
 * una segunda fuente de estado y HomeScreen conserva el control de la lista.
 */
@Composable
private fun ManualReminderInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val isSaveEnabled = text.trim().isNotEmpty()

    // LaunchedEffect se ejecuta después de que el TextField entre en la composición.
    // En ese momento puede recibir foco y abrir el teclado sin que el usuario toque otra vez.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun saveAndHideKeyboard() {
        if (isSaveEnabled) {
            keyboardController?.hide()
            onSave()
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(text = stringResource(R.string.new_reminder_placeholder))
                },
                singleLine = false,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { saveAndHideKeyboard() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PrimaryLightColor,
                    unfocusedContainerColor = PrimaryLightColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onCancel()
                    },
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                FilledTonalButton(
                    onClick = ::saveAndHideKeyboard,
                    enabled = isSaveEnabled,
                ) {
                    Text(text = stringResource(R.string.save_reminder))
                }
            }
        }
    }
}

@Composable
private fun PensieveHeader() {
    val appName = stringResource(R.string.app_name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                // La inicial repite el nombre que aparece a su lado, así que un lector
                // de pantalla no debe anunciarla como contenido independiente.
                .clearAndSetSemantics { },
            shape = RoundedCornerShape(14.dp),
            color = PrimaryColor,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = appName.take(1),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = appName,
            // Indica al lector de pantalla que este texto es el encabezado de la pantalla.
            modifier = Modifier.semantics { heading() },
            color = TextColor,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// La tarjeta recibe los datos y una acción; no conoce ni modifica la lista completa.
// Así seguirá siendo reutilizable cuando el estado pase a un ViewModel.
@Composable
private fun ReminderCard(
    text: String,
    createdAtLabel: String,
    onCompleteClick: () -> Unit,
) {
    // La descripción incluye el texto de la nota para que la acción sea clara
    // cuando se navega por la pantalla con TalkBack.
    val completeDescription = stringResource(R.string.complete_reminder, text)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 104.dp)
                .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    color = TextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = createdAtLabel,
                    color = SecondaryTextColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onCompleteClick,
                modifier = Modifier
                    // El círculo visible mide 30 dp, pero el área pulsable alcanza 48 dp,
                    // que es el mínimo recomendado para una interacción táctil cómoda.
                    .size(48.dp)
                    .semantics { contentDescription = completeDescription },
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(3.dp, PrimaryColor, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun HomeActions(
    onNewNoteClick: () -> Unit,
    onVoiceNoteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalButton(
            onClick = onNewNoteClick,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = PrimaryLightColor,
                contentColor = PrimaryColor,
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                // El botón ya tiene texto; ocultamos el icono para evitar que TalkBack
                // anuncie dos veces la misma acción.
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.new_reminder),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        FloatingActionButton(
            onClick = onVoiceNoteClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            containerColor = PrimaryColor,
            contentColor = Color.White,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = stringResource(R.string.create_reminder_by_voice),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

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
        // El preview no usa MainActivity, así que crea su propio estado de snackbar.
        val snackbarHostState = remember { SnackbarHostState() }
        HomeScreen(snackbarHostState = snackbarHostState)
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
        val snackbarHostState = remember { SnackbarHostState() }
        HomeScreen(snackbarHostState = snackbarHostState)
    }
}
