package com.example.myroutine.features.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myroutine.R

@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.report),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.WEEKLY)) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.selectedPeriod == PeriodType.WEEKLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.selectedPeriod == PeriodType.WEEKLY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.report_weekly),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onEvent(ReportEvent.SelectPeriod(PeriodType.MONTHLY)) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.selectedPeriod == PeriodType.MONTHLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.selectedPeriod == PeriodType.MONTHLY) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.report_monthly),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ReportCard(
            title = stringResource(R.string.report_completion_rate),
            value = "${(state.completionRate * 100).toInt()}%"
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReportCard(
            title = stringResource(R.string.report_most_kept_routine),
            value = if (state.mostKeptRoutine.isNotEmpty()) {
                stringResource(R.string.report_most_kept_routine_with_count, state.mostKeptRoutine, state.mostKeptRoutineCount)
            } else {
                stringResource(R.string.report_na)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        ReportCard(
            title = stringResource(R.string.report_most_missed_routine),
            value = if (state.mostMissedRoutine.isNotEmpty()) {
                stringResource(R.string.report_most_missed_routine_with_count, state.mostMissedRoutine, state.mostMissedRoutineCount)
            } else {
                stringResource(R.string.report_na)
            }
        )
    }
}

@Composable
fun ReportCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start
            )
        }
    }
}