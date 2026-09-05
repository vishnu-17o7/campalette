package com.vishnu.campalette.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Check
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
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.components.CameraPreview
import com.vishnu.campalette.ui.components.CaptureProgress
import com.vishnu.campalette.ui.components.MagnifierLoupe
import com.vishnu.campalette.ui.components.PrimaryButton
import com.vishnu.campalette.ui.components.SecondaryButton
import com.vishnu.campalette.ui.components.CampaletteWindowSizeClass
import com.vishnu.campalette.ui.components.LocalWindowSizeClass
import com.vishnu.campalette.ui.components.dockClearance
import com.vishnu.campalette.ui.components.shutterClearance
import com.vishnu.campalette.ui.theme.ExpressiveEffectsColorSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsSpring
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import com.vishnu.campalette.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * State Holders
 */
@Immutable
data class SamplingLoupeState(
    val bitmap: Bitmap,
    val touchPoint: Offset,
    val containerSize: IntSize,
    val hexCode: String
)

@Immutable
data class LiveCameraState(
    val palette: List<PaletteColor>,
    val selectedColor: PaletteColor?,
    val isCapturing: Boolean,
    val hasCapturedPalette: Boolean,
    val paletteName: String,
    val harmonyLabel: String,
    val previewSize: IntSize,
    val isPaletteSaved: Boolean = false
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
    samplingLoupe: SamplingLoupeState?,
    gridEnabled: Boolean,
    flashEnabled: Boolean,
    flashAvailable: Boolean,
    lensFacing: Int,
    largeTouchTargets: Boolean,
    onCapture: () -> Unit,
    onCapturePress: () -> Unit = {},
    onClearCapture: () -> Unit,
    onMenuClick: () -> Unit,
    onEditPalette: () -> Unit,
    onSavePalette: () -> Unit,
    onPreviewMeasured: (IntSize) -> Unit,
    onFlipCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleGrid: () -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onSampleTap: (Bitmap, Offset, IntSize) -> Unit,
    onSampleLongPress: (Bitmap, Offset, IntSize) -> Unit,
    onSampleDrag: (Bitmap, Offset, IntSize) -> Unit,
    onSampleDragEnd: () -> Unit,
    onColorSelected: (PaletteColor) -> Unit,
    onCameraError: (String) -> Unit,
    onImportGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnSampleTap = rememberUpdatedState(onSampleTap)
    val latestOnSampleLongPress = rememberUpdatedState(onSampleLongPress)
    val latestOnSampleDrag = rememberUpdatedState(onSampleDrag)
    val latestOnSampleDragEnd = rememberUpdatedState(onSampleDragEnd)
    val hexCache = remember { LiveLoupeHexCache() }
    var localLoupe by remember { mutableStateOf<SamplingLoupeState?>(null) }
    val visibleLoupe = localLoupe ?: samplingLoupe

    val handleSampleTap = remember {
        { bitmap: Bitmap, offset: Offset, size: IntSize ->
            localLoupe = null
            latestOnSampleTap.value(bitmap, offset, size)
        }
    }
    val handleSampleLongPress = remember {
        { bitmap: Bitmap, offset: Offset, size: IntSize ->
            val sampled = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
            if (sampled != null) {
                localLoupe = SamplingLoupeState(
                    bitmap = bitmap,
                    touchPoint = offset,
                    containerSize = size,
                    hexCode = hexCache.hex(sampled)
                )
            }
            latestOnSampleLongPress.value(bitmap, offset, size)
        }
    }
    val handleSampleDrag = remember {
        { bitmap: Bitmap, offset: Offset, size: IntSize ->
            val sampled = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
            if (sampled != null) {
                val previous = localLoupe
                val hexCode = hexCache.hex(sampled)
                localLoupe = if (
                    previous != null &&
                    previous.bitmap === bitmap &&
                    previous.containerSize == size
                ) {
                    if (previous.touchPoint == offset && previous.hexCode == hexCode) {
                        previous
                    } else {
                        previous.copy(touchPoint = offset, hexCode = hexCode)
                    }
                } else {
                    SamplingLoupeState(bitmap, offset, size, hexCode)
                }
            }
            latestOnSampleDrag.value(bitmap, offset, size)
        }
    }
    val handleSampleDragEnd = remember {
        {
            localLoupe = null
            latestOnSampleDragEnd.value()
        }
    }

    BackHandler(enabled = localLoupe != null) {
        localLoupe = null
    }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        LiveCameraBody(
            state = state,
            activity = activity,
            capturedImage = capturedImage,
            gridEnabled = gridEnabled,
            flashEnabled = flashEnabled,
            flashAvailable = flashAvailable,
            lensFacing = lensFacing,
            largeTouchTargets = largeTouchTargets,
            onCapture = onCapture,
            onCapturePress = onCapturePress,
            onClearCapture = onClearCapture,
            onMenuClick = onMenuClick,
            onEditPalette = onEditPalette,
            onSavePalette = onSavePalette,
            onPreviewMeasured = onPreviewMeasured,
            onFlipCamera = onFlipCamera,
            onToggleFlash = onToggleFlash,
            onToggleGrid = onToggleGrid,
            onFlashAvailabilityChanged = onFlashAvailabilityChanged,
            onSampleTap = handleSampleTap,
            onSampleLongPress = handleSampleLongPress,
            onSampleDrag = handleSampleDrag,
            onSampleDragEnd = handleSampleDragEnd,
            onColorSelected = onColorSelected,
            onCameraError = onCameraError,
            onImportGallery = onImportGallery
        )
        visibleLoupe?.let { loupe ->
            MagnifierLoupe(
                bitmap = loupe.bitmap,
                touchPoint = loupe.touchPoint,
                containerSize = loupe.containerSize,
                hexCode = loupe.hexCode
            )
        }
    }
}

@Composable
private fun LiveCameraBody(
    state: LiveCameraState,
    activity: MainActivity,
    capturedImage: Bitmap?,
    gridEnabled: Boolean,
    flashEnabled: Boolean,
    flashAvailable: Boolean,
    lensFacing: Int,
    largeTouchTargets: Boolean,
    onCapture: () -> Unit,
    onCapturePress: () -> Unit,
    onClearCapture: () -> Unit,
    onMenuClick: () -> Unit,
    onEditPalette: () -> Unit,
    onSavePalette: () -> Unit,
    onPreviewMeasured: (IntSize) -> Unit,
    onFlipCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleGrid: () -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onSampleTap: (Bitmap, Offset, IntSize) -> Unit,
    onSampleLongPress: (Bitmap, Offset, IntSize) -> Unit,
    onSampleDrag: (Bitmap, Offset, IntSize) -> Unit,
    onSampleDragEnd: () -> Unit,
    onColorSelected: (PaletteColor) -> Unit,
    onCameraError: (String) -> Unit,
    onImportGallery: () -> Unit
) {
    val dockPadding = dockClearance()
    val liveStripPadding = shutterClearance()
    val sizeClass = LocalWindowSizeClass.current
    val reducedMotion = LocalReducedMotion.current
    val reviewOnSide = sizeClass == CampaletteWindowSizeClass.Expanded
    val reviewTransformOrigin = if (reviewOnSide) TransformOrigin(1f, 0.5f) else TransformOrigin(0.5f, 1f)
    val compactHeight = LocalConfiguration.current.screenHeightDp < 480
    val layoutDirection = LocalLayoutDirection.current
    val safeDrawingPadding = WindowInsets.safeDrawing.asPaddingValues()
    val safeStartPadding = safeDrawingPadding.calculateStartPadding(layoutDirection)
    val safeEndPadding = safeDrawingPadding.calculateEndPadding(layoutDirection)
    Box(Modifier.fillMaxSize()) {
        if (capturedImage != null) {
            androidx.compose.foundation.Image(
                bitmap = capturedImage.asImageBitmap(),
                contentDescription = stringResource(R.string.captured_image),
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged(onPreviewMeasured)
                    .pointerInput(capturedImage) {
                        detectTapGestures(
                            onTap = { onSampleTap(capturedImage, it, size) }
                        )
                    }
                    .pointerInput(capturedImage) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onSampleLongPress(capturedImage, it, size) },
                            onDrag = { change, _ ->
                                change.consume()
                                onSampleDrag(capturedImage, change.position, size)
                            },
                            onDragEnd = onSampleDragEnd,
                            onDragCancel = onSampleDragEnd
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
                onSampleDrag = onSampleDrag,
                onSampleDragEnd = onSampleDragEnd,
                activity = activity
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    start = 14.dp + safeStartPadding,
                    top = 10.dp,
                    end = 14.dp + safeEndPadding,
                    bottom = 10.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CameraChromeButton(
                onClick = if (capturedImage != null) onClearCapture else onImportGallery,
                contentDescription = stringResource(
                    if (capturedImage != null) R.string.clear_capture else R.string.import_photo
                )
            ) {
                Icon(
                    if (capturedImage != null) Icons.Rounded.Close else Icons.Rounded.Image,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            if (capturedImage != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CameraChromeButton(
                        onClick = onSavePalette,
                        contentDescription = stringResource(if (state.isPaletteSaved) R.string.palette_saved else R.string.save_palette)
                    ) {
                        Icon(
                            if (state.isPaletteSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    CameraChromeButton(onClick = onMenuClick, contentDescription = stringResource(R.string.open_library)) {
                        Icon(Icons.Rounded.Palette, contentDescription = null, tint = Color.White)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (flashAvailable) {
                        val flashTint by animateColorAsState(
                            targetValue = if (flashEnabled) Color(0xFFFFD60A) else Color.White,
                            animationSpec = if (reducedMotion) snap() else ExpressiveEffectsColorSpring,
                            label = "Flash tint"
                        )
                        CameraChromeButton(onClick = onToggleFlash, contentDescription = stringResource(R.string.toggle_flash)) {
                            Icon(
                                if (flashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                                contentDescription = null,
                                tint = flashTint
                            )
                        }
                    }
                    val gridTint by animateColorAsState(
                        targetValue = if (gridEnabled) Color(0xFFFFD60A) else Color.White,
                        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsColorSpring,
                        label = "Grid tint"
                    )
                    CameraChromeButton(onClick = onToggleGrid, contentDescription = stringResource(R.string.toggle_grid)) {
                        Icon(
                            Icons.Rounded.GridOn,
                            contentDescription = null,
                            tint = gridTint
                        )
                    }
                    CameraChromeButton(onClick = onFlipCamera, contentDescription = stringResource(R.string.flip_camera)) {
                        Icon(Icons.Rounded.FlipCameraIos, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.isCapturing,
            modifier = Modifier.align(Alignment.Center),
            enter = if (reducedMotion) {
                fadeIn(snap())
            } else {
                fadeIn(tween(120)) + scaleIn(tween(140), initialScale = 0.92f)
            },
            exit = if (reducedMotion) {
                fadeOut(snap())
            } else {
                fadeOut(tween(100)) + scaleOut(tween(120), targetScale = 0.92f)
            }
        ) {
            CaptureProgress()
        }

        AnimatedVisibility(
            visible = capturedImage != null && !state.isCapturing && state.palette.isNotEmpty(),
            modifier = if (reviewOnSide && compactHeight) {
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 20.dp + safeEndPadding,
                        bottom = dockClearance(extraAboveIsland = 8.dp)
                    )
                    .width(380.dp)
            } else if (reviewOnSide) {
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(
                        end = 20.dp + safeEndPadding,
                        top = 88.dp,
                        bottom = dockPadding
                    )
                    .width(380.dp)
            } else {
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 14.dp + safeStartPadding,
                        end = 14.dp + safeEndPadding,
                        bottom = dockPadding
                    )
                    .widthIn(max = 480.dp)
            },
            enter = if (reducedMotion) {
                fadeIn(snap())
            } else {
                fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.96f, transformOrigin = reviewTransformOrigin)
            },
            exit = if (reducedMotion) {
                fadeOut(snap())
            } else {
                fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.96f, transformOrigin = reviewTransformOrigin)
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(22.dp), clip = false),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                if (state.palette.isNotEmpty()) {
                    Column(modifier = Modifier.padding(if (compactHeight && reviewOnSide) 12.dp else 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.paletteName.ifBlank { stringResource(R.string.live_palette) },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = pluralStringResource(
                                        R.plurals.library_color_count,
                                        state.palette.size,
                                        state.palette.size
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = stringResource(R.string.tap_to_inspect),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(if (compactHeight && reviewOnSide) 8.dp else 14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    when {
                                        largeTouchTargets -> 64.dp
                                        compactHeight && reviewOnSide -> 40.dp
                                        else -> 56.dp
                                    }
                                )
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            state.palette.forEach { paletteColor ->
                                key(paletteColor.hexCode) {
                                    PaletteSwatchChip(paletteColor = paletteColor, onClick = onColorSelected)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(if (compactHeight && reviewOnSide) 8.dp else 14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SecondaryButton(
                                text = stringResource(R.string.retake),
                                onClick = onClearCapture,
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryButton(
                                text = stringResource(R.string.editor_title),
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
                .padding(start = 18.dp, end = 18.dp, bottom = liveStripPadding)
                .widthIn(max = 480.dp),
            enter = if (reducedMotion) {
                fadeIn(snap())
            } else {
                fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.96f, transformOrigin = TransformOrigin(0.5f, 1f))
            },
            exit = if (reducedMotion) {
                fadeOut(snap())
            } else {
                fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.96f, transformOrigin = TransformOrigin(0.5f, 1f))
            }
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
                        text = stringResource(R.string.live_sampling),
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
                            key(paletteColor.hexCode) {
                                PaletteSwatchChip(paletteColor = paletteColor, onClick = onColorSelected)
                            }
                        }
                    }
                    CameraChromeButton(
                        onClick = onSavePalette,
                        contentDescription = stringResource(if (state.isPaletteSaved) R.string.palette_saved else R.string.save_palette)
                    ) {
                        Icon(
                            if (state.isPaletteSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = capturedImage == null && !state.isCapturing,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = dockPadding),
            enter = if (reducedMotion) fadeIn(snap()) else fadeIn(tween(140)),
            exit = if (reducedMotion) fadeOut(snap()) else fadeOut(tween(100))
        ) {
            CameraShutterButton(onClick = onCapture, onPress = onCapturePress)
        }
    }
}

@Composable
private fun CameraShutterButton(
    onClick: () -> Unit,
    onPress: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val latestOnPress = rememberUpdatedState(onPress)
    LaunchedEffect(isPressed) {
        if (isPressed) latestOnPress.value()
    }
    val reducedMotion = LocalReducedMotion.current
    val ringScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.96f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Shutter ring press"
    )
    val discScale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.93f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Shutter disc press"
    )
    val captureDescription = stringResource(R.string.capture_photo)
    Box(
        modifier = Modifier
            .size(76.dp)
            .semantics {
                contentDescription = captureDescription
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
                .size(76.dp)
                .scale(ringScale)
                .shadow(8.dp, CircleShape, clip = false)
                .border(4.dp, Color.White, CircleShape)
                .background(Color.Black.copy(alpha = 0.16f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(discScale)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.97f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Chrome button press"
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            ),
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

@Composable
private fun RowScope.PaletteSwatchChip(
    paletteColor: PaletteColor,
    onClick: (PaletteColor) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed) 0.96f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Swatch press"
    )
    val inspectDescription = stringResource(R.string.inspect_color, paletteColor.hexCode)
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .scale(scale)
            .background(Color(paletteColor.color))
            .semantics {
                contentDescription = inspectDescription
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = { onClick(paletteColor) }
            )
    )
}

/**
 * Color Detail Screen
 */
@Composable
fun ColorDetailScreen(
    paletteColor: PaletteColor,
    isColorSaved: Boolean,
    isPaletteSaved: Boolean,
    onCopy: (String) -> Unit,
    onSaveColor: (PaletteColor) -> Unit,
    onSavePalette: () -> Unit,
    onAddToPalette: () -> Unit,
    onRemoveFromPalette: () -> Unit,
    onShowColorBlindness: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroColor = Color(paletteColor.color)
    val heroContentColor = if (heroColor.luminance() > 0.179f) Color.Black else Color.White
    val groupedShape = RoundedCornerShape(12.dp)
    val copyHexDescription = stringResource(R.string.copy_hex)
    val navigationBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

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
                    contentDescription = stringResource(R.string.back_to_app),
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
                    color = heroContentColor,
                    modifier = Modifier
                        .clickable(onClick = { onCopy(paletteColor.hexCode) })
                        .semantics { contentDescription = copyHexDescription }
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
                .padding(
                    start = 16.dp,
                    top = 20.dp,
                    end = 16.dp,
                    bottom = 20.dp + navigationBottomPadding
                )
        ) {
            val hexLabel = stringResource(R.string.format_hex)
            val rgbLabel = stringResource(R.string.format_rgb)
            val hslLabel = stringResource(R.string.format_hsl)
            val formats = remember(paletteColor, hexLabel, rgbLabel, hslLabel) {
                val hslArray = AtelierData.rgbToHsl(paletteColor.red, paletteColor.green, paletteColor.blue)
                listOf(
                    hexLabel to paletteColor.hexCode,
                    rgbLabel to "rgb(${paletteColor.red}, ${paletteColor.green}, ${paletteColor.blue})",
                    hslLabel to "hsl(${hslArray[0].roundToInt()}, ${hslArray[1].roundToInt()}%, ${hslArray[2].roundToInt()}%)"
                )
            }

            Text(
                text = stringResource(R.string.color_values),
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

            val relatedColors = remember(paletteColor.color) {
                AtelierData.generateRelatedColors(paletteColor.color)
            }

            Text(
                text = stringResource(R.string.tints),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(relatedColors.first, key = PaletteColor::hexCode) { relatedColor ->
                    RelatedColorSwatch(
                        color = relatedColor,
                        onClick = { onSaveColor(relatedColor) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.shades),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(relatedColors.second, key = PaletteColor::hexCode) { relatedColor ->
                    RelatedColorSwatch(
                        color = relatedColor,
                        onClick = { onSaveColor(relatedColor) }
                    )
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
                        text = stringResource(R.string.add_to_palette),
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
                        label = stringResource(if (isColorSaved) R.string.color_saved else R.string.save_color),
                        trailingIcon = if (isColorSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
                        onClick = { onSaveColor(paletteColor) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    )
                    DetailActionRow(
                        label = stringResource(if (isPaletteSaved) R.string.palette_saved else R.string.save_palette),
                        trailingIcon = if (isPaletteSaved) Icons.Rounded.Check else Icons.Rounded.BookmarkBorder,
                        onClick = onSavePalette
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    )
                    DetailActionRow(
                        label = stringResource(R.string.color_vision),
                        trailingIcon = Icons.Rounded.ChevronRight,
                        onClick = onShowColorBlindness
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    )
                    DetailActionRow(
                        label = stringResource(R.string.remove_from_palette),
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
                text = stringResource(R.string.copy),
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
private fun RelatedColorSwatch(
    color: PaletteColor,
    onClick: () -> Unit
) {
    val saveDescription = stringResource(R.string.save_related_color, color.hexCode)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(color.color))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .semantics {
                contentDescription = saveDescription
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomEnd
    ) {
        Icon(
            imageVector = Icons.Rounded.BookmarkBorder,
            contentDescription = null,
            tint = if (Color(color.color).luminance() > 0.179f) Color.Black.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(6.dp).size(17.dp)
        )
    }
}

@Composable
private fun DetailActionRow(
    label: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
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
        trailingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            )
        }
    }
}

private class LiveLoupeHexCache {
    private var lastRgb = Int.MIN_VALUE
    private var lastHex = ""

    fun hex(color: Int): String {
        val rgb = 0xFFFFFF and color
        if (rgb == lastRgb && lastHex.isNotEmpty()) return lastHex
        lastRgb = rgb
        val chars = CharArray(7)
        chars[0] = '#'
        var value = rgb
        for (index in 6 downTo 1) {
            val nibble = value and 0xF
            chars[index] = if (nibble < 10) ('0' + nibble) else ('A' + (nibble - 10))
            value = value ushr 4
        }
        lastHex = String(chars)
        return lastHex
    }
}
