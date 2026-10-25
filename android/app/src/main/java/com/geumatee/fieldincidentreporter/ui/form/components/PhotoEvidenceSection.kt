package com.geumatee.fieldincidentreporter.ui.form.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import com.geumatee.fieldincidentreporter.util.ImageUtils
import com.geumatee.fieldincidentreporter.R

@Composable
fun PhotoEvidenceSection(
    imageDataUri: String?,
    enabled: Boolean,
    onTakePhotoClick: () -> Unit,
    onChooseGalleryClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewBitmap = remember(imageDataUri) {
        ImageUtils.base64DataUriToBitmap(imageDataUri)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.label_photo_evidence),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )

        if (previewBitmap != null) {
            val aspectRatio = remember(previewBitmap) {
                (previewBitmap.width.toFloat() / previewBitmap.height.toFloat()).coerceAtLeast(0.1f)
            }
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.cd_captured_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Fit
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onTakePhotoClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_take_photo))
            }

            OutlinedButton(
                onClick = onChooseGalleryClick,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.action_choose_gallery))
            }
        }

        if (imageDataUri != null) {
            TextButton(
                onClick = onRemovePhotoClick,
                enabled = enabled
            ) {
                Text(
                    text = stringResource(R.string.action_remove_photo),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PhotoEvidenceSectionPreview() {
    val sampleDataUri = remember {
        val bitmap = createBitmap(320, 180).apply {
            eraseColor(0xFF64748B.toInt())
        }
        ImageUtils.bitmapToBase64DataUri(bitmap)
    }
    FieldIncidentReporterTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PhotoEvidenceSection(
                imageDataUri = sampleDataUri,
                enabled = true,
                onTakePhotoClick = {},
                onChooseGalleryClick = {},
                onRemovePhotoClick = {}
            )
        }
    }
}
