package com.truenorth.citizenshiptest.ui.screens

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeScreenTest {

    @Test
    fun daysUntilLabel_futureDate_showsDayCount() {
        val today = LocalDate(2027, 1, 1)
        val target = LocalDate(2027, 1, 6)
        assertEquals("5", daysUntilLabel(target, today))
    }

    @Test
    fun daysUntilLabel_today_showsTodayBanner() {
        val today = LocalDate(2027, 1, 1)
        assertEquals("Today!", daysUntilLabel(today, today))
    }

    @Test
    fun daysUntilLabel_pastDate_showsPassed() {
        val today = LocalDate(2027, 1, 6)
        val target = LocalDate(2027, 1, 1)
        assertEquals("Passed", daysUntilLabel(target, today))
    }

    @Test
    fun daysUntilLabel_acrossAYearBoundary() {
        val today = LocalDate(2026, 12, 30)
        val target = LocalDate(2027, 1, 2)
        assertEquals("3", daysUntilLabel(target, today))
    }
}
