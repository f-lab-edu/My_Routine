package com.example.myroutine.features.add

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myroutine.R
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun AddRoutineScreen(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var tabIndex by remember { mutableIntStateOf(1) }

    val alarmEnabled = remember { mutableStateOf(false) }
    val alarmTime = remember { mutableStateOf(LocalTime.now()) }

    val selectedDateMillis = remember { mutableStateOf<Long?>(null) }
    val selectedDays = remember { mutableStateListOf<String>() }
    val repeatIntervalText = remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.new_routine),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.size(48.dp)) // 닫기 버튼 크기 맞추기용
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.routine_title)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Tabs
            TabRow(selectedTabIndex = tabIndex, indicator = {}, divider = {}) {
                val tabs = listOf(
                    stringResource(R.string.tab_one_time),
                    stringResource(R.string.tab_specific_days),
                    stringResource(R.string.tab_repeat_x_days)
                )
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .background(
                                color = if (tabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = tabTitle,
                            color = if (tabIndex == index)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content depending on tab
            when (tabIndex) {
                0 -> OneTimeContent(selectedDateMillis)
                1 -> SpecificDaysContent(selectedDays)
                2 -> RepeatXDaysContent(repeatIntervalText)
            }

            AlarmSettingSection(
                alarmEnabled = alarmEnabled,
                alarmTime = alarmTime
            )

            Spacer(modifier = Modifier.weight(1f))

            val toastTitleRequired = stringResource(R.string.toast_title_required)
            val toastDateRequired = stringResource(R.string.toast_date_required)
            val toastDayRequired = stringResource(R.string.toast_day_required)
            val toastRepeatRequired = stringResource(R.string.toast_repeat_required)

            // Save button
            Button(
                onClick = {
                    val isInvalid = title.isBlank() || when (tabIndex) {
                        0 -> selectedDateMillis.value == null
                        1 -> selectedDays.isEmpty()
                        2 -> repeatIntervalText.value.isBlank()
                        else -> false
                    }

                    val errorMessage = when {
                        title.isBlank() -> toastTitleRequired
                        tabIndex == 0 -> toastDateRequired
                        tabIndex == 1 -> toastDayRequired
                        tabIndex == 2 -> toastRepeatRequired
                        else -> null
                    }

                    if (isInvalid && errorMessage != null) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = errorMessage,
                                withDismissAction = true
                            )
                        }
                    } else {
                        onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTimeContent(selectedDateMillis: MutableState<Long?>) {
    val datePickerState = rememberDatePickerState()
    val openDialog = remember { mutableStateOf(false) }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        selectedDateMillis.value = datePickerState.selectedDateMillis
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { openDialog.value = true }) {
            Text(stringResource(R.string.select_date))
        }

        Text(
            color = MaterialTheme.colorScheme.onBackground,
            text = datePickerState.selectedDateMillis?.let {
                val localDate =
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                stringResource(R.string.selected_date, localDate.toString())
            } ?: stringResource(R.string.no_date_selected))

        if (openDialog.value) {
            DatePickerDialog(onDismissRequest = { openDialog.value = false }, confirmButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    stringResource(R.string.confirm)
                }
            }, dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    stringResource(R.string.cancel)
                }
            }) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpecificDaysContent(selectedDays: MutableList<String>) {
    val days = listOf(
        stringResource(R.string.sun),
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri),
        stringResource(R.string.sat)
    )
    val excludeHolidays = remember { mutableStateOf(false) }

    Text(
        stringResource(R.string.repeat),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            Box(modifier = Modifier.weight(1f)) {
                FilterChip(
                    selected = selectedDays.contains(day),
                    onClick = {
                        if (selectedDays.contains(day)) selectedDays.remove(day)
                        else selectedDays.add(day)
                    },
                    label = {
                        Text(
                            text = day,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }


    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.exclude_holidays),
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = excludeHolidays.value, onCheckedChange = { excludeHolidays.value = it })
    }

    if (excludeHolidays.value) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.holiday_exclusion_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun RepeatXDaysContent(intervalText: MutableState<String>) {
    OutlinedTextField(
        value = intervalText.value,
        onValueChange = { intervalText.value = it.filter { c -> c.isDigit() } },
        label = { Text(stringResource(R.string.repeat_every_x_days)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun AlarmSettingSection(
    alarmEnabled: MutableState<Boolean>, alarmTime: MutableState<LocalTime>
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    Column {
        Text(
            stringResource(R.string.alarm),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.enable_alarm),
                color = MaterialTheme.colorScheme.onBackground
            )
            Switch(
                checked = alarmEnabled.value, onCheckedChange = {
                    alarmEnabled.value = it
                    if (it) showDialog.value = true
                })
        }

        if (alarmEnabled.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(
                        R.string.alarm_time,
                        alarmTime.value.hour,
                        alarmTime.value.minute
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(onClick = { showDialog.value = true }) {
                    Text(stringResource(R.string.select_time))
                }
            }
        }

        if (showDialog.value) {
            TimePickerDialog(
                context, { _, hour: Int, minute: Int ->
                    alarmTime.value = LocalTime.of(hour, minute)
                    showDialog.value = false
                },
                alarmTime.value.hour,
                alarmTime.value.minute,
                true
            ).apply {
                setOnCancelListener {
                    showDialog.value = false
                    alarmEnabled.value = false
                }
                setButton(
                    TimePickerDialog.BUTTON_NEGATIVE,
                    context.getString(R.string.cancel)
                ) { _, _ ->
                    showDialog.value = false
                    alarmEnabled.value = false
                }
            }.show()
        }

    }
}


