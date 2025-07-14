package com.example.myroutine.data.repository

import com.example.myroutine.BuildConfig
import com.example.myroutine.data.dto.HolidayDto
import com.example.myroutine.data.source.local.HolidayLocalDataSource
import com.example.myroutine.data.source.remote.HolidayApiService
import java.time.LocalDate
import javax.inject.Inject

class HolidayRepository @Inject constructor(
    private val holidayApiService: HolidayApiService,
    private val holidayLocalDataSource: HolidayLocalDataSource
) {
    suspend fun getHolidayInfo(year: Int, month: Int): HolidayDto {
        val startDate = year * 10000 + month * 100 + 1
        val endDate = year * 10000 + month * 100 + LocalDate.of(year, month, 1).lengthOfMonth()

        val cachedHolidays = holidayLocalDataSource.getHolidaysByMonth(startDate, endDate)
        if (cachedHolidays.isNotEmpty()) {
            // 캐시된 데이터가 있으면 HolidayDto 형태로 변환하여 반환
            return HolidayDto(
                response = HolidayDto.Response(
                    header = HolidayDto.Header("00", "NORMAL SERVICE"),
                    body = HolidayDto.Body(
                        items = HolidayDto.Items(cachedHolidays),
                        numOfRows = cachedHolidays.size,
                        pageNo = 1,
                        totalCount = cachedHolidays.size
                    )
                )
            )
        }

        // 캐시된 데이터가 없으면 API 호출
        val apiResponse = holidayApiService.getHolidayInfo(
            serviceKey = BuildConfig.HOLIDAY_API_KEY,
            pageNo = 1,
            numOfRows = 100,
            solYear = year,
            solMonth = month
        )

        // API 응답을 캐시에 저장
        apiResponse.response.body.items.item?.let { holidays ->
            holidayLocalDataSource.insertAll(holidays)
        }

        return apiResponse
    }
}
