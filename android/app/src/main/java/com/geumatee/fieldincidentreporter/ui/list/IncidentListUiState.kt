package com.geumatee.fieldincidentreporter.ui.list

import com.geumatee.fieldincidentreporter.model.Incident

sealed interface IncidentListUiState {
    data object Loading : IncidentListUiState
    data class Success(val incidents: List<Incident>) : IncidentListUiState
    data class Error(val message: String?) : IncidentListUiState
}
