package com.example.myroutine

import com.example.myroutine.common.L
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
        L.DEBUG = false
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

    /**
     * 요일 반복 탭에서 선택된 요일이 없을 경우,
     * 에러 콜백이 호출되어야 함을 검증한다.
     *
     * Given: 제목이 "Test Routine"이고, 탭 인덱스는 1(요일 반복), 선택된 요일은 빈 리스트이며, 제외 공휴일 옵션은 false
     * When: saveRoutine() 호출 시
     * Then: onError 콜백이 R.string.toast_day_required 값을 인자로 호출되어야 한다.
     */
    @Test
    fun shouldCallErrorCallback_whenSelectedDaysEmptyInWeekTab() {
        var errorCalledWith: Int? = null
        viewModel.onTitleChange("Test Routine")
        viewModel.onTabIndexChange(1)
        viewModel.onSelectedDaysChange(emptyList())
        viewModel.onExcludeHolidayToggle(false)

        viewModel.saveRoutine(
            onSuccess = { fail("onSuccess should not be called") },
            onError = { errorCalledWith = it }
        )

        assertEquals(R.string.toast_day_required, errorCalledWith)
    }

    /**
     * 요일 반복 탭에서 선택된 요일이 있을 경우,
     * 정상적으로 루틴이 저장되고 성공 콜백이 호출되는지 검증한다.
     *
     * Given: 제목이 "Weekly Routine", 탭 인덱스는 1, 선택된 요일은 월,수,금(1,3,5), 제외 공휴일 옵션은 false, 알람 설정이 활성화된 상태
     * When: saveRoutine() 호출 시
     * Then: repository.insertRoutine()가 호출되고, onSuccess 콜백이 호출되며, onError는 호출되지 않는다.
     *       저장된 RoutineItem은 입력한 값들과 일치해야 한다.
     */
    @Test
    fun shouldSaveRoutineAndCallOnSuccess_whenSelectedDaysValidInWeekTab() = runTest {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onError = mockk<(Int) -> Unit>(relaxed = true)

        val days = listOf(1, 3, 5) // 월, 수, 금
        viewModel.onTitleChange("Weekly Routine")
        viewModel.onTabIndexChange(1)
        viewModel.onSelectedDaysChange(days)
        viewModel.onExcludeHolidayToggle(false)
        viewModel.onAlarmToggle(true)
        viewModel.onAlarmTimeChange(LocalTime.of(8, 0))

        viewModel.saveRoutine(onSuccess = onSuccess, onError = onError)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertRoutine(any<RoutineItem>()) }
        verify(exactly = 1) { onSuccess() }
        verify(exactly = 0) { onError(any()) }

        // 저장되는 RoutineItem 검증
        val slot = slot<RoutineItem>()
        coVerify { repository.insertRoutine(capture(slot)) }
        val saved = slot.captured

        assertEquals("Weekly Routine", saved.title)
        assertEquals(RepeatType.WEEKLY, saved.repeatType)
        assertEquals(null, saved.specificDate)
        assertEquals(days, saved.repeatDays)
        assertEquals(null, saved.holidayType)
        assertEquals(null, saved.repeatIntervalDays)
        assertEquals(null, saved.startDate)
        assertEquals(true, saved.alarmTime != null)
    }

    /**
     * Given: 제목이 "Interval Routine", 탭 인덱스가 2(특정 일수 반복), 반복 간격 텍스트가 빈 문자열
     * When: saveRoutine() 호출 시
     * Then: onError 콜백이 R.string.toast_repeat_required 호출되어야 한다.
     */
    @Test
    fun shouldCallErrorCallback_whenRepeatIntervalIsBlank() {
        var errorCalledWith: Int? = null
        viewModel.onTitleChange("Interval Routine")
        viewModel.onTabIndexChange(2)
        viewModel.onRepeatIntervalChange("")

        viewModel.saveRoutine(
            onSuccess = { fail("onSuccess should not be called") },
            onError = { errorCalledWith = it }
        )

        assertEquals(R.string.toast_repeat_required, errorCalledWith)
    }

    /**
     * Given: 제목이 "Interval Routine", 탭 인덱스가 2, 반복 간격 텍스트가 숫자가 아닌 문자열("abc")
     * When: saveRoutine() 호출 시
     * Then: onError 콜백이 R.string.toast_repeat_required 호출되어야 한다.
     */
    @Test
    fun shouldCallErrorCallback_whenRepeatIntervalIsInvalid() {
        var errorCalledWith: Int? = null
        viewModel.onTitleChange("Interval Routine")
        viewModel.onTabIndexChange(2)
        viewModel.onRepeatIntervalChange("abc")

        viewModel.saveRoutine(
            onSuccess = { fail("onSuccess should not be called") },
            onError = { errorCalledWith = it }
        )

        assertEquals(R.string.toast_repeat_required, errorCalledWith)
    }

    /**
     * Given: 제목이 "Interval Routine", 탭 인덱스가 2, 반복 간격 텍스트가 "5" (유효한 숫자),
     *        알람이 활성화되고 알람 시간이 9:00으로 설정됨
     * When: saveRoutine() 호출 시
     * Then: repository.insertRoutine()가 호출되고 onSuccess 콜백이 호출되며,
     *       저장된 RoutineItem의 필드 값이 올바르게 설정되어야 한다.
     */
    @Test
    fun shouldSaveRoutineAndCallOnSuccess_whenRepeatIntervalIsValid() = runTest {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onError = mockk<(Int) -> Unit>(relaxed = true)

        viewModel.onTitleChange("Interval Routine")
        viewModel.onTabIndexChange(2)
        viewModel.onRepeatIntervalChange("5")
        viewModel.onAlarmToggle(true)
        viewModel.onAlarmTimeChange(LocalTime.of(9, 0))

        viewModel.saveRoutine(onSuccess = onSuccess, onError = onError)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repository.insertRoutine(any<RoutineItem>()) }
        verify(exactly = 1) { onSuccess() }
        verify(exactly = 0) { onError(any()) }

        val slot = slot<RoutineItem>()
        coVerify { repository.insertRoutine(capture(slot)) }
        val saved = slot.captured

        assertEquals("Interval Routine", saved.title)
        assertEquals(RepeatType.EVERY_X_DAYS, saved.repeatType)
        assertEquals(5, saved.repeatIntervalDays)
        assertEquals(LocalTime.of(9, 0), saved.alarmTime)
    }

    /**
     * Given: 제목이 "Interval Routine No Alarm", 탭 인덱스가 2, 반복 간격 텍스트가 "7",
     *        알람이 비활성화 상태일 때
     * When: saveRoutine() 호출 시
     * Then: 저장된 RoutineItem의 alarmTime 필드가 null 이어야 한다.
     */
    @Test
    fun shouldSaveRoutineWithNullAlarmTime_whenAlarmDisabled_inIntervalTab() = runTest {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onError = mockk<(Int) -> Unit>(relaxed = true)

        viewModel.onTitleChange("Interval Routine No Alarm")
        viewModel.onTabIndexChange(2)
        viewModel.onRepeatIntervalChange("7")
        viewModel.onAlarmToggle(false)

        viewModel.saveRoutine(onSuccess = onSuccess, onError = onError)
        testScheduler.advanceUntilIdle()

        val slot = slot<RoutineItem>()
        coVerify { repository.insertRoutine(capture(slot)) }
        val saved = slot.captured

        assertEquals(null, saved.alarmTime)
    }
}