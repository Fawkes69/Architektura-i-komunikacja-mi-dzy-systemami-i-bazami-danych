package com.coworking.di

import android.content.Context
import androidx.room.Room
import com.coworking.BuildConfig
import com.coworking.api.AuthApiService
import com.coworking.api.ReservationApiService
import com.coworking.data.local.CoworkingDatabase
import com.coworking.data.local.dao.ReservationDao
import com.coworking.data.local.dao.SpaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.AUTH_BASE_URL + "/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    @Named("reservation")
    fun provideReservationRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.RESERVATION_BASE_URL + "/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideReservationApiService(@Named("reservation") retrofit: Retrofit): ReservationApiService =
        retrofit.create(ReservationApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CoworkingDatabase =
        Room.databaseBuilder(context, CoworkingDatabase::class.java, "coworking.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSpaceDao(db: CoworkingDatabase): SpaceDao = db.spaceDao()

    @Provides
    fun provideReservationDao(db: CoworkingDatabase): ReservationDao = db.reservationDao()
}
