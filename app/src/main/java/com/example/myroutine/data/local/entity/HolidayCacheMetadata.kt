package com.example.myroutine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holiday_cache_metadata")
data class HolidayCacheMetadata(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastCachedTimestamp: Long
)
