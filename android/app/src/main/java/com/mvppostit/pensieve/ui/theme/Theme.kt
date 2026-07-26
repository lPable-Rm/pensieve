package com.mvppostit.pensieve.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Construye todo el esquema a partir del identificador persistido. */
fun colorSchemeForPalette(paletteId: PaletteId): ColorScheme {
    val palette = paletteColors(paletteId)

    return lightColorScheme(
        primary = palette.primary,
        onPrimary = palette.onPrimary,
        primaryContainer = palette.primaryContainer,
        onPrimaryContainer = palette.onPrimaryContainer,
        secondary = palette.primary,
        onSecondary = palette.onPrimary,
        secondaryContainer = palette.primaryContainer,
        onSecondaryContainer = palette.onPrimaryContainer,
        tertiary = palette.primary,
        onTertiary = palette.onPrimary,
        background = NolvidaBackground,
        onBackground = NolvidaTextPrimary,
        surface = NolvidaSurface,
        onSurface = NolvidaTextPrimary,
        surfaceVariant = palette.primaryContainer,
        onSurfaceVariant = NolvidaTextSecondary,
        surfaceContainerLow = NolvidaSurface,
        inverseSurface = NolvidaTextPrimary,
        inverseOnSurface = NolvidaSurface,
        inversePrimary = palette.primaryContainer,
        outline = NolvidaTextSecondary,
    )
}

@Composable
fun PensieveTheme(
    paletteId: PaletteId = PaletteId.LAVENDER,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeForPalette(paletteId),
        typography = Typography,
        content = content,
    )
}
