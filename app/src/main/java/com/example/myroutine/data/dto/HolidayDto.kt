package com.example.myroutine.data.dto

import androidx.room.Entity
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class HolidayDto(
    @field:Element(name = "header")
    @param:Element(name = "header")
    val header: Header,
    @field:Element(name = "body")
    @param:Element(name = "body")
    val body: Body
)

@Root(name = "header", strict = false)
data class Header(
    @field:Element(name = "resultCode")
    @param:Element(name = "resultCode")
    val resultCode: String,
    @field:Element(name = "resultMsg")
    @param:Element(name = "resultMsg")
    val resultMsg: String
)

@Root(name = "body", strict = false)
data class Body(
    @field:Element(name = "items")
    @param:Element(name = "items")
    val items: Items,
    @field:Element(name = "numOfRows")
    @param:Element(name = "numOfRows")
    val numOfRows: Int,
    @field:Element(name = "pageNo")
    @param:Element(name = "pageNo")
    val pageNo: Int,
    @field:Element(name = "totalCount")
    @param:Element(name = "totalCount")
    val totalCount: Int
)

@Root(name = "items", strict = false)
data class Items(
    @field:ElementList(inline = true, required = false)
    @param:ElementList(inline = true, required = false)
    val item: List<HolidayItem>?
)

@Entity(primaryKeys = ["locdate", "seq"])
@Root(name = "item", strict = false)
data class HolidayItem(
    @field:Element(name = "dateKind")
    @param:Element(name = "dateKind")
    val dateKind: String,
    @field:Element(name = "dateName")
    @param:Element(name = "dateName")
    val dateName: String,
    @field:Element(name = "isHoliday")
    @param:Element(name = "isHoliday")
    val isHoliday: String,
    @field:Element(name = "locdate")
    @param:Element(name = "locdate")
    val locdate: Int,
    @field:Element(name = "seq")
    @param:Element(name = "seq")
    val seq: Int
)