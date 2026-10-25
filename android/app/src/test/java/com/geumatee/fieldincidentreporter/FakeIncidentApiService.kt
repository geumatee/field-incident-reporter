package com.geumatee.fieldincidentreporter

import com.geumatee.fieldincidentreporter.data.IncidentApiService
import com.geumatee.fieldincidentreporter.data.IncidentDto
import retrofit2.Response

class FakeIncidentApiService(
    var incidentsToReturn: List<IncidentDto> = emptyList(),
    var errorToThrow: Throwable? = null
) : IncidentApiService {
    override suspend fun getIncidents(): List<IncidentDto> {
        errorToThrow?.let { throw it }
        return incidentsToReturn
    }

    override suspend fun getIncident(id: String): IncidentDto = TODO()
    override suspend fun createIncident(incident: IncidentDto): IncidentDto = TODO()
    override suspend fun updateIncident(id: String, incident: IncidentDto): IncidentDto = TODO()
    override suspend fun deleteIncident(id: String): Response<Unit> = TODO()
}