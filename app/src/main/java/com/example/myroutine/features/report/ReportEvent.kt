package com.example.myroutine.features.report

import java.time.LocalDate

sealed interface ReportEvent {
    data class SelectDate(val date: LocalDate) : ReportEvent
    data class SelectPeriod(val period: PeriodType) : ReportEvent
}