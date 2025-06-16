package com.example.myroutine.data.repository

import com.example.myroutine.data.local.entity.RoutineItem
import java.time.LocalDate

interface RoutineRepository {
    suspend fun insertRoutine(routine: RoutineItem)
    suspend fun getRoutines(): List<RoutineItem>
    suspend fun insertMockDataIfEmpty()
    suspend fun getTodayRoutines(today: LocalDate): List<RoutineItem>
    suspend fun setRoutineChecked(routineId: Int, date: LocalDate, isChecked: Boolean)
}


