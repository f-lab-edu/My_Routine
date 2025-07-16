package com.example.myroutine

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myroutine.data.dto.Body
import com.example.myroutine.data.dto.Header
import com.example.myroutine.data.dto.HolidayDto
import com.example.myroutine.data.dto.Items
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.database.AppDatabase
import com.example.myroutine.data.repository.HolidayRepository
import com.example.myroutine.data.repository.RoutineRepository
import com.example.myroutine.data.repository.RoutineRepositoryImpl
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import com.example.myroutine.data.source.remote.HolidayApiService
import com.example.myroutine.data.source.local.HolidayLocalDataSource
import com.example.myroutine.data.local.dao.HolidayCacheMetadataDao
import io.mockk.mockk

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RoutineRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var routineDao: RoutineDao
    private lateinit var checkDao: RoutineCheckDao
    private lateinit var holidayRepository: HolidayRepository
    private lateinit var repository: RoutineRepository

    // HolidayRepository의 의존성들을 목킹합니다.
    private val holidayApiService: HolidayApiService = mockk()
    private val holidayLocalDataSource: HolidayLocalDataSource = mockk()
    private val holidayCacheMetadataDao: HolidayCacheMetadataDao = mockk()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        routineDao = db.routineDao()
        checkDao = db.routineCheckDao()

        // HolidayRepository의 실제 구현체를 사용하되, 그 의존성들은 목킹된 것을 주입합니다.
        holidayRepository = HolidayRepository(
            holidayApiService = holidayApiService,
            holidayLocalDataSource = holidayLocalDataSource,
            holidayCacheMetadataDao = holidayCacheMetadataDao
        )

        // RoutineRepositoryImpl 생성자에 holidayRepository 주입
        repository = RoutineRepositoryImpl(routineDao, checkDao, holidayRepository)
    }

    @After
    fun teardown() {
        db.close()
    }

    /**
     * 루틴 체크 상태를 설정/해제하는 기능을 테스트함.
     *
     * 기대 결과: 체크 시 isDone=true, 해제 시 isDone=false.
     * 검증 방법: getTodayRoutines()를 호출하여 상태를 비교함.
     */
    @Test
    fun setRoutineChecked_marksAsDone_andUndone() = runTest {
        // given: 목 데이터를 삽입하고 첫 번째 루틴을 가져옵니다.
        repository.insertMockDataIfEmpty()
        val routine = repository.getRoutines().first()
        val today = LocalDate.now()

        // when: 루틴을 완료 상태로 설정합니다.
        repository.setRoutineChecked(routine.id, today, true)
        // then: 오늘 루틴을 가져와 첫 번째 루틴이 완료되었는지 확인합니다.
        val todayRoutines = repository.getTodayRoutines(today)
        assertTrue("루틴이 완료 상태로 설정되어야 합니다.", todayRoutines.first().isDone)

        // when: 루틴을 미완료 상태로 설정합니다.
        repository.setRoutineChecked(routine.id, today, false)
        // then: 오늘 루틴을 가져와 첫 번째 루틴이 미완료 상태인지 확인합니다.
        val todayRoutines2 = repository.getTodayRoutines(today)
        assertFalse("루틴이 미완료 상태로 설정되어야 합니다.", todayRoutines2.first().isDone)
    }
}
