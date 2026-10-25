package com.luismi.adviento.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismi.adviento.data.LAST_DAY
import com.luismi.adviento.data.OpenedDay
import com.luismi.adviento.data.Song
import com.luismi.adviento.i18n.LocalStrings
import kotlinx.coroutines.launch

enum class DoorState { Locked, Available, Opened }

private const val COLUMNS = 4
private const val ROWS = 6

@Composable
fun CalendarScreen(
    songs: Map<Int, Song>,
    opened: Map<Int, OpenedDay>,
    unlockedDay: Int,
    testMode: Boolean,
    pagerState: PagerState,
    onOpenDay: (Int) -> Unit,
    onLockedDay: (Int) -> Unit,
    onSettings: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    fun stateOf(day: Int) = when {
        day in opened -> DoorState.Opened
        day <= unlockedDay -> DoorState.Available
        else -> DoorState.Locked
    }
    fun tap(day: Int) = if (stateOf(day) == DoorState.Locked) onLockedDay(day) else onOpenDay(day)

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("LUIS MIGUEL", color = AdventColors.Gold, fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
                Text(strings.calendarTitle, color = AdventColors.Cream, fontSize = 24.sp, fontFamily = SerifFamily)
            }
            if (testMode) TestBadge(Modifier.padding(end = 6.dp))
            Text("${opened.size}/$LAST_DAY", color = AdventColors.Muted, fontSize = 13.sp)
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = strings.settings, tint = AdventColors.Cream)
            }
        }

        VerticalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
            if (page == 0) {
                Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(ROWS) { row ->
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(COLUMNS) { col ->
                                    val day = row * COLUMNS + col + 1
                                    DoorCell(
                                        day = day,
                                        song = songs[day],
                                        state = stateOf(day),
                                        onClick = { tap(day) },
                                        modifier = Modifier.weight(1f).fillMaxHeight(),
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        strings.swipeToChristmas,
                        modifier = Modifier.fillMaxWidth()
                            .clickable { scope.launch { pagerState.animateScrollToPage(1) } }
                            .padding(top = 10.dp, bottom = 2.dp),
                        color = AdventColors.Muted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp,
                    )
                }
            } else {
                ChristmasDoor(
                    song = songs[LAST_DAY],
                    state = stateOf(LAST_DAY),
                    onClick = { tap(LAST_DAY) },
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                )
            }
        }
    }
}

/** Shakes horizontally when a locked door is tapped. */
@Composable
private fun rememberShake(): Pair<Animatable<Float, *>, suspend () -> Unit> {
    val offset = remember { Animatable(0f) }
    return offset to {
        offset.animateTo(0f, keyframes {
            durationMillis = 360
            -14f at 60; 12f at 120; -8f at 190; 5f at 260; 0f at 360
        })
    }
}

@Composable
private fun DoorCell(day: Int, song: Song?, state: DoorState, onClick: () -> Unit, modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val (shake, runShake) = rememberShake()
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .graphicsLayer { translationX = shake.value }
            .clip(shape)
            .background(AdventColors.Surface)
            .then(if (state == DoorState.Available) Modifier.border(BorderStroke(1.5.dp, AdventColors.Gold), shape) else Modifier)
            .clickable {
                if (state == DoorState.Locked) scope.launch { runShake() }
                onClick()
            },
    ) {
        CoverImage(
            song?.cover,
            Modifier.fillMaxSize().then(
                when (state) {
                    DoorState.Locked -> Modifier.blur(18.dp)
                    DoorState.Available -> Modifier.blur(10.dp)
                    DoorState.Opened -> Modifier
                }
            ),
        )
        when (state) {
            DoorState.Locked -> {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.78f)))
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$day", color = AdventColors.Muted, fontSize = 26.sp, fontFamily = SerifFamily)
                    Icon(Icons.Filled.Lock, null, Modifier.size(12.dp), tint = AdventColors.Muted.copy(alpha = 0.6f))
                }
            }
            DoorState.Available -> {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
                Text(
                    "$day",
                    Modifier.align(Alignment.Center),
                    color = AdventColors.Gold,
                    fontSize = 32.sp,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
            DoorState.Opened -> {
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.75f))
                    )
                )
                Text(
                    "$day",
                    Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 4.dp),
                    color = AdventColors.Cream,
                    fontSize = 16.sp,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ChristmasDoor(song: Song?, state: DoorState, onClick: () -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    val (shake, runShake) = rememberShake()
    val shape = RoundedCornerShape(28.dp)
    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(
            strings.backToDays,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onBack).padding(bottom = 10.dp),
            color = AdventColors.Muted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
        )
        Box(
            Modifier.weight(1f).fillMaxWidth()
                .graphicsLayer { translationX = shake.value }
                .clip(shape)
                .background(AdventColors.Surface)
                .border(BorderStroke(1.5.dp, if (state == DoorState.Locked) AdventColors.GoldDim else AdventColors.Gold), shape)
                .clickable {
                    if (state == DoorState.Locked) scope.launch { runShake() }
                    onClick()
                },
        ) {
            CoverImage(
                song?.cover,
                Modifier.fillMaxSize().then(if (state == DoorState.Opened) Modifier else Modifier.blur(28.dp)),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = if (state == DoorState.Opened) 0.1f else 0.55f),
                        1f to Color.Black.copy(alpha = 0.85f),
                    )
                )
            )
            Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "25",
                    color = AdventColors.Gold,
                    fontSize = 140.sp,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 140.sp,
                )
                Text(strings.christmas, color = AdventColors.Cream, fontSize = 16.sp, letterSpacing = 8.sp)
            }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (state) {
                    DoorState.Locked -> {
                        Icon(Icons.Filled.Lock, null, Modifier.size(16.dp), tint = AdventColors.Muted)
                        Spacer(Modifier.height(6.dp))
                        Text(strings.christmasLocked, color = AdventColors.Muted, fontSize = 14.sp)
                    }
                    DoorState.Available ->
                        Text(strings.christmasAvailable, color = AdventColors.Gold, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    DoorState.Opened -> {
                        Text(
                            song?.title ?: "",
                            color = AdventColors.Cream,
                            fontSize = 22.sp,
                            fontFamily = SerifFamily,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.width(4.dp).height(4.dp))
                        Text(song?.album ?: "", color = AdventColors.Muted, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
