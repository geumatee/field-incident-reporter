package com.geumatee.fieldincidentreporter

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
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
                repository.getIncidents()
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

        lifecycleScope.launch {
            repository.getIncidents()
            repository.createIncident(
                Incident(
                    id = "id",
                    title = "test",
                    description = "description",
                    category = IncidentCategory.OTHER,
                    severity = IncidentSeverity.LOW,
                    status = IncidentStatus.OPEN,
                    location = "location",
                    imageUrl = "data:image/jpeg;base64,abc",
                    createdAt = "2026-10-25T10:00:00Z",
                    updatedAt = "2026-10-26T10:00:00Z"
                )
            )
            repository.updateIncident(
                Incident(
                    id = "id",
                    title = "test*",
                    description = "description*",
                    category = IncidentCategory.ELECTRICAL,
                    severity = IncidentSeverity.HIGH,
                    status = IncidentStatus.IN_PROGRESS,
                    location = "location",
                    imageUrl = "data:image/jpeg;base64,abcde",
                    createdAt = "2026-10-25T10:00:00Z",
                    updatedAt = "2026-10-27T10:00:00Z"
                )
            )
            repository.deleteIncident("id")
        }

        setContent {
            FieldIncidentReporterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FieldIncidentReporterTheme {
        Greeting("Android")
    }
}