package com.example.myroutine.data.alarm

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmSchedulerModule {

    @Binds
    abstract fun bindAlarmScheduler(
        impl: AlarmSchedulerImpl
    ): AlarmScheduler
}
