package com.geumatee.fieldincidentreporter.list

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.list.IncidentListScreen
import com.geumatee.fieldincidentreporter.ui.list.IncidentListUiState
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class IncidentListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleIncidents = listOf(
        Incident(
            id = "inc-101",
            title = "Water leak near Server Rack B4",
            description = "Valve dripping",
            category = IncidentCategory.EQUIPMENT,
            severity = IncidentSeverity.HIGH,
            status = IncidentStatus.OPEN,
            location = "Building A",
            imageUrl = null,
            createdAt = "2026-10-23T08:30:00Z",
            updatedAt = "2026-10-23T08:30:00Z"
        ),
        Incident(
            id = "inc-102",
            title = "Cracked concrete walkway",
            description = "Tripping hazard",
            category = IncidentCategory.HAZARD,
            severity = IncidentSeverity.MEDIUM,
            status = IncidentStatus.RESOLVED,
            location = "North Dock",
            imageUrl = null,
            createdAt = "2026-10-22T14:15:00Z",
            updatedAt = "2026-10-23T09:00:00Z"
        )
    )

    @Test
    fun displaysIncidentsAndHandlesCardAndThemeToggleClick() {
        var clickedId: String? = null
        var themeToggled = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Success(sampleIncidents),
                    selectedStatus = null,
                    isDarkTheme = false,
                    onToggleTheme = { themeToggled = true },
                    onRefresh = {},
                    onSelectStatus = {},
                    onCreateIncident = {},
                    onOpenIncident = { clickedId = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Water leak near Server Rack B4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cracked concrete walkway").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Toggle dark theme").performClick()
        assertTrue(themeToggled)

        composeTestRule.onNodeWithText("Water leak near Server Rack B4").performClick()
        assertEquals("inc-101", clickedId)
    }

    @Test
    fun displaysLoadingState() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Loading,
                    selectedStatus = null,
                    onRefresh = {},
                    onSelectStatus = {},
                    onCreateIncident = {},
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Water leak near Server Rack B4").assertDoesNotExist()
        composeTestRule
            .onNodeWithText("Report Incident", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun displaysEmptyStateWhenNoIncidentsMatchFilter() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Success(emptyList()),
                    selectedStatus = null,
                    onRefresh = {},
                    onSelectStatus = {},
                    onCreateIncident = {},
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("No incidents found for the selected filter.")
            .assertIsDisplayed()
    }

    @Test
    fun filtersIncidentsBySelectedStatus() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Success(sampleIncidents),
                    selectedStatus = IncidentStatus.OPEN,
                    onRefresh = {},
                    onSelectStatus = {},
                    onCreateIncident = {},
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Water leak near Server Rack B4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cracked concrete walkway").assertDoesNotExist()
    }

    @Test
    fun handlesStatusFilterBarClicks() {
        var selected: IncidentStatus? = null

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Success(emptyList()),
                    selectedStatus = null,
                    onRefresh = {},
                    onSelectStatus = { selected = it },
                    onCreateIncident = {},
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Open").performClick()
        assertEquals(IncidentStatus.OPEN, selected)

        composeTestRule.onNodeWithText("In Progress").performClick()
        assertEquals(IncidentStatus.IN_PROGRESS, selected)

        composeTestRule.onNodeWithText("Resolved").performClick()
        assertEquals(IncidentStatus.RESOLVED, selected)

        composeTestRule.onNodeWithText("All").performClick()
        assertNull(selected)
    }

    @Test
    fun handlesTopBarRefreshAndFabCreateCallbacks() {
        var refreshCalled = false
        var createCalled = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Success(sampleIncidents),
                    selectedStatus = null,
                    onRefresh = { refreshCalled = true },
                    onSelectStatus = {},
                    onCreateIncident = { createCalled = true },
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Refresh incidents").performClick()
        assertTrue(refreshCalled)

        composeTestRule
            .onNodeWithText("Report Incident", useUnmergedTree = true)
            .assertIsEnabled()
            .performClick()
        assertTrue(createCalled)
    }

    @Test
    fun displaysErrorStateAndTriggersRetry() {
        var retryClicked = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Error("Server offline"),
                    selectedStatus = null,
                    onRefresh = { retryClicked = true },
                    onSelectStatus = {},
                    onCreateIncident = {},
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Could not load incidents").assertIsDisplayed()
        composeTestRule.onNodeWithText("Server offline").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Report Incident", useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue(retryClicked)
    }

    @Test
    fun displaysDefaultErrorMessageWhenMessageIsNull() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentListScreen(
                    uiState = IncidentListUiState.Error(message = null),
                    selectedStatus = null,
                    onRefresh = {},
                    onSelectStatus = {},
                    onCreateIncident = {},
                    onOpenIncident = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Could not load incidents").assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Unable to connect to incident server.")
            .assertIsDisplayed()
    }
}
