package com.example.myroutine.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myroutine.data.dto.HolidayItem

@Dao
interface HolidayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(holidays: List<HolidayItem>)

    @Query("SELECT * FROM HolidayItem WHERE locdate BETWEEN :startDate AND :endDate")
    suspend fun getHolidaysByMonth(startDate: Int, endDate: Int): List<HolidayItem>

    @Query("DELETE FROM HolidayItem")
    suspend fun deleteAll()
}
