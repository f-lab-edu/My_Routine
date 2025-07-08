package com.example.myroutine.common

import java.time.LocalDate
import javax.inject.Inject

class DateProviderImpl @Inject constructor() : DateProvider {
    override fun now(): LocalDate = LocalDate.now()
}