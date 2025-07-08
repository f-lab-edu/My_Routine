package com.example.myroutine

import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepositoryImpl
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RoutineFilteringTest {

    private lateinit var repository: RoutineRepositoryImpl

    @Before
    fun setup() {
        // Mocking the DAOs is not strictly necessary for this test, as we are only testing
        // the `isRoutineApplicableForDate` method, which does not interact with DAOs.
        // However, if the method were to be moved into a separate utility class, then
        // this setup would be simpler.
        repository = RoutineRepositoryImpl(mockk(), mockk())
    }

    @Test
    fun `isRoutineApplicableForDate should correctly filter for NONE repeat type`() {
        val today = LocalDate.of(2024, 7, 7)
        val routine = RoutineItem.mock("Single Day Routine", specificDate = today, repeatType = RepeatType.NONE)

        assertTrue(repository.isRoutineApplicableForDate(routine, today))
        assertFalse(repository.isRoutineApplicableForDate(routine, today.plusDays(1)))
    }

    @Test
    fun `isRoutineApplicableForDate should correctly filter for ONCE repeat type`() {
        val today = LocalDate.of(2024, 7, 7)
        val routine = RoutineItem.mock("Once Routine", specificDate = today, repeatType = RepeatType.ONCE)

        assertTrue(repository.isRoutineApplicableForDate(routine, today))
        assertFalse(repository.isRoutineApplicableForDate(routine, today.plusDays(1)))
    }

    @Test
    fun `isRoutineApplicableForDate should correctly filter for WEEKLY repeat type`() {
        val monday = LocalDate.of(2024, 7, 8) // Monday
        val tuesday = LocalDate.of(2024, 7, 9) // Tuesday
        val routine = RoutineItem.mock("Weekly Routine", repeatDays = listOf(DayOfWeek.MONDAY.value, DayOfWeek.WEDNESDAY.value), repeatType = RepeatType.WEEKLY)

        assertTrue(repository.isRoutineApplicableForDate(routine, monday))
        assertFalse(repository.isRoutineApplicableForDate(routine, tuesday))
    }

    @Test
    fun `isRoutineApplicableForDate should correctly filter for EVERY_X_DAYS repeat type`() {
        val startDate = LocalDate.of(2024, 7, 1)
        val routine = RoutineItem.mock("Every 3 Days Routine", startDate = startDate, repeatIntervalDays = 3, repeatType = RepeatType.EVERY_X_DAYS)

        assertTrue(repository.isRoutineApplicableForDate(routine, LocalDate.of(2024, 7, 1))) // Day 0
        assertFalse(repository.isRoutineApplicableForDate(routine, LocalDate.of(2024, 7, 2))) // Day 1
        assertFalse(repository.isRoutineApplicableForDate(routine, LocalDate.of(2024, 7, 3))) // Day 2
        assertTrue(repository.isRoutineApplicableForDate(routine, LocalDate.of(2024, 7, 4))) // Day 3
        assertTrue(repository.isRoutineApplicableForDate(routine, LocalDate.of(2024, 7, 7))) // Day 6
    }

    @Test
    fun `isRoutineApplicableForDate should correctly filter for WEEKDAY_HOLIDAY repeat type`() {
        val monday = LocalDate.of(2024, 7, 8) // Monday
        val saturday = LocalDate.of(2024, 7, 6) // Saturday
        val sunday = LocalDate.of(2024, 7, 7) // Sunday
        val routine = RoutineItem.mock("Weekday Routine", repeatType = RepeatType.WEEKDAY_HOLIDAY)

        assertTrue(repository.isRoutineApplicableForDate(routine, monday))
        assertFalse(repository.isRoutineApplicableForDate(routine, saturday))
        assertFalse(repository.isRoutineApplicableForDate(routine, sunday))
    }

    @Test
    fun `isRoutineApplicableForDate should not apply weekly routine before its start date`() {
        val startDate = LocalDate.of(2024, 7, 8) // Monday
        val previousMonday = LocalDate.of(2024, 7, 1) // Previous Monday
        val routine = RoutineItem.mock(
            "Weekly Routine with Start Date",
            repeatDays = listOf(DayOfWeek.MONDAY.value),
            repeatType = RepeatType.WEEKLY,
            startDate = startDate
        )

        // Should be applicable on or after startDate
        assertTrue(repository.isRoutineApplicableForDate(routine, startDate))
        assertTrue(repository.isRoutineApplicableForDate(routine, startDate.plusWeeks(1)))

        // Should NOT be applicable before startDate, even if it's the correct day of week
        assertFalse(repository.isRoutineApplicableForDate(routine, previousMonday))
    }
}