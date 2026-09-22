package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.ui.screens.LoginRegisterScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LoginRegisterScreenLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w640dp-h360dp-land", sdk = [36])
    fun testLoginRegisterScreen_SmallLandscape_ZeroOverlapAndTappable() {
        var langToggled = false
        var audioToggled = false
        var loginClicked = false
        var registerClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                LoginRegisterScreen(
                    currentLanguage = "in",
                    isAudioMuted = false,
                    errorMessage = null,
                    onCheckUsernameTaken = { false },
                    onLogin = { _, _ -> loginClicked = true },
                    onRegister = { _, _ -> registerClicked = true },
                    onToggleLanguage = { langToggled = true },
                    onToggleAudioMute = { audioToggled = true }
                )
            }
        }

        // Verify Bahasa and Audio mute buttons are displayed
        val langNode = composeTestRule.onNodeWithTag("language_toggle_btn")
        val muteNode = composeTestRule.onNodeWithTag("audio_mute_btn")
        val loginTab = composeTestRule.onNodeWithTag("tab_login")
        val registerTab = composeTestRule.onNodeWithTag("tab_register")

        langNode.assertIsDisplayed()
        muteNode.assertIsDisplayed()
        loginTab.assertIsDisplayed()
        registerTab.assertIsDisplayed()

        // Verify tappability
        langNode.performClick()
        assertTrue("Language toggle button must be tappable", langToggled)

        muteNode.performClick()
        assertTrue("Audio mute button must be tappable", audioToggled)

        registerTab.performClick()
        loginTab.performClick()

        // Verify bounding boxes: Tabs must be strictly below Bahasa and Volume buttons (positive vertical gap)
        val langBounds = langNode.getBoundsInRoot()
        val muteBounds = muteNode.getBoundsInRoot()
        val loginTabBounds = loginTab.getBoundsInRoot()
        val registerTabBounds = registerTab.getBoundsInRoot()

        val lowestTopControlBottom = maxOf(langBounds.bottom, muteBounds.bottom)
        val highestTabTop = minOf(loginTabBounds.top, registerTabBounds.top)

        val gap = highestTabTop - lowestTopControlBottom
        assertTrue(
            "Expected positive gap between top bar controls and login/register tabs on small landscape, but gap was $gap",
            gap > 0.dp
        )

        // Capture visual snapshot for small landscape
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/login_register_small_landscape.png")
    }

    @Test
    @Config(qualifiers = "w840dp-h480dp-land", sdk = [36])
    fun testLoginRegisterScreen_LargerLandscape_ZeroOverlapAndTappable() {
        var langToggled = false
        var audioToggled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                LoginRegisterScreen(
                    currentLanguage = "en",
                    isAudioMuted = true,
                    errorMessage = null,
                    onCheckUsernameTaken = { false },
                    onLogin = { _, _ -> },
                    onRegister = { _, _ -> },
                    onToggleLanguage = { langToggled = true },
                    onToggleAudioMute = { audioToggled = true }
                )
            }
        }

        val langNode = composeTestRule.onNodeWithTag("language_toggle_btn")
        val muteNode = composeTestRule.onNodeWithTag("audio_mute_btn")
        val loginTab = composeTestRule.onNodeWithTag("tab_login")
        val registerTab = composeTestRule.onNodeWithTag("tab_register")

        langNode.assertIsDisplayed()
        muteNode.assertIsDisplayed()
        loginTab.assertIsDisplayed()
        registerTab.assertIsDisplayed()

        langNode.performClick()
        assertTrue(langToggled)

        muteNode.performClick()
        assertTrue(audioToggled)

        val langBounds = langNode.getBoundsInRoot()
        val muteBounds = muteNode.getBoundsInRoot()
        val loginTabBounds = loginTab.getBoundsInRoot()

        val lowestTopControlBottom = maxOf(langBounds.bottom, muteBounds.bottom)
        val gap = loginTabBounds.top - lowestTopControlBottom
        assertTrue(
            "Expected positive gap between top bar controls and login/register tabs on larger landscape, but gap was $gap",
            gap > 0.dp
        )

        // Capture visual snapshot for larger landscape
        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/login_register_large_landscape.png")
    }
}
