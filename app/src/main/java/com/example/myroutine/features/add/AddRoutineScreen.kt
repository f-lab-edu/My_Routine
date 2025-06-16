package com.example.myroutine.features.add

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myroutine.R
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun AddRoutineScreen(
    onBack: () -> Unit,
    onSave: () -> Unit,
    viewModel: AddRoutineViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val tabIndex by viewModel.tabIndex.collectAsState()
    val alarmEnabled by viewModel.alarmEnabled.collectAsState()
    val alarmTime by viewModel.alarmTime.collectAsState()

    val selectedDate by viewModel.selectedDate.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
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
                Spacer(modifier = Modifier.size(48.dp)) // Close button space
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { viewModel.onTitleChange(it) },
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
                        onClick = { viewModel.onTabIndexChange(index) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .background(
                                color = if (tabIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
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

            // Tab-specific content
            when (tabIndex) {
                0 -> OneTimeContent(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.onSelectedDateChange(it) }
                )
            }

            AlarmSettingSection(
                alarmEnabled = alarmEnabled,
                alarmTime = alarmTime,
                onAlarmToggle = { viewModel.onAlarmToggle(it) },
                onAlarmTimeChange = { viewModel.onAlarmTimeChange(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            val context = LocalContext.current

            // Save button
            Button(
                onClick = {
                    viewModel.saveRoutine(
                        onSuccess = onSave,
                        onError = { resId ->
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar(
                                    message = context.getString(resId),
                                    withDismissAction = true
                                )
                            }
                        }
                    )
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
fun OneTimeContent(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val openDialog = remember { mutableStateOf(false) }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            val localDate = Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onDateSelected(localDate)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { openDialog.value = true }) {
            Text(stringResource(R.string.select_date))
        }

        Text(
            color = MaterialTheme.colorScheme.onBackground,
            text = selectedDate?.toString() ?: stringResource(R.string.no_date_selected)
        )

        if (openDialog.value) {
            DatePickerDialog(
                onDismissRequest = { openDialog.value = false },
                confirmButton = {
                    TextButton(onClick = { openDialog.value = false }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { openDialog.value = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}


@Composable
fun AlarmSettingSection(
    alarmEnabled: Boolean,
    alarmTime: LocalTime,
    onAlarmToggle: (Boolean) -> Unit,
    onAlarmTimeChange: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }

    Column {
        Text(
            stringResource(R.string.alarm),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
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
                checked = alarmEnabled,
                onCheckedChange = {
                    onAlarmToggle(it)
                    if (it) showDialog.value = true
                }
            )
        }

        if (alarmEnabled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(
                        R.string.alarm_time,
                        alarmTime.hour,
                        alarmTime.minute
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
                context,
                { _, hour: Int, minute: Int ->
                    onAlarmTimeChange(LocalTime.of(hour, minute))
                    showDialog.value = false
                },
                alarmTime.hour,
                alarmTime.minute,
                true
            ).apply {
                setOnCancelListener {
                    showDialog.value = false
                    onAlarmToggle(false)
                }
                setButton(
                    TimePickerDialog.BUTTON_NEGATIVE,
                    context.getString(R.string.cancel)
                ) { _, _ ->
                    showDialog.value = false
                    onAlarmToggle(false)
                }
            }.show()
        }
    }
}



