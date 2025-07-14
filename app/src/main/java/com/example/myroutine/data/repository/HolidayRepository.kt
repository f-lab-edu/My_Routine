package com.example.myroutine.data.repository

import com.example.myroutine.BuildConfig
import com.example.myroutine.data.source.remote.HolidayApiService
import com.example.myroutine.data.dto.HolidayDto
import javax.inject.Inject

class HolidayRepository @Inject constructor(
    private val holidayApiService: HolidayApiService
) {
    suspend fun getHolidayInfo(year: Int, month: Int): HolidayDto {
        return holidayApiService.getHolidayInfo(
            serviceKey = BuildConfig.HOLIDAY_API_KEY,
            pageNo = 1,
            numOfRows = 100,
            solYear = year,
            solMonth = month
        )
    }
}
