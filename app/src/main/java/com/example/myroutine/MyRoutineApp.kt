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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myroutine.ui.screens.TodayScreen

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val onBackPressedDispatcher = (context as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher

    if (onBackPressedDispatcher != null) {
        MyRoutineApp(onBackPressedDispatcher = onBackPressedDispatcher)
    } else {
        Log.e("MainScreen", "OnBackPressedDispatcher를 얻을 수 없습니다.")
    }
}

@Composable
fun MyRoutineApp(onBackPressedDispatcher: OnBackPressedDispatcher) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("add") }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("today") {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = "today"
                ) {
                    TodayScreen()
                }
            }
            composable("calendar") {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = "calendar"
                ) {
                    Text("Calendar")
                }
            }
            composable("report") {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = "report"
                ) {
                    Text("Report")
                }
            }
            composable("settings") {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = "settings"
                ) {
                    Text("Settings")
                }
            }
            composable("add") {
                BackPressHandler(
                    onBackPressedDispatcher = onBackPressedDispatcher,
                    navController = navController,
                    route = "add"
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
    if (showDialog && route != "add") {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("앱 종료") },
            text = { Text("앱을 종료하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    activity?.finish() // 앱 종료
                }) {
                    Text("종료")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}
