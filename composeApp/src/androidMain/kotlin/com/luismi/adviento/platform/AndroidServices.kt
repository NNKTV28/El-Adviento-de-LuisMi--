package com.luismi.adviento.platform

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.luismi.adviento.data.AdventStore
import com.luismi.adviento.db.AdventDb

/** Process-wide singletons, shared by the activity and the broadcast receivers. */
object AndroidServices {
    @Volatile private var services: PlatformServices? = null

    fun get(context: Context): PlatformServices = services ?: synchronized(this) {
        services ?: PlatformServices(
            sqlDriver = AndroidSqliteDriver(AdventDb.Schema, context.applicationContext, AdventStore.DATABASE_NAME),
            audioPlayer = AndroidAudioPlayer(context.applicationContext),
            notifier = AndroidNotifier(context.applicationContext),
        ).also { services = it }
    }

    fun notifier(context: Context): AndroidNotifier = get(context).notifier as AndroidNotifier
}
