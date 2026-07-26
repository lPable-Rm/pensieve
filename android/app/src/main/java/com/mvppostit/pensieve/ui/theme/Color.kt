package com.mvppostit.pensieve.ui.theme

import androidx.compose.ui.graphics.Color

/** Identificadores estables: DataStore guarda estos textos, no colores hexadecimales. */
enum class PaletteId(val storageId: String) {
    LAVENDER("lavender"),
    PETROL("petrol"),
    DEEP_BLUE("deep_blue"),
    SOFT_CORAL("soft_coral"),
    GRAYSCALE("grayscale"),
    TURQUOISE("turquoise"),
    ;

    companion object {
        /** Un valor antiguo o escrito a mano nunca debe dejar la app sin tema. */
        fun fromStorage(value: String?): PaletteId =
            values().firstOrNull { palette -> palette.storageId == value } ?: LAVENDER
    }
}

/** Colores que cambian cuando la persona elige una paleta. */
data class NolvidaPalette(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color,
)

val LavenderPrimary = Color(0xFF7C3AED)
val LavenderPrimaryContainer = Color(0xFFEDE9FF)
val NolvidaBackground = Color(0xFFFAF9FF)
val NolvidaSurface = Color(0xFFFFFFFF)
val NolvidaTextPrimary = Color(0xFF172033)
// Este tono mantiene la jerarquía visual, pero supera el contraste mínimo
// para texto normal sobre las superficies blancas y lavanda del tema.
val NolvidaTextSecondary = Color(0xFF596275)
val NolvidaRecording = Color(0xFFE5484D)

/**
 * Tabla única de diseño.
 *
 * Si añadimos una paleta, el compilador nos obliga a decidir también sus
 * colores del widget y de Compose, porque ambos leen esta misma función.
 */
fun paletteColors(paletteId: PaletteId): NolvidaPalette = when (paletteId) {
    PaletteId.LAVENDER -> NolvidaPalette(
        primary = LavenderPrimary,
        primaryContainer = LavenderPrimaryContainer,
        onPrimary = NolvidaSurface,
        onPrimaryContainer = NolvidaTextPrimary,
    )

    PaletteId.PETROL -> NolvidaPalette(
        primary = Color(0xFF0D9488),
        primaryContainer = Color(0xFFE6F6F3),
        onPrimary = Color(0xFF111827),
        onPrimaryContainer = NolvidaTextPrimary,
    )

    PaletteId.DEEP_BLUE -> NolvidaPalette(
        primary = Color(0xFF2563EB),
        primaryContainer = Color(0xFFE7F0FF),
        onPrimary = NolvidaSurface,
        onPrimaryContainer = NolvidaTextPrimary,
    )

    PaletteId.SOFT_CORAL -> NolvidaPalette(
        primary = Color(0xFFFF6B6B),
        primaryContainer = Color(0xFFFFF1EF),
        onPrimary = NolvidaTextPrimary,
        onPrimaryContainer = NolvidaTextPrimary,
    )

    PaletteId.GRAYSCALE -> NolvidaPalette(
        primary = Color(0xFF6B7280),
        primaryContainer = Color(0xFFF3F4F6),
        onPrimary = NolvidaSurface,
        onPrimaryContainer = NolvidaTextPrimary,
    )

    PaletteId.TURQUOISE -> NolvidaPalette(
        primary = Color(0xFF06B6D4),
        primaryContainer = Color(0xFFE0F7FA),
        onPrimary = NolvidaTextPrimary,
        onPrimaryContainer = NolvidaTextPrimary,
    )
}
