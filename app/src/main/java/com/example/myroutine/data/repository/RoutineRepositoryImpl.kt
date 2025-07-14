package com.example.myroutine.data.repository

import android.util.Log
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem
import java.time.LocalDate
import javax.inject.Inject

import com.example.myroutine.data.local.entity.HolidayType

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao,
    private val checkDao: RoutineCheckDao,
    private val holidayRepository: HolidayRepository
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
        val allRoutines = routineDao.getAll()
        val checks = checkDao.getChecksForDate(today).associateBy { it.routineId }

        Log.d(TAG, "Filtering routines for $today")

        return allRoutines.filter { routine ->
            isRoutineApplicableForDate(routine, today)
        }.map { routine ->
            val isDone = checks.containsKey(routine.id)
            Log.d(TAG, "Routine id=${routine.id}, title=${routine.title} isDone=$isDone")
            routine.copy(isDone = isDone)
        }
    }

    override suspend fun getRoutineChecksForPeriod(startDate: LocalDate, endDate: LocalDate): List<RoutineCheck> {
        return checkDao.getChecksForPeriod(startDate, endDate)
    }

    override suspend fun getRoutineItemsForPeriod(startDate: LocalDate, endDate: LocalDate): List<RoutineItem> {
        val routines = mutableListOf<RoutineItem>()
        val addedRoutineIds = mutableSetOf<Int>()
        var currentDate = startDate

        // 먼저 반복 없는 루틴들(특정 날짜) 한번에 가져오기
        routines.addAll(routineDao.getNonRepeatingRoutinesInPeriod(startDate, endDate).also {
            addedRoutineIds.addAll(it.map { r -> r.id })
        })

        // 기간 내 날짜별로 반복되는 루틴 쿼리 호출
        while (!currentDate.isAfter(endDate)) {
            // WEEKLY
            routineDao.getWeeklyRoutinesByDate(currentDate).forEach { routine ->
                if (addedRoutineIds.add(routine.id)) {
                    routines.add(routine)
                }
            }
            // EVERY_X_DAYS
            routineDao.getEveryXDaysRoutinesByDate(currentDate).forEach { routine ->
                if (addedRoutineIds.add(routine.id)) {
                    routines.add(routine)
                }
            }
            // WEEKDAY_HOLIDAY
            routineDao.getWeekdayHolidayRoutinesByDate(currentDate).forEach { routine ->
                if (addedRoutineIds.add(routine.id)) {
                    routines.add(routine)
                }
            }
            currentDate = currentDate.plusDays(1)
        }
        return routines
    }

    override suspend fun isRoutineApplicableForDate(routine: RoutineItem, date: LocalDate): Boolean {
        return when (routine.repeatType) {
            RepeatType.NONE -> routine.specificDate == date
            RepeatType.ONCE -> routine.specificDate == date
            RepeatType.WEEKLY -> {
                routine.repeatDays?.contains(date.dayOfWeek.value) == true &&
                (routine.startDate == null || !date.isBefore(routine.startDate))
            }
            RepeatType.EVERY_X_DAYS -> {
                routine.startDate?.let { startDate ->
                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, date).toInt()
                    routine.repeatIntervalDays?.let { interval ->
                        daysBetween >= 0 && daysBetween % interval == 0
                    } ?: false
                } ?: false
            }
            RepeatType.WEEKDAY_HOLIDAY -> {
                val isHoliday = isHoliday(date)
                (routine.holidayType == HolidayType.WEEKDAY && !isHoliday) || (routine.holidayType == HolidayType.HOLIDAY && isHoliday)
            }
        }
    }

    private suspend fun isHoliday(date: LocalDate): Boolean {
        val holidayResponse = holidayRepository.getHolidayInfo(date.year, date.monthValue)
        return holidayResponse.body.items.item?.any {
            it.locdate == date.year * 10000 + date.monthValue * 100 + date.dayOfMonth && it.holidayFlag == "Y"
        } ?: false
    }
}