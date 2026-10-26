package com.geumatee.fieldincidentreporter.ui.form

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class IncidentFormScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleDataUri =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="

    @Test
    fun `create mode renders titles and hides delete and status and handles save and back`() {
        var backClicked = false
        var saveClicked = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = false,
                        title = "New Incident",
                        location = "Floor 1"
                    ),
                    onNavigateBack = { backClicked = true },
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = { saveClicked = true },
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Report Incident").assertIsDisplayed()
        composeTestRule.onNodeWithText("Submit Report").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Delete incident").assertDoesNotExist()
        composeTestRule.onNodeWithText("Open").assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backClicked)

        composeTestRule.onNodeWithText("Submit Report").performScrollTo().performClick()
        assertTrue(saveClicked)
    }

    @Test
    fun `edit mode shows delete dialog and handles cancel and confirm`() {
        var deleteConfirmed = false
        var statusChanged: IncidentStatus? = null

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = true,
                        title = "Water leak",
                        location = "Building A",
                        status = IncidentStatus.OPEN
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = { statusChanged = it },
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = { deleteConfirmed = true },
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Edit Incident").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Changes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open").assertIsDisplayed()
        composeTestRule.onNodeWithText("In Progress").assertIsDisplayed()
        composeTestRule.onNodeWithText("Resolved").assertIsDisplayed()

        composeTestRule.onNodeWithText("In Progress").performScrollTo().performClick()
        assertEquals(IncidentStatus.IN_PROGRESS, statusChanged)

        composeTestRule.onNodeWithContentDescription("Delete incident").performClick()
        composeTestRule
            .onNodeWithText("Delete this incident report? This action cannot be undone.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Cancel").performClick()
        assertFalse(deleteConfirmed)
        composeTestRule
            .onNodeWithText("Delete this incident report? This action cannot be undone.")
            .assertDoesNotExist()

        composeTestRule.onNodeWithContentDescription("Delete incident").performClick()
        composeTestRule.onNodeWithText("Delete").performClick()
        assertTrue(deleteConfirmed)
    }

    @Test
    fun `displays loading initial data state`() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isLoadingInitialData = true
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Title *").assertDoesNotExist()
    }

    @Test
    fun `displays syncing indicator and disables action buttons during network load`() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = true,
                        isLoadingNetwork = true,
                        title = "Water leak",
                        location = "Building A"
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Save Changes").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Delete incident").assertIsNotEnabled()
    }

    @Test
    fun `displays validation errors when present`() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = false,
                        titleErrorRes = R.string.error_title_required,
                        locationErrorRes = R.string.error_location_required
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Title is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Location is required").assertIsDisplayed()
    }

    @Test
    fun `handles text inputs and calls callbacks`() {
        var changedTitle: String? = null
        var changedLocation: String? = null
        var changedDescription: String? = null

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = false,
                        title = "",
                        location = "",
                        description = ""
                    ),
                    onNavigateBack = {},
                    onTitleChange = { changedTitle = it },
                    onLocationChange = { changedLocation = it },
                    onDescriptionChange = { changedDescription = it },
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Title *").performTextInput("Gas Smell")
        assertEquals("Gas Smell", changedTitle)

        composeTestRule.onNodeWithText("Location *").performTextInput("Floor 2 Lab")
        assertEquals("Floor 2 Lab", changedLocation)

        composeTestRule.onNodeWithText("Description").performTextInput("Strong odor near vent")
        assertEquals("Strong odor near vent", changedDescription)
    }

    @Test
    fun `handles category and severity chip clicks`() {
        var categoryChanged: IncidentCategory? = null
        var severityChanged: IncidentSeverity? = null

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = false,
                        category = IncidentCategory.EQUIPMENT,
                        severity = IncidentSeverity.LOW
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = { categoryChanged = it },
                    onSeverityChange = { severityChanged = it },
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Electrical").performScrollTo().performClick()
        assertEquals(IncidentCategory.ELECTRICAL, categoryChanged)

        composeTestRule.onNodeWithText("High").performScrollTo().performClick()
        assertEquals(IncidentSeverity.HIGH, severityChanged)
    }

    @Test
    fun `handles photo evidence states and remove callback`() {
        var photoRemoved = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = true,
                        imageDataUri = sampleDataUri
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = { photoRemoved = true },
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Captured incident photo")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Remove Photo")
            .performScrollTo()
            .performClick()

        assertTrue(photoRemoved)
    }

    @Test
    fun `submitting state disables inputs and action buttons`() {
        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = true,
                        isSubmitting = true,
                        title = "Water leak",
                        location = "Building A"
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Delete incident").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Water leak").assertIsNotEnabled()
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun `error state disables action buttons and invokes reload`() {
        var reloadClicked = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = true,
                        title = "Water leak",
                        location = "Building A",
                        errorMessageRes = R.string.error_load_incident_details
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = { reloadClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to load incident details.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Changes").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Delete incident").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Reload").performClick()
        assertTrue(reloadClicked)
    }

    @Test
    fun `save error shows retry button and invokes reload`() {
        var retryClicked = false

        composeTestRule.setContent {
            FieldIncidentReporterTheme {
                IncidentFormScreen(
                    uiState = IncidentFormUiState(
                        isEditMode = true,
                        title = "Water leak",
                        location = "Building A",
                        errorMessageRes = R.string.error_save_incident
                    ),
                    onNavigateBack = {},
                    onTitleChange = {},
                    onLocationChange = {},
                    onDescriptionChange = {},
                    onCategoryChange = {},
                    onSeverityChange = {},
                    onStatusChange = {},
                    onPhotoSelected = {},
                    onRemovePhoto = {},
                    onSaveIncident = {},
                    onDeleteIncident = {},
                    onReload = { retryClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to save incident report.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Changes").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Delete incident").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Retry").performClick()
        assertTrue(retryClicked)
    }
}
