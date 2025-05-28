package com.example.myroutine

import BottomNavigationBar
import android.app.Activity
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myroutine.ui.screens.TodayScreen

private const val TAG_MAIN_SCREEN = "MainScreen"

object Routes {
    const val TODAY = "today"
    const val CALENDAR = "calendar"
    const val REPORT = "report"
    const val SETTINGS = "settings"
    const val ADD = "add"
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val onBackPressedDispatcher = (context as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher

    if (onBackPressedDispatcher != null) {
        MyRoutineApp(onBackPressedDispatcher = onBackPressedDispatcher)
    } else {
        Log.e(TAG_MAIN_SCREEN, "OnBackPressedDispatcher를 얻을 수 없습니다.")
    }
}

@Composable
fun MyRoutineApp(onBackPressedDispatcher: OnBackPressedDispatcher) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD) }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TODAY) {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = Routes.TODAY
                ) {
                    TodayScreen()
                }
            }
            composable(Routes.CALENDAR) {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = Routes.CALENDAR
                ) {
                    Text("Calendar")
                }
            }
            composable(Routes.REPORT) {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = Routes.REPORT
                ) {
                    Text("Report")
                }
            }
            composable(Routes.SETTINGS) {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = Routes.SETTINGS
                ) {
                    Text("Settings")
                }
            }
            composable(Routes.ADD) {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = Routes.ADD
                ) {
                    Text("Add Routine")
                }
            }
        }
    }
}

@Composable
fun BackPressHandler(
    onBackPressedDispatcher: OnBackPressedDispatcher,
    navController: NavHostController,
    route: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showDialog by remember { mutableStateOf(false) }

    val onBackPressedCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (route == "add") {
                    navController.popBackStack() // 'add' 화면에서는 이전 화면으로 돌아감
                } else {
                    showDialog = true // 나머지 화면에서는 다이얼로그 표시
                }
            }
        }
    }

    DisposableEffect(onBackPressedDispatcher) {
        onBackPressedDispatcher.addCallback(onBackPressedCallback)
        onDispose {
            onBackPressedCallback.remove()
        }
    }

    content() // 원래 화면 콘텐츠 표시

    // 앱 종료 확인 다이얼로그
    if (showDialog && route != Routes.ADD) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.quit_app)) },
            text = { Text(stringResource(R.string.q_quite_app)) },
            confirmButton = {
                Button(onClick = {
                    activity?.finish() // 앱 종료
                }) {
                    Text(stringResource(R.string.quit))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
