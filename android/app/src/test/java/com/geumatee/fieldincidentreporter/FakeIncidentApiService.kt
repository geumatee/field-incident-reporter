package com.geumatee.fieldincidentreporter

import com.geumatee.fieldincidentreporter.data.IncidentApiService
import com.geumatee.fieldincidentreporter.data.IncidentDto
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

class FakeIncidentApiService(
    initialItems: List<IncidentDto> = emptyList()
) : IncidentApiService {

    val items = initialItems.toMutableList()
    var errorToThrow: Throwable? = null
    var deleteResponseCode: Int = 200

    override suspend fun getIncidents(): List<IncidentDto> {
        errorToThrow?.let { throw it }
        return items.toList()
    }

    override suspend fun getIncident(id: String): IncidentDto {
        errorToThrow?.let { throw it }
        return items.find { it.id == id }
            ?: throw IOException("Incident not found: $id")
    }

    override suspend fun createIncident(incident: IncidentDto): IncidentDto {
        errorToThrow?.let { throw it }
        items.add(0, incident)
        return incident
    }

    override suspend fun updateIncident(id: String, incident: IncidentDto): IncidentDto {
        errorToThrow?.let { throw it }
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) {
            items[index] = incident
        } else {
            items.add(incident)
        }
        return incident
    }

    override suspend fun deleteIncident(id: String): Response<Unit> {
        errorToThrow?.let { throw it }
        if (deleteResponseCode != 200) {
            return Response.error(deleteResponseCode, "".toResponseBody(null))
        }
        items.removeAll { it.id == id }
        return Response.success(Unit)
    }
}
