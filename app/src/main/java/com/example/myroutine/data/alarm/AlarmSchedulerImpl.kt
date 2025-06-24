package com.example.myroutine.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.myroutine.data.local.entity.HolidayType
import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
): AlarmScheduler {
    override fun schedule(routine: RoutineItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 기존 알람 취소
        cancel(routine.id)

        val alarmTime = routine.alarmTime ?: return

        val nextTriggerTime = calculateNextAlarmTime(routine) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ROUTINE_ID", routine.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routine.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // 권한 없을 때 처리 (로그 출력, 사용자 안내 등)
        }
    }

    override fun cancel(routineId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routineId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }


    override fun calculateNextAlarmTime(routine: RoutineItem): Long? {
        val nowMillis = System.currentTimeMillis()

        fun toMillis(date: LocalDate, time: LocalTime): Long {
            val dt = LocalDateTime.of(date, time)
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, dt.year)
                set(Calendar.MONTH, dt.monthValue - 1)
                set(Calendar.DAY_OF_MONTH, dt.dayOfMonth)
                set(Calendar.HOUR_OF_DAY, dt.hour)
                set(Calendar.MINUTE, dt.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        // TODO: 실제 공휴일 체크 로직 구현 예정
        fun isHoliday(date: LocalDate): Boolean {
            // 임시로 주말만 공휴일로 간주
            val day = date.dayOfWeek.value
            return day == 6 || day == 7
        }

        val alarmTime = routine.alarmTime ?: return null

        when (routine.repeatType) {
            RepeatType.ONCE -> {
                val date = routine.specificDate ?: return null
                if (routine.holidayType == HolidayType.WEEKDAY && isHoliday(date)) return null
                val trigger = toMillis(date, alarmTime)
                return if (trigger > nowMillis) trigger else null
            }

            RepeatType.WEEKLY, RepeatType.WEEKDAY_HOLIDAY -> {
                val days = routine.repeatDays ?: return null
                val today = LocalDate.now()

                for (offset in 0..7) {
                    val candidateDate = today.plusDays(offset.toLong())
                    val dayOfWeek = candidateDate.dayOfWeek.value // 월=1..일=7

                    if (days.contains(dayOfWeek)) {
                        if (routine.holidayType == HolidayType.WEEKDAY && isHoliday(candidateDate)) continue
                        val trigger = toMillis(candidateDate, alarmTime)
                        if (trigger > nowMillis) return trigger
                    }
                }
                return null
            }

            RepeatType.EVERY_X_DAYS -> {
                val startDate = routine.startDate ?: return null
                val interval = routine.repeatIntervalDays ?: return null

                var candidateDate = startDate
                while (toMillis(candidateDate, alarmTime) <= nowMillis) {
                    candidateDate = candidateDate.plusDays(interval.toLong())
                }

                if (routine.holidayType == HolidayType.WEEKDAY && isHoliday(candidateDate)) {
                    candidateDate = candidateDate.plusDays(interval.toLong())
                }

                return toMillis(candidateDate, alarmTime)
            }

            else -> return null
        }
    }

}