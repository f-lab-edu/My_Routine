package com.example.myroutine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkerParameters
import com.example.myroutine.data.alarm.AlarmScheduler
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class BootAlarmRescheduleWorkerTest {

    private val routineRepository = mockk<RoutineRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)

    private lateinit var worker: BootAlarmRescheduleWorker

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val workerParams = mockk<WorkerParameters>(relaxed = true) // WorkerParameters는 Mock 생성

        worker = BootAlarmRescheduleWorker(
            appContext = context,
            workerParams = workerParams,
            routineRepository = routineRepository,
            alarmScheduler = alarmScheduler
        )
    }

    @Test
    fun doWork_schedulesAlarmsForRoutinesWithAlarmTime() = runTest {
        val routineWithAlarm = RoutineItem(
            id = 1,
            title = "Test",
            alarmTime = LocalTime.of(9, 0)
        )
        val routineWithoutAlarm = RoutineItem(
            id = 2,
            title = "No Alarm",
            alarmTime = null
        )

        coEvery { routineRepository.getRoutines() } returns listOf(routineWithAlarm, routineWithoutAlarm)
        coEvery { alarmScheduler.schedule(any()) } just Runs

        val result = worker.doWork()

        coVerify(exactly = 1) { alarmScheduler.schedule(routineWithAlarm) }
        coVerify(exactly = 0) { alarmScheduler.schedule(routineWithoutAlarm) }

        TestCase.assertEquals(androidx.work.ListenableWorker.Result.success(), result)
    }

    @Test
    fun doWork_handlesExceptionAndReturnsSuccess() = runTest {
        val routineWithAlarm = RoutineItem(
            id = 1,
            title = "Test",
            alarmTime = LocalTime.of(9, 0)
        )

        coEvery { routineRepository.getRoutines() } returns listOf(routineWithAlarm)
        coEvery { alarmScheduler.schedule(any()) } throws RuntimeException("Failed")

        val result = worker.doWork()

        coVerify(exactly = 1) { alarmScheduler.schedule(routineWithAlarm) }
        TestCase.assertEquals(androidx.work.ListenableWorker.Result.success(), result)
    }
}