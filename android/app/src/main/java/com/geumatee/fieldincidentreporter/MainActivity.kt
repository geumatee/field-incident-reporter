package com.geumatee.fieldincidentreporter

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.ui.form.IncidentFormScreen
import com.geumatee.fieldincidentreporter.ui.list.IncidentListScreen
import com.geumatee.fieldincidentreporter.ui.list.IncidentListViewModel
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private companion object {
        const val PERMISSION_ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
    }

    private val requestLocalNetworkLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val repository = (application as IncidentApplication).container.incidentRepository
            lifecycleScope.launch {
                repository.refreshIncidents()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as IncidentApplication).container.incidentRepository

        if (Build.VERSION.SDK_INT >= 37) {
            if (checkSelfPermission(PERMISSION_ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED) {
                requestLocalNetworkLauncher.launch(PERMISSION_ACCESS_LOCAL_NETWORK)
            }
        }

        setContent {

            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }
            FieldIncidentReporterTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "incident_list"
                    ) {
                        composable("incident_list") {
                            val listViewModel: IncidentListViewModel = viewModel(
                                factory = IncidentListViewModel.provideFactory(repository)
                            )

                            IncidentListScreen(
                                viewModel = listViewModel,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme },
                                onCreateIncident = {
                                    navController.navigate("incident_form") {
                                        launchSingleTop = true
                                    }
                                },
                                onOpenIncident = { id ->
                                    navController.navigate("incident_form?incidentId=$id") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable(
                            route = "incident_form?incidentId={incidentId}",
                            arguments = listOf(
                                navArgument("incidentId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            IncidentFormScreen(
                                onNavigateBack = {
                                    if (backStackEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}