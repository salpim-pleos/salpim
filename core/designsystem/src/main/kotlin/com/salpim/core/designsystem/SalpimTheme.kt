package com.salpim.core.designsystem

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private val SalpimColorScheme = darkColorScheme(
    background = SalpimColors.Background,
    surface = SalpimColors.Surface,
    surfaceVariant = SalpimColors.SurfaceRaised,
    primary = SalpimColors.Action,
    onPrimary = SalpimColors.OnAction,
    onBackground = SalpimColors.Text,
    onSurface = SalpimColors.Text,
    onSurfaceVariant = SalpimColors.TextSecondary,
    outline = SalpimColors.TextTertiary,
    error = SalpimColors.Urgent,
)

@Composable
fun SalpimTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SalpimColorScheme, typography = SalpimType.material) {
        Surface(Modifier.fillMaxSize(), color = SalpimColors.Background, content = content)
    }
}
