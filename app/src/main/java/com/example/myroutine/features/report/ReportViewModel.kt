package com.example.myroutine.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myroutine.common.DateProvider
import com.example.myroutine.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val dateProvider: DateProvider
) : ViewModel() {
    private val _state = MutableStateFlow(ReportState())
    val state = _state.asStateFlow()

    init {
        calculateReportData()
    }

    fun onEvent(event: ReportEvent) {
        when (event) {
            is ReportEvent.SelectDate -> {
                _state.update {
                    it.copy(
                        selectedDate = event.date
                    )
                }
                calculateReportData()
            }
            is ReportEvent.SelectPeriod -> {
                _state.update {
                    it.copy(
                        selectedPeriod = event.period
                    )
                }
                calculateReportData()
            }
        }
    }

    private fun calculateReportData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val selectedDate = _state.value.selectedDate
            val selectedPeriod = _state.value.selectedPeriod

            val startDate: LocalDate
            val endDate: LocalDate

            when (selectedPeriod) {
                PeriodType.WEEKLY -> {
                    startDate = selectedDate.with(java.time.DayOfWeek.MONDAY)
                    endDate = selectedDate.with(java.time.DayOfWeek.SUNDAY)
                }
                PeriodType.MONTHLY -> {
                    startDate = selectedDate.withDayOfMonth(1)
                    endDate = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
                }
            }

            val allRoutines = routineRepository.getRoutines()
            val routineChecks = routineRepository.getRoutineChecksForPeriod(startDate, endDate)

            val expectedRoutineCounts = mutableMapOf<Int, Int>()
            val completedRoutineCounts = mutableMapOf<Int, Int>()

            var totalExpectedRoutines = 0
            var totalCompletedRoutines = 0

            var currentDate = startDate
            val today = dateProvider.now() // LocalDate.now() 대신 dateProvider.now() 사용

            while (!currentDate.isAfter(endDate) && !currentDate.isAfter(today)) { // 현재 날짜 이후는 계산에서 제외
                val applicableRoutinesForDay = allRoutines.filter { routine ->
                    routineRepository.isRoutineApplicableForDate(routine, currentDate)
                }

                val checkedRoutinesForDay = routineChecks.filter { it.completeDate == currentDate }

                applicableRoutinesForDay.forEach { routine ->
                    expectedRoutineCounts[routine.id] = (expectedRoutineCounts[routine.id] ?: 0) + 1
                    totalExpectedRoutines++
                }

                checkedRoutinesForDay.forEach { check ->
                    completedRoutineCounts[check.routineId] = (completedRoutineCounts[check.routineId] ?: 0) + 1
                    totalCompletedRoutines++
                }
                currentDate = currentDate.plusDays(1)
            }

            val completionRate = if (totalExpectedRoutines > 0) {
                totalCompletedRoutines.toFloat() / totalExpectedRoutines
            } else {
                0f
            }

            val mostKeptRoutine = completedRoutineCounts.maxByOrNull { it.value }?.key?.let { routineId ->
                allRoutines.find { it.id == routineId }?.title
            } ?: "N/A"

            val mostKeptRoutineCount = completedRoutineCounts.maxByOrNull { it.value }?.value ?: 0

            val mostMissedRoutine = expectedRoutineCounts.mapValues { (routineId, expectedCount) ->
                expectedCount - (completedRoutineCounts[routineId] ?: 0)
            }.maxByOrNull { it.value }?.key?.let { routineId ->
                allRoutines.find { it.id == routineId }?.title
            } ?: "N/A"

            val mostMissedRoutineCount = expectedRoutineCounts.mapValues { (routineId, expectedCount) ->
                expectedCount - (completedRoutineCounts[routineId] ?: 0)
            }.maxByOrNull { it.value }?.value ?: 0

            _state.update {
                it.copy(
                    completionRate = completionRate,
                    mostKeptRoutine = mostKeptRoutine,
                    mostKeptRoutineCount = mostKeptRoutineCount,
                    mostMissedRoutine = mostMissedRoutine,
                    mostMissedRoutineCount = mostMissedRoutineCount,
                    isLoading = false
                )
            }
        }
    }
}