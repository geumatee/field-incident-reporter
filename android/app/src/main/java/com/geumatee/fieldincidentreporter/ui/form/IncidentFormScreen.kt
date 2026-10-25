package com.geumatee.fieldincidentreporter.ui.form

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.form.components.DeleteIncidentDialog
import com.geumatee.fieldincidentreporter.ui.form.components.PhotoEvidenceSection
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun IncidentFormScreen(
    viewModel: IncidentFormViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSavedOrDeleted) {
        if (uiState.isSavedOrDeleted) {
            onNavigateBack()
        }
    }

    IncidentFormScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTitleChange = viewModel::onTitleChange,
        onLocationChange = viewModel::onLocationChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategoryChange = viewModel::onCategoryChange,
        onSeverityChange = viewModel::onSeverityChange,
        onStatusChange = viewModel::onStatusChange,
        onPhotoSelected = viewModel::onPhotoSelected,
        onRemovePhoto = viewModel::onRemovePhoto,
        onSaveIncident = viewModel::saveIncident,
        onDeleteIncident = viewModel::deleteIncident,
        onReload = viewModel::reload
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentFormScreen(
    uiState: IncidentFormUiState,
    onNavigateBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (IncidentCategory) -> Unit,
    onSeverityChange: (IncidentSeverity) -> Unit,
    onStatusChange: (IncidentStatus) -> Unit,
    onPhotoSelected: (Bitmap) -> Unit,
    onRemovePhoto: () -> Unit,
    onSaveIncident: () -> Unit,
    onDeleteIncident: () -> Unit,
    onReload: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = uiState.isSubmitting) {}

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onPhotoSelected(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }.getOrNull()
                }
                if (bitmap != null) {
                    onPhotoSelected(bitmap)
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteIncidentDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteIncident()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.isEditMode) R.string.form_title_edit else R.string.form_title_create
                        ),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !uiState.isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = uiState.isActionButtonsEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete_incident),
                                tint = if (uiState.isActionButtonsEnabled) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoadingInitialData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val inputsEnabled = !uiState.isSubmitting
            val resolvedError = uiState.errorMessage
                ?: uiState.errorMessageRes?.let { stringResource(it) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState.isLoadingNetwork) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (resolvedError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = resolvedError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = onReload
                            ) {
                                Text(
                                    text = stringResource(
                                        if (uiState.errorMessageRes == R.string.error_load_incident_details) {
                                            R.string.action_reload
                                        } else {
                                            R.string.action_retry
                                        }
                                    ),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.label_title_required)) },
                    placeholder = { Text(stringResource(R.string.placeholder_title)) },
                    isError = uiState.titleErrorRes != null,
                    supportingText = uiState.titleErrorRes?.let { errRes ->
                        { Text(stringResource(errRes)) }
                    },
                    enabled = inputsEnabled,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = onLocationChange,
                    label = { Text(stringResource(R.string.label_location_required)) },
                    placeholder = { Text(stringResource(R.string.placeholder_location)) },
                    isError = uiState.locationErrorRes != null,
                    supportingText = uiState.locationErrorRes?.let { errRes ->
                        { Text(stringResource(errRes)) }
                    },
                    enabled = inputsEnabled,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = { Text(stringResource(R.string.label_description)) },
                    placeholder = { Text(stringResource(R.string.placeholder_description)) },
                    enabled = inputsEnabled,
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.label_category),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IncidentCategory.entries.forEach { category ->
                            FilterChip(
                                selected = uiState.category == category,
                                onClick = { onCategoryChange(category) },
                                enabled = inputsEnabled,
                                label = { Text(stringResource(category.labelRes)) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.label_severity),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IncidentSeverity.entries.forEach { severity ->
                            FilterChip(
                                selected = uiState.severity == severity,
                                onClick = { onSeverityChange(severity) },
                                enabled = inputsEnabled,
                                label = { Text(stringResource(severity.labelRes)) }
                            )
                        }
                    }
                }

                if (uiState.isEditMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.label_status),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IncidentStatus.entries.forEach { status ->
                                FilterChip(
                                    selected = uiState.status == status,
                                    onClick = { onStatusChange(status) },
                                    enabled = inputsEnabled,
                                    label = { Text(stringResource(status.labelRes)) }
                                )
                            }
                        }
                    }
                }

                PhotoEvidenceSection(
                    imageDataUri = uiState.imageDataUri,
                    enabled = inputsEnabled,
                    onTakePhotoClick = { cameraLauncher.launch(null) },
                    onChooseGalleryClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemovePhotoClick = onRemovePhoto
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSaveIncident,
                    enabled = uiState.isActionButtonsEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(
                                if (uiState.isEditMode) {
                                    R.string.action_save_changes
                                } else {
                                    R.string.action_submit_report
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IncidentFormScreenPreview() {
    FieldIncidentReporterTheme {
        IncidentFormScreen(
            uiState = IncidentFormUiState(
                isEditMode = true,
                title = "Water leak near Server Rack B4",
                location = "Building A - 3F Server Room",
                description = "Cooling pipe condensation valve is dripping.",
                category = IncidentCategory.EQUIPMENT,
                severity = IncidentSeverity.HIGH,
                status = IncidentStatus.OPEN
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onLocationChange = {},
            onDescriptionChange = {},
            onCategoryChange = {},
            onSeverityChange = {},
            onStatusChange = {},
            onPhotoSelected = {},
            onRemovePhoto = {},
            onSaveIncident = {},
            onDeleteIncident = {},
            onReload = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IncidentFormScreenErrorPreview() {
    FieldIncidentReporterTheme {
        IncidentFormScreen(
            uiState = IncidentFormUiState(
                isEditMode = true,
                title = "Water leak near Server Rack B4",
                location = "Building A - 3F Server Room",
                errorMessageRes = R.string.error_load_incident_details
            ),
            onNavigateBack = {},
            onTitleChange = {},
            onLocationChange = {},
            onDescriptionChange = {},
            onCategoryChange = {},
            onSeverityChange = {},
            onStatusChange = {},
            onPhotoSelected = {},
            onRemovePhoto = {},
            onSaveIncident = {},
            onDeleteIncident = {},
            onReload = {}
        )
    }
}
