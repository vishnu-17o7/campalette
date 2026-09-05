package com.vishnu.campalette.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.vishnu.campalette.ui.theme.CampaletteTheme
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import dev.chrisbanes.haze.HazeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BottomBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun islandIsCompactAndTabsRemainSelectable() {
        var currentScreen by mutableStateOf(AppScreen.Live)

        setBottomBar(currentScreen = { currentScreen }) { currentScreen = it }

        composeRule.onNodeWithTag("bottom_navigation_island")
            .assertWidthIsEqualTo(252.dp)
            .assertHeightIsEqualTo(60.dp)
        composeRule.onNodeWithContentDescription("Camera").assertIsSelected()
        composeRule.onNodeWithContentDescription("Library")
            .assertIsNotSelected()
            .performClick()
        composeRule.onNodeWithContentDescription("Library").assertIsSelected()
        assertEquals(AppScreen.History, currentScreen)
    }

    @Test
    fun horizontalDragChangesScreensInBothDirections() {
        var currentScreen by mutableStateOf(AppScreen.Live)

        setBottomBar(currentScreen = { currentScreen }) { currentScreen = it }

        composeRule.onNodeWithTag("bottom_navigation_island").performTouchInput {
            swipe(
                start = center,
                end = centerRight,
                durationMillis = 320
            )
        }
        composeRule.waitUntil(timeoutMillis = 2_000) {
            currentScreen == AppScreen.Settings
        }
        composeRule.onNodeWithContentDescription("Settings").assertIsSelected()

        composeRule.onNodeWithTag("bottom_navigation_island").performTouchInput {
            swipe(
                start = centerRight,
                end = centerLeft,
                durationMillis = 420
            )
        }
        composeRule.waitUntil(timeoutMillis = 2_000) {
            currentScreen == AppScreen.History
        }
        composeRule.onNodeWithContentDescription("Library").assertIsSelected()
    }

    @Test
    fun reducedMotionKeepsTapNavigationAvailable() {
        var currentScreen by mutableStateOf(AppScreen.Live)

        setBottomBar(
            currentScreen = { currentScreen },
            reducedMotion = true,
            onScreenSelected = { currentScreen = it }
        )

        composeRule.onNodeWithContentDescription("Settings").performClick()
        composeRule.onNodeWithContentDescription("Settings").assertIsSelected()
        assertEquals(AppScreen.Settings, currentScreen)
    }

    @Test
    fun rtlPlacesLogicalStartOnTheRightAndTapSelectsTheRequestedTab() {
        var currentScreen by mutableStateOf(AppScreen.History)

        setBottomBar(
            currentScreen = { currentScreen },
            layoutDirection = LayoutDirection.Rtl,
            onScreenSelected = { currentScreen = it }
        )

        val libraryCenterX = composeRule.onNodeWithContentDescription("Library")
            .fetchSemanticsNode().boundsInRoot.center.x
        val cameraCenterX = composeRule.onNodeWithContentDescription("Camera")
            .fetchSemanticsNode().boundsInRoot.center.x
        val settingsCenterX = composeRule.onNodeWithContentDescription("Settings")
            .fetchSemanticsNode().boundsInRoot.center.x

        assertTrue("Library should occupy logical start in RTL", libraryCenterX > cameraCenterX)
        assertTrue("Settings should occupy logical end in RTL", settingsCenterX < cameraCenterX)
        composeRule.onNodeWithContentDescription("Library").assertIsSelected()

        composeRule.onNodeWithContentDescription("Settings").performClick()

        composeRule.onNodeWithContentDescription("Library").assertIsNotSelected()
        composeRule.onNodeWithContentDescription("Settings").assertIsSelected()
        assertEquals(AppScreen.Settings, currentScreen)
    }

    @Test
    fun rtlHorizontalDragMapsPhysicalDirectionToLogicalDestinations() {
        var currentScreen by mutableStateOf(AppScreen.Live)

        setBottomBar(
            currentScreen = { currentScreen },
            layoutDirection = LayoutDirection.Rtl,
            onScreenSelected = { currentScreen = it }
        )

        composeRule.onNodeWithTag("bottom_navigation_island").performTouchInput {
            swipe(
                start = center,
                end = centerLeft,
                durationMillis = 320
            )
        }
        composeRule.waitUntil(timeoutMillis = 2_000) {
            currentScreen == AppScreen.Settings
        }
        composeRule.onNodeWithContentDescription("Settings").assertIsSelected()

        composeRule.onNodeWithTag("bottom_navigation_island").performTouchInput {
            swipe(
                start = centerLeft,
                end = centerRight,
                durationMillis = 420
            )
        }
        composeRule.waitUntil(timeoutMillis = 2_000) {
            currentScreen == AppScreen.History
        }
        composeRule.onNodeWithContentDescription("Library").assertIsSelected()
    }

    @Test
    fun reducedMotionIgnoresFlingProjectionButKeepsDistanceBasedDragNavigation() {
        var currentScreen by mutableStateOf(AppScreen.Live)

        setBottomBar(
            currentScreen = { currentScreen },
            reducedMotion = true,
            onScreenSelected = { currentScreen = it }
        )

        composeRule.onNodeWithTag("bottom_navigation_island").performTouchInput {
            val shortTravel = Offset(width * 0.10f, 0f)
            swipe(
                start = center,
                end = center + shortTravel,
                durationMillis = 48
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Camera").assertIsSelected()
        assertEquals(AppScreen.Live, currentScreen)

        composeRule.onNodeWithTag("bottom_navigation_island").performTouchInput {
            val deliberateTravel = Offset(width * 0.24f, 0f)
            swipe(
                start = center,
                end = center + deliberateTravel,
                durationMillis = 420
            )
        }
        composeRule.waitUntil(timeoutMillis = 2_000) {
            currentScreen == AppScreen.Settings
        }
        composeRule.onNodeWithContentDescription("Settings").assertIsSelected()
    }

    private fun setBottomBar(
        currentScreen: () -> AppScreen,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        reducedMotion: Boolean = false,
        onScreenSelected: (AppScreen) -> Unit
    ) {
        composeRule.setContent {
            CampaletteTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides layoutDirection,
                    LocalReducedMotion provides reducedMotion
                ) {
                    Box(Modifier.fillMaxSize()) {
                        BottomBar(
                            currentScreen = currentScreen(),
                            onScreenSelected = onScreenSelected,
                            hazeState = HazeState()
                        )
                    }
                }
            }
        }
    }
}
