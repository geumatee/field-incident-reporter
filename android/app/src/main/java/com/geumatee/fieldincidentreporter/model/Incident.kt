package com.geumatee.fieldincidentreporter.model

import androidx.annotation.StringRes
import com.geumatee.fieldincidentreporter.R

enum class IncidentCategory(@param:StringRes val labelRes: Int) {
    EQUIPMENT(R.string.category_equipment),
    HAZARD(R.string.category_hazard),
    ELECTRICAL(R.string.category_electrical),
    OTHER(R.string.category_other)
}

enum class IncidentSeverity(@param:StringRes val labelRes: Int) {
    LOW(R.string.severity_low),
    MEDIUM(R.string.severity_medium),
    HIGH(R.string.severity_high)
}

enum class IncidentStatus(@param:StringRes val labelRes: Int) {
    OPEN(R.string.status_open),
    IN_PROGRESS(R.string.status_in_progress),
    RESOLVED(R.string.status_resolved)
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