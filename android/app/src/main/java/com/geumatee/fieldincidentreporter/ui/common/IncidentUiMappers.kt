package com.geumatee.fieldincidentreporter.ui.common

import androidx.annotation.StringRes
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus

val IncidentCategory.labelRes: Int
    @StringRes get() = when (this) {
        IncidentCategory.EQUIPMENT -> R.string.category_equipment
        IncidentCategory.HAZARD -> R.string.category_hazard
        IncidentCategory.ELECTRICAL -> R.string.category_electrical
        IncidentCategory.OTHER -> R.string.category_other
    }

val IncidentSeverity.labelRes: Int
    @StringRes get() = when (this) {
        IncidentSeverity.LOW -> R.string.severity_low
        IncidentSeverity.MEDIUM -> R.string.severity_medium
        IncidentSeverity.HIGH -> R.string.severity_high
    }

val IncidentStatus.labelRes: Int
    @StringRes get() = when (this) {
        IncidentStatus.OPEN -> R.string.status_open
        IncidentStatus.IN_PROGRESS -> R.string.status_in_progress
        IncidentStatus.RESOLVED -> R.string.status_resolved
    }
