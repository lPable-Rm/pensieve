package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R

/** Botones inferiores para crear una nota manual o empezar a hablar. */
@Composable
internal fun HomeActions(
    onNewNoteClick: () -> Unit,
    onVoiceNoteClick: () -> Unit,
    isManualInputVisible: Boolean,
    isVoiceInputActive: Boolean,
    isSavingReminder: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val canCreateManualReminder = !isVoiceInputActive && !isSavingReminder
    val canCreateVoiceReminder =
        !isManualInputVisible && !isVoiceInputActive && !isSavingReminder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalButton(
            onClick = onNewNoteClick,
            enabled = canCreateManualReminder,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.new_reminder),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        FloatingActionButton(
            onClick = {
                if (canCreateVoiceReminder) onVoiceNoteClick()
            },
            modifier = Modifier
                .size(64.dp)
                .semantics {
                    if (!canCreateVoiceReminder) disabled()
                },
            shape = CircleShape,
            // FloatingActionButton no tiene enabled. El callback y el color
            // cambian juntos para impedir una segunda captura simultánea.
            containerColor = if (canCreateVoiceReminder) {
                colorScheme.primary
            } else {
                colorScheme.primaryContainer
            },
            contentColor = if (canCreateVoiceReminder) {
                colorScheme.onPrimary
            } else {
                colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = stringResource(R.string.create_reminder_by_voice),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}
