package com.example.myroutine.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.LocalDate

@Dao
interface RoutineCheckDao {

    @Query("SELECT * FROM RoutineCheck WHERE routineId = :routineId AND date = :date")
    suspend fun getCheck(routineId: Int, date: LocalDate): RoutineCheck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: RoutineCheck)

    @Query("DELETE FROM RoutineCheck WHERE routineId = :routineId AND date = :date")
    suspend fun deleteCheck(routineId: Int, date: LocalDate)
}