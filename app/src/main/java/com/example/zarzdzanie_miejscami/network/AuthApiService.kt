package com.example.zarzdzanie_miejscami.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<TokenResponse>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): Response<TokenResponse>

    @GET("api/v1/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<UserDto>

    @PATCH("api/v1/users/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Response<UserDto>
}
