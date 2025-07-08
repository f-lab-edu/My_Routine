package com.example.myroutine.features.report

import com.example.myroutine.common.DateProvider
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * ReportViewModel 테스트 클래스
 * Mockk로 Repository를 목킹하고 kotlinx-coroutines-test로 코루틴 테스트 수행
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {

    private lateinit var viewModel: ReportViewModel
    private val routineRepository: RoutineRepository = mockk()
    private val dateProvider: DateProvider = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val routine1 = RoutineItem.mock(id = 1, title = "Routine A")
        val routine2 = RoutineItem.mock(id = 2, title = "Routine B")
        val routine3 = RoutineItem.mock(id = 3, title = "Routine C")

        coEvery { routineRepository.getRoutines() } returns listOf(routine1, routine2, routine3)
        coEvery { routineRepository.isRoutineApplicableForDate(any(), any()) } returns true
        coEvery { dateProvider.now() } returns LocalDate.of(2024, 7, 7)

        coEvery { routineRepository.getRoutineChecksForPeriod(LocalDate.of(2025, 7, 7), LocalDate.of(2025, 7, 13)) } returns listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2025, 7, 7)),
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2025, 7, 8)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2025, 7, 7))
        )

        viewModel = ReportViewModel(routineRepository, dateProvider)
    }

    @Test
    fun `calculateReportData should correctly calculate completion rate`() = runTest {
        val checks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 2)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 1))
        )
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns checks

        viewModel.onEvent(ReportEvent.SelectDate(LocalDate.of(2024,7,7)))  // 선택 날짜 이벤트로 재계산 유도
        viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.WEEKLY))

        advanceUntilIdle()

        val state = viewModel.state.first()

        println("=== Test: Completion Rate ===")
        println("Expected Total Expected Routines: 21 (3 routines * 7 days)")
        println("Expected Total Completed Routines: ${checks.size}")
        println("Calculated completionRate: ${state.completionRate}")
        println("Most kept routine: ${state.mostKeptRoutine} (${state.mostKeptRoutineCount})")
        println("Most missed routine: ${state.mostMissedRoutine} (${state.mostMissedRoutineCount})")

        assertEquals(1f / 7f, state.completionRate, 0.001f)
    }

    @Test
    fun `calculateReportData should correctly identify most kept routine`() = runTest {
        val checks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 2)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 1))
        )
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns checks

        viewModel.onEvent(ReportEvent.SelectDate(LocalDate.of(2024,7,7)))
        viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.WEEKLY))

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals("Routine A", state.mostKeptRoutine)
        assertEquals(2, state.mostKeptRoutineCount)
    }

    @Test
    fun `calculateReportData should correctly identify most missed routine`() = runTest {
        val checks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 2)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 1))
        )
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns checks

        viewModel.onEvent(ReportEvent.SelectDate(LocalDate.of(2024,7,7)))
        viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.WEEKLY))

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals("Routine C", state.mostMissedRoutine)
        assertEquals(7, state.mostMissedRoutineCount)
    }

    @Test
    fun `calculateReportData should exclude future dates from calculation`() = runTest {
        val testToday = LocalDate.of(2024, 6, 15)
        coEvery { dateProvider.now() } returns testToday

        val routine1 = RoutineItem.mock(id = 1, title = "Routine A")
        val routine2 = RoutineItem.mock(id = 2, title = "Routine B")

        val checks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 6, 15))
        )

        coEvery { routineRepository.getRoutines() } returns listOf(routine1, routine2)
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns checks
        coEvery { routineRepository.isRoutineApplicableForDate(any(), any()) } returns true

        viewModel = ReportViewModel(routineRepository, dateProvider)
        viewModel.onEvent(ReportEvent.SelectDate(LocalDate.of(2024, 6, 10)))
        viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.WEEKLY))

        advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(1f / 12f, state.completionRate, 0.001f)
    }

    @Test
    fun `onEvent SelectPeriod should trigger data recalculation`() = runTest {
        coEvery { dateProvider.now() } returns LocalDate.of(2024, 7, 15)

        val monthlyChecks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 15))
        )
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns monthlyChecks

        viewModel.onEvent(ReportEvent.SelectDate(LocalDate.of(2024, 7, 15))) // 추가: 날짜 선택 이벤트 먼저
        viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.MONTHLY))
        advanceUntilIdle()

        val state = viewModel.state.first()
        assertEquals(2f / 45f, state.completionRate, 0.001f)
    }
}
