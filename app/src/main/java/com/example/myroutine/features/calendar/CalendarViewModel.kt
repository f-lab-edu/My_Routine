package com.example.myroutine.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.myroutine.data.repository.RoutineRepository
import com.example.myroutine.data.local.entity.RoutineItem
import kotlinx.coroutines.launch
import com.example.myroutine.data.repository.HolidayRepository

data class CalendarDay(
    val date: LocalDate?,
    val isSelected: Boolean,
    val isWeekend: Boolean,
    val isHoliday: Boolean
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    internal val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    internal val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _holidays = MutableStateFlow<Set<LocalDate>>(emptySet())
    val holidays: StateFlow<Set<LocalDate>> = _holidays.asStateFlow()

    val calendarDays = combine(
        _currentMonth,
        _selectedDate,
        _holidays
    ) { month, selected, holidays ->
        generateCalendarDays(month, selected, holidays)
    }

    private val _routinesForSelectedDate = MutableStateFlow<List<RoutineItem>>(emptyList())
    val routinesForSelectedDate: StateFlow<List<RoutineItem>> =
        _routinesForSelectedDate.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedDate.collect { date ->
                _routinesForSelectedDate.value = routineRepository.getTodayRoutines(date)
            }
        }
        viewModelScope.launch {
            _currentMonth.collect { yearMonth ->
                fetchHolidays(yearMonth.year, yearMonth.monthValue)
            }
        }
    }

    private suspend fun fetchHolidays(year: Int, month: Int) {
        try {
            val holidayResponse = holidayRepository.getHolidayInfo(year, month)
            val holidayDates = holidayResponse.body.items.item
                ?.filter { it.holidayFlag == "Y" }
                ?.map {
                    LocalDate.of(
                        it.locdate / 10000,
                        (it.locdate % 10000) / 100,
                        it.locdate % 100
                    )
                }
                ?.toSet() ?: emptySet()
            _holidays.value = holidayDates
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
        _selectedDate.value = _currentMonth.value.atDay(1)
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
        _selectedDate.value = _currentMonth.value.atDay(1)
    }

    fun goToToday() {
        _currentMonth.value = YearMonth.now()
        _selectedDate.value = LocalDate.now()
    }

    fun selectDay(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onRoutineChecked(routineId: Int, isChecked: Boolean) {
        viewModelScope.launch {
            routineRepository.setRoutineChecked(routineId, _selectedDate.value, isChecked)
            _routinesForSelectedDate.value = routineRepository.getTodayRoutines(_selectedDate.value)
        }
    }

    fun setCurrentMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
    }

    private fun generateCalendarDays(
        yearMonth: YearMonth,
        selectedDate: LocalDate,
        holidays: Set<LocalDate>
    ): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val firstDayOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()

        // Calculate offset for Sunday start (Sunday=0, Monday=1, ..., Saturday=6)
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7

        // Add empty days for the beginning of the week (to align with Sunday start)
        for (i in 0 until firstDayOfWeekValue) {
            days.add(CalendarDay(null, false, false, false))
        }

        // Add actual days of the month
        for (i in 1..daysInMonth) {
            val date = yearMonth.atDay(i)
            val isSelected = date == selectedDate
            val isWeekend =
                date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
            val isHoliday = holidays.contains(date) // Placeholder for holiday check
            days.add(CalendarDay(date, isSelected, isWeekend, isHoliday))
        }

        // Pad with empty days at the end to ensure 6 full weeks (42 days total)
        while (days.size < 42) {
            days.add(CalendarDay(null, false, false, false))
        }

        return days
    }
}