package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = IndigoLight,
    secondary = EmeraldLight,
    onSecondary = Color.Black,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldLight,
    tertiary = AmberStreak,
    onTertiary = Color.Black,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberStreak,
    background = SlateBackgroundDark,
    onBackground = SlateTextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = SlateSurfaceVariantDark,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = SlateCardBorderDark,
    error = RosePriority
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = IndigoHover,
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = EmeraldAccent,
    tertiary = AmberStreak,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = AmberStreak,
    background = SlateBackgroundLight,
    onBackground = SlateTextPrimaryLight,
    surface = SlateSurfaceLight,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = SlateSurfaceVariantLight,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = SlateCardBorderLight,
    error = RosePriority
)

@Composable
fun StudyFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    fontSizePercentage: Int = 100,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        fontSizePercentage = fontSizePercentage,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional SaaS branding colors
    fontSizePercentage: Int = 100,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentDensity = LocalDensity.current
    val scaleFactor = (fontSizePercentage.coerceIn(80, 150) / 100f)
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * scaleFactor
    )

    CompositionLocalProvider(
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
