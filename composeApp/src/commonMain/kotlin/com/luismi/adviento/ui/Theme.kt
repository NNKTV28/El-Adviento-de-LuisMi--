package com.luismi.adviento.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

object AdventColors {
    val Background = Color(0xFF0E0C0A)
    val Surface = Color(0xFF1A1714)
    val SurfaceHigh = Color(0xFF26211C)
    val Gold = Color(0xFFE2B866)
    val GoldDim = Color(0xFF8C7444)
    val Cream = Color(0xFFF4EDE1)
    val Muted = Color(0xFFA69C8E)
    val Red = Color(0xFFC0473F)
}

val SerifFamily = FontFamily.Serif

@Composable
fun AdventTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = AdventColors.Gold,
            onPrimary = AdventColors.Background,
            secondary = AdventColors.GoldDim,
            background = AdventColors.Background,
            onBackground = AdventColors.Cream,
            surface = AdventColors.Surface,
            onSurface = AdventColors.Cream,
            surfaceVariant = AdventColors.SurfaceHigh,
            onSurfaceVariant = AdventColors.Muted,
            surfaceContainerHigh = AdventColors.SurfaceHigh,
            error = AdventColors.Red,
        ),
        content = content,
    )
}
