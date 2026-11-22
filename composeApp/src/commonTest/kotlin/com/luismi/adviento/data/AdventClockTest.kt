package com.luismi.adviento.data

import com.luismi.adviento.BuildFlags
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals

class AdventClockTest {
    private fun day(month: Month, day: Int) = LocalDate(2026, month, day)

    @Test
    fun nothingUnlocksBeforeDecember() {
        assertEquals(0, AdventClock.realUnlockedDay(day(Month.NOVEMBER, 30)))
        assertEquals(0, AdventClock.realUnlockedDay(day(Month.OCTOBER, 25)))
    }

    @Test
    fun decemberUnlocksOneDoorPerDay() {
        assertEquals(1, AdventClock.realUnlockedDay(day(Month.DECEMBER, 1)))
        assertEquals(13, AdventClock.realUnlockedDay(day(Month.DECEMBER, 13)))
        assertEquals(LAST_DAY, AdventClock.realUnlockedDay(day(Month.DECEMBER, 25)))
    }

    @Test
    fun everythingStaysOpenAfterChristmasAndInJanuary() {
        assertEquals(LAST_DAY, AdventClock.realUnlockedDay(day(Month.DECEMBER, 31)))
        assertEquals(LAST_DAY, AdventClock.realUnlockedDay(LocalDate(2027, Month.JANUARY, 15)))
        assertEquals(0, AdventClock.realUnlockedDay(LocalDate(2027, Month.FEBRUARY, 1)))
    }

    @Test
    fun simulatedDayOnlyAppliesWhenTestModeShips() {
        val settings = Settings(testMode = true, simulatedDay = 7)
        val expected = if (BuildFlags.TEST_MODE_AVAILABLE) 7 else AdventClock.realUnlockedDay()
        assertEquals(expected, AdventClock.unlockedDay(settings))
    }

    @Test
    fun simulatedDayIsClamped() {
        if (!BuildFlags.TEST_MODE_AVAILABLE) return
        assertEquals(LAST_DAY, AdventClock.unlockedDay(Settings(testMode = true, simulatedDay = 40)))
        assertEquals(0, AdventClock.unlockedDay(Settings(testMode = true, simulatedDay = -3)))
    }
}
