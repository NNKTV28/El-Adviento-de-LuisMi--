package com.luismi.adviento.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.luismi.adviento.db.AdventDb
import com.luismi.adviento.i18n.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

data class OpenedDay(val day: Int, val openedAtMillis: Long, val playCount: Int)

data class Settings(
    val testMode: Boolean = false,
    /** Day the calendar pretends it is while test mode is on (0 = nothing open, 25 = everything). */
    val simulatedDay: Int = 25,
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 9,
    val language: Language = Language.ES,
)

/** Local SQLite persistence for opened days and settings. */
class AdventStore(driver: SqlDriver) {
    private val queries = AdventDb(driver).adventQueries

    val openedDays: Flow<Map<Int, OpenedDay>> = queries.selectOpened()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { rows -> rows.associate { it.day.toInt() to OpenedDay(it.day.toInt(), it.opened_at, it.play_count.toInt()) } }

    val settings: Flow<Settings> = queries.selectSettings()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { rows -> rows.associate { it.key to it.setting_value }.toSettings() }

    suspend fun currentSettings(): Settings = withContext(Dispatchers.Default) {
        queries.selectSettings().executeAsList().associate { it.key to it.setting_value }.toSettings()
    }

    suspend fun markOpened(day: Int) = io {
        queries.markOpened(day.toLong(), Clock.System.now().toEpochMilliseconds())
    }

    suspend fun incrementPlayCount(day: Int) = io { queries.incrementPlayCount(day.toLong()) }

    suspend fun resetOpened() = io { queries.deleteAllOpened() }

    suspend fun setTestMode(enabled: Boolean) = put(KEY_TEST_MODE, enabled.toString())
    suspend fun setSimulatedDay(day: Int) = put(KEY_SIMULATED_DAY, day.toString())
    suspend fun setNotificationsEnabled(enabled: Boolean) = put(KEY_NOTIFICATIONS, enabled.toString())
    suspend fun setNotificationHour(hour: Int) = put(KEY_NOTIFICATION_HOUR, hour.toString())
    suspend fun setLanguage(language: Language) = put(KEY_LANGUAGE, language.code)

    private suspend fun put(key: String, value: String) = io { queries.putSetting(key, value) }

    private suspend fun io(block: () -> Unit) = withContext(Dispatchers.Default) { block() }

    private fun Map<String, String>.toSettings() = Settings(
        testMode = this[KEY_TEST_MODE]?.toBoolean() ?: false,
        simulatedDay = this[KEY_SIMULATED_DAY]?.toIntOrNull() ?: 25,
        notificationsEnabled = this[KEY_NOTIFICATIONS]?.toBoolean() ?: false,
        notificationHour = this[KEY_NOTIFICATION_HOUR]?.toIntOrNull() ?: 9,
        language = Language.fromCode(this[KEY_LANGUAGE]),
    )

    companion object {
        const val DATABASE_NAME = "adviento.db"
        private const val KEY_TEST_MODE = "test_mode"
        private const val KEY_SIMULATED_DAY = "simulated_day"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_NOTIFICATION_HOUR = "notification_hour"
        private const val KEY_LANGUAGE = "language"
    }
}
