package com.luismi.adviento

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.luismi.adviento.data.AdventClock
import com.luismi.adviento.data.AdventStore
import com.luismi.adviento.data.OpenedDay
import com.luismi.adviento.data.Settings
import com.luismi.adviento.data.Song
import com.luismi.adviento.data.SongCatalog
import com.luismi.adviento.i18n.Language
import com.luismi.adviento.i18n.LocalStrings
import com.luismi.adviento.i18n.stringsFor
import com.luismi.adviento.platform.PlatformServices
import com.luismi.adviento.ui.AdventColors
import com.luismi.adviento.ui.AdventTheme
import com.luismi.adviento.ui.CalendarScreen
import com.luismi.adviento.ui.DayScreen
import com.luismi.adviento.ui.SettingsScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Calendar : Screen
    data class Day(val day: Int) : Screen
    data object Settings : Screen
}

class AppNavigator {
    var screen: Screen by mutableStateOf(Screen.Calendar)
        private set

    fun open(target: Screen) {
        screen = target
    }

    /** Returns false when already on the calendar, so the platform can handle back itself. */
    fun back(): Boolean {
        if (screen == Screen.Calendar) return false
        screen = Screen.Calendar
        return true
    }
}

@Composable
fun App(services: PlatformServices, navigator: AppNavigator = remember { AppNavigator() }) {
    val store = remember { AdventStore(services.sqlDriver) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val pagerState = rememberPagerState { 2 }

    val settings: Settings? by store.settings.collectAsState(initial = null)
    val opened: Map<Int, OpenedDay> by store.openedDays.collectAsState(initial = emptyMap())
    val songs: Map<Int, Song> by produceState(emptyMap()) {
        value = runCatching { SongCatalog.load() }.getOrElse { emptyMap() }
    }

    // Re-evaluate the real date every minute so doors unlock at midnight while the app is open.
    var minuteTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            minuteTick++
        }
    }
    val unlockedDay = remember(settings, minuteTick) { settings?.let(AdventClock::unlockedDay) ?: 0 }
    val strings = stringsFor(settings?.language ?: Language.ES)

    // Keep the OS schedule in sync with the stored notification settings (texts depend on the language).
    LaunchedEffect(settings?.notificationsEnabled, settings?.notificationHour, settings?.language) {
        val s = settings ?: return@LaunchedEffect
        if (s.notificationsEnabled) services.notifier.schedule(AdventClock.upcomingReminders(s.notificationHour, strings))
        else services.notifier.cancelAll()
    }

    fun message(text: String) = scope.launch {
        snackbar.currentSnackbarData?.dismiss()
        snackbar.showSnackbar(text)
    }

    AdventTheme {
        CompositionLocalProvider(LocalStrings provides strings) {
            Box(Modifier.fillMaxSize().background(AdventColors.Background)) {
                val current = settings
                if (current != null) {
                    AnimatedContent(
                        targetState = navigator.screen,
                        transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith fadeOut() },
                        modifier = Modifier.fillMaxSize(),
                    ) { screen ->
                        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                            when (screen) {
                                Screen.Calendar -> CalendarScreen(
                                    songs = songs,
                                    opened = opened,
                                    unlockedDay = unlockedDay,
                                    testMode = BuildFlags.TEST_MODE_AVAILABLE && current.testMode,
                                    pagerState = pagerState,
                                    onOpenDay = { day ->
                                        scope.launch { store.markOpened(day) }
                                        navigator.open(Screen.Day(day))
                                    },
                                    onLockedDay = { day ->
                                        message(strings.lockedDay(day))
                                    },
                                    onSettings = { navigator.open(Screen.Settings) },
                                )

                                is Screen.Day -> DayScreen(
                                    day = screen.day,
                                    song = songs[screen.day],
                                    player = services.audioPlayer,
                                    onPlayStarted = { scope.launch { store.incrementPlayCount(screen.day) } },
                                    onBack = { navigator.back() },
                                )

                                Screen.Settings -> SettingsScreen(
                                    settings = current,
                                    openedCount = opened.size,
                                    realUnlockedDay = AdventClock.realUnlockedDay(),
                                    onBack = { navigator.back() },
                                    onNotificationsChange = { enabled ->
                                        scope.launch {
                                            if (!enabled) {
                                                store.setNotificationsEnabled(false)
                                            } else if (services.notifier.requestPermission()) {
                                                store.setNotificationsEnabled(true)
                                                message(strings.remindersOn)
                                            } else {
                                                message(strings.permissionDenied)
                                            }
                                        }
                                    },
                                    onNotificationHourChange = { hour -> scope.launch { store.setNotificationHour(hour) } },
                                    onTestModeChange = { enabled -> scope.launch { store.setTestMode(enabled) } },
                                    onSimulatedDayChange = { day -> scope.launch { store.setSimulatedDay(day) } },
                                    onLanguageChange = { language -> scope.launch { store.setLanguage(language) } },
                                    onSendTestNotification = {
                                        scope.launch {
                                            if (services.notifier.requestPermission()) {
                                                val day = unlockedDay.coerceIn(1, 25)
                                                services.notifier.sendTest(AdventClock.reminder(day, 0, strings), delaySeconds = 5)
                                                message(strings.testNotificationSent)
                                            } else {
                                                message(strings.permissionDeniedShort)
                                            }
                                        }
                                    },
                                    onResetOpened = {
                                        scope.launch {
                                            store.resetOpened()
                                            message(strings.daysReset)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                SnackbarHost(
                    snackbar,
                    Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing),
                )
            }
        }
    }
}
