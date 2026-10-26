package com.geumatee.fieldincidentreporter.ui.list.components

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.common.labelRes
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme

@Composable
fun StatusFilterBar(
    selectedStatus: IncidentStatus?,
    onSelectStatus: (IncidentStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onSelectStatus(null) },
            label = { Text(stringResource(R.string.filter_all)) }
        )
        IncidentStatus.entries.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onSelectStatus(status) },
                label = { Text(stringResource(status.labelRes)) }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusFilterBarPreview() {
    FieldIncidentReporterTheme {
        Surface {
            StatusFilterBar(
                selectedStatus = IncidentStatus.OPEN,
                onSelectStatus = {}
            )
        }
    }
}