package com.example.myroutine.data.repository

import com.example.myroutine.BuildConfig
import com.example.myroutine.common.L
import com.example.myroutine.data.dto.Body
import com.example.myroutine.data.dto.Header
import com.example.myroutine.data.dto.HolidayDto
import com.example.myroutine.data.dto.Items
import com.example.myroutine.data.local.dao.HolidayCacheMetadataDao
import com.example.myroutine.data.local.entity.HolidayCacheMetadata
import com.example.myroutine.data.source.local.HolidayLocalDataSource
import com.example.myroutine.data.source.remote.HolidayApiService
import java.time.LocalDate
import javax.inject.Inject
import kotlin.collections.isNotEmpty


class HolidayRepository @Inject constructor(
    private val holidayApiService: HolidayApiService,
    private val holidayLocalDataSource: HolidayLocalDataSource,
    private val holidayCacheMetadataDao: HolidayCacheMetadataDao
) {
    companion object {
        private const val CACHE_EXPIRATION_DAYS = 15
        private const val TAG = "HolidayRepository"
    }

    suspend fun getHolidayInfo(year: Int, month: Int): HolidayDto {
        L.d(TAG, "getHolidayInfo called with year=$year, month=$month")

        val startDate = year * 10000 + month * 100 + 1
        val endDate = year * 10000 + month * 100 + LocalDate.of(year, month, 1).lengthOfMonth()
        L.d(TAG, "Computed date range: startDate=$startDate, endDate=$endDate")

        val lastCachedMetadata = holidayCacheMetadataDao.getMetadataByMonth(year, month)
        L.d(TAG, "Loaded lastCachedMetadata: $lastCachedMetadata")

        val isCacheValid = lastCachedMetadata != null &&
                (System.currentTimeMillis() - lastCachedMetadata.lastCachedTimestamp) < CACHE_EXPIRATION_DAYS * 24 * 60 * 60 * 1000L
        L.d(TAG, "Cache valid? $isCacheValid")

        val cachedHolidays = holidayLocalDataSource.getHolidaysByMonth(startDate, endDate)
        L.d(TAG, "Cached holidays count: ${cachedHolidays.size}")

        if (cachedHolidays.isNotEmpty() && isCacheValid) {
            L.d(TAG, "Using cached holidays data")

            val cachedResponse = HolidayDto(
                header = Header("00", "NORMAL SERVICE"),
                body = Body(
                    items = Items(cachedHolidays),
                    numOfRows = cachedHolidays.size,
                    pageNo = 1,
                    totalCount = cachedHolidays.size
                )
            )
            L.d(TAG, "Returning cached HolidayDto: $cachedResponse")
            return cachedResponse
        }

        val monthString = month.toString().padStart(2, '0')
        L.d(TAG, "Calling API with solYear=$year, solMonth=$monthString")

        val apiResponse = try {
            holidayApiService.getHolidayInfo(
                serviceKey = BuildConfig.HOLIDAY_API_KEY,
                solYear = year,
                solMonth = monthString
            ).also {
                L.d(TAG, "API response received: $it")
            }
        } catch (e: Exception) {
            L.e(TAG, "API call failed", e)
            return HolidayDto(
                header = Header("99", "API CALL FAILED"),
                body = Body(
                    items = Items(emptyList()),
                    numOfRows = 0,
                    pageNo = 1,
                    totalCount = 0
                )
            )
        }
        L.d(TAG, "Received API response: $apiResponse")

        apiResponse.body.items.item?.let { holidays ->
            L.d(TAG, "API response holiday items count: ${holidays.size}")
            L.d(TAG, "Deleting old holidays cache for month")
            holidayLocalDataSource.deleteHolidaysByMonth(startDate, endDate)

            L.d(TAG, "Inserting new holidays into local data source")
            holidayLocalDataSource.insertAll(holidays)

            val newMetadata = HolidayCacheMetadata(
                year = year,
                month = month,
                lastCachedTimestamp = System.currentTimeMillis()
            )
            L.d(TAG, "Updating cache metadata: $newMetadata")
            holidayCacheMetadataDao.insert(newMetadata)
        } ?: run {
            L.d(TAG, "API response body.item is null or empty")
        }

        L.d(TAG, "Returning API response")
        return apiResponse
    }
}
