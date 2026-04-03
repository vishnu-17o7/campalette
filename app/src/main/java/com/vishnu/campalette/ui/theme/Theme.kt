package com.vishnu.campalette.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AtelierColorScheme = lightColorScheme(
    primary = AtelierPrimary,
    onPrimary = AtelierOnPrimary,
    primaryContainer = AtelierPrimaryContainer,
    onPrimaryContainer = AtelierOnPrimaryContainer,
    secondary = AtelierSecondary,
    onSecondary = AtelierOnSecondary,
    secondaryContainer = AtelierSecondaryContainer,
    onSecondaryContainer = AtelierOnSecondaryContainer,
    tertiary = AtelierTertiary,
    onTertiary = AtelierOnTertiary,
    tertiaryContainer = AtelierTertiaryContainer,
    onTertiaryContainer = AtelierOnTertiaryContainer,
    background = AtelierBackground,
    onBackground = AtelierOnBackground,
    surface = AtelierSurface,
    onSurface = AtelierOnSurface,
    surfaceVariant = AtelierSurfaceVariant,
    onSurfaceVariant = AtelierOnSurfaceVariant,
    outline = AtelierOutline,
    outlineVariant = AtelierOutlineVariant,
    inversePrimary = AtelierInversePrimary,
    inverseSurface = AtelierInverseSurface,
    inverseOnSurface = AtelierInverseOnSurface,
    error = AtelierError,
    onError = AtelierOnError,
    errorContainer = AtelierErrorContainer,
    onErrorContainer = AtelierOnErrorContainer,
    surfaceTint = AtelierSurfaceTint,
    scrim = AtelierOnSurface
)

@Immutable
data class AtelierColors(
    val primaryFixed: Color = AtelierPrimaryFixed,
    val primaryFixedDim: Color = AtelierPrimaryFixedDim,
    val secondaryFixed: Color = AtelierSecondaryFixed,
    val secondaryFixedDim: Color = AtelierSecondaryFixedDim,
    val tertiaryFixed: Color = AtelierTertiaryFixed,
    val tertiaryFixedDim: Color = AtelierTertiaryFixedDim,
    val surfaceBright: Color = AtelierSurfaceBright,
    val surfaceDim: Color = AtelierSurfaceDim,
    val surfaceContainerLowest: Color = AtelierSurfaceContainerLowest,
    val surfaceContainerLow: Color = AtelierSurfaceContainerLow,
    val surfaceContainer: Color = AtelierSurfaceContainer,
    val surfaceContainerHigh: Color = AtelierSurfaceContainerHigh,
    val surfaceContainerHighest: Color = AtelierSurfaceContainerHighest
)

val LocalAtelierColors = staticCompositionLocalOf { AtelierColors() }

object AtelierTheme {
    val colors: AtelierColors
        @Composable
        get() = LocalAtelierColors.current
}

@Composable
fun CampaletteTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val systemBarColor = AtelierSurface.copy(alpha = 0.95f)
            window.statusBarColor = systemBarColor.toArgb()
            window.navigationBarColor = systemBarColor.toArgb()
            val isLightBars = systemBarColor.luminance() > 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightBars
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLightBars
        }
    }

    CompositionLocalProvider(LocalAtelierColors provides AtelierColors()) {
        MaterialTheme(
            colorScheme = AtelierColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
