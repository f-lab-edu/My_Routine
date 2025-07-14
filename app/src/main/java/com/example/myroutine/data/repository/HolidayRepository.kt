package com.example.myroutine.data.repository

import com.example.myroutine.BuildConfig
import com.example.myroutine.data.dto.HolidayDto
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
    private val CACHE_EXPIRATION_DAYS = 15

    suspend fun getHolidayInfo(year: Int, month: Int): HolidayDto {
        val startDate = year * 10000 + month * 100 + 1
        val endDate = year * 10000 + month * 100 + LocalDate.of(year, month, 1).lengthOfMonth()

        val cachedHolidays = holidayLocalDataSource.getHolidaysByMonth(startDate, endDate)
        val lastCachedMetadata = holidayCacheMetadataDao.getLatestMetadata()

        val isCacheValid = lastCachedMetadata != null &&
                (System.currentTimeMillis() - lastCachedMetadata.lastCachedTimestamp) < CACHE_EXPIRATION_DAYS * 24 * 60 * 60 * 1000L

        if (cachedHolidays.isNotEmpty() && isCacheValid) {
            // 캐시된 데이터가 있고 유효하면 HolidayDto 형태로 변환하여 반환
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

        // 캐시된 데이터가 없거나 유효하지 않으면 API 호출
        val apiResponse = holidayApiService.getHolidayInfo(
            serviceKey = BuildConfig.HOLIDAY_API_KEY,
            pageNo = 1,
            numOfRows = 100,
            solYear = year,
            solMonth = month
        )

        // API 응답을 캐시에 저장하고 메타데이터 업데이트
        apiResponse.response.body.items.item?.let { holidays ->
            holidayLocalDataSource.deleteAll() // 기존 캐시 삭제
            holidayLocalDataSource.insertAll(holidays)
            holidayCacheMetadataDao.insert(HolidayCacheMetadata(lastCachedTimestamp = System.currentTimeMillis()))
        }

        return apiResponse
    }
}
