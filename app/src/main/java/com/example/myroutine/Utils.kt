package com.example.myroutine

import android.Manifest
import android.content.Context
import android.os.Build
import android.app.AlarmManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun hasPostNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() ?: false
        } else {
            true
        }
    }

    /**
     * 정확한 알람 예약 권한 요청 인텐트 (API 31 이상)
     */
    fun getScheduleExactAlarmIntent(): android.content.Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        } else {
            null
        }
    }
}
