package com.luismi.adviento.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismi.adviento.BuildFlags
import com.luismi.adviento.data.LAST_DAY
import com.luismi.adviento.data.Settings
import com.luismi.adviento.i18n.Language
import com.luismi.adviento.i18n.LocalStrings

@Composable
fun SettingsScreen(
    settings: Settings,
    openedCount: Int,
    realUnlockedDay: Int,
    onBack: () -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onNotificationHourChange: (Int) -> Unit,
    onTestModeChange: (Boolean) -> Unit,
    onSimulatedDayChange: (Int) -> Unit,
    onLanguageChange: (Language) -> Unit,
    onSendTestNotification: () -> Unit,
    onResetOpened: () -> Unit,
) {
    val strings = LocalStrings.current
    val uriHandler = LocalUriHandler.current
    var confirmReset by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = AdventColors.Cream)
            }
            Text(strings.settings, color = AdventColors.Cream, fontSize = 24.sp, fontFamily = SerifFamily)
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Section(strings.progress) {
                Text(strings.doorsOpened(openedCount, LAST_DAY), color = AdventColors.Cream, fontSize = 15.sp)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { openedCount / LAST_DAY.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = AdventColors.Gold,
                    trackColor = AdventColors.SurfaceHigh,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
                if (BuildFlags.TEST_MODE_AVAILABLE) {
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { confirmReset = true },
                        enabled = openedCount > 0,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(strings.resetDays, color = if (openedCount > 0) AdventColors.Red else AdventColors.Muted)
                    }
                }
            }

            Section(strings.language) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Language.entries.forEach { language ->
                        LanguageOption(
                            label = language.label,
                            selected = settings.language == language,
                            onClick = { onLanguageChange(language) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Section(strings.notifications) {
                SwitchRow(
                    title = strings.dailyReminder,
                    subtitle = strings.dailyReminderHint,
                    checked = settings.notificationsEnabled,
                    onCheckedChange = onNotificationsChange,
                )
                if (settings.notificationsEnabled) {
                    Divider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.reminderTime, color = AdventColors.Cream, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onNotificationHourChange((settings.notificationHour + 23) % 24) }) {
                            Text("−", fontSize = 20.sp, color = AdventColors.Gold)
                        }
                        Text(
                            "${settings.notificationHour.toString().padStart(2, '0')}:00",
                            color = AdventColors.Cream,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        TextButton(onClick = { onNotificationHourChange((settings.notificationHour + 1) % 24) }) {
                            Text("+", fontSize = 20.sp, color = AdventColors.Gold)
                        }
                    }
                }
            }

            if (BuildFlags.TEST_MODE_AVAILABLE) {
                Section(strings.testMode, badge = settings.testMode) {
                    SwitchRow(
                        title = strings.enableTestMode,
                        subtitle = strings.enableTestModeHint,
                        checked = settings.testMode,
                        onCheckedChange = onTestModeChange,
                    )
                    if (settings.testMode) {
                        Divider()
                        var sliderValue by remember(settings.simulatedDay) { mutableFloatStateOf(settings.simulatedDay.toFloat()) }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.simulatedDay, color = AdventColors.Cream, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Text(
                                if (sliderValue.toInt() == 0) strings.noDay else strings.simulatedDayValue(sliderValue.toInt()),
                                color = AdventColors.Gold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { onSimulatedDayChange(sliderValue.toInt()) },
                            valueRange = 0f..LAST_DAY.toFloat(),
                            steps = LAST_DAY - 1,
                            colors = SliderDefaults.colors(
                                thumbColor = AdventColors.Gold,
                                activeTrackColor = AdventColors.Gold,
                                inactiveTrackColor = AdventColors.SurfaceHigh,
                                activeTickColor = AdventColors.Background.copy(alpha = 0.4f),
                                inactiveTickColor = AdventColors.Muted.copy(alpha = 0.4f),
                            ),
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(onClick = onSendTestNotification, modifier = Modifier.fillMaxWidth()) {
                            Text(strings.sendTestNotification, color = AdventColors.Cream)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            strings.realDate(realUnlockedDay),
                            color = AdventColors.Muted,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Column(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    strings.footer,
                    color = AdventColors.Muted.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    strings.madeBy,
                    color = AdventColors.Gold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { uriHandler.openUri("https://nikicoding.com") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(strings.resetDialogTitle) },
            text = { Text(strings.resetDialogText) },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onResetOpened() }) { Text(strings.resetConfirm, color = AdventColors.Red) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text(strings.cancel, color = AdventColors.Cream) }
            },
            containerColor = AdventColors.SurfaceHigh,
        )
    }
}

@Composable
private fun Section(title: String, badge: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Row(Modifier.padding(start = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title.uppercase(), color = AdventColors.Gold, fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
            if (badge) {
                Spacer(Modifier.size(8.dp))
                TestBadge()
            }
        }
        Column(
            Modifier.fillMaxWidth()
                .background(AdventColors.Surface, RoundedCornerShape(20.dp))
                .padding(18.dp),
            content = content,
        )
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = AdventColors.Cream, fontSize = 15.sp)
            Text(subtitle, color = AdventColors.Muted, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AdventColors.Background,
                checkedTrackColor = AdventColors.Gold,
                uncheckedThumbColor = AdventColors.Muted,
                uncheckedTrackColor = AdventColors.SurfaceHigh,
                uncheckedBorderColor = AdventColors.SurfaceHigh,
            ),
        )
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(14.dp)
    Text(
        label,
        modifier = modifier
            .clip(shape)
            .background(if (selected) AdventColors.Gold else AdventColors.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        color = if (selected) AdventColors.Background else AdventColors.Cream,
        fontSize = 15.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Divider() = HorizontalDivider(Modifier.padding(vertical = 12.dp), color = AdventColors.SurfaceHigh)
