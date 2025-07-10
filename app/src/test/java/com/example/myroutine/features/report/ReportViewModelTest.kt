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

    // 공통 루틴 데이터 (테스트에서 재사용)
    private val routine1 = RoutineItem.mock(id = 1, title = "Routine A")
    private val routine2 = RoutineItem.mock(id = 2, title = "Routine B")
    private val routine3 = RoutineItem.mock(id = 3, title = "Routine C")

    @Before
    fun setup() {
        // Main dispatcher를 테스트용 디스패처로 설정
        Dispatchers.setMain(testDispatcher)

        // 기본 루틴 리스트 반환 세팅 (변경 없으면 공통 사용)
        coEvery { routineRepository.getRoutines() } returns listOf(routine1, routine2, routine3)

        // isRoutineApplicableForDate 기본 true 반환 (필요시 개별 테스트에서 수정 가능)
        coEvery { routineRepository.isRoutineApplicableForDate(any(), any()) } returns true

        // dateProvider.now() 기본값 세팅
        coEvery { dateProvider.now() } returns LocalDate.of(2024, 7, 7)

        // 뷰모델 초기화
        viewModel = ReportViewModel(routineRepository, dateProvider)
    }

    /**
     * 테스트용 RoutineCheck 목록 목킹 헬퍼 함수
     */
    private fun mockRoutineChecks(checks: List<RoutineCheck>) {
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns checks
    }

    /**
     * 날짜와 기간을 선택하고, 이벤트를 전달하는 헬퍼 함수
     */
    private suspend fun selectDateAndPeriod(date: LocalDate, period: PeriodType) {
        viewModel.onEvent(ReportEvent.SelectDate(date))
        viewModel.onEvent(ReportEvent.SelectPeriod(period))
    }

    @Test
    fun calculateReportData_should_correctly_calculate_completion_rate() = runTest {
        // 완료 체크 데이터를 목킹
        mockRoutineChecks(
            listOf(
                RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
                RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 2)),
                RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 1))
            )
        )
        // 날짜와 기간 선택 이벤트 발생
        selectDateAndPeriod(LocalDate.of(2024,7,7), PeriodType.WEEKLY)
        // 모든 코루틴 작업 완료 대기
        advanceUntilIdle()

        val state = viewModel.state.first()
        // 예상 완료율 검증
        assertEquals(1f / 7f, state.completionRate, 0.001f)
    }

    @Test
    fun calculateReportData_should_correctly_identify_most_kept_routine() = runTest {
        mockRoutineChecks(
            listOf(
                RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
                RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 2)),
                RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 1))
            )
        )
        selectDateAndPeriod(LocalDate.of(2024,7,7), PeriodType.WEEKLY)
        advanceUntilIdle()

        val state = viewModel.state.first()
        // 가장 많이 수행한 루틴 이름과 횟수 검증
        assertEquals("Routine A", state.mostKeptRoutine)
        assertEquals(2, state.mostKeptRoutineCount)
    }

    @Test
    fun calculateReportData_should_correctly_identify_most_missed_routine() = runTest {
        mockRoutineChecks(
            listOf(
                RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
                RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 2)),
                RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 1))
            )
        )
        selectDateAndPeriod(LocalDate.of(2024,7,7), PeriodType.WEEKLY)
        advanceUntilIdle()

        val state = viewModel.state.first()
        // 가장 많이 놓친 루틴 이름과 횟수 검증
        assertEquals("Routine C", state.mostMissedRoutine)
        assertEquals(7, state.mostMissedRoutineCount)
    }

    @Test
    fun calculateReportData_should_exclude_future_dates_from_calculation() = runTest {
        // 현재 날짜를 테스트용으로 변경
        val testToday = LocalDate.of(2024, 6, 15)
        coEvery { dateProvider.now() } returns testToday

        val routineA = RoutineItem.mock(id = 1, title = "Routine A")
        val routineB = RoutineItem.mock(id = 2, title = "Routine B")

        val checks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 6, 15))
        )

        // 테스트용 루틴과 체크 데이터 목킹
        coEvery { routineRepository.getRoutines() } returns listOf(routineA, routineB)
        coEvery { routineRepository.getRoutineChecksForPeriod(any(), any()) } returns checks
        coEvery { routineRepository.isRoutineApplicableForDate(any(), any()) } returns true

        viewModel = ReportViewModel(routineRepository, dateProvider)
        selectDateAndPeriod(LocalDate.of(2024, 6, 10), PeriodType.WEEKLY)
        advanceUntilIdle()

        val state = viewModel.state.first()
        // 미래 날짜 제외 후 완료율 검증
        assertEquals(1f / 12f, state.completionRate, 0.001f)
    }

    @Test
    fun onEvent_SelectPeriod_should_trigger_data_recalculation() = runTest {
        coEvery { dateProvider.now() } returns LocalDate.of(2024, 7, 15)

        val monthlyChecks = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 15))
        )
        mockRoutineChecks(monthlyChecks)

        selectDateAndPeriod(LocalDate.of(2024, 7, 15), PeriodType.MONTHLY)
        advanceUntilIdle()

        val state = viewModel.state.first()
        // 월별 완료율 검증
        assertEquals(2f / 45f, state.completionRate, 0.001f)
    }
}
