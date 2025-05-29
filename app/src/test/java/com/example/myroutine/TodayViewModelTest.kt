package com.example.myroutine

import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import com.example.myroutine.features.today.TodayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class TodayViewModelTest {

    private val repository: RoutineRepository = mockk()
    private lateinit var viewModel: TodayViewModel

    @Before
    fun setup() {
        // 테스트 전 Dispatcher를 테스트용으로 설정합니다.
        // 실제 UI Dispatcher를 대체하여 코루틴의 흐름을 제어합니다.
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        // 테스트 후 Dispatcher를 원래 상태로 복원합니다.
        // 다른 테스트나 앱 실행에 영향을 주지 않도록 정리합니다.
        Dispatchers.resetMain()
    }

    /**
     * ViewModel 초기화 시 오늘 날짜의 루틴 목록을 정상적으로 불러오는지 테스트합니다.
     *
     * - 가짜 루틴 2개를 리턴하도록 repository를 설정합니다.
     * - ViewModel 생성 후 내부 상태(routines)가 예상대로 설정됐는지 확인합니다.
     */
    @Test
    fun `init loads today routines`() = runTest {
        // Given
        val today = LocalDate.now()
        val mockRoutines = listOf(
            RoutineItem.mock("Test1").copy(id = 1, isDone = false),
            RoutineItem.mock("Test2").copy(id = 2, isDone = true)
        )

        coEvery { repository.insertMockDataIfEmpty() } just Runs
        coEvery { repository.getTodayRoutines(today) } returns mockRoutines

        // When
        viewModel = TodayViewModel(repository)

        // Then
        advanceUntilIdle() // wait for coroutine
        val result = viewModel.routines.value
        assertEquals(2, result.size)
        assertTrue(result.any { it.title == "Test2" && it.isDone })
    }

    /**
     * 특정 루틴의 체크 상태 변경 시 ViewModel 내부 상태가 올바르게 반영되는지 테스트합니다.
     *
     * - 초기 루틴은 isDone = false 상태이며,
     * - 체크 처리 이후 isDone = true로 업데이트되어야 합니다.
     */
    @Test
    fun `onRoutineChecked updates routine isDone`() = runTest {
        val today = LocalDate.now()
        val initial = RoutineItem.mock("A").copy(id = 10, isDone = false)

        coEvery { repository.insertMockDataIfEmpty() } just Runs
        coEvery { repository.getTodayRoutines(today) } returns listOf(initial)
        coEvery { repository.setRoutineChecked(10, today, true) } just Runs

        viewModel = TodayViewModel(repository)
        advanceUntilIdle()

        viewModel.onRoutineChecked(10, true)
        advanceUntilIdle()

        val result = viewModel.routines.value
        assertTrue(result.find { it.id == 10 }?.isDone == true)
    }
}
