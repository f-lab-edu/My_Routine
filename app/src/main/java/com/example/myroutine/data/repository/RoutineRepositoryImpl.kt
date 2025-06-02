package com.example.myroutine.data.repository

import android.util.Log
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem
import java.time.LocalDate
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val checkDao: RoutineCheckDao
) : RoutineRepository {

    private val TAG = "RoutineRepository"

    override suspend fun getRoutines(): List<RoutineItem> {
        val routines = routineDao.getAll()
        Log.d(TAG, "Fetched ${routines.size} routines from DB")
        return routines
    }

    override suspend fun setRoutineChecked(routineId: Int, date: LocalDate, isChecked: Boolean) {
        if (isChecked) {
            Log.d(TAG, "Setting routineId=$routineId as CHECKED for date=$date")
            checkDao.insertCheck(RoutineCheck(routineId = routineId, completeDate = date))
        } else {
            Log.d(TAG, "Setting routineId=$routineId as UNCHECKED for date=$date")
            checkDao.deleteCheck(routineId, date)
        }
    }

    override suspend fun insertMockDataIfEmpty() {
        val existing = routineDao.getAll()
        if (existing.isEmpty()) {
            val mockData = listOf(
                RoutineItem.mock("물 마시기"),
                RoutineItem.mock("운동하기"),
                RoutineItem.mock("책 읽기")
            )
            Log.d(TAG, "Inserting mock data: ${mockData.map { it.title }}")
            routineDao.insertAll(mockData)
        } else {
            Log.d(TAG, "Skipping mock data insertion; ${existing.size} routines already exist")
        }
    }

    override suspend fun getTodayRoutines(today: LocalDate): List<RoutineItem> {
        val routines = routineDao.getAll()
        val checks = checkDao.getChecksForDate(today).associateBy { it.routineId }

        Log.d(TAG, "Checking completion status for ${routines.size} routines on $today")

        return routines.map { routine ->
            val isDone = checks.containsKey(routine.id)
            Log.d(TAG, "Routine id=${routine.id}, title=${routine.title} isDone=$isDone")
            routine.copy(isDone = isDone)
        }
    }
}