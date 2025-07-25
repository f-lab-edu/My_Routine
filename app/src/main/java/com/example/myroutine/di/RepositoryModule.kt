package com.example.myroutine.di

import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.repository.HolidayRepository
import com.example.myroutine.data.repository.RoutineRepository
import com.example.myroutine.data.repository.RoutineRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideRoutineRepository(
        routineDao: RoutineDao,
        checkDao: RoutineCheckDao,
        holidayRepository: HolidayRepository
    ): RoutineRepository {
        return RoutineRepositoryImpl(routineDao, checkDao, holidayRepository)
    }
}
