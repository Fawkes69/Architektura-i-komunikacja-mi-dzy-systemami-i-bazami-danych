package com.example.zarzdzanie_miejscami.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ReservationApiService {

    @GET("api/v1/spaces/")
    suspend fun getSpaces(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null
    ): Response<List<SpaceDto>>

    @POST("api/v1/spaces/")
    suspend fun createSpace(
        @Header("Authorization") token: String,
        @Body body: SpaceCreateRequest
    ): Response<SpaceDto>

    @PATCH("api/v1/spaces/{spaceId}")
    suspend fun updateSpace(
        @Header("Authorization") token: String,
        @Path("spaceId") spaceId: Int,
        @Body body: SpaceUpdateRequest
    ): Response<SpaceDto>

    @DELETE("api/v1/spaces/{spaceId}")
    suspend fun deleteSpace(
        @Header("Authorization") token: String,
        @Path("spaceId") spaceId: Int
    ): Response<Unit>

    @GET("api/v1/reservations/")
    suspend fun getReservations(@Header("Authorization") token: String): Response<List<ReservationDto>>

    @POST("api/v1/reservations/")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body body: ReservationCreateRequest
    ): Response<ReservationDto>

    @DELETE("api/v1/reservations/{reservationId}")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("reservationId") reservationId: Int
    ): Response<ReservationDto>
}
