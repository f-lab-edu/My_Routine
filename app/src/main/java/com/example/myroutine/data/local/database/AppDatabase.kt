package com.example.myroutine.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem

import com.example.myroutine.data.dto.HolidayItem
import com.example.myroutine.data.local.dao.HolidayDao

import com.example.myroutine.data.local.dao.HolidayCacheMetadataDao
import com.example.myroutine.data.local.entity.HolidayCacheMetadata

@Database(entities = [RoutineItem::class, RoutineCheck::class, HolidayItem::class, HolidayCacheMetadata::class], version = 5)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun routineCheckDao(): RoutineCheckDao
    abstract fun holidayDao(): HolidayDao
    abstract fun holidayCacheMetadataDao(): HolidayCacheMetadataDao
}
