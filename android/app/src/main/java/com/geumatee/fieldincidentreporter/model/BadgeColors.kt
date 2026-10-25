package com.geumatee.fieldincidentreporter.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.geumatee.fieldincidentreporter.ui.theme.Amber100
import com.geumatee.fieldincidentreporter.ui.theme.Amber200
import com.geumatee.fieldincidentreporter.ui.theme.Amber800
import com.geumatee.fieldincidentreporter.ui.theme.Amber900
import com.geumatee.fieldincidentreporter.ui.theme.Blue200
import com.geumatee.fieldincidentreporter.ui.theme.Blue50
import com.geumatee.fieldincidentreporter.ui.theme.Blue700
import com.geumatee.fieldincidentreporter.ui.theme.Blue900
import com.geumatee.fieldincidentreporter.ui.theme.Emerald200
import com.geumatee.fieldincidentreporter.ui.theme.Emerald50
import com.geumatee.fieldincidentreporter.ui.theme.Emerald700
import com.geumatee.fieldincidentreporter.ui.theme.Emerald900
import com.geumatee.fieldincidentreporter.ui.theme.LocalDarkTheme
import com.geumatee.fieldincidentreporter.ui.theme.Red100
import com.geumatee.fieldincidentreporter.ui.theme.Red200
import com.geumatee.fieldincidentreporter.ui.theme.Red50
import com.geumatee.fieldincidentreporter.ui.theme.Red700
import com.geumatee.fieldincidentreporter.ui.theme.Red800
import com.geumatee.fieldincidentreporter.ui.theme.Red900
import com.geumatee.fieldincidentreporter.ui.theme.Slate200
import com.geumatee.fieldincidentreporter.ui.theme.Slate700

data class BadgeColors(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
fun severityBadgeColors(
    severity: IncidentSeverity,
    darkTheme: Boolean = LocalDarkTheme.current
): BadgeColors {
    return if (darkTheme) {
        when (severity) {
            IncidentSeverity.HIGH -> BadgeColors(Red900, Red200)
            IncidentSeverity.MEDIUM -> BadgeColors(Amber900, Amber200)
            IncidentSeverity.LOW -> BadgeColors(Slate700, Slate200)
        }
    } else {
        when (severity) {
            IncidentSeverity.HIGH -> BadgeColors(Red100, Red800)
            IncidentSeverity.MEDIUM -> BadgeColors(Amber100, Amber800)
            IncidentSeverity.LOW -> BadgeColors(Slate200, Slate700)
        }
    }
}

@Composable
fun statusBadgeColors(
    status: IncidentStatus,
    darkTheme: Boolean = LocalDarkTheme.current
): BadgeColors {
    return if (darkTheme) {
        when (status) {
            IncidentStatus.OPEN -> BadgeColors(Red900, Red200)
            IncidentStatus.IN_PROGRESS -> BadgeColors(Blue900, Blue200)
            IncidentStatus.RESOLVED -> BadgeColors(Emerald900, Emerald200)
        }
    } else {
        when (status) {
            IncidentStatus.OPEN -> BadgeColors(Red50, Red700)
            IncidentStatus.IN_PROGRESS -> BadgeColors(Blue50, Blue700)
            IncidentStatus.RESOLVED -> BadgeColors(Emerald50, Emerald700)
        }
    }
}