package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus

data class IncidentDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val severity: String,
    val status: String,
    val location: String,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
) {
    fun toDomain(): Incident = Incident(
        id = id,
        title = title,
        description = description,
        category = IncidentCategory.entries.find { it.name == category } ?: IncidentCategory.OTHER,
        severity = IncidentSeverity.entries.find { it.name == severity } ?: IncidentSeverity.MEDIUM,
        status = IncidentStatus.entries.find { it.name == status } ?: IncidentStatus.OPEN,
        location = location,
        imageUrl = imageUrl,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(incident: Incident): IncidentDto = IncidentDto(
            id = incident.id,
            title = incident.title,
            description = incident.description,
            category = incident.category.name,
            severity = incident.severity.name,
            status = incident.status.name,
            location = incident.location,
            imageUrl = incident.imageUrl,
            createdAt = incident.createdAt,
            updatedAt = incident.updatedAt
        )
    }
}