package com.example.myroutine.data.dto

data class HolidayDto(
    val response: Response
)

data class Response(
    val header: Header,
    val body: Body
)

data class Header(
    val resultCode: String,
    val resultMsg: String
)

data class Body(
    val items: Items,
    val numOfRows: Int,
    val pageNo: Int,
    val totalCount: Int
)

data class Items(
    val item: List<HolidayItem>
)

data class HolidayItem(
    val dateKind: String,
    val dateName: String,
    val isHoliday: String,
    val locdate: Int,
    val seq: Int
)
