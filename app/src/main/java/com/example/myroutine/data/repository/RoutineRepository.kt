package com.example.myroutine.data.repository

import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem
import java.time.LocalDate

interface RoutineRepository {
    suspend fun insertRoutine(routine: RoutineItem)
    suspend fun getRoutines(): List<RoutineItem>
    suspend fun insertMockDataIfEmpty()
    suspend fun getTodayRoutines(today: LocalDate): List<RoutineItem>
    suspend fun setRoutineChecked(routineId: Int, date: LocalDate, isChecked: Boolean)
    suspend fun getRoutineChecksForPeriod(startDate: LocalDate, endDate: LocalDate): List<RoutineCheck>
    suspend fun getRoutineItemsForPeriod(startDate: LocalDate, endDate: LocalDate): List<RoutineItem>
    suspend fun isRoutineApplicableForDate(routine: RoutineItem, date: LocalDate): Boolean
}




