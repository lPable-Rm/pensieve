package com.mvppostit.pensieve.ui.home

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
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

        composeTestRule.onNodeWithContentDescription("Crear nota por voz")
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

        composeTestRule.onNodeWithContentDescription("Crear nota por voz")
            .assertIsEnabled()
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
}
