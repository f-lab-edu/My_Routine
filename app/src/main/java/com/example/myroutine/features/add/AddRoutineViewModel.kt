package com.example.myroutine.features.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myroutine.R
import com.example.myroutine.common.LogWrapper
import com.example.myroutine.data.local.entity.HolidayType
import com.example.myroutine.data.local.entity.RepeatType
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddRoutineViewModel @Inject constructor(
    private val repository: RoutineRepository
) : ViewModel() {

    private val TAG = "AddRoutineViewModel"

    // StateFlows (입력 상태)
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _tabIndex = MutableStateFlow(1)
    val tabIndex: StateFlow<Int> = _tabIndex

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    private val _alarmEnabled = MutableStateFlow(false)
    val alarmEnabled: StateFlow<Boolean> = _alarmEnabled

    private val _alarmTime = MutableStateFlow(LocalTime.now())
    val alarmTime: StateFlow<LocalTime> = _alarmTime

    // 상태 변경 함수
    fun onTitleChange(newTitle: String) {
        LogWrapper.d(TAG, "onTitleChange: $newTitle")
        _title.value = newTitle
    }

    fun onTabIndexChange(index: Int) {
        LogWrapper.d(TAG, "onTabIndexChange: $index")
        _tabIndex.value = index
    }

    fun onSelectedDateChange(date: LocalDate?) {
        LogWrapper.d(TAG, "onSelectedDateChange: $date")
        _selectedDate.value = date
    }


    fun onAlarmToggle(enabled: Boolean) {
        LogWrapper.d(TAG, "onAlarmToggle: $enabled")
        _alarmEnabled.value = enabled
    }

    fun onAlarmTimeChange(time: LocalTime) {
        LogWrapper.d(TAG, "onAlarmTimeChange: $time")
        _alarmTime.value = time
    }

    // 저장 처리
    fun saveRoutine(
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ) {
        val title = _title.value.trim()
        val tabIndex = _tabIndex.value

        LogWrapper.d(TAG, "saveRoutine() called")
        LogWrapper.d(TAG, "Current title: '$title'")
        LogWrapper.d(TAG, "Tab index: $tabIndex")

        if (title.isEmpty()) {
            LogWrapper.w(TAG, "Validation failed: title is empty")
            onError(R.string.toast_title_required)
            return
        }

        val specificDate: LocalDate?
        val repeatDays: List<Int>?
        val holidayType: HolidayType?
        val repeatType: RepeatType
        val repeatIntervalDays: Int?
        val startDate: LocalDate?

        when (tabIndex) {
            0 -> {
                specificDate = _selectedDate.value
                if (specificDate == null) {
                    LogWrapper.w(TAG, "Validation failed: specific date not selected")
                    onError(R.string.toast_date_required)
                    return
                }
                repeatType = RepeatType.ONCE
                repeatDays = null
                holidayType = null
                repeatIntervalDays = null
                startDate = null
            }

            else -> {
                LogWrapper.w(TAG, "Validation failed: invalid tab index $tabIndex")
                specificDate = null
                repeatDays = null
                holidayType = null
                repeatType = RepeatType.NONE
                repeatIntervalDays = null
                startDate = null
            }
        }

        val routine = RoutineItem(
            title = title,
            repeatType = repeatType,
            specificDate = specificDate,
            repeatDays = repeatDays,
            holidayType = holidayType,
            alarmTime = if (_alarmEnabled.value) _alarmTime.value else null,
            repeatIntervalDays = repeatIntervalDays,
            startDate = startDate
        )

        LogWrapper.d(TAG, "Saving routine: $routine")

        viewModelScope.launch {
            repository.insertRoutine(routine)
            LogWrapper.d(TAG, "Routine saved successfully")
            onSuccess()
        }
    }
}
