package com.example.myroutine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity
data class RoutineItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,                            // Room 저장용 기본 키
    val title: String,                          // 루틴 이름
    val description: String? = null,            // 추가 설명
    val isDone: Boolean = false,                // 완료 여부

    // 반복 유형 (요일/날짜/공휴일)
    val repeatType: RepeatType = RepeatType.NONE,

    // 반복 요일 (월=1, ..., 일=7), repeatType == WEEKLY 인 경우만 사용
    val repeatDays: List<Int>? = null,

    // 특정 날짜에만 실행, repeatType == ONCE 인 경우만 사용
    val specificDate: LocalDate? = null,

    // 평일/공휴일 구분용, repeatType == WEEKDAY_HOLIDAY 인 경우 사용
    val holidayType: HolidayType? = null,

    val repeatIntervalDays: Int? = null,           // N일 주기
    val startDate: LocalDate? = null,              // 반복 시작일 기준

    // 알람 설정 시간 (nullable이면 알람 없음)
    val alarmTime: LocalTime? = null
){
    companion object {
        fun mock(
            title: String,
            isDone: Boolean = false
        ): RoutineItem {
            return RoutineItem(
                title = title,
                isDone = isDone
            )
        }
    }
}

enum class RepeatType {
    NONE,           // 반복 없음
    WEEKLY,         // 요일 반복
    ONCE,           // 특정 날짜
    EVERY_X_DAYS,   // X일마다 반복
    WEEKDAY_HOLIDAY // 평일/공휴일 구분
}

enum class HolidayType {
    WEEKDAY, HOLIDAY
}