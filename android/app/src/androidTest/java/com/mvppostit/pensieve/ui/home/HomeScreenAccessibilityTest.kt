package com.mvppostit.pensieve.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.mvppostit.pensieve.R
import com.mvppostit.pensieve.ui.theme.PensieveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Verifica que el micrófono comunique a accesibilidad su disponibilidad real. */
class HomeScreenAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun voiceButton_isDisabledWhileVoiceInputIsActive() {
        var voiceClicks = 0
        setHomeContent(
            state = HomeUiState(
                voiceInputState = VoiceInputState.Error(VoiceInputError.NoSpeech),
            ),
            onVoiceClick = { voiceClicks += 1 },
        )

        composeTestRule.onNodeWithContentDescription(
            stringResource(R.string.create_reminder_by_voice),
        )
            .assertIsNotEnabled()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(0, voiceClicks)
        }
    }

    @Test
    fun voiceButton_isEnabledInTheNormalState() {
        var voiceClicks = 0
        setHomeContent(
            state = HomeUiState(),
            onVoiceClick = { voiceClicks += 1 },
        )

        composeTestRule.onNodeWithContentDescription(
            stringResource(R.string.create_reminder_by_voice),
        )
            .assertIsEnabled()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, voiceClicks)
        }
    }

    @Test
    fun newNoteButton_isDisabledWhileManualInputIsVisible() {
        setHomeContent(
            state = HomeUiState(isManualInputVisible = true),
            onVoiceClick = {},
        )

        composeTestRule
            .onNodeWithText(stringResource(R.string.new_reminder_placeholder))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(stringResource(R.string.new_reminder))
            .assertIsNotEnabled()
    }

    @Test
    fun emptyState_voiceActionRemainsReachableWithLargeTextAndLittleHeight() {
        var voiceClicks = 0

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
                        .height(480.dp),
                ) {
                    PensieveTheme {
                        HomeScreen(
                            uiState = HomeUiState(),
                            onVoiceNoteClick = { voiceClicks += 1 },
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText(stringResource(R.string.widget_tap_to_speak))
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, voiceClicks)
        }
    }

    private fun setHomeContent(
        state: HomeUiState,
        onVoiceClick: () -> Unit,
    ) {
        composeTestRule.setContent {
            PensieveTheme {
                HomeScreen(
                    uiState = state,
                    onVoiceNoteClick = onVoiceClick,
                )
            }
        }
    }

    private fun stringResource(resourceId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resourceId)
}
