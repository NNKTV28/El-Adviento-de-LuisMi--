package com.luismi.adviento

import androidx.compose.ui.window.ComposeUIViewController
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.luismi.adviento.data.AdventStore
import com.luismi.adviento.db.AdventDb
import com.luismi.adviento.platform.IosAudioPlayer
import com.luismi.adviento.platform.IosNotifier
import com.luismi.adviento.platform.PlatformServices
import platform.UIKit.UIViewController

private val services by lazy {
    PlatformServices(
        sqlDriver = NativeSqliteDriver(AdventDb.Schema, AdventStore.DATABASE_NAME),
        audioPlayer = IosAudioPlayer(),
        notifier = IosNotifier(),
    )
}

@Suppress("FunctionName", "unused") // Called from Swift.
fun MainViewController(): UIViewController = ComposeUIViewController { App(services) }
