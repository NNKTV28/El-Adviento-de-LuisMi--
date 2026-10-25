package com.luismi.adviento.platform

import com.luismi.adviento.data.LAST_DAY
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume
import kotlin.time.Clock

class IosNotifier : Notifier {
    private val center get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, _ -> continuation.resume(granted) }
    }

    override fun schedule(reminders: List<Reminder>) {
        cancelAll()
        val now = Clock.System.now().toEpochMilliseconds()
        reminders.forEach { reminder ->
            val seconds = (reminder.epochMillis - now) / 1000.0
            if (seconds > 0) post(identifier(reminder.day), reminder, seconds)
        }
    }

    override fun cancelAll() {
        center.removePendingNotificationRequestsWithIdentifiers((1..LAST_DAY).map(::identifier))
    }

    override fun sendTest(reminder: Reminder, delaySeconds: Int) {
        post("advent-test", reminder, delaySeconds.toDouble())
    }

    private fun post(id: String, reminder: Reminder, afterSeconds: Double) {
        val content = UNMutableNotificationContent().apply {
            setTitle(reminder.title)
            setBody(reminder.body)
            setSound(UNNotificationSound.defaultSound())
        }
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(afterSeconds, repeats = false)
        center.addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(id, content, trigger),
            withCompletionHandler = null,
        )
    }

    private fun identifier(day: Int) = "advent-day-$day"
}
