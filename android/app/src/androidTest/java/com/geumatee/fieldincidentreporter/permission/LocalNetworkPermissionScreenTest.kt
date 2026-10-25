package com.geumatee.fieldincidentreporter.permission

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.geumatee.fieldincidentreporter.ui.permission.LocalNetworkPermissionScreen
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LocalNetworkPermissionScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysMessageAndHandlesGrantPermissionAndThemeToggleClicks() {
        var permissionRequested = false
        var themeToggled = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                LocalNetworkPermissionScreen(
                    onRequestPermission = { permissionRequested = true },
                    isDarkTheme = false,
                    onToggleTheme = { themeToggled = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Field Incidents").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                "Local network permission is required to connect to the incident server."
            )
            .assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Toggle dark theme").performClick()
        assertTrue(themeToggled)

        composeTestRule.onNodeWithText("Grant Permission").performClick()
        assertTrue(permissionRequested)
    }
}
