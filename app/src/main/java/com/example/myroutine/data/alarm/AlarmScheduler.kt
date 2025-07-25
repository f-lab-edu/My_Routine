package com.example.myroutine.data.alarm

import com.example.myroutine.data.local.entity.RoutineItem

interface AlarmScheduler {
    suspend fun schedule(routine: RoutineItem)
    fun cancel(routineId: Int)
    suspend fun calculateNextAlarmTime(routine: RoutineItem): Long?
}