package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R

/**
 * Cabecera de la única pantalla principal.
 *
 * La única acción abre el selector de paletas que coordina MainActivity.
 */
@Composable
internal fun NolvidaHeader(
    modifier: Modifier = Modifier,
    onAppearanceClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val appearanceDescription = stringResource(R.string.appearance_open)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(48.dp)
                // El símbolo ya es decorativo porque el nombre se anuncia
                // como heading en la zona central.
                .clearAndSetSemantics { },
            shape = RoundedCornerShape(14.dp),
            color = colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_nolvida),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colorScheme.onPrimaryContainer,
                )
            }
        }

        // La zona central queda entre los dos extremos táctiles de 48 dp.
        // Así el título permanece centrado incluso si el texto necesita elipsis.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() },
            )
        }

        IconButton(
            onClick = onAppearanceClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp)
                .semantics {
                    this.contentDescription = appearanceDescription
                },
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_palette),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
