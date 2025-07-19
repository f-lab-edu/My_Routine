package com.example.myroutine

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

                // 1. SCHEDULE_EXACT_ALARM 권한 안내 다이얼로그 상태
                var showExactAlarmDialog by remember { mutableStateOf(false) }

                // 2. POST_NOTIFICATIONS 권한 상태
                var hasNotificationPermission by remember { mutableStateOf(true) } // 기본 true로 시작

                // 권한 요청 런처
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { granted -> hasNotificationPermission = granted }
                )

                LaunchedEffect(Unit) {
                    // SCHEDULE_EXACT_ALARM 권한 없으면 다이얼로그 띄우기
                    showExactAlarmDialog = !PermissionUtils.canScheduleExactAlarms(context)

                    // Android 13 이상이면 알림 권한 요청
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                // 1. SCHEDULE_EXACT_ALARM 권한 안내 다이얼로그
                if (showExactAlarmDialog) {
                    AlertDialog(
                        onDismissRequest = { /* 닫히지 않도록 빈 처리 */ },
                        confirmButton = {
                            Button(onClick = {
                                PermissionUtils.getScheduleExactAlarmIntent()?.let { intent ->
                                    startActivity(intent)
                                }
                                showExactAlarmDialog = false
                            }) {
                                Text(text = context.getString(R.string.settings_open))
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showExactAlarmDialog = false }) {
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

                // 2. POST_NOTIFICATIONS 권한 거부 시 안내 다이얼로그 (optional)
                if (!hasNotificationPermission) {
                    AlertDialog(
                        onDismissRequest = { /* 닫히지 않도록 빈 처리 */ },
                        confirmButton = {
                            Button(onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) {
                                Text(text = context.getString(R.string.permission_allow))
                            }
                        },
                        dismissButton = {
                            Button(onClick = { /* 필요 시 앱 종료 등 처리 가능 */ }) {
                                Text(text = context.getString(R.string.cancel))
                            }
                        },
                        title = { Text(text = context.getString(R.string.notification_permission_title)) },
                        text = { Text(text = context.getString(R.string.notification_permission_text)) },
                        properties = DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false
                        )
                    )
                }

                // 메인 화면
                MainScreen()
            }
        }
    }
}
