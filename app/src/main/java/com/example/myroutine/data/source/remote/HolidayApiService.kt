package com.example.myroutine.data.source.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface HolidayApiService {
    @GET("getRestDeInfo")
    suspend fun getHolidayInfo(
        @Query("serviceKey") serviceKey: String,
        @Query("solYear") solYear: Int,
        @Query("solMonth") solMonth: String
    ): String
}
