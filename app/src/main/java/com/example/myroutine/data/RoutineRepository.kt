package com.example.myroutine.data

import java.time.LocalDate

interface RoutineRepository {
    suspend fun getRoutines(): List<RoutineItem>
    suspend fun insertMockDataIfEmpty()
    suspend fun getTodayRoutines(today: LocalDate): List<RoutineItem>
    suspend fun setRoutineChecked(routineId: Int, date: LocalDate, isChecked: Boolean)
}


