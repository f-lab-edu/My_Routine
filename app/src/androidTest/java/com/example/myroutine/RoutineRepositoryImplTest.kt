package com.example.myroutine

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myroutine.data.local.dao.RoutineCheckDao
import com.example.myroutine.data.local.dao.RoutineDao
import com.example.myroutine.data.local.database.AppDatabase
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RoutineRepositoryImplTest {

    private lateinit var db: AppDatabase
    private lateinit var routineDao: RoutineDao
    private lateinit var checkDao: RoutineCheckDao
    private lateinit var repository: RoutineRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        routineDao = db.routineDao()
        checkDao = db.routineCheckDao()
        repository = RoutineRepositoryImpl(routineDao, checkDao)
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
        repository.insertMockDataIfEmpty()
        val routine = repository.getRoutines().first()
        val today = LocalDate.now()

        repository.setRoutineChecked(routine.id, today, true)
        val todayRoutines = repository.getTodayRoutines(today)
        assertTrue(todayRoutines.first().isDone)

        repository.setRoutineChecked(routine.id, today, false)
        val todayRoutines2 = repository.getTodayRoutines(today)
        assertFalse(todayRoutines2.first().isDone)
    }
}
