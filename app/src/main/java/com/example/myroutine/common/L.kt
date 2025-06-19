package com.example.myroutine.common

object L {
    var DEBUG = true // 테스트 환경에서는 false로 설정 가능

    fun d(tag: String, message: String) {
        if (DEBUG) android.util.Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (DEBUG) android.util.Log.w(tag, message)
    }

    fun i(tag: String, message: String) {
        if (DEBUG) android.util.Log.i(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG) android.util.Log.e(tag, message, throwable)
    }

    fun v(tag: String, message: String) {
        if (DEBUG) android.util.Log.v(tag, message)
    }

    fun wtf(tag: String, message: String, throwable: Throwable? = null) {
        if (DEBUG) android.util.Log.wtf(tag, message, throwable)
    }
}