package com.example.myroutine.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem

@Database(entities = [RoutineItem::class, RoutineCheck::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun routineCheckDao(): RoutineCheckDao
}
