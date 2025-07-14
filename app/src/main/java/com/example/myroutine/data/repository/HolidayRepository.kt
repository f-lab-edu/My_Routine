package com.example.myroutine.data.repository

import com.example.myroutine.BuildConfig
import com.example.myroutine.data.dto.Body
import com.example.myroutine.data.dto.Header
import com.example.myroutine.data.dto.HolidayDto
import com.example.myroutine.data.dto.Items
import com.example.myroutine.data.dto.Response
import com.example.myroutine.data.local.dao.HolidayCacheMetadataDao
import com.example.myroutine.data.local.entity.HolidayCacheMetadata
import com.example.myroutine.data.source.local.HolidayLocalDataSource
import com.example.myroutine.data.source.remote.HolidayApiService
import java.time.LocalDate
import javax.inject.Inject


class HolidayRepository @Inject constructor(
    private val holidayApiService: HolidayApiService,
    private val holidayLocalDataSource: HolidayLocalDataSource,
    private val holidayCacheMetadataDao: HolidayCacheMetadataDao
) {
    companion object {
        private const val CACHE_EXPIRATION_DAYS = 15
    }

    suspend fun getHolidayInfo(year: Int, month: Int): HolidayDto {
        val startDate = year * 10000 + month * 100 + 1
        val endDate = year * 10000 + month * 100 + LocalDate.of(year, month, 1).lengthOfMonth()

        val lastCachedMetadata = holidayCacheMetadataDao.getMetadataByMonth(year, month)

        val isCacheValid = lastCachedMetadata != null &&
                (System.currentTimeMillis() - lastCachedMetadata.lastCachedTimestamp) < CACHE_EXPIRATION_DAYS * 24 * 60 * 60 * 1000L

        val cachedHolidays = holidayLocalDataSource.getHolidaysByMonth(startDate, endDate)

        if (cachedHolidays.isNotEmpty() && isCacheValid) {
            // 캐시된 데이터가 있고 유효하면 HolidayDto 형태로 변환하여 반환
            return HolidayDto(
                response = Response(
                    header = Header("00", "NORMAL SERVICE"),
                    body = Body(
                        items = Items(cachedHolidays),
                        numOfRows = cachedHolidays.size,
                        pageNo = 1,
                        totalCount = cachedHolidays.size
                    )
                )
            )
        }

        val monthString = month.toString().padStart(2, '0')

        // 캐시된 데이터가 없거나 유효하지 않으면 API 호출
        val apiResponse = holidayApiService.getHolidayInfo(
            serviceKey = BuildConfig.HOLIDAY_API_KEY,
            solYear = year,
            solMonth = monthString
        )

        // API 응답을 캐시에 저장하고 메타데이터 업데이트
        apiResponse.response.body.items.item?.let { holidays ->
            holidayLocalDataSource.deleteHolidaysByMonth(startDate, endDate) // 해당 월의 기존 캐시 삭제
            holidayLocalDataSource.insertAll(holidays)
            holidayCacheMetadataDao.insert(HolidayCacheMetadata(year = year, month = month, lastCachedTimestamp = System.currentTimeMillis()))
        }

        return apiResponse
    }
}
