package com.example.myroutine.data.local.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date.toDbString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value.toLocalDate()

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time.toDbString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value.toLocalTime()

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? = list.toDbString()

    @TypeConverter
    fun toIntList(value: String?): List<Int>? = value.toIntList()
}