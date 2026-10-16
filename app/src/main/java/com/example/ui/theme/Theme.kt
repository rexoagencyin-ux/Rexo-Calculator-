package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pure, modern light theme color palette
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = EmeraldGreenDark,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = OnEmeraldContainerLight,
    background = BackgroundLight, // #F8F9FA
    onBackground = OnBackgroundLight, // #0F172A
    surface = SurfaceLight, // #FFFFFF
    onSurface = OnSurfaceLight, // #0F172A
    surfaceVariant = SurfaceVariantLight, // #F1F5F9
    onSurfaceVariant = OnSurfaceVariantLight, // #64748B
    outline = OutlineLight, // #E2E8F0
    outlineVariant = OutlineStrongLight
)

@Composable
fun RexoCalculatorTheme(
    darkTheme: Boolean = false, // Strictly Light Theme as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
