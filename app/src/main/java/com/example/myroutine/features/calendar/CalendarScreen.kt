
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen() {
    val currentMonth = remember { YearMonth.now() }
    val pagerState = rememberPagerState(initialPage = Int.MAX_VALUE / 2) {
        Int.MAX_VALUE
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar Header (Year and Month selection)
        Text(text = "Year Month Selector (Placeholder)")

        // Calendar Grid (Swipeable)
        HorizontalPager(state = pagerState) { page ->
            val monthOffset = page - (Int.MAX_VALUE / 2)
            val displayMonth = currentMonth.plusMonths(monthOffset.toLong())
            CalendarGrid(displayMonth = displayMonth)
        }

        // Routine List for selected date (Placeholder)
        Text(text = "Routine List for Selected Date (Placeholder)")
    }
}

@Composable
fun CalendarGrid(displayMonth: YearMonth) {
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
                Box(
                    modifier = Modifier.padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Text(text = day.dayOfMonth.toString())
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

