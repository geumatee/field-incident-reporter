package com.geumatee.fieldincidentreporter.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
        loadIncidents()
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = IncidentListUiState.Loading
            repository.getIncidents()
                .onSuccess { list ->
                    _uiState.value = IncidentListUiState.Success(list)
                }
                .onFailure { error ->
                    _uiState.value = IncidentListUiState.Error(error.message)
                }
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