package com.geumatee.fieldincidentreporter.ui.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.list.components.IncidentCard
import com.geumatee.fieldincidentreporter.ui.list.components.IncidentListErrorState
import com.geumatee.fieldincidentreporter.ui.list.components.StatusFilterBar
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import com.geumatee.fieldincidentreporter.ui.theme.LocalDarkTheme

@Composable
fun IncidentListScreen(
    viewModel: IncidentListViewModel,
    isDarkTheme: Boolean = LocalDarkTheme.current,
    onToggleTheme: () -> Unit = {},
    onCreateIncident: () -> Unit,
    onOpenIncident: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    IncidentListScreen(
        uiState = uiState,
        selectedStatus = selectedStatus,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onRefresh = viewModel::loadIncidents,
        onSelectStatus = viewModel::selectStatusFilter,
        onCreateIncident = onCreateIncident,
        onOpenIncident = onOpenIncident
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentListScreen(
    uiState: IncidentListUiState,
    selectedStatus: IncidentStatus?,
    isDarkTheme: Boolean = LocalDarkTheme.current,
    onToggleTheme: () -> Unit = {},
    onRefresh: () -> Unit,
    onSelectStatus: (IncidentStatus?) -> Unit,
    onCreateIncident: () -> Unit,
    onOpenIncident: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.list_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) {
                                Icons.Default.LightMode
                            } else {
                                Icons.Default.DarkMode
                            },
                            contentDescription = stringResource(R.string.cd_toggle_theme)
                        )
                    }
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh_incidents)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (uiState is IncidentListUiState.Success) {
                ExtendedFloatingActionButton(
                    onClick = onCreateIncident,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.fab_report_incident)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            StatusFilterBar(
                selectedStatus = selectedStatus,
                onSelectStatus = onSelectStatus
            )

            when (uiState) {
                is IncidentListUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is IncidentListUiState.Error -> {
                    IncidentListErrorState(
                        message = uiState.message,
                        onRetry = onRefresh,
                        modifier = Modifier.navigationBarsPadding()
                    )
                }

                is IncidentListUiState.Success -> {
                    val filtered = remember(uiState.incidents, selectedStatus) {
                        if (selectedStatus == null) {
                            uiState.incidents
                        } else {
                            uiState.incidents.filter { it.status == selectedStatus }
                        }
                    }

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.empty_incidents_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = innerPadding.calculateBottomPadding() + 88.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtered, key = { it.id }) { incident ->
                                IncidentCard(
                                    incident = incident,
                                    onClick = { onOpenIncident(incident.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IncidentListScreenPreview() {
    FieldIncidentReporterTheme {
        IncidentListScreen(
            uiState = IncidentListUiState.Success(
                incidents = listOf(
                    Incident(
                        id = "inc-101",
                        title = "Water leak near Server Rack B4",
                        description = "Cooling pipe condensation valve is dripping.",
                        category = IncidentCategory.EQUIPMENT,
                        severity = IncidentSeverity.HIGH,
                        status = IncidentStatus.OPEN,
                        location = "Building A - 3F Server Room",
                        imageUrl = null,
                        createdAt = "2026-10-23T08:30:00Z",
                        updatedAt = "2026-10-23T08:30:00Z"
                    ),
                    Incident(
                        id = "inc-102",
                        title = "Cracked concrete walkway at North Loading Dock",
                        description = "Uneven surface creating a tripping hazard.",
                        category = IncidentCategory.HAZARD,
                        severity = IncidentSeverity.MEDIUM,
                        status = IncidentStatus.IN_PROGRESS,
                        location = "North Loading Bay",
                        imageUrl = null,
                        createdAt = "2026-10-22T14:15:00Z",
                        updatedAt = "2026-10-23T09:00:00Z"
                    )
                )
            ),
            selectedStatus = null,
            onRefresh = {},
            onSelectStatus = {},
            onCreateIncident = {},
            onOpenIncident = {}
        )
    }
}