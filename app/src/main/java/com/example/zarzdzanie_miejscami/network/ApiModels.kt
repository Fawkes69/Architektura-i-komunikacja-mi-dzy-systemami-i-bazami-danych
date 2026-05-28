package com.example.zarzdzanie_miejscami.network

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    @SerializedName("full_name") val fullName: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String
)

data class UserDto(
    val id: Int,
    val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_admin") val isAdmin: Boolean,
    @SerializedName("created_at") val createdAt: String
)

enum class SpaceType {
    @SerializedName("desk") DESK,
    @SerializedName("meeting_room") MEETING_ROOM
}

data class SpaceDto(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("space_type") val spaceType: SpaceType,
    val floor: Int,
    val capacity: Int,
    @SerializedName("is_available") val isAvailable: Boolean,
    @SerializedName("pos_x") val posX: Float,
    @SerializedName("pos_y") val posY: Float,
    @SerializedName("created_at") val createdAt: String
)

data class SpaceCreateRequest(
    val name: String,
    val description: String = "",
    @SerializedName("space_type") val spaceType: String,
    val floor: Int,
    val capacity: Int = 1,
    @SerializedName("is_available") val isAvailable: Boolean = true,
    @SerializedName("pos_x") val posX: Float = 0f,
    @SerializedName("pos_y") val posY: Float = 0f
)

data class SpaceUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    @SerializedName("is_available") val isAvailable: Boolean? = null,
    val capacity: Int? = null,
    @SerializedName("pos_x") val posX: Float? = null,
    @SerializedName("pos_y") val posY: Float? = null
)

enum class ReservationStatus {
    @SerializedName("active") ACTIVE,
    @SerializedName("cancelled") CANCELLED
}

data class ReservationCreateRequest(
    @SerializedName("space_id") val spaceId: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val notes: String = ""
)

data class ReservationDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("space_id") val spaceId: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    val status: ReservationStatus,
    val notes: String,
    @SerializedName("created_at") val createdAt: String,
    val space: SpaceDto? = null
)
