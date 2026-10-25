package com.luismi.adviento.data

import com.luismi.adviento.BuildFlags
import com.luismi.adviento.i18n.Strings
import com.luismi.adviento.platform.Reminder
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

const val LAST_DAY = 25

object AdventClock {
    private fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    /**
     * Highest door that may be opened today: December N unlocks door N,
     * everything stays open after Christmas and through January.
     */
    fun realUnlockedDay(today: LocalDate = now().date): Int = when (today.month) {
        Month.DECEMBER -> minOf(today.day, LAST_DAY)
        Month.JANUARY -> LAST_DAY
        else -> 0
    }

    fun unlockedDay(settings: Settings): Int =
        if (BuildFlags.TEST_MODE_AVAILABLE && settings.testMode) settings.simulatedDay.coerceIn(0, LAST_DAY) else realUnlockedDay()

    /** Reminders for every door whose unlock moment (December N at [hour]:00) is still ahead. */
    fun upcomingReminders(hour: Int, strings: Strings): List<Reminder> {
        val now = now()
        val zone = TimeZone.currentSystemDefault()
        val year = if (now.month == Month.DECEMBER && now.day > LAST_DAY) now.year + 1 else now.year
        return (1..LAST_DAY).mapNotNull { day ->
            val at = LocalDateTime(LocalDate(year, Month.DECEMBER, day), LocalTime(hour, 0))
            if (at <= now) return@mapNotNull null
            reminder(day, at.toInstant(zone).toEpochMilliseconds(), strings)
        }
    }

    fun reminder(day: Int, epochMillis: Long, strings: Strings) =
        Reminder(day, epochMillis, strings.notificationTitle(day), strings.notificationBody(day))
}
