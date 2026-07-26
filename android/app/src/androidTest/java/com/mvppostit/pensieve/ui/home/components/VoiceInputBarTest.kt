package com.mvppostit.pensieve.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mvppostit.pensieve.R
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
        var stopClicks = 0
        setVoiceInputContent(
            state = VoiceInputState.Listening(
                partialText = "Comprar pan",
                startedAtMillis = System.currentTimeMillis() + 60_000,
            ),
            onStopRecording = { stopClicks += 1 },
        )

        composeTestRule.onNodeWithText(stringResource(R.string.voice_listening))
            .assertIsDisplayed()
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText("Comprar pan")
            .assertIsDisplayed()
            .assert(isHiddenFromAccessibility)
            .assert(hasNoLiveRegion)
        composeTestRule.onNodeWithText("0:00").assert(hasNoLiveRegion)
        composeTestRule
            .onNodeWithContentDescription(stringResource(R.string.voice_stop_recording))
            .assertIsDisplayed()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, stopClicks)
        }
    }

    @Test
    fun processing_announcesTheStableStateAndCancels() {
        var cancelClicks = 0
        setVoiceInputContent(
            state = VoiceInputState.Processing,
            onCancel = { cancelClicks += 1 },
        )

        composeTestRule.onNodeWithText(stringResource(R.string.voice_processing))
            .assertIsDisplayed()
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText(stringResource(R.string.voice_cancel)).performClick()
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
        composeTestRule.onNodeWithText(stringResource(R.string.voice_save_reminder))
            .assertIsDisplayed()
    }

    @Test
    fun review_actionsRemainReachableWithLargeTextAndLittleHeight() {
        composeTestRule.setContent {
            val currentDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = currentDensity.density,
                    fontScale = 1.8f,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(160.dp),
                ) {
                    PensieveTheme {
                        VoiceInputBar(
                            state = VoiceInputState.Review(
                                "Comprar leche, huevos y pan integral para mañana",
                            ),
                            onStopRecording = {},
                            onTextChange = {},
                            onSave = {},
                            onCancel = {},
                            onRetry = {},
                            onOpenSettings = {},
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText(stringResource(R.string.voice_save_reminder))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(stringResource(R.string.voice_cancel))
            .assertIsDisplayed()
    }

    @Test
    fun saving_showsAVisibleProgressState() {
        setVoiceInputContent(VoiceInputState.Saving("Llamar al taller"))

        composeTestRule.onNodeWithText(stringResource(R.string.voice_saving))
            .assertIsDisplayed()
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText(stringResource(R.string.voice_saving_support))
            .assertIsDisplayed()
    }

    @Test
    fun permanentlyDeniedPermission_offersApplicationSettings() {
        setVoiceInputContent(
            VoiceInputState.Error(
                VoiceInputError.PermissionDenied(canOpenSettings = true),
            ),
        )

        composeTestRule.onNodeWithText(stringResource(R.string.voice_permission_denied))
            .assert(hasPoliteLiveRegion)
        composeTestRule.onNodeWithText(stringResource(R.string.voice_open_settings))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(stringResource(R.string.voice_close))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(stringResource(R.string.voice_retry))
            .assertDoesNotExist()
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

        composeTestRule.onNodeWithText(stringResource(R.string.voice_retry))
            .assertIsDisplayed()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, retryClicks)
        }
    }

    private fun setVoiceInputContent(
        state: VoiceInputState,
        onStopRecording: () -> Unit = {},
        onCancel: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            PensieveTheme {
                VoiceInputBar(
                    state = state,
                    onStopRecording = onStopRecording,
                    onTextChange = {},
                    onSave = {},
                    onCancel = onCancel,
                    onRetry = onRetry,
                    onOpenSettings = {},
                )
            }
        }
    }

    /** Usa el idioma real del dispositivo para que la prueba no dependa del español. */
    private fun stringResource(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)

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
