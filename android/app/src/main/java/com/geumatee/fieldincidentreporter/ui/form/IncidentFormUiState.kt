package com.geumatee.fieldincidentreporter.ui.form

import androidx.annotation.StringRes
import com.geumatee.fieldincidentreporter.model.IncidentCategory
import com.geumatee.fieldincidentreporter.model.IncidentSeverity
import com.geumatee.fieldincidentreporter.model.IncidentStatus

data class IncidentFormUiState(
    val isEditMode: Boolean = false,
    val isLoadingInitialData: Boolean = false,
    val isLoadingNetwork: Boolean = false,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: IncidentCategory = IncidentCategory.EQUIPMENT,
    val severity: IncidentSeverity = IncidentSeverity.MEDIUM,
    val status: IncidentStatus = IncidentStatus.OPEN,
    val imageDataUri: String? = null,
    val createdAt: String? = null,
    @param:StringRes val titleErrorRes: Int? = null,
    @param:StringRes val locationErrorRes: Int? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    @param:StringRes val errorMessageRes: Int? = null,
    val isSavedOrDeleted: Boolean = false
) {
    val hasError: Boolean
        get() = errorMessage != null || errorMessageRes != null

    val isActionButtonsEnabled: Boolean
        get() = !isSubmitting && !isLoadingInitialData && !isLoadingNetwork && !hasError
}