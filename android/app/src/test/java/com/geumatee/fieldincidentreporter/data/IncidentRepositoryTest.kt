package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.FakeIncidentApiService
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class IncidentRepositoryTest {

    private val sampleIncident = Incident(
        id = "inc-101",
        title = "Water leak",
        description = "Valve dripping",
        category = IncidentCategory.EQUIPMENT,
        severity = IncidentSeverity.HIGH,
        status = IncidentStatus.OPEN,
        location = "Server Room",
        imageUrl = null,
        createdAt = "2026-10-23T08:30:00Z",
        updatedAt = "2026-10-23T08:30:00Z"
    )

    private val sampleDto = IncidentDto.fromDomain(sampleIncident)

    // --- incidentsFlow & refreshIncidents ---

    @Test
    fun `incidentsFlow emits initial items and reloads after create update delete`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(sampleDto))
        val repository = IncidentRepository(fakeApi)

        val emissions = mutableListOf<Result<List<Incident>>>()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.incidentsFlow.collect { emissions.add(it) }
        }

        assertEquals(1, emissions.size)
        assertEquals(1, emissions.last().getOrThrow().size)

        val newIncident = sampleIncident.copy(
            id = "inc-102",
            title = "Cracked walkway",
            category = IncidentCategory.HAZARD,
            severity = IncidentSeverity.MEDIUM
        )
        repository.createIncident(newIncident)
        assertEquals(2, emissions.last().getOrThrow().size)

        val updatedIncident = newIncident.copy(status = IncidentStatus.RESOLVED)
        repository.updateIncident(updatedIncident)
        assertEquals(
            IncidentStatus.RESOLVED,
            emissions.last().getOrThrow().first { it.id == "inc-102" }.status
        )

        repository.deleteIncident("inc-102")
        assertEquals(1, emissions.last().getOrThrow().size)

        collectJob.cancel()
    }

    @Test
    fun `incidentsFlow emits failure when api throws error`() = runTest {
        val fakeApi = FakeIncidentApiService().apply {
            errorToThrow = IOException("Network error")
        }
        val repository = IncidentRepository(fakeApi)

        val firstResult = repository.incidentsFlow.first()

        assertTrue(firstResult.isFailure)
        assertEquals("Network error", firstResult.exceptionOrNull()?.message)
    }

    @Test
    fun `refreshIncidents while initial onSubscription is in flight cancels initial load and emits fresh result`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val fakeApi = FakeIncidentApiService(listOf(sampleDto)).apply {
            getIncidentsGate = gate
        }
        val repository = IncidentRepository(fakeApi)

        val emissions = mutableListOf<Result<List<Incident>>>()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.incidentsFlow.collect { emissions.add(it) }
        }

        assertTrue(emissions.isEmpty())

        fakeApi.getIncidentsGate = null
        repository.refreshIncidents()

        assertEquals(1, emissions.size)
        assertEquals("Water leak", emissions.single().getOrThrow().single().title)

        collectJob.cancel()
    }

    // --- getIncident ---

    @Test
    fun `getIncident updates cached incidentsFlow with fresh item`() = runTest {
        val fakeApi = FakeIncidentApiService(listOf(sampleDto))
        val repository = IncidentRepository(fakeApi)

        val firstList = repository.incidentsFlow.first().getOrThrow()
        assertEquals("Water leak", firstList.first().title)

        fakeApi.setDirectItem(sampleDto.copy(title = "Water leak - updated by operator"))
        val result = repository.getIncident("inc-101")

        assertTrue(result.isSuccess)
        assertEquals("Water leak - updated by operator", result.getOrThrow().title)

        val updatedList = repository.incidentsFlow.first().getOrThrow()
        assertEquals("Water leak - updated by operator", updatedList.first().title)
    }

    @Test
    fun `getIncident returns failure when api throws exception`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = IOException("Incident not found")
            }
            val repository = IncidentRepository(fakeApi)

            val result = repository.getIncident("inc-999")

            assertTrue(result.isFailure)
            assertEquals("Incident not found", result.exceptionOrNull()?.message)
        }
    }

    @Test(expected = CancellationException::class)
    fun `getIncident rethrows CancellationException`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = CancellationException("Cancelled")
            }
            val repository = IncidentRepository(fakeApi)

            repository.getIncident("inc-101")
        }
    }

    // --- createIncident ---

    @Test
    fun `createIncident returns created incident on success`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService()
            val repository = IncidentRepository(fakeApi)

            val newIncident = sampleIncident.copy(id = "inc-201", title = "New incident")
            val result = repository.createIncident(newIncident)

            assertTrue(result.isSuccess)
            assertEquals("inc-201", result.getOrThrow().id)
            assertEquals("New incident", result.getOrThrow().title)
            assertEquals(1, fakeApi.items.size)
        }
    }

    @Test
    fun `createIncident returns failure when api throws exception`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = IOException("Failed to create")
            }
            val repository = IncidentRepository(fakeApi)

            val result = repository.createIncident(sampleIncident)

            assertTrue(result.isFailure)
            assertEquals("Failed to create", result.exceptionOrNull()?.message)
        }
    }

    @Test(expected = CancellationException::class)
    fun `createIncident rethrows CancellationException`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = CancellationException("Cancelled")
            }
            val repository = IncidentRepository(fakeApi)

            repository.createIncident(sampleIncident)
        }
    }

    // --- updateIncident ---

    @Test
    fun `updateIncident returns updated incident on success`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(listOf(sampleDto))
            val repository = IncidentRepository(fakeApi)

            val updatedIncident = sampleIncident.copy(title = "Updated title", status = IncidentStatus.RESOLVED)
            val result = repository.updateIncident(updatedIncident)

            assertTrue(result.isSuccess)
            assertEquals("Updated title", result.getOrThrow().title)
            assertEquals(IncidentStatus.RESOLVED, result.getOrThrow().status)
        }
    }

    @Test
    fun `updateIncident returns failure when api throws exception`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(listOf(sampleDto)).apply {
                errorToThrow = IOException("Failed to update")
            }
            val repository = IncidentRepository(fakeApi)

            val result = repository.updateIncident(sampleIncident)

            assertTrue(result.isFailure)
            assertEquals("Failed to update", result.exceptionOrNull()?.message)
        }
    }

    @Test(expected = CancellationException::class)
    fun `updateIncident rethrows CancellationException`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = CancellationException("Cancelled")
            }
            val repository = IncidentRepository(fakeApi)

            repository.updateIncident(sampleIncident)
        }
    }

    // --- deleteIncident ---

    @Test
    fun `deleteIncident returns success when api responds successfully`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(listOf(sampleDto))
            val repository = IncidentRepository(fakeApi)

            val result = repository.deleteIncident("inc-101")

            assertTrue(result.isSuccess)
            assertTrue(fakeApi.items.isEmpty())
        }
    }

    @Test
    fun `deleteIncident returns failure when api response is not successful`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(listOf(sampleDto)).apply {
                deleteResponseCode = 500
            }
            val repository = IncidentRepository(fakeApi)

            val result = repository.deleteIncident("inc-101")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }
    }

    @Test
    fun `deleteIncident returns failure when api throws exception`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(listOf(sampleDto)).apply {
                errorToThrow = IOException("Network error")
            }
            val repository = IncidentRepository(fakeApi)

            val result = repository.deleteIncident("inc-101")

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
    }

    @Test(expected = CancellationException::class)
    fun `deleteIncident rethrows CancellationException`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = CancellationException("Cancelled")
            }
            val repository = IncidentRepository(fakeApi)

            repository.deleteIncident("inc-101")
        }
    }
}
