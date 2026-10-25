package com.geumatee.fieldincidentreporter

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geumatee.fieldincidentreporter.ui.form.IncidentFormScreen
import com.geumatee.fieldincidentreporter.ui.form.IncidentFormViewModel
import com.geumatee.fieldincidentreporter.ui.list.IncidentListScreen
import com.geumatee.fieldincidentreporter.ui.list.IncidentListViewModel
import com.geumatee.fieldincidentreporter.ui.permission.LocalNetworkPermissionScreen
import com.geumatee.fieldincidentreporter.ui.theme.FieldIncidentReporterTheme

class MainActivity : ComponentActivity() {

    private companion object {
        const val PERMISSION_ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK"
    }

    private var hasLocalNetworkPermission by mutableStateOf(false)

    private fun checkLocalNetworkPermission(): Boolean {
        return Build.VERSION.SDK_INT < 37 || checkSelfPermission(PERMISSION_ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED
    }

    private val requestLocalNetworkLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocalNetworkPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as IncidentApplication).container.incidentRepository

        hasLocalNetworkPermission = checkLocalNetworkPermission()
        if (!hasLocalNetworkPermission && Build.VERSION.SDK_INT >= 37) {
            requestLocalNetworkLauncher.launch(PERMISSION_ACCESS_LOCAL_NETWORK)
        }

        setContent {

            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }
            FieldIncidentReporterTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (hasLocalNetworkPermission) {
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
                                val incidentId = backStackEntry.arguments?.getString("incidentId")
                                val formViewModel: IncidentFormViewModel = viewModel(
                                    factory = IncidentFormViewModel.provideFactory(
                                        repository = repository,
                                        incidentId = incidentId
                                    )
                                )

                                IncidentFormScreen(
                                    viewModel = formViewModel,
                                    onNavigateBack = {
                                        if (backStackEntry.lifecycle.currentState.isAtLeast(
                                                Lifecycle.State.RESUMED
                                            )
                                        ) {
                                            navController.popBackStack()
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        LocalNetworkPermissionScreen(
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            onRequestPermission = {
                                if (shouldShowRequestPermissionRationale(
                                        PERMISSION_ACCESS_LOCAL_NETWORK
                                    )
                                ) {
                                    requestLocalNetworkLauncher.launch(
                                        PERMISSION_ACCESS_LOCAL_NETWORK
                                    )
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    )
                                    startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasLocalNetworkPermission = checkLocalNetworkPermission()
    }
}