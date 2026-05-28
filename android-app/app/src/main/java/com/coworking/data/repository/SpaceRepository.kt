package com.coworking.data.repository

import com.coworking.api.ReservationApiService
import com.coworking.api.SpaceCreateRequest
import com.coworking.api.SpaceDto
import com.coworking.data.local.TokenManager
import com.coworking.data.local.dao.SpaceDao
import com.coworking.data.local.entities.SpaceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpaceRepository @Inject constructor(
    private val api: ReservationApiService,
    private val spaceDao: SpaceDao,
    private val tokenManager: TokenManager
) {
    fun observeSpaces(): Flow<List<SpaceEntity>> = spaceDao.observeAll()
    fun observeSpacesByFloor(floor: Int): Flow<List<SpaceEntity>> = spaceDao.observeByFloor(floor)

    suspend fun refreshSpaces(date: String? = null, floor: Int? = null): Result<List<SpaceDto>> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.getSpaces("Bearer $token", floor = floor, date = date)
            if (response.isSuccessful) {
                val spaces = response.body()!!
                spaceDao.upsertAll(spaces.map { it.toEntity() })
                Result.Success(spaces)
            } else {
                Result.Error("Failed to load spaces: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun createSpace(request: SpaceCreateRequest): Result<SpaceDto> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.createSpace("Bearer $token", request)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to create space: ${response.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun deleteSpace(id: Int): Result<Unit> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.deleteSpace("Bearer $token", id)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Failed to delete space")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    private fun SpaceDto.toEntity() = SpaceEntity(
        id = id, name = name, description = description,
        spaceType = spaceType.name, floor = floor, capacity = capacity,
        isAvailable = isAvailable, posX = posX, posY = posY
    )
}
