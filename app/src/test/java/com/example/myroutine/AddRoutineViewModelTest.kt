package com.example.myroutine

import com.example.myroutine.common.LogWrapper
import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import com.example.myroutine.features.add.AddRoutineViewModel
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddRoutineViewModelTest {

    private lateinit var viewModel: AddRoutineViewModel
    private val repository: RoutineRepository = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        LogWrapper.DEBUG = false
        Dispatchers.setMain(testDispatcher)
        viewModel = AddRoutineViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 제목이 비어 있을 경우 에러 콜백이 호출되어야 한다.
     */
    @Test
    fun shouldCallErrorCallback_whenTitleIsEmpty() {
        var errorCalledWith: Int? = null
        viewModel.onTitleChange("")
        viewModel.onTabIndexChange(0)
        viewModel.onSelectedDateChange(LocalDate.now())

        viewModel.saveRoutine(
            onSuccess = { fail("onSuccess should not be called") },
            onError = { errorCalledWith = it }
        )

        assertEquals(R.string.toast_title_required, errorCalledWith)
    }

    /**
     * 날짜 지정 탭에서 날짜가 선택되지 않았을 경우 에러 콜백이 호출되어야 한다.
     */
    @Test
    fun shouldCallErrorCallback_whenDateNotSelectedInDateTab() {
        var errorCalledWith: Int? = null
        viewModel.onTitleChange("Drink Water")
        viewModel.onTabIndexChange(0)
        viewModel.onSelectedDateChange(null)

        viewModel.saveRoutine(
            onSuccess = { fail("onSuccess should not be called") },
            onError = { errorCalledWith = it }
        )

        assertEquals(R.string.toast_date_required, errorCalledWith)
    }

    /**
     * 모든 입력이 정상적일 경우 루틴이 저장되고 onSuccess 콜백이 호출되어야 한다.
     */
    @Test
    fun shouldSaveRoutineAndCallOnSuccess_whenInputIsValid() = runTest {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onError = mockk<(Int) -> Unit>(relaxed = true)

        viewModel.onTitleChange("Exercise")
        viewModel.onTabIndexChange(0)
        viewModel.onSelectedDateChange(LocalDate.of(2025, 6, 17))
        viewModel.onAlarmToggle(true)
        viewModel.onAlarmTimeChange(LocalTime.of(7, 30))

        viewModel.saveRoutine(onSuccess = onSuccess, onError = onError)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertRoutine(any<RoutineItem>()) }
        verify(exactly = 1) { onSuccess() }
        verify(exactly = 0) { onError(any()) }
    }

    /**
     * 저장된 RoutineItem의 값이 기대한 값과 일치하는지 검증한다.
     */
    @Test
    fun shouldSaveCorrectRoutineItem_whenInputIsValid() = runTest {
        // given
        val slot = slot<RoutineItem>()
        val title = "운동하기"
        val date = LocalDate.of(2025, 6, 18)
        val time = LocalTime.of(6, 0)

        viewModel.onTitleChange(title)
        viewModel.onTabIndexChange(0)
        viewModel.onSelectedDateChange(date)
        viewModel.onAlarmToggle(true)
        viewModel.onAlarmTimeChange(time)

        // when
        viewModel.saveRoutine(
            onSuccess = {},
            onError = { fail("onError should not be called") }
        )
        testScheduler.advanceUntilIdle()

        // then
        coVerify { repository.insertRoutine(capture(slot)) }
        val saved = slot.captured

        assertEquals(title, saved.title)
        assertEquals(RepeatType.ONCE, saved.repeatType)
        assertEquals(date, saved.specificDate)
        assertEquals(time, saved.alarmTime)
        assertEquals(null, saved.repeatDays)
        assertEquals(null, saved.holidayType)
        assertEquals(null, saved.repeatIntervalDays)
        assertEquals(null, saved.startDate)
    }
}