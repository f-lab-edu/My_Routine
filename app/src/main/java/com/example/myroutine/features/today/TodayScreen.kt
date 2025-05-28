package com.example.myroutine.features.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.myroutine.data.RoutineItem

@Composable
fun TodayScreen() {
    var routineList by remember {
        mutableStateOf(
            listOf(
                RoutineItem.mock("물 마시기"),
                RoutineItem.mock("운동하기"),
                RoutineItem.mock("책 읽기")
            )
        )
    }

    LazyColumn {
        itemsIndexed(routineList) { index, item ->
            val backgroundColor = Color.Transparent
            val textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(backgroundColor)
                    .clip(RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = {
                        routineList = routineList.toMutableList().apply {
                            this[index] = this[index].copy(isDone = it)
                        }
                    }
                )
                Text(
                    text = item.title,
                    textDecoration = textDecoration,
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

