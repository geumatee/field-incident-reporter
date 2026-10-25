package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.FakeIncidentApiService
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

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

    // --- getIncidents ---

    @Test
    fun `getIncidents returns mapped domain incidents on success`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(listOf(sampleDto))
            val repository = IncidentRepository(fakeApi)

            val result = repository.getIncidents()

            assertTrue(result.isSuccess)
            val incidents = result.getOrThrow()
            assertEquals(1, incidents.size)
            assertEquals("inc-101", incidents[0].id)
            assertEquals("Water leak", incidents[0].title)
            assertEquals(IncidentCategory.EQUIPMENT, incidents[0].category)
            assertEquals(IncidentSeverity.HIGH, incidents[0].severity)
            assertEquals(IncidentStatus.OPEN, incidents[0].status)
        }
    }

    @Test
    fun `getIncidents returns failure when api throws exception`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = IOException("Network error")
            }
            val repository = IncidentRepository(fakeApi)

            val result = repository.getIncidents()

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
    }

    @Test(expected = CancellationException::class)
    fun `getIncidents rethrows CancellationException`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService().apply {
                errorToThrow = CancellationException("Job was cancelled")
            }
            val repository = IncidentRepository(fakeApi)

            repository.getIncidents()
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
