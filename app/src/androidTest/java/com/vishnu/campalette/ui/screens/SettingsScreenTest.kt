package com.vishnu.campalette.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.vishnu.campalette.data.ThemeMode
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.theme.CampaletteTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appearanceSelectorUpdatesSelectedMode() {
        var selectedMode by mutableStateOf(ThemeMode.System)

        composeRule.setContent {
            CampaletteTheme {
                SettingsScreen(
                    themeMode = selectedMode,
                    onThemeModeChange = { selectedMode = it },
                    paletteTintEnabled = true,
                    onPaletteTintChange = {},
                    reducedMotion = false,
                    onReducedMotionChange = {},
                    paletteColorCount = 7,
                    onPaletteColorCountChange = {},
                    hapticsEnabled = true,
                    onHapticsChange = {},
                    versionName = "1.0.0",
                    onOpenPrivacyPolicy = {},
                    onOpenTerms = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("System").assertIsSelected()
        composeRule.onNodeWithContentDescription("Dark").assertIsNotSelected().performClick()
        composeRule.onNodeWithContentDescription("Dark").assertIsSelected()
        assertEquals(ThemeMode.Dark, selectedMode)
    }

    @Test
    fun paletteSizeSliderSnapsToSupportedRange() {
        var count by mutableStateOf(4)

        composeRule.setContent {
            CampaletteTheme {
                SettingsScreen(
                    themeMode = ThemeMode.System,
                    onThemeModeChange = {},
                    paletteTintEnabled = true,
                    onPaletteTintChange = {},
                    reducedMotion = false,
                    onReducedMotionChange = {},
                    paletteColorCount = count,
                    onPaletteColorCountChange = { count = it },
                    hapticsEnabled = true,
                    onHapticsChange = {},
                    versionName = "1.0.0",
                    onOpenPrivacyPolicy = {},
                    onOpenTerms = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("4 colors")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        assertEquals(AtelierData.MIN_PALETTE_COLOR_COUNT, count)

        composeRule.onNodeWithContentDescription("2 colors")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        assertEquals(AtelierData.MAX_PALETTE_COLOR_COUNT, count)
    }
}
