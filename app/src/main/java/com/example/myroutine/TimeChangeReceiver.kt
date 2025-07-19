package com.example.myroutine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TimeChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val workRequest = OneTimeWorkRequestBuilder<BootAlarmRescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}