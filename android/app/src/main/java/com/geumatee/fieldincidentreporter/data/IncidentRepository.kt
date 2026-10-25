package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.model.Incident
import kotlin.collections.map
import kotlin.coroutines.cancellation.CancellationException

class IncidentRepository(private val api: IncidentApiService) {

    suspend fun getIncidents(): Result<List<Incident>> = runCatchingCancellable {
        api.getIncidents().map { it.toDomain() }
    }

    suspend fun createIncident(incident: Incident): Result<Incident> = runCatchingCancellable {
        api.createIncident(IncidentDto.fromDomain(incident)).toDomain()
    }.onSuccess {
        getIncidents()
    }

    suspend fun updateIncident(incident: Incident): Result<Incident> = runCatchingCancellable {
        api.updateIncident(incident.id, IncidentDto.fromDomain(incident)).toDomain()
    }.onSuccess {
        getIncidents()
    }

    suspend fun deleteIncident(id: String): Result<Unit> = runCatchingCancellable {
        val response = api.deleteIncident(id)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to delete incident (HTTP ${response.code()})")
        }
    }.onSuccess {
        getIncidents()
    }

    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
        runCatching(block).onFailure { if (it is CancellationException) throw it }
}