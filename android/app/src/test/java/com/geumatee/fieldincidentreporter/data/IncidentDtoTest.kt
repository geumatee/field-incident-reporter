package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class IncidentDtoTest {

    @Test
    fun `toDomain maps valid enum values and fields`() {
        val dto = IncidentDto(
            id = "inc-101",
            title = "Water leak",
            description = "Pipe dripping",
            category = "EQUIPMENT",
            severity = "HIGH",
            status = "IN_PROGRESS",
            location = "Building A",
            imageUrl = "data:image/jpeg;base64,abc",
            createdAt = "2026-10-23T08:30:00Z",
            updatedAt = "2026-10-23T09:00:00Z"
        )

        val domain = dto.toDomain()

        assertEquals("inc-101", domain.id)
        assertEquals("Water leak", domain.title)
        assertEquals("Pipe dripping", domain.description)
        assertEquals(IncidentCategory.EQUIPMENT, domain.category)
        assertEquals(IncidentSeverity.HIGH, domain.severity)
        assertEquals(IncidentStatus.IN_PROGRESS, domain.status)
        assertEquals("Building A", domain.location)
        assertEquals("data:image/jpeg;base64,abc", domain.imageUrl)
    }

    @Test
    fun `toDomain falls back to default enums for unknown values`() {
        val dto = IncidentDto(
            id = "inc-102",
            title = "Unknown enum test",
            description = "",
            category = "UNKNOWN_CAT",
            severity = "UNKNOWN_SEV",
            status = "UNKNOWN_STATUS",
            location = "Dock",
            imageUrl = null,
            createdAt = "2026-10-23T08:30:00Z",
            updatedAt = "2026-10-23T08:30:00Z"
        )

        val domain = dto.toDomain()

        assertEquals(IncidentCategory.OTHER, domain.category)
        assertEquals(IncidentSeverity.MEDIUM, domain.severity)
        assertEquals(IncidentStatus.OPEN, domain.status)
    }

    @Test
    fun `fromDomain converts enum names accurately`() {
        val incident = Incident(
            id = "inc-103",
            title = "Exit sign",
            description = "Flickering",
            category = IncidentCategory.ELECTRICAL,
            severity = IncidentSeverity.LOW,
            status = IncidentStatus.RESOLVED,
            location = "Corridor C",
            imageUrl = null,
            createdAt = "2026-10-21T11:00:00Z",
            updatedAt = "2026-10-22T16:45:00Z"
        )

        val dto = IncidentDto.fromDomain(incident)

        assertEquals("ELECTRICAL", dto.category)
        assertEquals("LOW", dto.severity)
        assertEquals("RESOLVED", dto.status)
        assertEquals(incident, dto.toDomain())
    }
}