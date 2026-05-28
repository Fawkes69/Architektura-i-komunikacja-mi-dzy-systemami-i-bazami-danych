package com.coworking.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class SpaceEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val spaceType: String,
    val floor: Int,
    val capacity: Int,
    val isAvailable: Boolean,
    val posX: Float,
    val posY: Float,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val spaceId: Int,
    val startTime: String,
    val endTime: String,
    val status: String,
    val notes: String,
    val createdAt: String,
    val cachedAt: Long = System.currentTimeMillis()
)
