package com.refillfinder.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.refillfinder.app.ui.screens.loading.LoadingScreen
import com.refillfinder.app.ui.theme.ReFillFinderTheme
import org.junit.Rule
import org.junit.Test

/**
 * Basic Jetpack Compose UI Tests.
 *
 * Syllabus: Basic Jetpack Compose UI Testing
 * Run with: ./gradlew connectedAndroidTest
 */
class ComposeUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── LoadingScreen Tests ───────────────────────────────────────────

    @Test
    fun loadingScreen_displaysAppName() {
        composeTestRule.setContent {
            ReFillFinderTheme {
                LoadingScreen(onLoadingComplete = {})
            }
        }
        composeTestRule
            .onNodeWithText("ReFillFinder")
            .assertIsDisplayed()
    }

    @Test
    fun loadingScreen_displaysCampusSubtitle() {
        composeTestRule.setContent {
            ReFillFinderTheme {
                LoadingScreen(onLoadingComplete = {})
            }
        }
        composeTestRule
            .onNodeWithText("Campus Water Intelligence")
            .assertIsDisplayed()
    }

    @Test
    fun loadingScreen_displaysCoolerCount() {
        composeTestRule.setContent {
            ReFillFinderTheme {
                LoadingScreen(onLoadingComplete = {})
            }
        }
        composeTestRule
            .onNodeWithText("300 Coolers • 60 Blocks • Floors 1,3,5,7,9")
            .assertIsDisplayed()
    }
}
