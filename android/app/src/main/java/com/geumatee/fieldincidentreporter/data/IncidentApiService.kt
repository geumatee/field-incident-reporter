package com.geumatee.fieldincidentreporter.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface IncidentApiService {
    @GET("incidents?_sort=createdAt&_order=desc")
    suspend fun getIncidents(): List<IncidentDto>

    @GET("incidents/{id}")
    suspend fun getIncident(@Path("id") id: String): IncidentDto

    @POST("incidents")
    suspend fun createIncident(@Body incident: IncidentDto): IncidentDto

    @PATCH("incidents/{id}")
    suspend fun updateIncident(
        @Path("id") id: String,
        @Body incident: IncidentDto
    ): IncidentDto

    @DELETE("incidents/{id}")
    suspend fun deleteIncident(@Path("id") id: String): Response<Unit>
}