package com.mvppostit.pensieve.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

/** Comprueba que un valor ausente o desconocido siempre vuelve a Lavanda. */
class PaletteIdTest {

    @Test
    fun missingPalette_usesLavender() {
        assertEquals(PaletteId.LAVENDER, PaletteId.fromStorage(null))
    }

    @Test
    fun unknownPalette_usesLavender() {
        assertEquals(PaletteId.LAVENDER, PaletteId.fromStorage("old_palette"))
    }

    @Test
    fun storedIds_roundTripToTheirPalette() {
        PaletteId.values().forEach { paletteId ->
            assertEquals(paletteId, PaletteId.fromStorage(paletteId.storageId))
        }
    }
}
