package com.nvmeacademy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NvmeBlue = Color(0xFF1565C0)
private val NvmeCyan = Color(0xFF00ACC1)
private val NvmeAmber = Color(0xFFFFA000)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = NvmeCyan,
    tertiary = NvmeAmber,
    background = Color(0xFF0B0F14),
    surface = Color(0xFF0D1117),
    surfaceVariant = Color(0xFF161D26),
    onSurface = Color(0xFFE6EAF0),
    onSurfaceVariant = Color(0xFFC7D0DC),
    onBackground = Color(0xFFE6EAF0),
    primaryContainer = Color(0xFF1B2733),
    onPrimaryContainer = Color(0xFFCFE6FF),
    secondaryContainer = Color(0xFF15242A),
    onSecondaryContainer = Color(0xFFB8E7EF),
)

private val LightColors = lightColorScheme(
    primary = NvmeBlue,
    secondary = NvmeCyan,
    tertiary = NvmeAmber,
    background = Color(0xFFF7F9FC),
    surface = Color.White,
)

@Composable
fun NvmeAcademyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = NvmeTypography,
        content = content
    )
}
