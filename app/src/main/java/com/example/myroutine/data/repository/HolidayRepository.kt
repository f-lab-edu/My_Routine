package com.example.myroutine.data.repository

import android.util.Xml
import com.example.myroutine.BuildConfig
import com.example.myroutine.common.L
import com.example.myroutine.data.dto.Body
import com.example.myroutine.data.dto.Header
import com.example.myroutine.data.dto.HolidayDto
import com.example.myroutine.data.dto.HolidayItem
import com.example.myroutine.data.dto.Items
import com.example.myroutine.data.dto.Response
import com.example.myroutine.data.local.dao.HolidayCacheMetadataDao
import com.example.myroutine.data.local.entity.HolidayCacheMetadata
import com.example.myroutine.data.source.local.HolidayLocalDataSource
import com.example.myroutine.data.source.remote.HolidayApiService
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.StringReader
import java.time.LocalDate
import javax.inject.Inject


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

        val isCacheValid = lastCachedMetadata != null &&
                (System.currentTimeMillis() - lastCachedMetadata.lastCachedTimestamp) < CACHE_EXPIRATION_DAYS * 24 * 60 * 60 * 1000L

        val cachedHolidays = holidayLocalDataSource.getHolidaysByMonth(startDate, endDate)

        if (cachedHolidays.isNotEmpty() && isCacheValid) {
            L.d(TAG, "Using cached holidays data")
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
        L.d(TAG, "Calling API with solYear=$year, solMonth=$monthString")

        val apiResponseXml = try {
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
                response = Response(
                    header = Header("99", "API CALL FAILED"),
                    body = Body(
                        items = Items(emptyList()),
                        numOfRows = 0,
                        pageNo = 1,
                        totalCount = 0
                    )
                )
            )
        }

        val apiResponse = parseHolidayXml(apiResponseXml)
        L.d(TAG, "Parsed API response: $apiResponse")

        apiResponse.response.body.items.item?.let { holidays ->
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
            L.d(TAG, "API response body.items.item is null or empty")
        }

        L.d(TAG, "Returning API response")
        return apiResponse
    }

    private fun parseHolidayXml(xmlString: String): HolidayDto {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlString))

        var eventType = parser.eventType
        var holidayDto: HolidayDto? = null
        var header: Header? = null
        var body: Body? = null
        val holidayItems = mutableListOf<HolidayItem>()
        var currentHolidayItem: HolidayItem? = null

        var resultCode: String? = null
        var resultMsg: String? = null
        var numOfRows: Int = 0
        var pageNo: Int = 0
        var totalCount: Int = 0

        var dateKind: String? = null
        var dateName: String? = null
        var isHoliday: String? = null
        var locdate: Int? = null
        var seq: Int? = null

        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "header" -> {
                                resultCode = null
                                resultMsg = null
                            }
                            "body" -> {
                                numOfRows = 0
                                pageNo = 0
                                totalCount = 0
                            }
                            "item" -> {
                                dateKind = null
                                dateName = null
                                isHoliday = null
                                locdate = null
                                seq = null
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        when (parser.parent.name) {
                            "resultCode" -> resultCode = parser.text
                            "resultMsg" -> resultMsg = parser.text
                            "numOfRows" -> numOfRows = parser.text.toInt()
                            "pageNo" -> pageNo = parser.text.toInt()
                            "totalCount" -> totalCount = parser.text.toInt()
                            "dateKind" -> dateKind = parser.text
                            "dateName" -> dateName = parser.text
                            "isHoliday" -> isHoliday = parser.text
                            "locdate" -> locdate = parser.text.toInt()
                            "seq" -> seq = parser.text.toInt()
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tagName) {
                            "header" -> {
                                header = Header(resultCode ?: "", resultMsg ?: "")
                            }
                            "body" -> {
                                body = Body(Items(holidayItems), numOfRows, pageNo, totalCount)
                            }
                            "item" -> {
                                currentHolidayItem = HolidayItem(
                                    dateKind = dateKind ?: "",
                                    dateName = dateName ?: "",
                                    isHoliday = isHoliday ?: "",
                                    locdate = locdate ?: 0,
                                    seq = seq ?: 0
                                )
                                holidayItems.add(currentHolidayItem)
                            }
                            "response" -> {
                                if (header != null && body != null) {
                                    holidayDto = HolidayDto(Response(header, body))
                                }
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: XmlPullParserException) {
            L.e(TAG, "XML Pull Parser Exception", e)
        } catch (e: IOException) {
            L.e(TAG, "IO Exception during XML parsing", e)
        } catch (e: Exception) {
            L.e(TAG, "General Exception during XML parsing", e)
        }

        return holidayDto ?: HolidayDto(
            response = Response(
                header = Header("99", "XML PARSING FAILED"),
                body = Body(
                    items = Items(emptyList()),
                    numOfRows = 0,
                    pageNo = 1,
                    totalCount = 0
                )
            )
        )
    }
}
