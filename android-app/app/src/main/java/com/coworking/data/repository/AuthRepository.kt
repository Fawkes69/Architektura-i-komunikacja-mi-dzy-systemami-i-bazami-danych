package com.coworking.data.repository

import com.coworking.api.AuthApiService
import com.coworking.api.RegisterRequest
import com.coworking.api.UserDto
import com.coworking.data.local.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(email, password)
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveTokens(body.accessToken, body.refreshToken)
                Result.Success(Unit)
            } else {
                Result.Error("Invalid credentials")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun register(email: String, fullName: String, password: String): Result<Unit> {
        return try {
            val response = api.register(RegisterRequest(email, fullName, password))
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("Registration failed: ${response.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMe(): Result<UserDto> {
        return try {
            val token = tokenManager.accessToken.firstOrNull() ?: return Result.Error("Not logged in")
            val response = api.getMe("Bearer $token")
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to load profile")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() = tokenManager.clearTokens()

    fun accessTokenFlow() = tokenManager.accessToken
}
