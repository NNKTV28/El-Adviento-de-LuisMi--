package com.luismi.adviento.platform

import app.cash.sqldelight.db.SqlDriver

/** Native audio playback (MediaPlayer on Android, AVAudioPlayer on iOS). */
interface AudioPlayer {
    val isPlaying: Boolean
    val positionMs: Long
    val durationMs: Long

    /** Loads a bundled file from its platform URI. Replaces whatever was loaded before. */
    fun load(uri: String)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun stop()
}

/** A reminder that fires on the morning a door unlocks. Texts are already in the chosen language. */
data class Reminder(val day: Int, val epochMillis: Long, val title: String, val body: String)

/** Native local notifications (NotificationManager + AlarmManager / UNUserNotificationCenter). */
interface Notifier {
    /** Asks the OS for permission to post notifications. Returns true when granted. */
    suspend fun requestPermission(): Boolean

    /** Replaces every pending advent reminder with [reminders]. */
    fun schedule(reminders: List<Reminder>)

    fun cancelAll()

    /** Test mode helper: posts [reminder] after [delaySeconds], ignoring its timestamp. */
    fun sendTest(reminder: Reminder, delaySeconds: Int)
}

class PlatformServices(
    val sqlDriver: SqlDriver,
    val audioPlayer: AudioPlayer,
    val notifier: Notifier,
)
