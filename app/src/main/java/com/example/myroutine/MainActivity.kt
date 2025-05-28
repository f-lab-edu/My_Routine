package com.example.myroutine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
                MainScreen() // 여기서 호출 (컴포저블 함수 안)
            }
        }
    }
}