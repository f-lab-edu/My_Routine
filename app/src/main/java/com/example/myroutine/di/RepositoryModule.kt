package com.example.myroutine.di

import com.example.myroutine.data.RoutineRepository
import com.example.myroutine.data.RoutineRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        impl: RoutineRepositoryImpl
    ): RoutineRepository
}
