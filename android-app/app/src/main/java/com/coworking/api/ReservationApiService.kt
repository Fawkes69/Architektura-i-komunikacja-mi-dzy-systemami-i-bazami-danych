package com.coworking.api

import retrofit2.Response
import retrofit2.http.*

interface ReservationApiService {

    // Spaces
    @GET("api/v1/spaces/")
    suspend fun getSpaces(
        @Header("Authorization") token: String,
        @Query("floor") floor: Int? = null,
        @Query("space_type") spaceType: String? = null,
        @Query("date") date: String? = null
    ): Response<List<SpaceDto>>

    @GET("api/v1/spaces/{id}")
    suspend fun getSpace(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<SpaceDto>

    @POST("api/v1/spaces/")
    suspend fun createSpace(
        @Header("Authorization") token: String,
        @Body request: SpaceCreateRequest
    ): Response<SpaceDto>

    @PATCH("api/v1/spaces/{id}")
    suspend fun updateSpace(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, Any>
    ): Response<SpaceDto>

    @DELETE("api/v1/spaces/{id}")
    suspend fun deleteSpace(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    // Reservations
    @GET("api/v1/reservations/")
    suspend fun getReservations(
        @Header("Authorization") token: String
    ): Response<List<ReservationDto>>

    @POST("api/v1/reservations/")
    suspend fun createReservation(
        @Header("Authorization") token: String,
        @Body request: ReservationCreateRequest
    ): Response<ReservationDto>

    @GET("api/v1/reservations/{id}")
    suspend fun getReservation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ReservationDto>

    @DELETE("api/v1/reservations/{id}")
    suspend fun cancelReservation(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ReservationDto>
}
