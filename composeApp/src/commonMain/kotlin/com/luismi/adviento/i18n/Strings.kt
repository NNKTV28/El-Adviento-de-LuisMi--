package com.luismi.adviento.i18n

import androidx.compose.runtime.staticCompositionLocalOf

enum class Language(val code: String, val label: String) {
    ES("es", "Español"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: ES
    }
}

/** Every user-facing text, switchable at runtime from the settings screen. */
interface Strings {
    val calendarTitle: String
    val settings: String
    val back: String
    val swipeToChristmas: String
    val backToDays: String
    val christmas: String
    val christmasLocked: String
    val christmasAvailable: String
    fun lockedDay(day: Int): String

    fun dayLabel(day: Int): String
    val songUnavailable: String
    val songUnavailableHint: String
    val play: String
    val merryChristmas: String

    val progress: String
    fun doorsOpened(count: Int, total: Int): String
    val resetDays: String
    val resetDialogTitle: String
    val resetDialogText: String
    val resetConfirm: String
    val cancel: String
    val daysReset: String

    val language: String

    val notifications: String
    val dailyReminder: String
    val dailyReminderHint: String
    val reminderTime: String
    val remindersOn: String
    val permissionDenied: String
    val permissionDeniedShort: String

    val testMode: String
    val enableTestMode: String
    val enableTestModeHint: String
    val simulatedDay: String
    val noDay: String
    fun simulatedDayValue(day: Int): String
    val sendTestNotification: String
    val testNotificationSent: String
    fun realDate(unlockedDay: Int): String
    val footer: String
    val madeBy: String

    fun notificationTitle(day: Int): String
    fun notificationBody(day: Int): String
}

object SpanishStrings : Strings {
    override val calendarTitle = "Calendario de Adviento"
    override val settings = "Ajustes"
    override val back = "Volver"
    override val swipeToChristmas = "Desliza para el día 25  ↓"
    override val backToDays = "↑  Días 1 – 24"
    override val christmas = "NAVIDAD"
    override val christmasLocked = "Se abre el 25 de diciembre"
    override val christmasAvailable = "Toca para abrir tu regalo"
    override fun lockedDay(day: Int) =
        if (day == 25) "Paciencia… el regalo se abre el 25 de diciembre" else "Paciencia… esta puerta se abre el $day de diciembre"

    override fun dayLabel(day: Int) = if (day == 25) "25 · NAVIDAD" else "DÍA $day"
    override val songUnavailable = "Canción no disponible"
    override val songUnavailableHint = "Ejecuta tools/download_songs.py"
    override val play = "Reproducir"
    override val merryChristmas = "¡Feliz Navidad!"

    override val progress = "Progreso"
    override fun doorsOpened(count: Int, total: Int) = "$count de $total puertas abiertas"
    override val resetDays = "Reiniciar días"
    override val resetDialogTitle = "¿Reiniciar los días?"
    override val resetDialogText = "Todas las puertas vuelven a cerrarse y se borra el progreso guardado. Las canciones no se tocan."
    override val resetConfirm = "Reiniciar"
    override val cancel = "Cancelar"
    override val daysReset = "Días reiniciados"

    override val language = "Idioma"

    override val notifications = "Notificaciones"
    override val dailyReminder = "Recordatorio diario"
    override val dailyReminderHint = "Un aviso cada mañana del 1 al 25 de diciembre"
    override val reminderTime = "Hora del aviso"
    override val remindersOn = "Recordatorios activados"
    override val permissionDenied = "Permiso de notificaciones denegado. Actívalo en Ajustes del sistema."
    override val permissionDeniedShort = "Permiso de notificaciones denegado"

    override val testMode = "Modo test"
    override val enableTestMode = "Activar modo test"
    override val enableTestModeHint = "Ignora la fecha real y simula el día que elijas"
    override val simulatedDay = "Día simulado"
    override val noDay = "Ninguno"
    override fun simulatedDayValue(day: Int) = "$day dic"
    override val sendTestNotification = "Enviar notificación de prueba (5 s)"
    override val testNotificationSent = "Notificación en 5 segundos"
    override fun realDate(unlockedDay: Int) =
        if (unlockedDay == 0) "Fecha real: todavía no hay puertas disponibles"
        else "Fecha real: puertas disponibles hasta el día $unlockedDay"
    override val footer = "Hecho con cariño · El Sol de México"
    override val madeBy = "Hecho por NikiCoding"

    override fun notificationTitle(day: Int) = if (day == 25) "¡Feliz Navidad!" else "Día $day de adviento"
    override fun notificationBody(day: Int) =
        if (day == 25) "Se abrió la última puerta. Luis Miguel te espera."
        else "Tu canción de hoy ya está lista. Abre la puerta $day."
}

object EnglishStrings : Strings {
    override val calendarTitle = "Advent Calendar"
    override val settings = "Settings"
    override val back = "Back"
    override val swipeToChristmas = "Swipe down for day 25  ↓"
    override val backToDays = "↑  Days 1 – 24"
    override val christmas = "CHRISTMAS"
    override val christmasLocked = "Opens on December 25"
    override val christmasAvailable = "Tap to open your gift"
    override fun lockedDay(day: Int) =
        if (day == 25) "Patience… the gift opens on December 25" else "Patience… this door opens on December $day"

    override fun dayLabel(day: Int) = if (day == 25) "25 · CHRISTMAS" else "DAY $day"
    override val songUnavailable = "Song not available"
    override val songUnavailableHint = "Run tools/download_songs.py"
    override val play = "Play"
    override val merryChristmas = "Merry Christmas!"

    override val progress = "Progress"
    override fun doorsOpened(count: Int, total: Int) = "$count of $total doors opened"
    override val resetDays = "Reset days"
    override val resetDialogTitle = "Reset all days?"
    override val resetDialogText = "Every door closes again and saved progress is deleted. The songs stay."
    override val resetConfirm = "Reset"
    override val cancel = "Cancel"
    override val daysReset = "Days reset"

    override val language = "Language"

    override val notifications = "Notifications"
    override val dailyReminder = "Daily reminder"
    override val dailyReminderHint = "A notification every morning from December 1 to 25"
    override val reminderTime = "Reminder time"
    override val remindersOn = "Reminders turned on"
    override val permissionDenied = "Notification permission denied. Turn it on in system settings."
    override val permissionDeniedShort = "Notification permission denied"

    override val testMode = "Test mode"
    override val enableTestMode = "Enable test mode"
    override val enableTestModeHint = "Ignores the real date and simulates the day you pick"
    override val simulatedDay = "Simulated day"
    override val noDay = "None"
    override fun simulatedDayValue(day: Int) = "Dec $day"
    override val sendTestNotification = "Send test notification (5 s)"
    override val testNotificationSent = "Notification in 5 seconds"
    override fun realDate(unlockedDay: Int) =
        if (unlockedDay == 0) "Real date: no doors available yet"
        else "Real date: doors available up to day $unlockedDay"
    override val footer = "Made with love · El Sol de México"
    override val madeBy = "Made by NikiCoding"

    override fun notificationTitle(day: Int) = if (day == 25) "Merry Christmas!" else "Advent day $day"
    override fun notificationBody(day: Int) =
        if (day == 25) "The last door is open. Luis Miguel is waiting for you."
        else "Today's song is ready. Open door $day."
}

fun stringsFor(language: Language): Strings = when (language) {
    Language.ES -> SpanishStrings
    Language.EN -> EnglishStrings
}

val LocalStrings = staticCompositionLocalOf<Strings> { SpanishStrings }
