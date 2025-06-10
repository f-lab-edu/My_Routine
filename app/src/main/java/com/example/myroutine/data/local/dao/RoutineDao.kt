package com.example.myroutine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myroutine.data.local.entity.RoutineItem

@Dao
interface RoutineDao {
    @Query("SELECT * FROM RoutineItem")
    suspend fun getAll(): List<RoutineItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routines: List<RoutineItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineItem)
}
