package com.example.myroutine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday_cache_metadata", primaryKeys = ["year", "month"])
data class HolidayCacheMetadata(
    val year: Int,
    val month: Int,
    val lastCachedTimestamp: Long
)
