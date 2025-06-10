package com.example.myroutine.di

import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.repository.RoutineRepository
import com.example.myroutine.data.repository.RoutineRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideRoutineRepository(
        routineDao: RoutineDao,
        checkDao: RoutineCheckDao
    ): RoutineRepository {
        return RoutineRepositoryImpl(routineDao, checkDao)
    }
}
