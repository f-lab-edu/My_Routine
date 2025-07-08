package com.example.myroutine.common

import java.time.LocalDate

interface DateProvider {
    fun now(): LocalDate
}