package com.example.myroutine.data.repository

import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineCheck
import com.example.myroutine.data.local.entity.RoutineItem
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * RoutineRepositoryImpl의 기능을 테스트하는 클래스입니다.
 * Mockk 라이브러리를 사용하여 DAO(Data Access Object)를 목킹하고,
 * kotlinx-coroutines-test를 사용하여 코루틴 테스트를 수행합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RoutineRepositoryImplTest {

    // 테스트 대상 Repository
    private lateinit var repository: RoutineRepositoryImpl

    // Mockk으로 생성된 RoutineDao 인스턴스
    private val routineDao: RoutineDao = mockk()

    // Mockk으로 생성된 RoutineCheckDao 인스턴스
    private val checkDao: RoutineCheckDao = mockk()

    /**
     * 각 테스트 실행 전에 호출되는 초기화 함수입니다.
     * - 목킹된 DAO를 사용하여 RoutineRepositoryImpl 인스턴스를 생성합니다.
     */
    @Before
    fun setup() {
        repository = RoutineRepositoryImpl(routineDao, checkDao)
    }

    /**
     * getRoutineChecksForPeriod 메서드가 지정된 기간 내의 체크 기록을 올바르게 반환하는지 테스트합니다.
     */
    @Test
    fun `getRoutineChecksForPeriod should return checks within the specified period`() = runTest {
        // Given: 테스트를 위한 시작 날짜와 종료 날짜를 정의합니다.
        val startDate = LocalDate.of(2024, 7, 1)
        val endDate = LocalDate.of(2024, 7, 7)

        // Given: 기간 내에 있는 가상의 루틴 체크 데이터와 기간 밖에 있는 데이터를 정의합니다.
        val checksInPeriod = listOf(
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 1)),
            RoutineCheck(routineId = 2, completeDate = LocalDate.of(2024, 7, 3)),
            RoutineCheck(routineId = 1, completeDate = LocalDate.of(2024, 7, 7))
        )
        val checksOutsidePeriod = listOf(
            RoutineCheck(routineId = 3, completeDate = LocalDate.of(2024, 6, 30)),
            RoutineCheck(routineId = 4, completeDate = LocalDate.of(2024, 7, 8))
        )

        // When: checkDao.getChecksForPeriod가 호출될 때, checksInPeriod를 반환하도록 목킹합니다.
        coEvery { checkDao.getChecksForPeriod(startDate, endDate) } returns checksInPeriod

        // Then: getRoutineChecksForPeriod를 호출하고 결과가 예상과 일치하는지 단언합니다.
        val result = repository.getRoutineChecksForPeriod(startDate, endDate)
        assertEquals(checksInPeriod.size, result.size)
        assertEquals(checksInPeriod, result)
    }

    /**
     * getRoutineItemsForPeriod 메서드가 지정된 기간 내에 적용 가능한 루틴을 올바르게 반환하는지 테스트합니다.
     */
    @Test
    fun `getRoutineItemsForPeriod should return applicable routines within the period`() = runTest {
        // Given: 테스트를 위한 시작 날짜와 종료 날짜를 정의합니다.
        val startDate = LocalDate.of(2024, 7, 1) // 월요일
        val endDate = LocalDate.of(2024, 7, 7) // 일요일

        // Given: 가상의 루틴 데이터를 정의합니다.
        val routine1 = RoutineItem.mock(
            id = 1,
            title = "Daily Routine",
            repeatType = RepeatType.EVERY_X_DAYS,
            repeatIntervalDays = 1,
            startDate = LocalDate.of(2024, 7, 1)  // 시작일도 명시해주는 게 좋아요
        ) // 매일 적용 가능
        val routine2 = RoutineItem.mock(
            id = 2,
            title = "Weekly Monday Routine",
            repeatType = RepeatType.WEEKLY,
            repeatDays = listOf(1)
        ) // 월요일에만 적용 가능
        val routine3 = RoutineItem.mock(
            id = 3,
            title = "Once on July 10",
            repeatType = RepeatType.ONCE,
            specificDate = LocalDate.of(2024, 7, 10)
        ) // 기간 밖에 있는 루틴

        // When: routineDao.getAll이 호출될 때, 모든 루틴을 반환하도록 목킹합니다.
        coEvery { routineDao.getAll() } returns listOf(routine1, routine2, routine3)

        // Then: getRoutineItemsForPeriod를 호출하고 결과가 예상과 일치하는지 단언합니다.
        val result = repository.getRoutineItemsForPeriod(startDate, endDate)

        println("Result routines:")
        result.forEach { println("- ${it.title} (id=${it.id})") }

        // routine1은 포함되어야 합니다 (매일 적용 가능).
        // routine2는 포함되어야 합니다 (7월 1일 월요일에 적용 가능).
        // routine3은 제외되어야 합니다 (특정 날짜가 기간 밖에 있음).
        assertEquals(2, result.size)
        assert(result.contains(routine1))
        assert(result.contains(routine2))
        assert(!result.contains(routine3))
    }
}