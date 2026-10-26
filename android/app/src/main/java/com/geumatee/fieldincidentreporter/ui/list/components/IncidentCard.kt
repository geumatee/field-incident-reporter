package com.geumatee.fieldincidentreporter.ui.list.components

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.common.labelRes
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import com.geumatee.fieldincidentreporter.util.ImageUtils
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun IncidentCard(
    incident: Incident,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<Bitmap?>(
        initialValue = ImageUtils.getCachedBitmap(incident.imageUrl),
        key1 = incident.imageUrl
    ) {
        if (value == null && !incident.imageUrl.isNullOrBlank()) {
            value = withContext(Dispatchers.Default) {
                ImageUtils.base64DataUriToBitmap(incident.imageUrl)
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            val currentBitmap = bitmap
            if (currentBitmap != null) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeverityBadge(severity = incident.severity)
                    StatusBadge(status = incident.status)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = incident.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(
                        R.string.category_location_format,
                        stringResource(incident.category.labelRes),
                        incident.location
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(incident.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val DisplayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withZone(ZoneId.systemDefault())

private fun formatTimestamp(isoString: String): String {
    return try {
        val instant = Instant.parse(isoString)
        DisplayFormatter.format(instant)
    } catch (_: Exception) {
        isoString
    }
}

private val previewImageDataUri: String by lazy {
    val bitmap = createBitmap(160, 160).apply {
        eraseColor(0xFF64748B.toInt())
    }
    ImageUtils.bitmapToBase64DataUri(bitmap)
}

@Preview(name = "Categories - Light", showBackground = true)
@Preview(
    name = "Categories - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentCardCategoriesPreview() {
    FieldIncidentReporterTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentCategory.entries.forEach { category ->
                    IncidentCard(
                        incident = Incident(
                            id = "inc-${category.name}",
                            title = "Sample ${category.name} Incident",
                            description = "Sample description",
                            category = category,
                            severity = IncidentSeverity.MEDIUM,
                            status = IncidentStatus.OPEN,
                            location = "Building A - 3F",
                            imageUrl = previewImageDataUri,
                            createdAt = "2026-10-23T08:30:00Z",
                            updatedAt = "2026-10-23T08:30:00Z"
                        ),
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(name = "Severities - Light", showBackground = true)
@Preview(
    name = "Severities - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentCardSeveritiesPreview() {
    FieldIncidentReporterTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentSeverity.entries.forEach { severity ->
                    IncidentCard(
                        incident = Incident(
                            id = "inc-${severity.name}",
                            title = "Severity ${severity.name} Incident",
                            description = "Sample description",
                            category = IncidentCategory.EQUIPMENT,
                            severity = severity,
                            status = IncidentStatus.OPEN,
                            location = "Building A - 3F",
                            imageUrl = previewImageDataUri,
                            createdAt = "2026-10-23T08:30:00Z",
                            updatedAt = "2026-10-23T08:30:00Z"
                        ),
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Preview(name = "Statuses - Light", showBackground = true)
@Preview(
    name = "Statuses - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentCardStatusesPreview() {
    FieldIncidentReporterTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IncidentStatus.entries.forEach { status ->
                    IncidentCard(
                        incident = Incident(
                            id = "inc-${status.name}",
                            title = "Status ${status.name} Incident",
                            description = "Sample description",
                            category = IncidentCategory.HAZARD,
                            severity = IncidentSeverity.HIGH,
                            status = status,
                            location = "North Loading Bay",
                            imageUrl = previewImageDataUri,
                            createdAt = "2026-10-23T08:30:00Z",
                            updatedAt = "2026-10-23T08:30:00Z"
                        ),
                        onClick = {}
                    )
                }
            }
        }
    }
}