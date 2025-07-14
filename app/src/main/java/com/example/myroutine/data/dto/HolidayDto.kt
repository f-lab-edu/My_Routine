package com.example.myroutine.data.dto

import androidx.room.Entity
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "response")
data class HolidayDto(
    @Element(name = "header")
    val header: Header,
    @Element(name = "body")
    val body: Body
)

@Xml(name = "header")
data class Header(
    @PropertyElement(name = "resultCode")
    val resultCode: String,
    @PropertyElement(name = "resultMsg")
    val resultMsg: String
)

@Xml(name = "body")
data class Body(
    @Element(name = "items")
    val items: Items,
    @PropertyElement(name = "numOfRows")
    val numOfRows: Int,
    @PropertyElement(name = "pageNo")
    val pageNo: Int,
    @PropertyElement(name = "totalCount")
    val totalCount: Int
)

@Xml(name = "items")
data class Items(
    @Element(name = "item")
    val item: List<HolidayItem>?
)

@Entity(primaryKeys = ["locdate", "seq"])
@Xml(name = "item")
data class HolidayItem(
    @PropertyElement(name = "dateKind")
    val dateKind: String,
    @PropertyElement(name = "dateName")
    val dateName: String,
    @PropertyElement(name = "isHoliday")
    val holidayFlag: String,
    @PropertyElement(name = "locdate")
    val locdate: Int,
    @PropertyElement(name = "seq")
    val seq: Int
)
