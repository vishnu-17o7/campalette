package com.vishnu.campalette.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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

private val AtelierDarkColorScheme = darkColorScheme(
    primary = AtelierDarkPrimary,
    onPrimary = AtelierDarkOnPrimary,
    primaryContainer = AtelierDarkPrimaryContainer,
    onPrimaryContainer = AtelierDarkOnPrimaryContainer,
    secondary = AtelierDarkSecondary,
    onSecondary = AtelierDarkOnSecondary,
    secondaryContainer = AtelierDarkSecondaryContainer,
    onSecondaryContainer = AtelierDarkOnSecondaryContainer,
    tertiary = AtelierDarkTertiary,
    onTertiary = AtelierDarkOnTertiary,
    tertiaryContainer = AtelierDarkTertiaryContainer,
    onTertiaryContainer = AtelierDarkOnTertiaryContainer,
    background = AtelierDarkBackground,
    onBackground = AtelierDarkOnBackground,
    surface = AtelierDarkSurface,
    onSurface = AtelierDarkOnSurface,
    surfaceVariant = AtelierDarkSurfaceVariant,
    onSurfaceVariant = AtelierDarkOnSurfaceVariant,
    outline = AtelierDarkOutline,
    outlineVariant = AtelierDarkOutlineVariant,
    inversePrimary = AtelierDarkInversePrimary,
    inverseSurface = AtelierDarkInverseSurface,
    inverseOnSurface = AtelierDarkInverseOnSurface,
    error = AtelierDarkError,
    onError = AtelierDarkOnError,
    errorContainer = AtelierDarkErrorContainer,
    onErrorContainer = AtelierDarkOnErrorContainer,
    surfaceTint = AtelierDarkSurfaceTint,
    scrim = AtelierDarkOnSurface
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

private val DarkAtelierColors = AtelierColors(
    primaryFixed = AtelierDarkPrimaryFixed,
    primaryFixedDim = AtelierDarkPrimaryFixedDim,
    secondaryFixed = AtelierDarkSecondaryFixed,
    secondaryFixedDim = AtelierDarkSecondaryFixedDim,
    tertiaryFixed = AtelierDarkTertiaryFixed,
    tertiaryFixedDim = AtelierDarkTertiaryFixedDim,
    surfaceBright = AtelierDarkSurfaceBright,
    surfaceDim = AtelierDarkSurfaceDim,
    surfaceContainerLowest = AtelierDarkSurfaceContainerLowest,
    surfaceContainerLow = AtelierDarkSurfaceContainerLow,
    surfaceContainer = AtelierDarkSurfaceContainer,
    surfaceContainerHigh = AtelierDarkSurfaceContainerHigh,
    surfaceContainerHighest = AtelierDarkSurfaceContainerHighest
)

val LocalAtelierColors = staticCompositionLocalOf { AtelierColors() }
val LocalReducedMotion = staticCompositionLocalOf { false }

object AtelierTheme {
    val colors: AtelierColors
        @Composable
        get() = LocalAtelierColors.current
}

@Composable
fun CampaletteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AtelierDarkColorScheme else AtelierColorScheme
    val atelierColors = if (darkTheme) DarkAtelierColors else AtelierColors()

    CompositionLocalProvider(LocalAtelierColors provides atelierColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
