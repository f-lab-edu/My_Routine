package com.example.myroutine.features.add

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myroutine.R
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

    private val _selectedDays = MutableStateFlow<List<Int>>(emptyList())
    val selectedDays: StateFlow<List<Int>> = _selectedDays

    private val _repeatIntervalText = MutableStateFlow("")
    val repeatIntervalText: StateFlow<String> = _repeatIntervalText

    private val _alarmEnabled = MutableStateFlow(false)
    val alarmEnabled: StateFlow<Boolean> = _alarmEnabled

    private val _alarmTime = MutableStateFlow(LocalTime.now())
    val alarmTime: StateFlow<LocalTime> = _alarmTime

    private val _excludeHolidays = MutableStateFlow(false)
    val excludeHolidays: StateFlow<Boolean> = _excludeHolidays

    // 상태 변경 함수
    fun onTitleChange(newTitle: String) {
        Log.d(TAG, "onTitleChange: $newTitle")
        _title.value = newTitle
    }

    fun onTabIndexChange(index: Int) {
        Log.d(TAG, "onTabIndexChange: $index")
        _tabIndex.value = index
    }

    fun onSelectedDateChange(date: LocalDate?) {
        Log.d(TAG, "onSelectedDateChange: $date")
        _selectedDate.value = date
    }

    fun onSelectedDaysChange(days: List<Int>) {
        Log.d(TAG, "onSelectedDaysChange: $days")
        _selectedDays.value = days
    }

    fun onRepeatIntervalChange(text: String) {
        Log.d(TAG, "onRepeatIntervalChange: $text")
        _repeatIntervalText.value = text
    }

    fun onAlarmToggle(enabled: Boolean) {
        Log.d(TAG, "onAlarmToggle: $enabled")
        _alarmEnabled.value = enabled
    }

    fun onAlarmTimeChange(time: LocalTime) {
        Log.d(TAG, "onAlarmTimeChange: $time")
        _alarmTime.value = time
    }

    fun onExcludeHolidayToggle(value: Boolean) {
        Log.d(TAG, "onExcludeHolidayToggle: $value")
        _excludeHolidays.value = value
    }

    // 저장 처리
    fun saveRoutine(
        onSuccess: () -> Unit,
        onError: (Int) -> Unit
    ) {
        val title = _title.value.trim()
        val tabIndex = _tabIndex.value

        Log.d(TAG, "saveRoutine() called")
        Log.d(TAG, "Current title: '$title'")
        Log.d(TAG, "Tab index: $tabIndex")

        if (title.isEmpty()) {
            Log.w(TAG, "Validation failed: title is empty")
            onError(R.string.toast_title_required)
            return
        }

        val specificDate: LocalDate?
        val repeatDays: List<Int>?
        val holidayType: HolidayType?
        val repeatType: RepeatType

        when (tabIndex) {
            0 -> {
                specificDate = _selectedDate.value
                if (specificDate == null) {
                    Log.w(TAG, "Validation failed: specific date not selected")
                    onError(R.string.toast_date_required)
                    return
                }
                repeatType = RepeatType.ONCE
                repeatDays = null
                holidayType = null
            }

            1 -> {
                repeatDays = _selectedDays.value
                if (repeatDays.isEmpty()) {
                    Log.w(TAG, "Validation failed: repeat days not selected")
                    onError(R.string.toast_day_required)
                    return
                }
                specificDate = null
                repeatType = if (_excludeHolidays.value) RepeatType.WEEKDAY_HOLIDAY else RepeatType.WEEKLY
                holidayType = if (_excludeHolidays.value) HolidayType.WEEKDAY else null
            }

            2 -> {
                if (_repeatIntervalText.value.isBlank()) {
                    Log.w(TAG, "Validation failed: repeat interval text is empty")
                    onError(R.string.toast_repeat_required)
                    return
                }
                specificDate = null
                repeatDays = null
                holidayType = null
                repeatType = RepeatType.NONE
            }

            else -> {
                Log.w(TAG, "Validation failed: invalid tab index $tabIndex")
                specificDate = null
                repeatDays = null
                holidayType = null
                repeatType = RepeatType.NONE
            }
        }

        val routine = RoutineItem(
            title = title,
            repeatType = repeatType,
            specificDate = specificDate,
            repeatDays = repeatDays,
            holidayType = holidayType,
            alarmTime = if (_alarmEnabled.value) _alarmTime.value else null
        )

        Log.d(TAG, "Saving routine: $routine")

        viewModelScope.launch {
            repository.insertRoutine(routine)
            Log.d(TAG, "Routine saved successfully")
            onSuccess()
        }
    }
}

