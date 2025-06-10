package com.example.myroutine.data.repository

import android.util.Log
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.entity.HolidayType
import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem
import java.time.LocalDate
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val checkDao: RoutineCheckDao
) : RoutineRepository {

    private val TAG = "RoutineRepository"

    override suspend fun insertRoutine(routine: RoutineItem) {
        routineDao.insert(routine)
    }

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

    override suspend fun getTodayRoutines(today: LocalDate): List<RoutineItem> {
        val routines = routineDao.getAll()
        val checks = checkDao.getChecksForDate(today).associateBy { it.routineId }
        val todayDayOfWeek = today.dayOfWeek.value // 월=1, ..., 일=7

        val filtered = routines.filter { routine ->
            when (routine.repeatType) {
                RepeatType.ONCE -> routine.specificDate == today

                RepeatType.WEEKLY -> routine.repeatDays?.contains(todayDayOfWeek) == true

                RepeatType.WEEKDAY_HOLIDAY -> {
                    routine.holidayType == HolidayType.WEEKDAY && todayDayOfWeek in 1..5
                    // TODO: 공휴일 데이터 기반 분리 필요
                }

                RepeatType.EVERY_X_DAYS -> {
                    val start = routine.startDate
                    val interval = routine.repeatIntervalDays
                    if (start != null && interval != null) {
                        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, today)
                        daysBetween >= 0 && daysBetween % interval == 0L
                    } else false
                }

                RepeatType.NONE -> false
            }
        }

        return filtered.map { routine ->
            val isDone = checks.containsKey(routine.id)
            routine.copy(isDone = isDone)
        }
    }

}