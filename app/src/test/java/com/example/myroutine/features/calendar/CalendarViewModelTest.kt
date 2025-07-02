package com.example.myroutine.features.calendar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@ExperimentalCoroutinesApi
class CalendarViewModelTest {

    private lateinit var viewModel: CalendarViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CalendarViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is current month and today selected`() = runTest {
        val initialMonth = YearMonth.now()
        val initialDate = LocalDate.now()

        assertEquals(initialMonth, viewModel.currentMonth.value)
        assertEquals(initialDate, viewModel.selectedDate.value)
    }

    @Test
    fun `goToPreviousMonth updates month and selects first day`() = runTest {
        val initialMonth = YearMonth.now()
        viewModel.goToPreviousMonth()
        advanceUntilIdle()

        assertEquals(initialMonth.minusMonths(1), viewModel.currentMonth.value)
        assertEquals(initialMonth.minusMonths(1).atDay(1), viewModel.selectedDate.value)
    }

    @Test
    fun `goToNextMonth updates month and selects first day`() = runTest {
        val initialMonth = YearMonth.now()
        viewModel.goToNextMonth()
        advanceUntilIdle()

        assertEquals(initialMonth.plusMonths(1), viewModel.currentMonth.value)
        assertEquals(initialMonth.plusMonths(1).atDay(1), viewModel.selectedDate.value)
    }

    @Test
    fun `goToToday resets to current month and today`() = runTest {
        // Move to a different month/day first
        viewModel.goToNextMonth()
        viewModel.selectDay(LocalDate.now().plusDays(5))
        advanceUntilIdle()

        viewModel.goToToday()
        advanceUntilIdle()

        assertEquals(YearMonth.now(), viewModel.currentMonth.value)
        assertEquals(LocalDate.now(), viewModel.selectedDate.value)
    }

    @Test
    fun `selectDay updates selected date`() = runTest {
        val newDate = LocalDate.now().plusDays(1)
        viewModel.selectDay(newDate)
        advanceUntilIdle()

        assertEquals(newDate, viewModel.selectedDate.value)
    }

    @Test
    fun `calendarDays generates correct days for current month`() = runTest {
        val currentMonth = YearMonth.now()
        val selectedDate = LocalDate.now()
        viewModel.goToToday()
        advanceUntilIdle()

        val days = viewModel.calendarDays.value
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7

        // Check empty days at the beginning
        for (i in 0 until firstDayOfWeekValue) {
            assertEquals(null, days[i].date)
        }

        // Check actual days of the month
        for (i in 0 until daysInMonth) {
            val expectedDate = currentMonth.atDay(i + 1)
            assertEquals(expectedDate, days[firstDayOfWeekValue + i].date)
            assertEquals(expectedDate == selectedDate, days[firstDayOfWeekValue + i].isSelected)
            assertEquals(expectedDate.dayOfWeek == DayOfWeek.SATURDAY || expectedDate.dayOfWeek == DayOfWeek.SUNDAY, days[firstDayOfWeekValue + i].isWeekend)
            assertEquals(false, days[firstDayOfWeekValue + i].isHoliday) // No holidays mocked yet
        }

        // Check total size is 42
        assertEquals(42, days.size)
    }

    @Test
    fun `calendarDays correctly identifies weekends`() = runTest {
        val month = YearMonth.of(2024, 7) // July 2024
        viewModel._currentMonth.value = month
        viewModel._selectedDate.value = month.atDay(1)
        advanceUntilIdle()

        val days = viewModel.calendarDays.value

        // July 6, 2024 is a Saturday
        val saturday = month.atDay(6)
        val saturdayCalendarDay = days.first { it.date == saturday }
        assertEquals(true, saturdayCalendarDay.isWeekend)

        // July 7, 2024 is a Sunday
        val sunday = month.atDay(7)
        val sundayCalendarDay = days.first { it.date == sunday }
        assertEquals(true, sundayCalendarDay.isWeekend)

        // July 8, 2024 is a Monday
        val monday = month.atDay(8)
        val mondayCalendarDay = days.first { it.date == monday }
        assertEquals(false, mondayCalendarDay.isWeekend)
    }
}