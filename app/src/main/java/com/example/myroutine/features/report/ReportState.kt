package com.example.myroutine.features.report

import java.time.LocalDate

enum class PeriodType {
    WEEKLY,
    MONTHLY
}

data class ReportState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedPeriod: PeriodType = PeriodType.WEEKLY,
    val completionRate: Float = 0f,
    val mostKeptRoutine: String = "",
    val mostKeptRoutineCount: Int = 0,
    val mostMissedRoutine: String = "",
    val mostMissedRoutineCount: Int = 0,
    val isLoading: Boolean = false
)