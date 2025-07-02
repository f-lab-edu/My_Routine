
package com.example.myroutine.features.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import java.time.YearMonth

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen() {
    val currentMonth = remember { YearMonth.now() }
    val pagerState = rememberPagerState(initialPage = 0) {
        // Infinite scroll for months
        Int.MAX_VALUE
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Calendar Header (Year and Month selection)
        Text(text = "Year Month Selector (Placeholder)")

        // Calendar Grid (Swipeable)
        HorizontalPager(state = pagerState) { page ->
            val monthOffset = page - pagerState.initialPage
            val displayMonth = currentMonth.plusMonths(monthOffset.toLong())
            CalendarGrid(displayMonth = displayMonth)
        }

        // Routine List for selected date (Placeholder)
        Text(text = "Routine List for Selected Date (Placeholder)")
    }
}

@Composable
fun CalendarGrid(displayMonth: YearMonth) {
    Column {
        Text(text = "Calendar Grid for ${displayMonth.year}년 ${displayMonth.monthValue}월")
        // TODO: Implement actual calendar grid with days
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    CalendarScreen()
}

