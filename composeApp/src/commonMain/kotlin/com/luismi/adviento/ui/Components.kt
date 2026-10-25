package com.luismi.adviento.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luismi.adviento.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

/** Decoded covers, kept for the whole session. Only touched from the main thread. */
private object CoverCache {
    private val cache = mutableMapOf<String, ImageBitmap>()

    fun peek(path: String): ImageBitmap? = cache[path]

    suspend fun load(path: String): ImageBitmap? = cache[path]
        ?: withContext(Dispatchers.Default) {
            runCatching { Res.readBytes(path).decodeToImageBitmap() }.getOrNull()
        }?.also { cache[path] = it }
}

@Composable
fun CoverImage(path: String?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val bitmap by produceState(path?.let(CoverCache::peek), path) {
        value = path?.let { CoverCache.load(it) }
    }
    val image = bitmap
    if (image != null) {
        Image(image, contentDescription, modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(AdventColors.SurfaceHigh))
    }
}

/** Two bars; the core icon set has no pause glyph. */
@Composable
fun PauseGlyph(color: Color, height: Dp = 22.dp) {
    Row(Modifier.size(height), horizontalArrangement = Arrangement.spacedBy(height / 4, Alignment.CenterHorizontally)) {
        repeat(2) {
            Box(
                Modifier.width(height / 4).fillMaxHeight()
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun TestBadge(modifier: Modifier = Modifier) {
    Text(
        "TEST",
        modifier = modifier
            .background(AdventColors.Red, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )
}

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val seconds = (total % 60).toString().padStart(2, '0')
    return "${total / 60}:$seconds"
}
