package com.example.myroutine.data.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myroutine.PermissionUtils
import com.example.myroutine.R
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var routineRepository: RoutineRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getIntExtra("ROUTINE_ID", -1)
        if (routineId == -1) return

        // 권한 체크를 Utils로 대체
        if (!PermissionUtils.hasPostNotificationPermission(context)) {
            // 권한 없으면 알림 표시 안함
            return
        }

        // 코루틴으로 비동기 처리 (DB 접근)
        CoroutineScope(Dispatchers.IO).launch {
            val routine: RoutineItem? = routineRepository.getRoutines()
                .find { it.id == routineId }

            if (routine == null) return@launch

            // 알림 내용 구성
            val notification = NotificationCompat.Builder(context, "routine_channel_id")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(routine.title)
                .setContentText("루틴 수행할 시간입니다!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                // 클릭 시 앱 열기 intent 추가 가능
                .build()

            // 메인 스레드에서 Notification 표시
            CoroutineScope(Dispatchers.Main).launch {
                NotificationManagerCompat.from(context).notify(routineId, notification)
            }

            // 다음 알람 예약 (반복 루틴인 경우)
            alarmScheduler.schedule(routine)
        }
    }
}
