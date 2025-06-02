package com.example.myroutine.data.local.database

import java.time.LocalDate
import java.time.LocalTime

fun LocalDate?.toDbString(): String? = this?.toString()
fun String?.toLocalDate(): LocalDate? = this?.let { LocalDate.parse(it) }

fun LocalTime?.toDbString(): String? = this?.toString()
fun String?.toLocalTime(): LocalTime? = this?.let { LocalTime.parse(it) }

fun List<Int>?.toDbString(): String? = this?.joinToString(",")
fun String?.toIntList(): List<Int>? = this?.split(",")?.mapNotNull { it.toIntOrNull() }
