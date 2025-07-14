package com.example.myroutine.data.dto

import androidx.room.Entity
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
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
    @Path("items") @Element(name = "item")
    val item: List<HolidayItem>?,
    @PropertyElement(name = "numOfRows")
    val numOfRows: Int,
    @PropertyElement(name = "pageNo")
    val pageNo: Int,
    @PropertyElement(name = "totalCount")
    val totalCount: Int
)

@Entity(primaryKeys = ["locdate", "seq"])
@Xml(name = "item")
data class HolidayItem(
    @PropertyElement(name = "dateKind")
    val dateKind: String,
    @PropertyElement(name = "dateName")
    val dateName: String,
    @PropertyElement(name = "isHoliday")
    val isHoliday: String,
    @PropertyElement(name = "locdate")
    val locdate: Int,
    @PropertyElement(name = "seq")
    val seq: Int
)
