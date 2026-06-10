package com.vishnu.campalette.ui.theme

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.palette.graphics.Palette

@Immutable
data class DynamicThemeColors(
    val primaryShift: Color = AtelierPrimary,
    val primaryContainerShift: Color = AtelierPrimaryContainer,
    val primaryFixedShift: Color = AtelierPrimaryFixed,
    val primaryFixedDimShift: Color = AtelierPrimaryFixedDim,
    val surfaceTintShift: Color = AtelierSurfaceTint,
    val surfaceContainerLowShift: Color = AtelierSurfaceContainerLow,
    val blendFactor: Float = 0f
)

val LocalDynamicThemeColors = staticCompositionLocalOf { DynamicThemeColors() }

fun blendColors(base: Color, overlay: Color, factor: Float): Color {
    if (factor <= 0f) return base
    if (factor >= 1f) return overlay
    val r = base.red * (1f - factor) + overlay.red * factor
    val g = base.green * (1f - factor) + overlay.green * factor
    val b = base.blue * (1f - factor) + overlay.blue * factor
    return Color(r, g, b, base.alpha)
}

fun Bitmap.extractDominantColor(): Int? {
    val palette = Palette.from(this).generate()
    return palette.vibrantSwatch?.rgb
        ?: palette.dominantSwatch?.rgb
        ?: palette.mutedSwatch?.rgb
}

fun createDynamicColors(
    seedColor: Int,
    blendFactor: Float = 0.22f,
    basePrimary: Color = AtelierPrimary,
    basePrimaryContainer: Color = AtelierPrimaryContainer,
    basePrimaryFixed: Color = AtelierPrimaryFixed,
    basePrimaryFixedDim: Color = AtelierPrimaryFixedDim,
    baseSurfaceTint: Color = AtelierSurfaceTint,
    baseSurfaceContainerLow: Color = AtelierSurfaceContainerLow
): DynamicThemeColors {
    if (blendFactor <= 0f) return DynamicThemeColors()

    val seed = Color(seedColor)
    val seedLuminance = seed.luminance()
    val isDark = seedLuminance < 0.35f
    val isLight = seedLuminance > 0.75f

    val adjustedBlend = when {
        isDark -> blendFactor * 0.75f
        isLight -> blendFactor * 0.6f
        else -> blendFactor
    }

    return DynamicThemeColors(
        primaryShift = blendColors(basePrimary, seed, adjustedBlend),
        primaryContainerShift = blendColors(basePrimaryContainer, seed, adjustedBlend * 0.85f),
        primaryFixedShift = blendColors(basePrimaryFixed, seed, adjustedBlend * 0.6f),
        primaryFixedDimShift = blendColors(basePrimaryFixedDim, seed, adjustedBlend * 0.6f),
        surfaceTintShift = blendColors(baseSurfaceTint, seed, adjustedBlend * 0.3f),
        surfaceContainerLowShift = blendColors(baseSurfaceContainerLow, seed, adjustedBlend * 0.04f),
        blendFactor = adjustedBlend
    )
}

@Composable
fun DynamicThemeProvider(
    dominantColor: Int?,
    content: @Composable () -> Unit
) {
    val dynamicColors = if (dominantColor != null) {
        createDynamicColors(
            seedColor = dominantColor,
            basePrimary = MaterialTheme.colorScheme.primary,
            basePrimaryContainer = MaterialTheme.colorScheme.primaryContainer,
            basePrimaryFixed = AtelierTheme.colors.primaryFixed,
            basePrimaryFixedDim = AtelierTheme.colors.primaryFixedDim,
            baseSurfaceTint = MaterialTheme.colorScheme.surfaceTint,
            baseSurfaceContainerLow = AtelierTheme.colors.surfaceContainerLow
        )
    } else {
        DynamicThemeColors()
    }

    val currentColorScheme = MaterialTheme.colorScheme
    val dynamicColorScheme = remember(dynamicColors, currentColorScheme) {
        if (dynamicColors.blendFactor > 0f) {
            currentColorScheme.copy(
                primary = dynamicColors.primaryShift,
                primaryContainer = dynamicColors.primaryContainerShift,
                surfaceTint = dynamicColors.surfaceTintShift
            )
        } else {
            currentColorScheme
        }
    }

    val currentAtelierColors = LocalAtelierColors.current
    val dynamicAtelierColors = remember(dynamicColors, currentAtelierColors) {
        if (dynamicColors.blendFactor > 0f) {
            currentAtelierColors.copy(
                primaryFixed = dynamicColors.primaryFixedShift,
                primaryFixedDim = dynamicColors.primaryFixedDimShift,
                surfaceContainerLow = dynamicColors.surfaceContainerLowShift
            )
        } else {
            currentAtelierColors
        }
    }

    CompositionLocalProvider(
        LocalDynamicThemeColors provides dynamicColors,
        LocalAtelierColors provides dynamicAtelierColors
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes
        ) {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    val systemBarColor = dynamicColorScheme.surface
                    window.statusBarColor = systemBarColor.copy(alpha = 0.95f).toArgb()
                    window.navigationBarColor = systemBarColor.copy(alpha = 0.95f).toArgb()
                    val isLightBars = systemBarColor.luminance() > 0.5f
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightBars
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLightBars
                }
            }
            content()
        }
    }
}

val ExpressiveSpatialSpring = androidx.compose.animation.core.spring<Float>(
    dampingRatio = 0.55f,
    stiffness = 300f
)

val ExpressiveEffectsSpring = androidx.compose.animation.core.spring<Float>(
    dampingRatio = 1.0f,
    stiffness = 400f
)

val ExpressiveEffectsColorSpring = androidx.compose.animation.core.spring<Color>(
    dampingRatio = 1.0f,
    stiffness = 400f
)
