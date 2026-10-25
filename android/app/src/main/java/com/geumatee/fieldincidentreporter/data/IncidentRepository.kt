package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.model.Incident
import kotlin.collections.map
import kotlin.coroutines.cancellation.CancellationException

class IncidentRepository(private val api: IncidentApiService) {

    suspend fun getIncidents(): Result<List<Incident>> = runCatchingCancellable {
        api.getIncidents().map { it.toDomain() }
    }

    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
        runCatching(block).onFailure { if (it is CancellationException) throw it }
}