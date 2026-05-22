package com.coworking.data.repository

import com.coworking.api.ReservationApiService
import com.coworking.api.ReservationCreateRequest
import com.coworking.api.ReservationDto
import com.coworking.data.local.TokenManager
import com.coworking.data.local.dao.ReservationDao
import com.coworking.data.local.entities.ReservationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val api: ReservationApiService,
    private val reservationDao: ReservationDao,
    private val tokenManager: TokenManager
) {
    fun observeReservations(): Flow<List<ReservationEntity>> = reservationDao.observeAll()

    suspend fun refreshReservations(): Result<List<ReservationDto>> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.getReservations("Bearer $token")
            if (response.isSuccessful) {
                val list = response.body()!!
                reservationDao.upsertAll(list.map { it.toEntity() })
                Result.Success(list)
            } else {
                Result.Error("Failed to load reservations: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun createReservation(
        spaceId: Int,
        startTime: String,
        endTime: String,
        notes: String = ""
    ): Result<ReservationDto> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.createReservation(
                "Bearer $token",
                ReservationCreateRequest(spaceId, startTime, endTime, notes)
            )
            if (response.isSuccessful) {
                val dto = response.body()!!
                reservationDao.upsertAll(listOf(dto.toEntity()))
                Result.Success(dto)
            } else {
                Result.Error("Booking failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun cancelReservation(id: Int): Result<ReservationDto> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.cancelReservation("Bearer $token", id)
            if (response.isSuccessful) {
                val dto = response.body()!!
                reservationDao.upsertAll(listOf(dto.toEntity()))
                Result.Success(dto)
            } else {
                Result.Error("Cancel failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    private fun ReservationDto.toEntity() = ReservationEntity(
        id = id, userId = userId, spaceId = spaceId,
        startTime = startTime, endTime = endTime,
        status = status.name, notes = notes, createdAt = createdAt
    )
}
