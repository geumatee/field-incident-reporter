package com.geumatee.fieldincidentreporter.ui.form

import com.geumatee.fieldincidentreporter.FakeIncidentApiService
import com.geumatee.fieldincidentreporter.MainDispatcherRule
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.data.IncidentDto
import com.geumatee.fieldincidentreporter.data.IncidentRepository
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class IncidentFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val existingDto = IncidentDto(
        id = "inc-101",
        title = "Water leak",
        description = "Cooling pipe",
        category = "EQUIPMENT",
        severity = "HIGH",
        status = "OPEN",
        location = "Building A",
        imageUrl = null,
        createdAt = "2026-10-23T08:30:00Z",
        updatedAt = "2026-10-23T08:30:00Z"
    )

    @Test
    fun `saveIncident in create mode validates required fields`() = runTest {
        val repository = IncidentRepository(FakeIncidentApiService())
        val viewModel = IncidentFormViewModel(repository, incidentId = null)

        viewModel.saveIncident()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_title_required, state.titleErrorRes)
        assertEquals(R.string.error_location_required, state.locationErrorRes)
        assertFalse(state.isSavedOrDeleted)

        viewModel.onTitleChange("New title")
        assertNull(viewModel.uiState.value.titleErrorRes)
    }

    @Test
    fun `saveIncident in create mode creates incident and sets isSavedOrDeleted`() = runTest {
        val fakeApi = FakeIncidentApiService()
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentFormViewModel(repository, incidentId = null)

        viewModel.onTitleChange("Broken panel")
        viewModel.onLocationChange("Floor 2")
        viewModel.onDescriptionChange("Panel door loose")
        viewModel.onCategoryChange(IncidentCategory.ELECTRICAL)
        viewModel.onSeverityChange(IncidentSeverity.HIGH)

        viewModel.saveIncident()

        val state = viewModel.uiState.value
        assertTrue(state.isSavedOrDeleted)
        assertEquals(1, fakeApi.getIncidents().size)
        assertEquals("Broken panel", fakeApi.getIncidents().first().title)
    }

    @Test
    fun `edit mode shows cached incident first and updates when getIncident finishes`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(existingDto))
        val repository = IncidentRepository(fakeApi)

        repository.incidentsFlow.first()

        val gate = CompletableDeferred<Unit>()
        fakeApi.getIncidentGate = gate
        fakeApi.setDirectItem(existingDto.copy(title = "Water leak (Server Updated)"))

        val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

        val cachedState = viewModel.uiState.value
        assertFalse(cachedState.isLoadingInitialData)
        assertTrue(cachedState.isLoadingNetwork)
        assertFalse(cachedState.isActionButtonsEnabled)
        assertEquals("Water leak", cachedState.title)

        gate.complete(Unit)

        val refreshedState = viewModel.uiState.value
        assertFalse(refreshedState.isLoadingNetwork)
        assertTrue(refreshedState.isActionButtonsEnabled)
        assertEquals("Water leak (Server Updated)", refreshedState.title)
    }

    @Test
    fun `edit mode loads existing incident and updates on save`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(existingDto))
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

        val loaded = viewModel.uiState.value
        assertTrue(loaded.isEditMode)
        assertEquals("Water leak", loaded.title)
        assertEquals("Building A", loaded.location)
        assertEquals(IncidentSeverity.HIGH, loaded.severity)

        viewModel.onStatusChange(IncidentStatus.RESOLVED)
        viewModel.saveIncident()

        assertTrue(viewModel.uiState.value.isSavedOrDeleted)
        assertEquals("RESOLVED", fakeApi.getIncident("inc-101").status)
    }

    @Test
    fun `deleteIncident removes item and sets isSavedOrDeleted`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(existingDto))
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

        viewModel.deleteIncident()

        assertTrue(viewModel.uiState.value.isSavedOrDeleted)
        assertTrue(fakeApi.getIncidents().isEmpty())
    }

    @Test
    fun `loadExistingIncident when server returns 404 refreshes repository and marks isSavedOrDeleted`() =
        runTest {
            val fakeApi = FakeIncidentApiService(listOf(existingDto))
            val repository = IncidentRepository(fakeApi)

            repository.incidentsFlow.first()
            fakeApi.deleteIncident("inc-101")

            val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

            val state = viewModel.uiState.value
            assertTrue(state.isSavedOrDeleted)
            assertFalse(state.isLoadingInitialData)

            val cachedList = repository.incidentsFlow.first().getOrNull()
            assertTrue(cachedList.orEmpty().none { it.id == "inc-101" })
        }

    @Test
    fun `loadExistingIncident when network fails shows error even with cached item and enables retry via reload`() =
        runTest {
            val fakeApi = FakeIncidentApiService(listOf(existingDto))
            val repository = IncidentRepository(fakeApi)

            repository.incidentsFlow.first()
            fakeApi.errorToThrow = IOException("Network error")

            val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

            val errorState = viewModel.uiState.value
            assertFalse(errorState.isLoadingInitialData)
            assertFalse(errorState.isLoadingNetwork)
            assertFalse(errorState.isActionButtonsEnabled)
            assertEquals("Water leak", errorState.title)
            assertEquals(R.string.error_load_incident_details, errorState.errorMessageRes)
            assertFalse(errorState.isSavedOrDeleted)

            fakeApi.errorToThrow = null
            viewModel.reload()

            val reloadedState = viewModel.uiState.value
            assertFalse(reloadedState.isLoadingNetwork)
            assertTrue(reloadedState.isActionButtonsEnabled)
            assertNull(reloadedState.errorMessageRes)
        }

    @Test
    fun `reload when update fails in edit mode retries saveIncident`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(existingDto))
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

        viewModel.onTitleChange("Updated title")
        fakeApi.errorToThrow = IOException("Failed to Update")
        viewModel.saveIncident()

        assertEquals(R.string.error_save_incident, viewModel.uiState.value.errorMessageRes)
        assertFalse(viewModel.uiState.value.isSavedOrDeleted)

        fakeApi.errorToThrow = null
        viewModel.reload()

        assertTrue(viewModel.uiState.value.isSavedOrDeleted)
        assertEquals("Updated title", fakeApi.getIncident("inc-101").title)
    }

    @Test
    fun `reload when delete fails in edit mode retries deleteIncident`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(existingDto))
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentFormViewModel(repository, incidentId = "inc-101")

        fakeApi.errorToThrow = IOException("Failed to Delete")
        viewModel.deleteIncident()

        assertEquals(R.string.error_delete_incident, viewModel.uiState.value.errorMessageRes)
        assertFalse(viewModel.uiState.value.isSavedOrDeleted)

        fakeApi.errorToThrow = null
        viewModel.reload()

        assertTrue(viewModel.uiState.value.isSavedOrDeleted)
        assertTrue(fakeApi.getIncidents().isEmpty())
    }

    @Test
    fun `saveIncident when network throws IOException maps to friendly error string resource`() = runTest {
        val fakeApi = FakeIncidentApiService().apply {
            errorToThrow = IOException("Failed to connect to /10.0.2.2:3001")
        }
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentFormViewModel(repository, incidentId = null)

        viewModel.onTitleChange("Power Outage")
        viewModel.onLocationChange("Building B")
        viewModel.saveIncident()

        val state = viewModel.uiState.value
        assertEquals(R.string.error_save_incident, state.errorMessageRes)
        assertFalse(state.isSubmitting)
        assertTrue(state.hasError)
        assertFalse(state.isSavedOrDeleted)
    }
}
