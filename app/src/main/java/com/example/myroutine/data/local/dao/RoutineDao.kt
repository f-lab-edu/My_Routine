package com.example.myroutine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myroutine.data.local.entity.RoutineItem
import java.time.LocalDate

@Dao
interface RoutineDao {
    @Query("SELECT * FROM RoutineItem")
    suspend fun getAll(): List<RoutineItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routines: List<RoutineItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineItem): Long

    @Query("""
        SELECT * FROM RoutineItem
        WHERE repeatType IN ('NONE', 'ONCE')
          AND specificDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getNonRepeatingRoutinesInPeriod(startDate: LocalDate, endDate: LocalDate): List<RoutineItem>

    @Query("""
        SELECT * FROM RoutineItem
        WHERE repeatType = 'WEEKLY'
          AND startDate <= :date
          AND (repeatDays & 
            CASE strftime('%w', :date)
                WHEN '0' THEN 64
                WHEN '1' THEN 1
                WHEN '2' THEN 2
                WHEN '3' THEN 4
                WHEN '4' THEN 8
                WHEN '5' THEN 16
                WHEN '6' THEN 32
            END
          ) != 0
    """)
    suspend fun getWeeklyRoutinesByDate(date: LocalDate): List<RoutineItem>

    @Query("""
        SELECT * FROM RoutineItem
        WHERE repeatType = 'EVERY_X_DAYS'
          AND startDate <= :date
          AND ((julianday(:date) - julianday(startDate)) % repeatIntervalDays) = 0
    """)
    suspend fun getEveryXDaysRoutinesByDate(date: LocalDate): List<RoutineItem>

    @Query("""
        SELECT * FROM RoutineItem
        WHERE repeatType = 'WEEKDAY_HOLIDAY'
          AND startDate <= :date
          AND strftime('%w', :date) NOT IN ('0', '6')
    """)
    suspend fun getWeekdayHolidayRoutinesByDate(date: LocalDate): List<RoutineItem>
}
