package com.example.zarzdzanie_miejscami.network

import com.example.zarzdzanie_miejscami.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val authRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.AUTH_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val reservationRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.RESERVATION_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApiService = authRetrofit.create(AuthApiService::class.java)
    val reservationApi: ReservationApiService = reservationRetrofit.create(ReservationApiService::class.java)
}
