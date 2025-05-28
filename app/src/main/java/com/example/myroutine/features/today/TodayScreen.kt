package com.example.myroutine.features.today

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myroutine.R


@Preview
@Composable
fun TodayScreen(viewModel: TodayViewModel = hiltViewModel()) {
    val routineList by viewModel.routines.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.today_todo),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(routineList) { index, item ->
                val textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { isChecked ->
                            viewModel.onRoutineChecked(item.id, isChecked)
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
}

