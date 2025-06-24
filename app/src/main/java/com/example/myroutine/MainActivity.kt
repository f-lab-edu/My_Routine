package com.example.myroutine

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import com.example.myroutine.common.MainScreen
import com.example.myroutine.ui.theme.MyRoutineTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyRoutineTheme {
                val context = this
                var showPermissionDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = getSystemService(AlarmManager::class.java)
                        if (alarmManager?.canScheduleExactAlarms() == false) {
                            showPermissionDialog = true
                        }
                    } else {
                        // API 30 이하 버전은 권한 필요 없음, 다이얼로그 띄우지 않음
                        showPermissionDialog = false
                    }
                }

                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { /* 빈 처리로 닫히지 않도록 설정 */ },
                        confirmButton = {
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    startActivity(intent)
                                }
                                showPermissionDialog = false
                            }) {
                                Text(text = context.getString(R.string.settings_open))
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showPermissionDialog = false }) {
                                Text(text = context.getString(R.string.cancel))
                            }
                        },
                        title = { Text(text = context.getString(R.string.permission_dialog_title)) },
                        text = { Text(text = context.getString(R.string.permission_dialog_text)) },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    )
                }

                MainScreen()
            }
        }
    }
}
