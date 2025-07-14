package com.example.myroutine.di

import android.content.Context
import androidx.room.Room
import com.example.myroutine.data.local.dao.HolidayDao
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my-routine-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRoutineDao(appDatabase: AppDatabase): RoutineDao {
        return appDatabase.routineDao()
    }

    @Provides
    fun provideRoutineCheckDao(appDatabase: AppDatabase): RoutineCheckDao {
        return appDatabase.routineCheckDao()
    }

    @Provides
    fun provideHolidayDao(appDatabase: AppDatabase): HolidayDao {
        return appDatabase.holidayDao()
    }

    @Provides
    fun provideHolidayCacheMetadataDao(appDatabase: AppDatabase): HolidayCacheMetadataDao {
        return appDatabase.holidayCacheMetadataDao()
    }
}