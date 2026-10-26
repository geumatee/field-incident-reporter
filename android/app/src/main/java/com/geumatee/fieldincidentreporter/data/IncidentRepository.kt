package com.geumatee.fieldincidentreporter.data

import com.geumatee.fieldincidentreporter.model.Incident
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException

class IncidentRepository(private val api: IncidentApiService) {

    // In-memory Single Source of Truth (SSOT) chosen for simplicity; Room/SQLite would be preferred if offline persistence was required.
    private val _incidentsFlow = MutableSharedFlow<Result<List<Incident>>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val hasInitialLoadStarted = AtomicBoolean(false)

    @Volatile
    private var activeRefreshJob: Job? = null

    val incidentsFlow: Flow<Result<List<Incident>>> = _incidentsFlow.onSubscription {
        if (hasInitialLoadStarted.compareAndSet(false, true)) {
            refreshIncidents()
        }
    }

    suspend fun refreshIncidents() = coroutineScope {
        hasInitialLoadStarted.set(true)
        activeRefreshJob?.cancel()
        val job = async { getIncidents() }
        activeRefreshJob = job
        try {
            val result = job.await()
            if (activeRefreshJob === job) {
                _incidentsFlow.emit(result)
            }
        } catch (_: CancellationException) {
            currentCoroutineContext().ensureActive()
        }
    }

    private suspend fun getIncidents(): Result<List<Incident>> = runCatchingCancellable {
        api.getIncidents().map { it.toDomain() }
    }

    suspend fun getIncident(id: String): Result<Incident> = runCatchingCancellable {
        api.getIncident(id).toDomain()
    }.onSuccess { freshIncident ->
        val current = _incidentsFlow.replayCache.firstOrNull()?.getOrNull() ?: return@onSuccess
        val updated = if (current.any { it.id == freshIncident.id }) {
            current.map { if (it.id == freshIncident.id) freshIncident else it }
        } else {
            listOf(freshIncident) + current
        }
        _incidentsFlow.emit(Result.success(updated))
    }

    suspend fun createIncident(incident: Incident): Result<Incident> = runCatchingCancellable {
        api.createIncident(IncidentDto.fromDomain(incident)).toDomain()
    }.onSuccess {
        refreshIncidents()
    }

    suspend fun updateIncident(incident: Incident): Result<Incident> = runCatchingCancellable {
        api.updateIncident(incident.id, IncidentDto.fromDomain(incident)).toDomain()
    }.onSuccess {
        refreshIncidents()
    }

    suspend fun deleteIncident(id: String): Result<Unit> = runCatchingCancellable {
        val response = api.deleteIncident(id)
        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to delete incident (HTTP ${response.code()})")
        }
    }.onSuccess {
        refreshIncidents()
    }

    // Re-throws CancellationException so coroutine cancellation propagates properly (standard runCatching swallows it).
    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
        runCatching(block).onFailure { if (it is CancellationException) throw it }
}