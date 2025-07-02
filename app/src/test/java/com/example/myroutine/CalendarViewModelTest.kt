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
     * CalendarViewModel의 초기 상태를 테스트합니다.
     * 현재 월과 선택된 날짜가 각각 현재 월과 오늘 날짜로 설정되었는지 확인합니다.
     */
    @Test
    fun `initial state should be current month and today selected`() = runTest {
        // Given: ViewModel이 초기화될 때
        val initialMonth = YearMonth.now()
        val initialDate = LocalDate.now()

        // When: 특별한 액션 없음

        // Then: currentMonth와 selectedDate가 초기 값과 일치해야 합니다.
        assertEquals(initialMonth, viewModel.currentMonth.value)
        assertEquals(initialDate, viewModel.selectedDate.value)
    }

    /**
     * `goToPreviousMonth` 함수를 테스트합니다.
     * `goToPreviousMonth` 호출 시 현재 월이 이전 월로 업데이트되고,
     * 선택된 날짜가 해당 이전 월의 1일로 설정되는지 확인합니다.
     */
    @Test
    fun `goToPreviousMonth should update month and select first day`() = runTest {
        // Given: 현재 월 상태
        val initialMonth = YearMonth.now()

        // When: goToPreviousMonth 함수 호출
        viewModel.goToPreviousMonth()
        advanceUntilIdle() // 코루틴 완료 대기

        // Then: 월이 이전 월로 변경되고, 선택된 날짜는 해당 월의 1일이어야 합니다.
        assertEquals(initialMonth.minusMonths(1), viewModel.currentMonth.value)
        assertEquals(initialMonth.minusMonths(1).atDay(1), viewModel.selectedDate.value)
    }

    /**
     * `goToNextMonth` 함수를 테스트합니다.
     * `goToNextMonth` 호출 시 현재 월이 다음 월로 업데이트되고,
     * 선택된 날짜가 해당 다음 월의 1일로 설정되는지 확인합니다.
     */
    @Test
    fun `goToNextMonth should update month and select first day`() = runTest {
        // Given: 현재 월 상태
        val initialMonth = YearMonth.now()

        // When: goToNextMonth 함수 호출
        viewModel.goToNextMonth()
        advanceUntilIdle() // 코루틴 완료 대기

        // Then: 월이 다음 월로 변경되고, 선택된 날짜는 해당 월의 1일이어야 합니다.
        assertEquals(initialMonth.plusMonths(1), viewModel.currentMonth.value)
        assertEquals(initialMonth.plusMonths(1).atDay(1), viewModel.selectedDate.value)
    }

    /**
     * `goToToday` 함수를 테스트합니다.
     * `goToToday` 호출 시 현재 월과 선택된 날짜가 현재 월과 오늘 날짜로 재설정되는지 확인합니다.
     */
    @Test
    fun `goToToday should reset to current month and today`() = runTest {
        // Given: ViewModel의 월과 날짜가 현재와 다른 상태
        viewModel.goToNextMonth() // 다음 달로 이동
        viewModel.selectDay(LocalDate.now().plusDays(5)) // 다른 날짜 선택
        advanceUntilIdle()

        // When: goToToday 함수 호출
        viewModel.goToToday()
        advanceUntilIdle() // 코루틴 완료 대기

        // Then: 월과 날짜가 현재 월과 오늘 날짜로 재설정되어야 합니다.
        assertEquals(YearMonth.now(), viewModel.currentMonth.value)
        assertEquals(LocalDate.now(), viewModel.selectedDate.value)
    }

    /**
     * `selectDay` 함수를 테스트합니다.
     * `selectDay` 호출 시 선택된 날짜가 업데이트되는지 확인합니다.
     */
    @Test
    fun `selectDay should update selected date`() = runTest {
        // Given: 선택할 새로운 날짜
        val newDate = LocalDate.now().plusDays(1)

        // When: selectDay 함수 호출
        viewModel.selectDay(newDate)
        advanceUntilIdle() // 코루틴 완료 대기

        // Then: 선택된 날짜가 새로운 날짜로 업데이트되어야 합니다.
        assertEquals(newDate, viewModel.selectedDate.value)
    }

    /**
     * 현재 월에 대한 `calendarDays` 생성을 테스트합니다.
     * `calendarDays` 플로우가 현재 월에 대한 올바른 날짜 목록을 생성하는지 확인합니다.
     * 고정된 6주 그리드를 보장하기 위해 시작과 끝에 빈 날짜가 포함됩니다.
     */
    @Test
    fun `calendarDays should generate correct days for current month`() = runTest {
        // Given: ViewModel의 현재 월과 선택된 날짜를 설정
        val currentMonth = YearMonth.now()
        val selectedDate = LocalDate.now()
        viewModel._currentMonth.value = currentMonth // ViewModel의 _currentMonth를 직접 설정
        viewModel._selectedDate.value = selectedDate // ViewModel의 _selectedDate를 직접 설정
        advanceUntilIdle() // ViewModel의 Flow가 업데이트될 때까지 대기

        // When: calendarDays Flow의 값 가져오기
        val days = viewModel.calendarDays.first()
        val firstDayOfMonth = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        // 일요일을 0으로 시작하는 요일 값으로 변환 (SUNDAY=0, MONDAY=1, ..., SATURDAY=6)
        val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value % 7

        // Then:
        // 1. 월 시작 전 빈 날짜 확인
        for (i in 0 until firstDayOfWeekValue) {
            assertEquals("월 시작 전 빈 날짜가 null이어야 합니다. 인덱스: $i", null, days[i].date)
        }

        // 2. 실제 월의 날짜 확인
        for (i in 0 until daysInMonth) {
            val expectedDate = currentMonth.atDay(i + 1)
            val actualCalendarDay = days[firstDayOfWeekValue + i]
            assertEquals("날짜가 일치해야 합니다. 인덱스: ${firstDayOfWeekValue + i}", expectedDate, actualCalendarDay.date)
            assertEquals("선택된 날짜가 일치해야 합니다. 인덱스: ${firstDayOfWeekValue + i}", expectedDate == selectedDate, actualCalendarDay.isSelected)
            assertEquals("주말 여부가 올바르게 식별되어야 합니다. 인덱스: ${firstDayOfWeekValue + i}", expectedDate.dayOfWeek == DayOfWeek.SATURDAY || expectedDate.dayOfWeek == DayOfWeek.SUNDAY, actualCalendarDay.isWeekend)
            assertEquals("공휴일 여부가 올바르게 식별되어야 합니다. 인덱스: ${firstDayOfWeekValue + i}", false, actualCalendarDay.isHoliday) // 공휴일은 아직 목업되지 않음
        }

        // 3. 총 날짜 수가 42개인지 확인 (고정된 6주)
        assertEquals("총 날짜 수가 42개여야 합니다.", 42, days.size)
    }

    /**
     * `calendarDays`가 주말을 올바르게 식별하는지 테스트합니다.
     * `CalendarDay`의 `isWeekend` 속성이 토요일과 일요일에 대해 올바르게 설정되었는지 확인합니다.
     */
    @Test
    fun `calendarDays should correctly identify weekends`() = runTest {
        // Given: 특정 월 (2024년 7월) 설정
        val month = YearMonth.of(2024, 7) // 2024년 7월
        viewModel._currentMonth.value = month
        viewModel._selectedDate.value = month.atDay(1) // 7월 1일 선택
        advanceUntilIdle()

        // When: calendarDays Flow의 값 가져오기
        val days = viewModel.calendarDays.first()

        // Then:
        // 1. 2024년 7월 6일 (토요일)이 주말로 식별되는지 확인
        val saturday = month.atDay(6)
        val saturdayCalendarDay = days.first { it.date == saturday }
        assertEquals(true, saturdayCalendarDay.isWeekend)

        // 2. 2024년 7월 7일 (일요일)이 주말로 식별되는지 확인
        val sunday = month.atDay(7)
        val sundayCalendarDay = days.first { it.date == sunday }
        assertEquals(true, sundayCalendarDay.isWeekend)

        // 3. 2024년 7월 8일 (월요일)이 주말이 아닌 것으로 식별되는지 확인
        val monday = month.atDay(8)
        val mondayCalendarDay = days.first { it.date == monday }
        assertEquals(false, mondayCalendarDay.isWeekend)
    }
}
