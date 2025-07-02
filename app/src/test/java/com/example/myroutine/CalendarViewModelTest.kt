package com.example.myroutine

import com.example.myroutine.features.calendar.CalendarViewModel
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

    /**
     * Test case for initial state of CalendarViewModel.
     * Verifies that the initial current month and selected date are set to the current month and today's date respectively.
     */
    @Test
    fun `initial state should be current month and today selected`() = runTest {
        // Given: ViewModel is initialized
        val initialMonth = YearMonth.now()
        val initialDate = LocalDate.now()

        // When: No specific action is performed

        // Then: currentMonth and selectedDate should match the initial values
        assertEquals(initialMonth, viewModel.currentMonth.value)
        assertEquals(initialDate, viewModel.selectedDate.value)
    }

    /**
     * Test case for `goToPreviousMonth` function.
     * Verifies that calling `goToPreviousMonth` updates the current month to the previous month
     * and sets the selected date to the 1st day of that previous month.
     */
    @Test
    fun `goToPreviousMonth should update month and select first day`() = runTest {
        // Given: Current month state
        val initialMonth = YearMonth.now()

        // When: goToPreviousMonth function is called
        viewModel.goToPreviousMonth()
        advanceUntilIdle() // Wait for coroutines to complete

        // Then: Month should be changed to the previous month, and selected date should be the 1st day of that month
        assertEquals(initialMonth.minusMonths(1), viewModel.currentMonth.value)
        assertEquals(initialMonth.minusMonths(1).atDay(1), viewModel.selectedDate.value)
    }

    /**
     * Test case for `goToNextMonth` function.
     * Verifies that calling `goToNextMonth` updates the current month to the next month
     * and sets the selected date to the 1st day of that next month.
     */
    @Test
    fun `goToNextMonth should update month and select first day`() = runTest {
        // Given: Current month state
        val initialMonth = YearMonth.now()

        // When: goToNextMonth function is called
        viewModel.goToNextMonth()
        advanceUntilIdle() // Wait for coroutines to complete

        // Then: Month should be changed to the next month, and selected date should be the 1st day of that month
        assertEquals(initialMonth.plusMonths(1), viewModel.currentMonth.value)
        assertEquals(initialMonth.plusMonths(1).atDay(1), viewModel.selectedDate.value)
    }

    /**
     * Test case for `goToToday` function.
     * Verifies that calling `goToToday` resets the current month and selected date to the current month and today's date.
     */
    @Test
    fun `goToToday should reset to current month and today`() = runTest {
        // Given: ViewModel's month and date are in a different state than current
        viewModel.goToNextMonth() // Move to next month
        viewModel.selectDay(LocalDate.now().plusDays(5)) // Select a different day
        advanceUntilIdle()

        // When: goToToday function is called
        viewModel.goToToday()
        advanceUntilIdle() // Wait for coroutines to complete

        // Then: Month and date should be reset to current month and today's date
        assertEquals(YearMonth.now(), viewModel.currentMonth.value)
        assertEquals(LocalDate.now(), viewModel.selectedDate.value)
    }

    /**
     * Test case for `selectDay` function.
     * Verifies that calling `selectDay` updates the selected date.
     */
    @Test
    fun `selectDay should update selected date`() = runTest {
        // Given: A new date to select
        val newDate = LocalDate.now().plusDays(1)

        // When: selectDay function is called
        viewModel.selectDay(newDate)
        advanceUntilIdle() // Wait for coroutines to complete

        // Then: The selected date should be updated to the new date
        assertEquals(newDate, viewModel.selectedDate.value)
    }

    /**
     * Test case for `calendarDays` generation for the current month.
     * Verifies that `calendarDays` flow generates the correct list of days for the current month,
     * including empty days at the beginning and end to ensure a fixed 6-week grid.
     */
    @Test
    fun `calendarDays should generate correct days for current month`() = runTest {
        // Given: Current month and selected date
        val currentMonth = YearMonth.now()
        val selectedDate = LocalDate.now()
        viewModel.goToToday() // Set ViewModel state to current
        advanceUntilIdle()

        // When: Get the value from calendarDays Flow
        val days = viewModel.calendarDays.value
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        // Convert DayOfWeek value to 0-indexed (Sunday=0, Monday=1, ..., Saturday=6)
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7

        // Then:
        // 1. Verify empty days before the start of the month
        for (i in 0 until firstDayOfWeekValue) {
            assertEquals(null, days[i].date)
        }

        // 2. Verify actual days of the month
        for (i in 0 until daysInMonth) {
            val expectedDate = currentMonth.atDay(i + 1)
            assertEquals(expectedDate, days[firstDayOfWeekValue + i].date)
            assertEquals(expectedDate == selectedDate, days[firstDayOfWeekValue + i].isSelected)
            assertEquals(expectedDate.dayOfWeek == DayOfWeek.SATURDAY || expectedDate.dayOfWeek == DayOfWeek.SUNDAY, days[firstDayOfWeekValue + i].isWeekend)
            assertEquals(false, days[firstDayOfWeekValue + i].isHoliday) // Holidays are not mocked yet
        }

        // 3. Verify total number of days is 42 (fixed 6 weeks)
        assertEquals(42, days.size)
    }

    /**
     * Test case for `calendarDays` correctly identifying weekends.
     * Verifies that the `isWeekend` property of `CalendarDay` is correctly set for Saturdays and Sundays.
     */
    @Test
    fun `calendarDays should correctly identify weekends`() = runTest {
        // Given: A specific month (July 2024) is set
        val month = YearMonth.of(2024, 7) // July 2024
        viewModel._currentMonth.value = month
        viewModel._selectedDate.value = month.atDay(1) // Select July 1st
        advanceUntilIdle()

        // When: Get the value from calendarDays Flow
        val days = viewModel.calendarDays.value

        // Then:
        // 1. Verify that July 6, 2024 (Saturday) is identified as a weekend
        val saturday = month.atDay(6)
        val saturdayCalendarDay = days.first { it.date == saturday }
        assertEquals(true, saturdayCalendarDay.isWeekend)

        // 2. Verify that July 7, 2024 (Sunday) is identified as a weekend
        val sunday = month.atDay(7)
        val sundayCalendarDay = days.first { it.date == sunday }
        assertEquals(true, sundayCalendarDay.isWeekend)

        // 3. Verify that July 8, 2024 (Monday) is not identified as a weekend
        val monday = month.atDay(8)
        val mondayCalendarDay = days.first { it.date == monday }
        assertEquals(false, mondayCalendarDay.isWeekend)
    }
}