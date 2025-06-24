package com.example.myroutine.data.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myroutine.R

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val routineId = intent.getIntExtra("ROUTINE_ID", -1)
        if (routineId == -1) return

        // TODO: 루틴 정보 DB에서 가져와서 알림 내용 구성 가능 (CoroutineScope 필요)

        val notification = NotificationCompat.Builder(context, "routine_channel_id")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("루틴 알림")
            .setContentText("루틴 수행할 시간입니다!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            // 클릭 시 앱 열기 intent 추가 가능
            .build()

        NotificationManagerCompat.from(context).notify(routineId, notification)

        // TODO: 다음 알람 예약 (반복 루틴이라면 AlarmScheduler 호출 필요)
    }
}
