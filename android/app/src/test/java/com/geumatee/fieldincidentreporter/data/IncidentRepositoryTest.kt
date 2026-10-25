package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.FakeIncidentApiService
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

    private val sampleDto = IncidentDto(
        id = "inc-101",
        title = "Water leak",
        description = "Valve dripping",
        category = "EQUIPMENT",
        severity = "HIGH",
        status = "OPEN",
        location = "Server Room",
        imageUrl = null,
        createdAt = "2026-10-23T08:30:00Z",
        updatedAt = "2026-10-23T08:30:00Z"
    )

    @Test
    fun `getIncidents returns mapped domain incidents on success`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(incidentsToReturn = listOf(sampleDto))
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
            val fakeApi = FakeIncidentApiService(errorToThrow = IOException("Network error"))
            val repository = IncidentRepository(fakeApi)

            val result = repository.getIncidents()

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
    }

    @Test(expected = CancellationException::class)
    fun `getIncidents rethrows CancellationException`() {
        runBlocking {
            val fakeApi = FakeIncidentApiService(errorToThrow = CancellationException("Job was cancelled"))
            val repository = IncidentRepository(fakeApi)

            repository.getIncidents()
        }
    }
}
