package com.example.myroutine.data.source.local

import com.example.myroutine.data.dto.HolidayItem
import com.example.myroutine.data.local.dao.HolidayDao
import javax.inject.Inject

class HolidayLocalDataSource @Inject constructor(
    private val holidayDao: HolidayDao
) {
    suspend fun getHolidaysByMonth(startDate: Int, endDate: Int): List<HolidayItem> {
        return holidayDao.getHolidaysByMonth(startDate, endDate)
    }

    suspend fun insertAll(holidays: List<HolidayItem>) {
        holidayDao.insertAll(holidays)
    }

    suspend fun deleteHolidaysByMonth(startDate: Int, endDate: Int) {
        holidayDao.deleteHolidaysByMonth(startDate, endDate)
    }

    suspend fun deleteAll() {
        holidayDao.deleteAll()
    }
}
