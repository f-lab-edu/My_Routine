package com.example.myroutine.di

import android.content.Context
import androidx.room.Room
import com.example.myroutine.data.local.database.AppDatabase
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "myroutine-db"
            ).fallbackToDestructiveMigration(true) // 개발과정에서 스키마 변경 시 데이터 초기화, 추후 생산 환경에서는 제거, 마이그레이션으로 처리
            .build()
    }

    @Provides
    fun provideRoutineDao(db: AppDatabase): RoutineDao = db.routineDao()

    @Provides
    fun provideRoutineCheckDao(db: AppDatabase): RoutineCheckDao = db.routineCheckDao()
}
