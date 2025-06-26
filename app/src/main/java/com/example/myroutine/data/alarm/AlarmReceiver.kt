package com.example.myroutine.data.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myroutine.AlarmConstants
import com.example.myroutine.PermissionUtils
import com.example.myroutine.R
import com.example.myroutine.data.repository.RoutineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        if (!PermissionUtils.hasPostNotificationPermission(context)) {
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val routine = withContext(Dispatchers.IO) {
                routineRepository.getRoutines().find { it.id == routineId }
            }

            if (routine == null) return@launch

            val notification = NotificationCompat.Builder(context, AlarmConstants.ROUTINE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle(routine.title)
                .setContentText(context.getString(R.string.notification_content_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(routineId, notification)

            alarmScheduler.schedule(routine)
        }
    }
}
