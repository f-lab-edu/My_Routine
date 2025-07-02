
package com.example.myroutine.features.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen() {
    val currentMonth = remember { YearMonth.now() }
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2) {
        Int.MAX_VALUE
    }
    val coroutineScope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val displayMonth = remember {
        derivedStateOf {
            val monthOffset = pagerState.currentPage - (Int.MAX_VALUE / 2)
            currentMonth.plusMonths(monthOffset.toLong())
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
            Text(text = "${displayMonth.value.year}년 ${displayMonth.value.monthValue}월")
            IconButton(onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        // Calendar Grid (Swipeable)
        HorizontalPager(state = pagerState) { page ->
            val monthOffset = page - (Int.MAX_VALUE / 2)
            val monthToDisplay = currentMonth.plusMonths(monthOffset.toLong())
            CalendarGrid(
                displayMonth = monthToDisplay,
                selectedDate = selectedDate,
                onDayClick = { date -> selectedDate = date }
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
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = displayMonth.atDay(1)
    val daysInMonth = displayMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 for Sunday, 6 for Saturday

    val days = mutableListOf<LocalDate?>()
    // Add empty days for the beginning of the week
    for (i in 0 until firstDayOfWeek) {
        days.add(null)
    }
    // Add actual days of the month
    for (i in 1..daysInMonth) {
        days.add(displayMonth.atDay(i))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Day of week headers
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(DayOfWeek.values()) { dayOfWeek ->
                Box(
                    modifier = Modifier.padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                }
            }
        }

        // Days of the month
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxWidth()) {
            items(days) { day ->
                val isSelected = day == selectedDate
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
                        Text(text = day.dayOfMonth.toString(), color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
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

