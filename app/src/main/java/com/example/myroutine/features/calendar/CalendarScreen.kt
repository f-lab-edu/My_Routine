package com.example.myroutine.features.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myroutine.R
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.Checkbox
import com.example.myroutine.data.local.entity.RoutineItem

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState(initial = emptyList())
    val routinesForSelectedDate by viewModel.routinesForSelectedDate.collectAsState()

    val pageCount = 1200 // 충분히 큰 유한한 페이지 수
    val initialPage = pageCount / 2

    val pagerState = rememberPagerState(initialPage = initialPage) {
        pageCount
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .debounce(300L) // 300ms 디바운스
            .collect { page ->
                val monthOffset = page - initialPage
                val newMonth = YearMonth.now().plusMonths(monthOffset.toLong())
                // 현재 ViewModel의 currentMonth와 다를 경우에만 업데이트 및 1일 선택
                if (newMonth != currentMonth) {
                    viewModel.setCurrentMonth(newMonth)
                    viewModel.selectDay(newMonth.atDay(1))
                } else if (newMonth == YearMonth.now() && selectedDate != LocalDate.now()) {
                    // 현재 월로 돌아왔을 때, 오늘 날짜가 선택되어 있지 않으면 오늘 날짜 선택
                    viewModel.selectDay(LocalDate.now())
                } else {
                    // Do nothing
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(id = R.string.calendar_year_month, currentMonth.year, currentMonth.monthValue),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Next Month",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = {
                viewModel.goToToday()
                coroutineScope.launch {
                    pagerState.scrollToPage(initialPage)
                }
            }) {
                Text(
                    text = stringResource(id = R.string.today),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Calendar Grid (Swipeable)
        HorizontalPager(state = pagerState) { page ->
            val monthOffset = page - initialPage
            val monthToDisplay = YearMonth.now().plusMonths(monthOffset.toLong())
            CalendarGrid(
                selectedDate = selectedDate,
                calendarDays = calendarDays,
                onDayClick = { date -> viewModel.selectDay(date) }
            )
        }

        // Routine List for selected date
        if (selectedDate != null) {
            if (routinesForSelectedDate.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.calendar_select_date_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(routinesForSelectedDate) { routine ->
                        val backgroundColor = when {
                            routine.isDone && selectedDate.isBefore(LocalDate.now()) -> Color.Green.copy(alpha = 0.2f) // 완료된 과거 루틴
                            routine.isDone && selectedDate.isEqual(LocalDate.now()) -> Color.Green.copy(alpha = 0.2f) // 완료된 오늘 루틴
                            !routine.isDone && selectedDate.isBefore(LocalDate.now()) -> Color.Red.copy(alpha = 0.2f) // 미완료 과거 루틴
                            else -> Color.Transparent // 미래 루틴 또는 미완료 오늘 루틴
                        }
                        ListItem(
                            headlineContent = { Text(routine.title) },
                            modifier = Modifier.background(backgroundColor),
                            trailingContent = {
                                Checkbox(
                                    checked = routine.isDone,
                                    onCheckedChange = { isChecked ->
                                        viewModel.onRoutineChecked(routine.id, isChecked)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        } else {
            Text(
                text = stringResource(id = R.string.calendar_select_date_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CalendarGrid(
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
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Days of the month
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp) // 고정 높이
        ) {
            items(calendarDays) { calendarDay ->
                val day = calendarDay.date
                if (day != null) {
                    val isSelected = calendarDay.isSelected
                    val isWeekend = calendarDay.isWeekend
                    val isHoliday = calendarDay.isHoliday

                    val textColor = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isHoliday || day.dayOfWeek == DayOfWeek.SUNDAY -> Color.Red
                        day.dayOfWeek == DayOfWeek.SATURDAY -> Color.Blue
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(enabled = true) { onDayClick(day) }
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = day.dayOfMonth.toString(), color = textColor)
                    }
                } else {
                    // null인 경우 아무것도 렌더링하지 않음
                    Spacer(modifier = Modifier.padding(4.dp))
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

