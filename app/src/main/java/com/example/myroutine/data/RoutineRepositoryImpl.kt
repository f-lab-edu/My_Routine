package com.example.myroutine.data

import java.time.LocalDate
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val checkDao: RoutineCheckDao
) : RoutineRepository {
    override suspend fun getRoutines(): List<RoutineItem> = routineDao.getAll()

    override suspend fun setRoutineChecked(routineId: Int, date: LocalDate, isChecked: Boolean) {
        if (isChecked) {
            checkDao.insertCheck(RoutineCheck(routineId = routineId, date = date))
        } else {
            checkDao.deleteCheck(routineId, date)
        }
    }

    override suspend fun insertMockDataIfEmpty() {
        if (routineDao.getAll().isEmpty()) {
            val mockData = listOf(
                RoutineItem.mock("물 마시기"),
                RoutineItem.mock("운동하기"),
                RoutineItem.mock("책 읽기")
            )
            routineDao.insertAll(mockData)
        }
    }

    override suspend fun getTodayRoutines(today: LocalDate): List<RoutineItem> {
        val routines = routineDao.getAll()
        val checks = routines.associateBy(
            keySelector = { it.id },
            valueTransform = { checkDao.getCheck(it.id, today) }
        )

        return routines.map { routine ->
            routine.copy(isDone = checks[routine.id] != null)
        }
    }
}