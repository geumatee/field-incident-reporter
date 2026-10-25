package com.geumatee.fieldincidentreporter.ui.list.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.severityBadgeColors
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme

@Composable
fun SeverityBadge(
    severity: IncidentSeverity,
    modifier: Modifier = Modifier
) {
    val colors = severityBadgeColors(severity = severity)
    Surface(
        color = colors.containerColor,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(severity.labelRes),
            color = colors.contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SeverityBadgePreview() {
    FieldIncidentReporterTheme {
        Surface {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentSeverity.entries.forEach { severity ->
                    SeverityBadge(severity = severity)
                }
            }
        }
    }
}
