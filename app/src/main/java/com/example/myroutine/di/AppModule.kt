package com.example.myroutine.di

import com.example.myroutine.common.DateProvider
import com.example.myroutine.common.DateProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDateProvider(impl: DateProviderImpl): DateProvider
}