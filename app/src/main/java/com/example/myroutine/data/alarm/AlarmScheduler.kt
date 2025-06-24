package com.example.myroutine.data.alarm

import com.example.myroutine.data.local.entity.RoutineItem

interface AlarmScheduler {
    fun schedule(routine: RoutineItem)
    fun cancel(routineId: Int)
    fun calculateNextAlarmTime(routine: RoutineItem): Long?
}