package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SelrDarkColorScheme = darkColorScheme(
    primary = SelrRed,
    onPrimary = Color.White,
    primaryContainer = SelrRedContainer,
    onPrimaryContainer = Color.White,
    secondary = SelrOlive,
    onSecondary = Color.White,
    secondaryContainer = SelrOliveMuted,
    onSecondaryContainer = SelrTextPrimary,
    tertiary = SelrSafeGreen,
    onTertiary = Color.White,
    tertiaryContainer = SelrSafeGreenContainer,
    onTertiaryContainer = Color.White,
    background = SelrNavyBg,
    onBackground = SelrTextPrimary,
    surface = SelrNavySurface,
    onSurface = SelrTextPrimary,
    surfaceVariant = SelrNavyCard,
    onSurfaceVariant = SelrTextSecondary,
    outline = SelrNavyBorder,
    error = SelrRedBright,
    onError = Color.White
)

private val SelrLightColorScheme = lightColorScheme(
    primary = SelrRed,
    onPrimary = Color.White,
    primaryContainer = SelrRedLightContainer,
    onPrimaryContainer = SelrRedDark,
    secondary = SelrOlive,
    onSecondary = Color.White,
    secondaryContainer = SelrOliveLightContainer,
    onSecondaryContainer = SelrOlive,
    tertiary = SelrSafeGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = SelrSafeGreen,
    background = SelrLightBg,
    onBackground = SelrLightTextPrimary,
    surface = SelrLightSurface,
    onSurface = SelrLightTextPrimary,
    surfaceVariant = SelrLightCard,
    onSurfaceVariant = SelrLightTextSecondary,
    outline = SelrLightBorder,
    error = SelrRedBright,
    onError = Color.White
)

@Composable
fun SELRTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SelrDarkColorScheme
        else -> SelrLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
