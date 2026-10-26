package com.geumatee.fieldincidentreporter.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.data.IncidentRepository
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IncidentListViewModel(
    private val repository: IncidentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<IncidentListUiState>(IncidentListUiState.Loading)
    val uiState: StateFlow<IncidentListUiState> = _uiState.asStateFlow()

    private val _selectedStatus = MutableStateFlow<IncidentStatus?>(null)
    val selectedStatus: StateFlow<IncidentStatus?> = _selectedStatus.asStateFlow()

    init {
        observeIncidents()
    }

    private fun observeIncidents() {
        viewModelScope.launch {
            repository.incidentsFlow.collect { result ->
                result
                    .onSuccess { list ->
                        _uiState.value = IncidentListUiState.Success(list)
                    }
                    .onFailure { error ->
                        Log.e("IncidentListViewModel", "Failed to fetch incidents", error)
                        _uiState.value =
                            IncidentListUiState.Error(R.string.error_load_incidents_default)
                    }
            }
        }
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = IncidentListUiState.Loading
            repository.refreshIncidents()
        }
    }

    fun selectStatusFilter(status: IncidentStatus?) {
        _selectedStatus.value = status
    }

    companion object {
        fun provideFactory(repository: IncidentRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return IncidentListViewModel(repository) as T
                }
            }
    }
}