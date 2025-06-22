package com.example.myroutine.features.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myroutine.data.local.entity.RoutineItem
import com.example.myroutine.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: RoutineRepository
) : ViewModel() {

    private val _routines = MutableStateFlow<List<RoutineItem>>(emptyList())
    val routines: StateFlow<List<RoutineItem>> = _routines.asStateFlow()

    init {
        viewModelScope.launch {

            val today = LocalDate.now()
            val todayRoutines = repository.getTodayRoutines(today)

            _routines.value = todayRoutines
        }
    }

    fun onRoutineChecked(routineId: Int, isChecked: Boolean) {
        viewModelScope.launch {
            val today = LocalDate.now()
            repository.setRoutineChecked(routineId, today, isChecked)

            _routines.update { list ->
                list.map {
                    if (it.id == routineId) it.copy(isDone = isChecked) else it
                }
            }
        }
    }
}
