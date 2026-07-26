package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R

/**
 * Tarjeta de un recordatorio activo.
 *
 * La tarjeta solo conoce el texto, la fecha ya formateada y la acción de
 * completar. HomeScreen coordina la animación y el ViewModel mantiene la
 * persistencia fuera de este componente visual.
 */
@Composable
internal fun ReminderCard(
    text: String,
    createdAtLabel: String,
    completeDescription: String,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colorScheme.surface,
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
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = createdAtLabel,
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = onCompleteClick,
                modifier = Modifier
                    // El círculo visible mide 30 dp, pero el área pulsable
                    // alcanza 48 dp, el mínimo táctil recomendado.
                    .size(48.dp)
                    .semantics { contentDescription = completeDescription },
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = colorScheme.primaryContainer,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_voice_check),
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp),
                            tint = colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
