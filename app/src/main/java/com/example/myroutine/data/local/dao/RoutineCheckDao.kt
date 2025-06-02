package com.example.myroutine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myroutine.data.local.entity.RoutineCheck
import java.time.LocalDate

@Dao
interface RoutineCheckDao {

    @Query("SELECT * FROM RoutineCheck WHERE routineId = :routineId AND completeDate = :date")
    suspend fun getCheck(routineId: Int, date: LocalDate): RoutineCheck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: RoutineCheck)

    @Query("DELETE FROM RoutineCheck WHERE routineId = :routineId AND completeDate = :date")
    suspend fun deleteCheck(routineId: Int, date: LocalDate)

    @Query("SELECT * FROM RoutineCheck WHERE completeDate = :date")
    suspend fun getChecksForDate(date: LocalDate): List<RoutineCheck>

}