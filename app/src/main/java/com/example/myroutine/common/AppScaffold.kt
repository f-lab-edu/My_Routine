package com.example.myroutine.common

import BottomNavigationBar
import android.app.Activity
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
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
import com.example.myroutine.R
import com.example.myroutine.common.LogTags.MAIN_SCREEN
import com.example.myroutine.features.add.AddRoutineScreen
import com.example.myroutine.features.today.TodayScreen


@Composable
fun MainScreen() {
    val context = LocalContext.current
    val onBackPressedDispatcher = (context as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher

    if (onBackPressedDispatcher != null) {
        AppScaffold(onBackPressedDispatcher = onBackPressedDispatcher)
    } else {
        Log.e(MAIN_SCREEN, "OnBackPressedDispatcher를 얻을 수 없습니다.")
    }
}

@Composable
fun AppScaffold(onBackPressedDispatcher: OnBackPressedDispatcher) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.systemBars),
        contentWindowInsets = WindowInsets.systemBars, // 시스템 패딩 위임
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD) },
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) { innerPadding ->

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            // 👇 상태바 높이만큼 배경을 수동으로 그려줌
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                    .background(MaterialTheme.colorScheme.background)
            )

            NavHost(
                navController = navController,
                startDestination = Routes.TODAY,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.TODAY) {
                    BackPressHandler(
                        onBackPressedDispatcher,
                        navController,
                        Routes.TODAY
                    ) {
                        TodayScreen()
                    }
                }
                composable(Routes.CALENDAR) {
                    BackPressHandler(
                        onBackPressedDispatcher,
                        navController,
                        Routes.CALENDAR
                    ) {
                        Text("Calendar")
                    }
                }
                composable(Routes.REPORT) {
                    BackPressHandler(
                        onBackPressedDispatcher,
                        navController,
                        Routes.REPORT
                    ) {
                        Text("Report")
                    }
                }
                composable(Routes.SETTINGS) {
                    BackPressHandler(
                        onBackPressedDispatcher,
                        navController,
                        Routes.SETTINGS
                    ) {
                        Text("Settings")
                    }
                }
                composable(Routes.ADD) {
                    BackPressHandler(
                        onBackPressedDispatcher,
                        navController,
                        Routes.ADD
                    ) {
                        AddRoutineScreen(
                            onBack = { navController.popBackStack() },
                            onSave = {
                                //TODO: 저장 로직 구현
                                navController.popBackStack()
                            }
                        )
                    }
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
                if (route == Routes.ADD) {
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
