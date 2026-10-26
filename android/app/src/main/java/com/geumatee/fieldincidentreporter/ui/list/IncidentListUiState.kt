package com.geumatee.fieldincidentreporter.ui.list

import androidx.annotation.StringRes
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.model.Incident

sealed interface IncidentListUiState {
    data object Loading : IncidentListUiState
    data class Success(val incidents: List<Incident>) : IncidentListUiState
    data class Error(@param:StringRes val messageRes: Int = R.string.error_load_incidents_default) :
        IncidentListUiState
}
