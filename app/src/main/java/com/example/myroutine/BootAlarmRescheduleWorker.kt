package com.example.myroutine

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myroutine.common.L
import com.example.myroutine.data.alarm.AlarmScheduler
import com.example.myroutine.data.repository.RoutineRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BootAlarmRescheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val routineRepository: RoutineRepository,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val routines = routineRepository.getRoutines()
        routines.forEach { routine ->
            if (routine.alarmTime != null) {
                try {
                    alarmScheduler.schedule(routine)
                } catch (e: Exception) {
                    L.e("BootAlarmRescheduleWorker", "Failed to schedule alarm for routine ${routine.id}", e)
                }
            }
        }
        return Result.success()
    }
}
