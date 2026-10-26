package com.geumatee.fieldincidentreporter.model

enum class IncidentCategory {
    EQUIPMENT,
    HAZARD,
    ELECTRICAL,
    OTHER
}

enum class IncidentSeverity {
    LOW,
    MEDIUM,
    HIGH
}

enum class IncidentStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED
}

data class Incident(
    val id: String,
    val title: String,
    val description: String,
    val category: IncidentCategory,
    val severity: IncidentSeverity,
    val status: IncidentStatus,
    val location: String,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)
