
package com.example.myroutine.features.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel()) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()

    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2) {
        Int.MAX_VALUE
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val monthOffset = pagerState.currentPage - (Int.MAX_VALUE / 2)
        val newMonth = YearMonth.now().plusMonths(monthOffset.toLong())
        if (newMonth != currentMonth) {
            viewModel.selectDay(newMonth.atDay(1)) // Select 1st day of new month
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }
            Text(text = "${currentMonth.year}년 ${currentMonth.monthValue}월")
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
            IconButton(onClick = {
                viewModel.goToToday()
                coroutineScope.launch {
                    pagerState.scrollToPage(Int.MAX_VALUE / 2)
                }
            }) {
                Icon(Icons.Default.Today, contentDescription = "Today")
            }
        }

        // Calendar Grid (Swipeable)
        HorizontalPager(state = pagerState) { page ->
            val monthOffset = page - (Int.MAX_VALUE / 2)
            val monthToDisplay = YearMonth.now().plusMonths(monthOffset.toLong())
            CalendarGrid(
                displayMonth = monthToDisplay,
                selectedDate = selectedDate,
                calendarDays = calendarDays,
                onDayClick = { date -> viewModel.selectDay(date) }
            )
        }

        // Routine List for selected date
        selectedDate?.let { date ->
            val dummyRoutines = listOf(
                "${date.dayOfMonth}일 루틴 1",
                "${date.dayOfMonth}일 루틴 2",
                "${date.dayOfMonth}일 루틴 3"
            )
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(dummyRoutines) {
                    ListItem(headlineContent = { Text(it) })
                }
            }
        } ?: Text(text = "날짜를 선택해주세요.")
    }
}

@Composable
fun CalendarGrid(
    displayMonth: YearMonth,
    selectedDate: LocalDate?,
    calendarDays: List<CalendarDay>,
    onDayClick: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day of week headers
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(daysOfWeek) { dayOfWeek ->
                Box(
                    modifier = Modifier.padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                }
            }
        }

        // Days of the month
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(280.dp) // 고정 높이
        ) {
            items(calendarDays) { calendarDay ->
                val day = calendarDay.date
                val isSelected = calendarDay.isSelected
                val isWeekend = calendarDay.isWeekend
                val isHoliday = calendarDay.isHoliday

                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isHoliday || day?.dayOfWeek == DayOfWeek.SUNDAY -> Color.Red
                    day?.dayOfWeek == DayOfWeek.SATURDAY -> Color.Blue
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(enabled = day != null) { day?.let { onDayClick(it) } }
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Text(text = day.dayOfMonth.toString(), color = textColor)
                    } else {
                        Text(text = "")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CalendarScreen()
}

