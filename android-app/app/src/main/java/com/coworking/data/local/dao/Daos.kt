package com.coworking.data.local.dao

import androidx.room.*
import com.coworking.data.local.entities.ReservationEntity
import com.coworking.data.local.entities.SpaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces ORDER BY floor, name")
    fun observeAll(): Flow<List<SpaceEntity>>

    @Query("SELECT * FROM spaces WHERE floor = :floor ORDER BY name")
    fun observeByFloor(floor: Int): Flow<List<SpaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(spaces: List<SpaceEntity>)

    @Query("DELETE FROM spaces")
    suspend fun clearAll()
}

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY startTime DESC")
    fun observeAll(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reservations: List<ReservationEntity>)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM reservations")
    suspend fun clearAll()
}
