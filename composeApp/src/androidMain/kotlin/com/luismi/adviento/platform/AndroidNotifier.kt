package com.luismi.adviento.platform

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.luismi.adviento.MainActivity
import com.luismi.adviento.R
import com.luismi.adviento.data.AdventClock
import com.luismi.adviento.data.AdventStore
import com.luismi.adviento.data.LAST_DAY
import com.luismi.adviento.i18n.stringsFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AndroidNotifier(private val context: Context) : Notifier {
    /** Set by the visible activity, which owns the runtime-permission launcher. */
    var permissionRequester: (suspend () -> Boolean)? = null

    private val alarms get() = context.getSystemService(AlarmManager::class.java)

    override suspend fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 33 || hasPermission()) {
            return NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
        return permissionRequester?.invoke() ?: false
    }

    private fun hasPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    override fun schedule(reminders: List<Reminder>) {
        cancelAll()
        // Inexact alarms are fine for a morning reminder and need no special permission.
        reminders.forEach { alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it.epochMillis, alarmIntent(it)) }
    }

    override fun cancelAll() {
        // PendingIntent matching ignores extras, so empty texts are enough to find each alarm.
        (1..LAST_DAY).forEach { alarms.cancel(alarmIntent(Reminder(it, 0, "", ""))) }
    }

    override fun sendTest(reminder: Reminder, delaySeconds: Int) {
        Handler(Looper.getMainLooper()).postDelayed({ show(context, reminder.day, reminder.title, reminder.body) }, delaySeconds * 1000L)
    }

    private fun alarmIntent(reminder: Reminder): PendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.day,
        Intent(context, AdventAlarmReceiver::class.java)
            .putExtra(EXTRA_DAY, reminder.day)
            .putExtra(EXTRA_TITLE, reminder.title)
            .putExtra(EXTRA_BODY, reminder.body),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val EXTRA_DAY = "day"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        private const val CHANNEL_ID = "advent_daily"

        fun show(context: Context, day: Int, title: String, body: String) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Luis Miguel", NotificationManager.IMPORTANCE_DEFAULT)
            )
            val openApp = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_sun)
                .setColor(0xFFE2B866.toInt())
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
            if (Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                manager.notify(day, notification)
            }
        }
    }
}

class AdventAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val day = intent.getIntExtra(AndroidNotifier.EXTRA_DAY, 0)
        val title = intent.getStringExtra(AndroidNotifier.EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(AndroidNotifier.EXTRA_BODY) ?: return
        if (day in 1..LAST_DAY) AndroidNotifier.show(context, day, title, body)
    }
}

/** Alarms are wiped on reboot, so put them back if reminders are enabled. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val services = AndroidServices.get(context)
                val settings = AdventStore(services.sqlDriver).currentSettings()
                if (settings.notificationsEnabled) {
                    services.notifier.schedule(
                        AdventClock.upcomingReminders(settings.notificationHour, stringsFor(settings.language))
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}
