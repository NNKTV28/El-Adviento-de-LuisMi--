package com.luismi.adviento.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismi.adviento.data.LAST_DAY
import com.luismi.adviento.data.Song
import com.luismi.adviento.data.SongCatalog
import com.luismi.adviento.i18n.LocalStrings
import com.luismi.adviento.platform.AudioPlayer
import kotlinx.coroutines.delay

@Composable
fun DayScreen(day: Int, song: Song?, player: AudioPlayer, onPlayStarted: () -> Unit, onBack: () -> Unit) {
    val strings = LocalStrings.current
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    if (song != null) {
        LaunchedEffect(song.audio) {
            player.load(SongCatalog.uriOf(song.audio))
            player.play()
            onPlayStarted()
            while (true) {
                isPlaying = player.isPlaying
                position = player.positionMs
                duration = player.durationMs
                delay(250)
            }
        }
        DisposableEffect(song.audio) { onDispose { player.stop() } }
    }

    Box(Modifier.fillMaxSize().background(AdventColors.Background)) {
        CoverImage(song?.cover, Modifier.fillMaxSize().blur(70.dp).alpha(0.45f))
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to AdventColors.Background.copy(alpha = 0.3f), 1f to AdventColors.Background)
            )
        )

        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.padding(start = 0.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = AdventColors.Cream)
                }
                Text(
                    strings.dayLabel(day),
                    modifier = Modifier.weight(1f),
                    color = AdventColors.Gold,
                    fontSize = 13.sp,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(48.dp))
            }

            Spacer(Modifier.weight(0.6f))

            CoverImage(
                song?.cover,
                Modifier.fillMaxWidth(0.86f).aspectRatio(1f).align(Alignment.CenterHorizontally)
                    .shadow(28.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp)),
                contentDescription = song?.title,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                song?.title ?: strings.songUnavailable,
                color = AdventColors.Cream,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                fontFamily = SerifFamily,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                song?.let { "${it.album} · ${it.year}" } ?: strings.songUnavailableHint,
                color = AdventColors.Muted,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            val progress = if (duration > 0) position.toFloat() / duration else 0f
            Slider(
                value = if (dragging) dragValue else progress,
                onValueChange = { dragging = true; dragValue = it },
                onValueChangeFinished = {
                    player.seekTo((dragValue * duration).toLong())
                    position = (dragValue * duration).toLong()
                    dragging = false
                },
                enabled = song != null && duration > 0,
                colors = SliderDefaults.colors(
                    thumbColor = AdventColors.Gold,
                    activeTrackColor = AdventColors.Gold,
                    inactiveTrackColor = AdventColors.SurfaceHigh,
                ),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(if (dragging) (dragValue * duration).toLong() else position), color = AdventColors.Muted, fontSize = 12.sp)
                Text(formatTime(duration), color = AdventColors.Muted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            FilledIconButton(
                onClick = {
                    if (player.isPlaying) player.pause() else player.play()
                    isPlaying = player.isPlaying
                },
                enabled = song != null,
                modifier = Modifier.size(76.dp).align(Alignment.CenterHorizontally),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = AdventColors.Gold,
                    contentColor = AdventColors.Background,
                ),
            ) {
                if (isPlaying) PauseGlyph(AdventColors.Background, 26.dp)
                else Icon(Icons.Filled.PlayArrow, contentDescription = strings.play, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.weight(1f))

            if (day == LAST_DAY) {
                Text(
                    strings.merryChristmas,
                    color = AdventColors.Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = SerifFamily,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
