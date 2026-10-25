package com.geumatee.fieldincidentreporter.ui.list

import com.geumatee.fieldincidentreporter.FakeIncidentApiService
import com.geumatee.fieldincidentreporter.MainDispatcherRule
import com.geumatee.fieldincidentreporter.data.IncidentDto
import com.geumatee.fieldincidentreporter.data.IncidentRepository
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class IncidentListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seedDto = IncidentDto(
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
    fun `initializes with Success state when repository succeeds`() = runTest {
        val repository = IncidentRepository(FakeIncidentApiService(listOf(seedDto)))
        val viewModel = IncidentListViewModel(repository)

        val state = viewModel.uiState.value
        assertTrue(state is IncidentListUiState.Success)
        assertEquals(1, (state as IncidentListUiState.Success).incidents.size)
    }

    @Test
    fun `initializes with Error state when repository fails`() = runTest {
        val fakeApi = FakeIncidentApiService().apply { errorToThrow = IOException("Network error") }
        val repository = IncidentRepository(fakeApi)
        val viewModel = IncidentListViewModel(repository)

        val state = viewModel.uiState.value
        assertTrue(state is IncidentListUiState.Error)
    }

    @Test
    fun `selectStatusFilter updates selectedStatus flow`() = runTest {
        val repository = IncidentRepository(FakeIncidentApiService(listOf(seedDto)))
        val viewModel = IncidentListViewModel(repository)

        viewModel.selectStatusFilter(IncidentStatus.IN_PROGRESS)
        assertEquals(IncidentStatus.IN_PROGRESS, viewModel.selectedStatus.value)

        viewModel.selectStatusFilter(null)
        assertEquals(null, viewModel.selectedStatus.value)
    }

    @Test
    fun `reactively updates uiState when repository creates or deletes incident`() = runTest {
        val repository = IncidentRepository(FakeIncidentApiService(listOf(seedDto)))
        val viewModel = IncidentListViewModel(repository)

        repository.createIncident(
            Incident(
                id = "inc-102",
                title = "Broken light",
                description = "",
                category = IncidentCategory.ELECTRICAL,
                severity = IncidentSeverity.LOW,
                status = IncidentStatus.OPEN,
                location = "Hallway",
                imageUrl = null,
                createdAt = "2026-10-23T09:00:00Z",
                updatedAt = "2026-10-23T09:00:00Z"
            )
        )

        viewModel.loadIncidents()
        val afterCreate = viewModel.uiState.value as IncidentListUiState.Success
        assertEquals(2, afterCreate.incidents.size)

        repository.deleteIncident("inc-101")
        viewModel.loadIncidents()
        val afterDelete = viewModel.uiState.value as IncidentListUiState.Success
        assertEquals(1, afterDelete.incidents.size)
        assertEquals("inc-102", afterDelete.incidents.first().id)
    }
}
