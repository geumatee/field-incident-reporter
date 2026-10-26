package com.geumatee.fieldincidentreporter.ui.common

import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class IncidentUiMappersTest {

    @Test
    fun `category labelRes maps each enum to expected string resource`() {
        assertEquals(R.string.category_equipment, IncidentCategory.EQUIPMENT.labelRes)
        assertEquals(R.string.category_hazard, IncidentCategory.HAZARD.labelRes)
        assertEquals(R.string.category_electrical, IncidentCategory.ELECTRICAL.labelRes)
        assertEquals(R.string.category_other, IncidentCategory.OTHER.labelRes)
    }

    @Test
    fun `severity labelRes maps each enum to expected string resource`() {
        assertEquals(R.string.severity_low, IncidentSeverity.LOW.labelRes)
        assertEquals(R.string.severity_medium, IncidentSeverity.MEDIUM.labelRes)
        assertEquals(R.string.severity_high, IncidentSeverity.HIGH.labelRes)
    }

    @Test
    fun `status labelRes maps each enum to expected string resource`() {
        assertEquals(R.string.status_open, IncidentStatus.OPEN.labelRes)
        assertEquals(R.string.status_in_progress, IncidentStatus.IN_PROGRESS.labelRes)
        assertEquals(R.string.status_resolved, IncidentStatus.RESOLVED.labelRes)
    }
}
