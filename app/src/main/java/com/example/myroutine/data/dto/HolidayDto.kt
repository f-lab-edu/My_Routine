package com.example.myroutine.data.dto

import androidx.room.Entity
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "response")
data class HolidayDto(
    @JacksonXmlProperty(localName = "header")
    val header: Header,
    @JacksonXmlProperty(localName = "body")
    val body: Body
)

data class Header(
    @JacksonXmlProperty(localName = "resultCode")
    val resultCode: String,
    @JacksonXmlProperty(localName = "resultMsg")
    val resultMsg: String
)

data class Body(
    @JacksonXmlProperty(localName = "items")
    val items: Items,
    @JacksonXmlProperty(localName = "numOfRows")
    val numOfRows: Int,
    @JacksonXmlProperty(localName = "pageNo")
    val pageNo: Int,
    @JacksonXmlProperty(localName = "totalCount")
    val totalCount: Int
)

data class Items(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val item: List<HolidayItem>?
)

@Entity(primaryKeys = ["locdate", "seq"])
data class HolidayItem(
    @JacksonXmlProperty(localName = "dateKind")
    val dateKind: String,
    @JacksonXmlProperty(localName = "dateName")
    val dateName: String,
    @JacksonXmlProperty(localName = "isHoliday")
    val isHoliday: String,
    @JacksonXmlProperty(localName = "locdate")
    val locdate: Int,
    @JacksonXmlProperty(localName = "seq")
    val seq: Int
)
