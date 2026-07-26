package com.mvppostit.pensieve.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protege los pares semánticos que consumen texto, iconos y controles.
 *
 * `primary` es un color de marca y no tiene que contrastar por sí solo contra
 * cada superficie. El contrato accesible siempre incluye su color `on*`.
 */
class ColorContrastTest {

    @Test
    fun everyPalette_hasAccessibleTextRoles() {
        PaletteId.entries.forEach { paletteId ->
            val scheme = colorSchemeForPalette(paletteId)

            assertContrastAtLeast(
                label = "$paletteId: botón principal",
                foreground = scheme.onPrimary,
                background = scheme.primary,
                minimum = NormalTextMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: contenido tonal",
                foreground = scheme.onPrimaryContainer,
                background = scheme.primaryContainer,
                minimum = NormalTextMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: texto de superficie",
                foreground = scheme.onSurface,
                background = scheme.surface,
                minimum = NormalTextMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: texto secundario",
                foreground = scheme.onSurfaceVariant,
                background = scheme.surfaceVariant,
                minimum = NormalTextMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: acción de snackbar",
                foreground = scheme.inversePrimary,
                background = scheme.inverseSurface,
                minimum = NormalTextMinimum,
            )
        }
    }

    @Test
    fun everyPalette_hasAccessibleControlRoles() {
        PaletteId.entries.forEach { paletteId ->
            val scheme = colorSchemeForPalette(paletteId)

            assertContrastAtLeast(
                label = "$paletteId: radio y progreso",
                foreground = scheme.onSurface,
                background = scheme.surfaceContainerLow,
                minimum = EssentialControlMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: contorno de muestras",
                foreground = scheme.outline,
                background = scheme.surfaceContainerLow,
                minimum = EssentialControlMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: micrófono del widget",
                foreground = scheme.onPrimaryContainer,
                background = scheme.primaryContainer,
                minimum = EssentialControlMinimum,
            )
            assertContrastAtLeast(
                label = "$paletteId: detener grabación",
                foreground = NolvidaRecording,
                background = scheme.primaryContainer,
                minimum = EssentialControlMinimum,
            )
        }
    }

    @Test
    fun recordingIndicator_remainsVisibleOnItsSoftBackground() {
        val recordingBackground = NolvidaRecording
            .copy(alpha = 0.12f)
            .compositeOver(NolvidaSurface)

        assertContrastAtLeast(
            label = "indicador de grabación",
            foreground = NolvidaRecording,
            background = recordingBackground,
            minimum = EssentialControlMinimum,
        )
    }

    private fun assertContrastAtLeast(
        label: String,
        foreground: Color,
        background: Color,
        minimum: Double,
    ) {
        val actual = contrastRatio(foreground, background)
        assertTrue(
            "$label tiene contraste $actual:1; mínimo requerido $minimum:1",
            actual + ContrastTolerance >= minimum,
        )
    }

    private fun contrastRatio(first: Color, second: Color): Double {
        val lighter = maxOf(relativeLuminance(first), relativeLuminance(second))
        val darker = minOf(relativeLuminance(first), relativeLuminance(second))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Color): Double =
        0.2126 * linearChannel(color.red.toDouble()) +
            0.7152 * linearChannel(color.green.toDouble()) +
            0.0722 * linearChannel(color.blue.toDouble())

    private fun linearChannel(channel: Double): Double =
        if (channel <= 0.04045) {
            channel / 12.92
        } else {
            Math.pow((channel + 0.055) / 1.055, 2.4)
        }

    private companion object {
        const val NormalTextMinimum = 4.5
        const val EssentialControlMinimum = 3.0
        const val ContrastTolerance = 0.0001
    }
}
