package com.mvppostit.pensieve.ui.appearance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.ui.theme.NolvidaPalette
import com.mvppostit.pensieve.ui.theme.PaletteId
import com.mvppostit.pensieve.ui.theme.paletteColors

/**
 * Selector pequeño que vive encima de Home, no en una pantalla de ajustes.
 *
 * Cada fila guarda un identificador estable. La tabla de colores se comparte
 * con el tema y con el widget, para que una misma elección se vea igual en
 * todos los lugares.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearancePaletteSheet(
    selectedPalette: PaletteId,
    onPaletteSelected: (PaletteId) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .selectableGroup()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Text(
                text = stringResource(R.string.appearance_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = stringResource(R.string.appearance_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(16.dp))

            PaletteId.values().forEach { paletteId ->
                PaletteRow(
                    paletteId = paletteId,
                    colors = paletteColors(paletteId),
                    selected = paletteId == selectedPalette,
                    onClick = { onPaletteSelected(paletteId) },
                )
            }

            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.appearance_applies_immediately),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PaletteRow(
    paletteId: PaletteId,
    colors: NolvidaPalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val paletteName = stringResource(paletteNameResource(paletteId))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaletteSwatches(colors = colors)
        Text(
            text = paletteName,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.onSurface,
                unselectedColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}

/** Dos círculos bastan para enseñar el color principal y su superficie clara. */
@Composable
private fun PaletteSwatches(colors: NolvidaPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .background(colors.primary, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .background(colors.primaryContainer, CircleShape),
        )
    }
}

private fun paletteNameResource(paletteId: PaletteId): Int = when (paletteId) {
    PaletteId.LAVENDER -> R.string.palette_lavender
    PaletteId.PETROL -> R.string.palette_petrol
    PaletteId.DEEP_BLUE -> R.string.palette_deep_blue
    PaletteId.SOFT_CORAL -> R.string.palette_soft_coral
    PaletteId.GRAYSCALE -> R.string.palette_grayscale
    PaletteId.TURQUOISE -> R.string.palette_turquoise
}
