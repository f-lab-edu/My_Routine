package com.example.myroutine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class RoutineCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int,            // RoutineItem.id 와 연결
    val completeDate: LocalDate,           // 언제 완료했는지
    val isDone: Boolean = true     // 완료 여부
)