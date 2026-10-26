package com.geumatee.fieldincidentreporter.ui.form

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geumatee.fieldincidentreporter.R
import com.geumatee.fieldincidentreporter.data.IncidentRepository
import com.geumatee.fieldincidentreporter.model.Incident
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus
import com.geumatee.fieldincidentreporter.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class IncidentFormViewModel(
    private val repository: IncidentRepository,
    private val incidentId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        IncidentFormUiState(
            isEditMode = incidentId != null,
            isLoadingInitialData = incidentId != null,
            isLoadingNetwork = incidentId != null
        )
    )
    val uiState: StateFlow<IncidentFormUiState> = _uiState.asStateFlow()

    private var hasUserEdited: Boolean = false

    private enum class PendingOperation { LOAD, SAVE, DELETE }

    private var lastFailedOperation: PendingOperation? = null

    init {
        if (incidentId != null) {
            observeCachedIncident(incidentId)
            loadExistingIncident(incidentId)
        }
    }

    private fun observeCachedIncident(id: String) {
        viewModelScope.launch {
            repository.incidentsFlow
                .mapNotNull { result ->
                    result.getOrNull()?.find { it.id == id }
                }
                .collect { incident ->
                    applyIncidentToState(incident)
                }
        }
    }

    private fun applyIncidentToState(incident: Incident) {
        if (!hasUserEdited) {
            _uiState.update {
                it.copy(
                    isLoadingInitialData = false,
                    createdAt = incident.createdAt,
                    title = incident.title,
                    description = incident.description,
                    location = incident.location,
                    category = incident.category,
                    severity = incident.severity,
                    status = incident.status,
                    imageDataUri = incident.imageUrl
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoadingInitialData = false,
                    createdAt = it.createdAt ?: incident.createdAt
                )
            }
        }
    }

    private fun loadExistingIncident(id: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingInitialData = it.createdAt == null,
                    isLoadingNetwork = true,
                    errorMessageRes = null
                )
            }

            repository.getIncident(id)
                .onSuccess { incident ->
                    lastFailedOperation = null
                    applyIncidentToState(incident)
                    _uiState.update {
                        it.copy(
                            isLoadingInitialData = false,
                            isLoadingNetwork = false,
                            errorMessageRes = null
                        )
                    }
                }
                .onFailure { error ->
                    if (isNotFound(error)) {
                        lastFailedOperation = null
                        _uiState.update {
                            it.copy(
                                isLoadingInitialData = false,
                                isLoadingNetwork = false,
                                isSavedOrDeleted = true
                            )
                        }
                        viewModelScope.launch {
                            runCatching { repository.refreshIncidents() }
                        }
                    } else {
                        Log.e("IncidentFormViewModel", "Failed to load incident $id", error)
                        lastFailedOperation = PendingOperation.LOAD
                        _uiState.update {
                            it.copy(
                                isLoadingInitialData = false,
                                isLoadingNetwork = false,
                                errorMessageRes = R.string.error_load_incident_details
                            )
                        }
                    }
                }
        }
    }

    private fun isNotFound(error: Throwable): Boolean {
        val httpException = (error as? HttpException)
            ?: (error.cause as? HttpException)
        return httpException?.code() == 404 || error.message?.contains("404") == true
    }

    fun reload() {
        if (_uiState.value.isLoadingNetwork || _uiState.value.isSubmitting) return
        when (lastFailedOperation) {
            PendingOperation.DELETE -> deleteIncident()
            PendingOperation.SAVE -> saveIncident()
            PendingOperation.LOAD, null -> {
                if (incidentId != null) {
                    loadExistingIncident(incidentId)
                } else {
                    saveIncident()
                }
            }
        }
    }

    private fun clearSaveErrorIfPresent() {
        if (_uiState.value.errorMessageRes == R.string.error_save_incident) {
            lastFailedOperation = null
            _uiState.update { it.copy(errorMessageRes = null) }
        }
    }

    fun onTitleChange(value: String) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(title = value, titleErrorRes = null) }
    }

    fun onDescriptionChange(value: String) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(description = value) }
    }

    fun onLocationChange(value: String) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(location = value, locationErrorRes = null) }
    }

    fun onCategoryChange(category: IncidentCategory) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(category = category) }
    }

    fun onSeverityChange(severity: IncidentSeverity) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(severity = severity) }
    }

    fun onStatusChange(status: IncidentStatus) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(status = status) }
    }

    fun onPhotoSelected(bitmap: Bitmap) {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        val dataUri = ImageUtils.bitmapToBase64DataUri(bitmap)
        _uiState.update { it.copy(imageDataUri = dataUri) }
    }

    fun onRemovePhoto() {
        hasUserEdited = true
        clearSaveErrorIfPresent()
        _uiState.update { it.copy(imageDataUri = null) }
    }

    fun saveIncident() {
        val current = _uiState.value
        if (current.isSubmitting) return

        val trimmedTitle = current.title.trim()
        val trimmedLocation = current.location.trim()

        val titleErrorRes = if (trimmedTitle.isEmpty()) R.string.error_title_required else null
        val locationErrorRes =
            if (trimmedLocation.isEmpty()) R.string.error_location_required else null

        if (titleErrorRes != null || locationErrorRes != null) {
            _uiState.update {
                it.copy(titleErrorRes = titleErrorRes, locationErrorRes = locationErrorRes)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessageRes = null
                )
            }
            val nowIso = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()

            val incident = Incident(
                id = incidentId ?: "inc-${UUID.randomUUID()}",
                title = trimmedTitle,
                description = current.description.trim(),
                category = current.category,
                severity = current.severity,
                status = current.status,
                location = trimmedLocation,
                imageUrl = current.imageDataUri,
                createdAt = current.createdAt ?: nowIso,
                updatedAt = nowIso
            )

            val result = if (incidentId == null) {
                repository.createIncident(incident)
            } else {
                repository.updateIncident(incident)
            }

            result
                .onSuccess {
                    lastFailedOperation = null
                    _uiState.update {
                        it.copy(isSavedOrDeleted = true)
                    }
                }
                .onFailure { error ->
                    Log.e("IncidentFormViewModel", "Failed to save incident", error)
                    lastFailedOperation = PendingOperation.SAVE
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessageRes = R.string.error_save_incident
                        )
                    }
                }
        }
    }

    fun deleteIncident() {
        val id = incidentId ?: return
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessageRes = null
                )
            }
            repository.deleteIncident(id)
                .onSuccess {
                    lastFailedOperation = null
                    _uiState.update {
                        it.copy(isSavedOrDeleted = true)
                    }
                }
                .onFailure { error ->
                    Log.e("IncidentFormViewModel", "Failed to delete incident $id", error)
                    lastFailedOperation = PendingOperation.DELETE
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessageRes = R.string.error_delete_incident
                        )
                    }
                }
        }
    }

    companion object {
        fun provideFactory(
            repository: IncidentRepository,
            incidentId: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return IncidentFormViewModel(
                    repository,
                    incidentId
                ) as T
            }
        }
    }
}
