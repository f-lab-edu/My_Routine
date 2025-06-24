package com.example.myroutine.data.alarm

import android.content.Context
import com.example.myroutine.data.local.entity.RoutineItem
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
): AlarmScheduler {
    override fun schedule(routine: RoutineItem) {
        // AlarmManager를 사용한 실제 알람 예약 구현
    }

    override fun cancel(routineId: Int) {
        // 알람 취소 구현
    }


    override fun calculateNextAlarmTime(routine: RoutineItem): Long? {
        // 다음 알람 시간 계산 구현
        return TODO("Provide the return value")
    }
}