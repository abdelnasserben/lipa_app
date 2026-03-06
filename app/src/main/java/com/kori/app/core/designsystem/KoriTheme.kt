package com.kori.app.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KoriLightColorScheme = lightColorScheme(
    primary = KoriPrimary,
    onPrimary = KoriBackground,
    secondary = KoriAccent,
    onSecondary = KoriPrimary,
    background = KoriBackground,
    onBackground = KoriTextPrimary,
    surface = KoriBackground,
    onSurface = KoriTextPrimary,
    surfaceVariant = KoriSurfaceVariant,
    onSurfaceVariant = KoriTextSecondary,
    error = KoriError,
    outline = KoriBorder,
)

@Composable
fun KoriTheme(
    content: @Composable () -> Unit,
) {
    val _isSystemDark = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme = KoriLightColorScheme,
        typography = KoriTypography,
        content = content,
    )
}