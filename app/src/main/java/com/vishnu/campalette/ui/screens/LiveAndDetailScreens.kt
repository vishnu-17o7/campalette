package com.vishnu.campalette.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.RadialMenuState
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.components.AdaptiveLayout
import com.vishnu.campalette.ui.components.AtelierTag
import com.vishnu.campalette.ui.components.CameraPreview
import com.vishnu.campalette.ui.components.CaptureProgress
import com.vishnu.campalette.ui.components.PaletteListCard
import com.vishnu.campalette.ui.components.PrimaryButton
import com.vishnu.campalette.ui.components.SecondaryButton
import com.vishnu.campalette.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * State Holders
 */
data class LiveCameraState(
    val palette: List<PaletteColor>,
    val selectedColor: PaletteColor?,
    val isCapturing: Boolean,
    val hasCapturedPalette: Boolean,
    val paletteName: String,
    val harmonyLabel: String,
    val previewSize: IntSize
)

/**
 * Live Camera Screen
 * Implements a 3-layer architecture:
 * 1. Background: Camera Preview or Captured Image
 * 2. Middle: Chrome (Top/Bottom bars)
 * 3. Foreground: Radial Menu or Overlays
 */
@Composable
fun LiveCameraScreen(
    state: LiveCameraState,
    activity: MainActivity,
    capturedImage: Bitmap?,
    radialMenuState: RadialMenuState?,
    gridEnabled: Boolean,
    flashEnabled: Boolean,
    flashAvailable: Boolean,
    lensFacing: Int,
    largeTouchTargets: Boolean,
    onCapture: () -> Unit,
    onClearCapture: () -> Unit,
    onMenuClick: () -> Unit,
    onEditPalette: () -> Unit,
    onPreviewMeasured: (IntSize) -> Unit,
    onFlipCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleGrid: () -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onSampleTap: (Bitmap, Offset, IntSize) -> Unit,
    onSampleLongPress: (Bitmap, Offset, IntSize) -> Unit,
    onColorSelected: (PaletteColor) -> Unit,
    onCameraError: (String) -> Unit,
    onImportGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (capturedImage != null) {
            androidx.compose.foundation.Image(
                bitmap = capturedImage.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged(onPreviewMeasured)
                    .pointerInput(capturedImage) {
                        detectTapGestures(
                            onTap = { onSampleTap(capturedImage, it, size) },
                            onLongPress = { onSampleLongPress(capturedImage, it, size) }
                        )
                    },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                lensFacing = lensFacing,
                flashEnabled = flashEnabled,
                gridEnabled = gridEnabled,
                onPreviewMeasured = onPreviewMeasured,
                onFlashAvailabilityChanged = onFlashAvailabilityChanged,
                onCameraError = onCameraError,
                onSampleTap = onSampleTap,
                onSampleLongPress = onSampleLongPress,
                activity = activity
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraChromeButton(
                onClick = if (capturedImage != null) onClearCapture else onImportGallery,
                contentDescription = if (capturedImage != null) "Clear capture" else "Import photo"
            ) {
                Icon(
                    if (capturedImage != null) Icons.Rounded.Close else Icons.Rounded.Image,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            if (capturedImage != null) {
                CameraChromeButton(onClick = onMenuClick, contentDescription = "Open library") {
                    Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color.White)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (flashAvailable) {
                        CameraChromeButton(onClick = onToggleFlash, contentDescription = "Toggle flash") {
                            Icon(
                                if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                                contentDescription = null,
                                tint = if (flashEnabled) Color(0xFFFFD60A) else Color.White
                            )
                        }
                    }
                    CameraChromeButton(onClick = onToggleGrid, contentDescription = "Toggle grid") {
                        Icon(
                            Icons.Rounded.GridOn,
                            contentDescription = null,
                            tint = if (gridEnabled) Color(0xFFFFD60A) else Color.White
                        )
                    }
                    CameraChromeButton(onClick = onFlipCamera, contentDescription = "Flip camera") {
                        Icon(Icons.Rounded.FlipCameraIos, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = capturedImage != null || state.isCapturing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 14.dp, end = 14.dp, bottom = 112.dp),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp), clip = false),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                if (state.isCapturing) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CaptureProgress()
                        Spacer(Modifier.width(14.dp))
                        Text("Finding colors", style = MaterialTheme.typography.titleMedium)
                    }
                } else if (state.palette.isNotEmpty()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.paletteName.ifBlank { "Live palette" },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${state.palette.size} colors",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Tap to inspect",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (largeTouchTargets) 64.dp else 56.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            state.palette.forEach { paletteColor ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(Color(paletteColor.color))
                                        .clickable { onColorSelected(paletteColor) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                text = "Retake",
                                onClick = onClearCapture,
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryButton(
                                text = "Edit palette",
                                onClick = onEditPalette,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.palette.isNotEmpty() && !state.isCapturing && capturedImage == null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 18.dp, end = 18.dp, bottom = 200.dp),
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(120))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.Black.copy(alpha = 0.46f)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        state.palette.forEach { paletteColor ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color(paletteColor.color))
                                    .clickable { onColorSelected(paletteColor) }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = capturedImage == null && !state.isCapturing,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 112.dp),
            enter = fadeIn(tween(140)),
            exit = fadeOut(tween(100))
        ) {
            CameraShutterButton(onClick = onCapture)
        }

        radialMenuState?.let { radial ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (radial.center.x - 24.dp.toPx()).roundToInt(),
                            (radial.center.y - 24.dp.toPx()).roundToInt()
                        )
                    }
                    .size(48.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(24.dp))
            )
        }
    }
}

@Composable
private fun CameraShutterButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shutterScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(110),
        label = "Shutter press"
    )
    Box(
        modifier = Modifier
            .size(76.dp)
            .scale(shutterScale)
            .shadow(8.dp, CircleShape, clip = false)
            .border(4.dp, Color.White, CircleShape)
            .background(Color.Black.copy(alpha = 0.16f), CircleShape)
            .semantics {
                contentDescription = "Capture photo"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
private fun CameraChromeButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(44.dp)
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.34f),
            contentColor = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
    }
}

/**
 * Color Detail Screen
 */
@Composable
fun ColorDetailScreen(
    paletteColor: PaletteColor,
    onCopy: (String) -> Unit,
    onAddToPalette: () -> Unit,
    onRemoveFromPalette: () -> Unit,
    onShowColorBlindness: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroColor = Color(paletteColor.color)
    val heroContentColor = if (heroColor.luminance() > 0.45f) Color.Black else Color.White
    val groupedShape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(232.dp)
                .background(heroColor)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 8.dp, top = 4.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (heroContentColor == Color.White) {
                            Color.Black.copy(alpha = 0.18f)
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = heroContentColor
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = paletteColor.hexCode,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        lineHeight = 41.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = heroContentColor
                )
                Text(
                    text = AtelierData.guessColorName(paletteColor.color),
                    style = MaterialTheme.typography.bodyLarge,
                    color = heroContentColor.copy(alpha = 0.78f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            val hslArray = AtelierData.rgbToHsl(paletteColor.red, paletteColor.green, paletteColor.blue)
            val formats = listOf(
                "Hex" to paletteColor.hexCode,
                "RGB" to "rgb(${paletteColor.red}, ${paletteColor.green}, ${paletteColor.blue})",
                "HSL" to "hsl(${hslArray[0].roundToInt()}, ${hslArray[1].roundToInt()}%, ${hslArray[2].roundToInt()}%)"
            )

            Text(
                text = "Color values",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = groupedShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    formats.forEachIndexed { index, (label, value) ->
                        FormatRow(
                            label = label,
                            value = value,
                            showDivider = index < formats.lastIndex,
                            onCopy = { onCopy(value) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            val relatedColors = AtelierData.generateRelatedColors(paletteColor.color)

            Text(
                text = "Tints",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(relatedColors.first.size) { i ->
                    RelatedColorSwatch(color = Color(relatedColors.first[i].color))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Shades",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(relatedColors.second.size) { i ->
                    RelatedColorSwatch(color = Color(relatedColors.second[i].color))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                onClick = onAddToPalette,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Edit palette",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = groupedShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    DetailActionRow(
                        label = "Color vision",
                        trailing = "›",
                        onClick = onShowColorBlindness
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    )
                    DetailActionRow(
                        label = "Remove from palette",
                        labelColor = Color(0xFFFF3B30),
                        onClick = onRemoveFromPalette
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FormatRow(
    label: String,
    value: String,
    showDivider: Boolean,
    onCopy: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(onClick = onCopy)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(48.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Copy",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 64.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun RelatedColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
    )
}

@Composable
private fun DetailActionRow(
    label: String,
    onClick: () -> Unit,
    trailing: String? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = labelColor,
            modifier = Modifier.weight(1f)
        )
        trailing?.let {
            Text(
                text = it,
                fontSize = 28.sp,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
        }
    }
}
