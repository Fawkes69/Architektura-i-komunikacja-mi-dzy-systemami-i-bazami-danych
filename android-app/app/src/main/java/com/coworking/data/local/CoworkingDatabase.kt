package com.coworking.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.coworking.data.local.dao.ReservationDao
import com.coworking.data.local.dao.SpaceDao
import com.coworking.data.local.entities.ReservationEntity
import com.coworking.data.local.entities.SpaceEntity

@Database(
    entities = [SpaceEntity::class, ReservationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CoworkingDatabase : RoomDatabase() {
    abstract fun spaceDao(): SpaceDao
    abstract fun reservationDao(): ReservationDao
}
