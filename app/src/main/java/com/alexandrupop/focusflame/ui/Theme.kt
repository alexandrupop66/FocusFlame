package com.alexandrupop.focusflame.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlameScheme = lightColorScheme(
    primary = Color(0xFF8A3A12),
    onPrimary = Color(0xFFFFF8F0),
    primaryContainer = Color(0xFFFFD8B5),
    onPrimaryContainer = Color(0xFF321004),
    secondary = Color(0xFF51635A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8DE),
    onSecondaryContainer = Color(0xFF102019),
    tertiary = Color(0xFF705D93),
    background = Color(0xFFFFF7ED),
    onBackground = Color(0xFF241812),
    surface = Color(0xFFFFFBF6),
    onSurface = Color(0xFF241812),
    surfaceVariant = Color(0xFFF2E2D6),
    onSurfaceVariant = Color(0xFF5A463B),
    outline = Color(0xFF846E61)
)

@Composable
fun FocusFlameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlameScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
