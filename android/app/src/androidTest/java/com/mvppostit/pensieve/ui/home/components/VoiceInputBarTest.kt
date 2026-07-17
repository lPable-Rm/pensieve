package com.mvppostit.pensieve.ui.home.components

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mvppostit.pensieve.ui.home.VoiceInputError
import com.mvppostit.pensieve.ui.home.VoiceInputState
import com.mvppostit.pensieve.ui.theme.PensieveTheme
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test

/** Comprueba los estados visibles sin depender de un micrófono o proveedor real. */
class VoiceInputBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun listening_showsThePartialAndTheStopAction() {
        setVoiceInputContent(
            VoiceInputState.Listening(
                partialText = "Comprar pan",
                startedAtMillis = System.currentTimeMillis() + 60_000,
            ),
        )

        composeTestRule.onNodeWithText("Escuchando…")
            .assertIsDisplayed()
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText("Comprar pan")
            .assertIsDisplayed()
            .assert(isHiddenFromAccessibility)
            .assert(hasNoLiveRegion)
        composeTestRule.onNodeWithText("0:00").assert(hasNoLiveRegion)
        composeTestRule.onNodeWithText("Detener grabación").assertIsDisplayed()
    }

    @Test
    fun processing_announcesTheStableStateAndCancels() {
        var cancelClicks = 0
        setVoiceInputContent(
            state = VoiceInputState.Processing,
            onCancel = { cancelClicks += 1 },
        )

        composeTestRule.onNodeWithText("Transcribiendo…")
            .assertIsDisplayed()
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText("Cancelar").performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, cancelClicks)
        }
    }

    @Test
    fun review_showsTheEditableTextAndSaveAction() {
        setVoiceInputContent(VoiceInputState.Review("Llamar al taller"))

        composeTestRule.onNodeWithText("Llamar al taller")
            .assertIsDisplayed()
            .assert(hasNoLiveRegion)
        composeTestRule.onNodeWithText("Guardar recordatorio").assertIsDisplayed()
    }

    @Test
    fun saving_showsAVisibleProgressState() {
        setVoiceInputContent(VoiceInputState.Saving("Llamar al taller"))

        composeTestRule.onNodeWithText("Guardando…")
            .assertIsDisplayed()
            .assert(hasPoliteLiveRegion)
    }

    @Test
    fun permanentlyDeniedPermission_offersApplicationSettings() {
        setVoiceInputContent(
            VoiceInputState.Error(
                VoiceInputError.PermissionDenied(canOpenSettings = true),
            ),
        )

        composeTestRule.onNodeWithText("Permite el micrófono para crear una nota por voz.")
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText("Abrir ajustes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cerrar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Reintentar").assertDoesNotExist()
    }

    @Test
    fun recoverablePermissionDenial_offersRetryAndInvokesIt() {
        var retryClicks = 0
        setVoiceInputContent(
            state = VoiceInputState.Error(
                VoiceInputError.PermissionDenied(canOpenSettings = false),
            ),
            onRetry = { retryClicks += 1 },
        )

        composeTestRule.onNodeWithText("Reintentar")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, retryClicks)
        }
    }

    private fun setVoiceInputContent(
        state: VoiceInputState,
        onCancel: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            PensieveTheme {
                VoiceInputBar(
                    state = state,
                    onStopRecording = {},
                    onTextChange = {},
                    onSave = {},
                    onCancel = onCancel,
                    onRetry = onRetry,
                    onOpenSettings = {},
                )
            }
        }
    }

    private companion object {
        val hasPoliteLiveRegion = SemanticsMatcher.expectValue(
            SemanticsProperties.LiveRegion,
            LiveRegionMode.Polite,
        )
        val hasNoLiveRegion = SemanticsMatcher.keyNotDefined(SemanticsProperties.LiveRegion)
        val isHiddenFromAccessibility =
            SemanticsMatcher.keyIsDefined(SemanticsProperties.HideFromAccessibility)
    }
}
