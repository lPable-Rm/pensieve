package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R

/**
 * Burbuja compacta para escribir un recordatorio sin dejar un campo vacío
 * ocupando la pantalla principal cuando no se está creando nada.
 */
@Composable
internal fun ManualReminderInput(
    text: String,
    isSaving: Boolean,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val colorScheme = MaterialTheme.colorScheme
    val isSaveEnabled = text.trim().isNotEmpty() && !isSaving

    // Al aparecer el componente pedimos foco y mostramos el teclado. Así la
    // persona puede empezar a escribir sin tocar el campo una segunda vez.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun saveReminder() {
        // La misma protección sirve para el botón y para la tecla «Done».
        if (isSaveEnabled) onSave()
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                enabled = !isSaving,
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
                keyboardActions = KeyboardActions(onDone = { saveReminder() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.primaryContainer,
                    unfocusedContainerColor = colorScheme.primaryContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = colorScheme.onPrimaryContainer,
                ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    enabled = !isSaving,
                    onClick = {
                        keyboardController?.hide()
                        onCancel()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorScheme.onSurface,
                    ),
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                FilledTonalButton(
                    onClick = ::saveReminder,
                    enabled = isSaveEnabled,
                ) {
                    Text(text = stringResource(R.string.save_reminder))
                }
            }
        }
    }
}
