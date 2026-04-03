package com.vishnu.campalette.ui.screens

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.West
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.RadialMenuState
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.components.AtelierLabelTag
import com.vishnu.campalette.ui.components.GlassPanel
import com.vishnu.campalette.ui.components.GradientPrimaryButton
import com.vishnu.campalette.ui.components.PaletteStrip
import com.vishnu.campalette.ui.components.TechnicalValue
import com.vishnu.campalette.ui.theme.AtelierPrimaryFixed
import com.vishnu.campalette.ui.theme.AtelierSurface
import com.vishnu.campalette.ui.theme.AtelierTheme

@Composable
fun LiveCameraScreen(
    activity: MainActivity,
    modifier: Modifier,
    capturedImage: android.graphics.Bitmap?,
    palette: List<PaletteColor>,
    selectedColor: PaletteColor?,
    radialMenuState: RadialMenuState?,
    keepReticle: Boolean,
    gridEnabled: Boolean,
    flashEnabled: Boolean,
    flashAvailable: Boolean,
    lensFacing: Int,
    previewSize: IntSize,
    hasCapturedPalette: Boolean,
    paletteName: String,
    paletteSource: String,
    harmonyLabel: String,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPreviewMeasured: (IntSize) -> Unit,
    onFlipCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleGrid: () -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onSampleTap: (android.graphics.Bitmap, androidx.compose.ui.geometry.Offset, IntSize) -> Unit,
    onSampleLongPress: (android.graphics.Bitmap, androidx.compose.ui.geometry.Offset, IntSize) -> Unit,
    onColorSelected: (PaletteColor) -> Unit,
    onCapture: () -> Unit,
    onCameraError: (String) -> Unit
) {
    val activeColor = selectedColor ?: palette.firstOrNull()
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val overlayWhite = MaterialTheme.colorScheme.onPrimary
    val overlayPanel = AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.14f)

    BoxWithConstraints(modifier = modifier.background(MaterialTheme.colorScheme.inverseSurface)) {
        val sidePadding = 18.dp
        val bottomStackPadding = navInset + 98.dp

        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                activity = activity,
                modifier = Modifier.fillMaxSize(),
                lensFacing = lensFacing,
                torchEnabled = flashEnabled,
                onFlashAvailabilityChanged = onFlashAvailabilityChanged,
                onError = onCameraError
            )

            capturedImage?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged(onPreviewMeasured)
                        .pointerInput(bitmap, previewSize) {
                            detectTapGestures(
                                onTap = { onSampleTap(bitmap, it, previewSize) },
                                onLongPress = { onSampleLongPress(bitmap, it, previewSize) }
                            )
                        }
                )
            }

            if (gridEnabled) {
                GridOverlay(modifier = Modifier.fillMaxSize())
            }

            LiveTopBar(
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick
            )

            if (keepReticle) {
                activeColor?.let {
                    ReticleChip(
                        modifier = Modifier.align(Alignment.Center),
                        activeColor = it,
                        overlayWhite = overlayWhite
                    )
                }
            }

            AnimatedVisibility(
                visible = radialMenuState != null && capturedImage != null,
                enter = fadeIn(tween(160)) + scaleIn(tween(180), initialScale = 0.96f),
                exit = fadeOut(tween(120)) + scaleOut(tween(140), targetScale = 0.98f)
            ) {
                radialMenuState?.let { state ->
                    RadialPaletteMenu(
                        modifier = Modifier.fillMaxSize(),
                        center = state.center,
                        colors = palette.map { it.color },
                        selectedIndex = state.selectedIndex,
                        touchedColor = state.touchedColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = sidePadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CameraControlButton(
                    icon = Icons.Rounded.FlipCameraIos,
                    active = lensFacing == CameraSelector.LENS_FACING_FRONT,
                    enabled = true,
                    label = stringResource(R.string.camera_control_flip),
                    onClick = onFlipCamera
                )
                CameraControlButton(
                    icon = Icons.Rounded.FlashOn,
                    active = flashEnabled,
                    enabled = flashAvailable,
                    label = stringResource(R.string.camera_control_flash),
                    onClick = onToggleFlash
                )
                CameraControlButton(
                    icon = Icons.Rounded.GridOn,
                    active = gridEnabled,
                    enabled = true,
                    label = stringResource(R.string.camera_control_grid),
                    onClick = onToggleGrid
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = bottomStackPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    background = overlayPanel,
                    contentPadding = PaddingValues(start = 24.dp, top = 22.dp, end = 20.dp, bottom = 22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AtelierLabelTag(
                                stringResource(R.string.live_palette_title),
                                color = overlayWhite.copy(alpha = 0.74f)
                            )
                            Text(
                                text = if (hasCapturedPalette) paletteName else stringResource(R.string.live_palette_waiting),
                                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                                color = overlayWhite,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (hasCapturedPalette) {
                                    paletteSource.ifBlank { stringResource(R.string.live_palette_body) }
                                } else {
                                    stringResource(R.string.live_palette_waiting_body)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = overlayWhite.copy(alpha = 0.78f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (hasCapturedPalette) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AtelierLabelTag(
                                    stringResource(R.string.editor_harmony_result),
                                    color = overlayWhite.copy(alpha = 0.74f)
                                )
                                Text(
                                    text = harmonyLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = overlayWhite
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                    if (hasCapturedPalette && activeColor != null) {
                        PaletteStrip(
                            palette = palette.take(5),
                            selectedHex = activeColor.hexCode,
                            labelColor = overlayWhite,
                            onColorSelected = onColorSelected,
                            trailingAdd = onCapture
                        )
                    } else {
                        PaletteStrip(
                            palette = emptyList(),
                            selectedHex = null,
                            labelColor = overlayWhite,
                            onColorSelected = onColorSelected,
                            trailingAdd = onCapture
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReticleChip(
    modifier: Modifier = Modifier,
    activeColor: PaletteColor,
    overlayWhite: Color
) {
    val compact = LocalConfiguration.current.screenWidthDp < 400 || LocalDensity.current.fontScale > 1.1f
    BoxWithConstraints(modifier = modifier.padding(horizontal = 24.dp)) {
        val textModifier = if (compact) Modifier.fillMaxWidth() else Modifier.widthIn(max = maxWidth * 0.46f)
        if (compact) {
            Column(
                modifier = Modifier.fillMaxWidth(0.78f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ReticleTarget(overlayWhite = overlayWhite)
                ReticleInfoCard(
                    modifier = textModifier,
                    activeColor = activeColor,
                    overlayWhite = overlayWhite
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ReticleTarget(overlayWhite = overlayWhite)
                ReticleInfoCard(
                    modifier = textModifier,
                    activeColor = activeColor,
                    overlayWhite = overlayWhite
                )
            }
        }
    }
}

@Composable
private fun ReticleTarget(overlayWhite: Color) {
    Canvas(modifier = Modifier.size(80.dp)) {
        drawCircle(
            color = overlayWhite.copy(alpha = 0.42f),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(color = overlayWhite, radius = 2.dp.toPx())
    }
}

@Composable
private fun ReticleInfoCard(
    modifier: Modifier,
    activeColor: PaletteColor,
    overlayWhite: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.34f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = activeColor.hexCode,
            style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
            color = overlayWhite,
            maxLines = 1
        )
        Text(
            text = activeColor.name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = overlayWhite.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CameraControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val background = when {
        !enabled -> AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.08f)
        active -> MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
        else -> AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.12f)
    }
    val state = when {
        !enabled -> stringResource(R.string.camera_control_unavailable, label)
        active -> stringResource(R.string.camera_control_on, label)
        else -> stringResource(R.string.camera_control_off, label)
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .semantics {
                role = Role.Button
                contentDescription = label
                stateDescription = state
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (enabled) 1f else 0.4f)
        )
    }
}

@Composable
private fun LiveTopBar(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val openLibraryLabel = stringResource(R.string.open_library)
    val openSettingsLabel = stringResource(R.string.open_settings)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(AtelierTheme.colors.surfaceContainerLow.copy(alpha = 0.88f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .semantics {
                            role = Role.Button
                            contentDescription = openLibraryLabel
                        }
                        .clickable(onClick = onMenuClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Text(
                            text = stringResource(R.string.live_view).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.atelier_brand),
                style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.primary
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AtelierSurface.copy(alpha = 0.66f))
                        .semantics {
                            role = Role.Button
                            contentDescription = openSettingsLabel
                        }
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun GridOverlay(modifier: Modifier = Modifier) {
    val overlayColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
    Canvas(modifier = modifier) {
        val stroke = 1.dp.toPx()
        val color = overlayColor
        val oneThirdX = size.width / 3f
        val twoThirdX = oneThirdX * 2f
        val oneThirdY = size.height / 3f
        val twoThirdY = oneThirdY * 2f
        drawLine(color, start = androidx.compose.ui.geometry.Offset(oneThirdX, 0f), end = androidx.compose.ui.geometry.Offset(oneThirdX, size.height), strokeWidth = stroke)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(twoThirdX, 0f), end = androidx.compose.ui.geometry.Offset(twoThirdX, size.height), strokeWidth = stroke)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, oneThirdY), end = androidx.compose.ui.geometry.Offset(size.width, oneThirdY), strokeWidth = stroke)
        drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, twoThirdY), end = androidx.compose.ui.geometry.Offset(size.width, twoThirdY), strokeWidth = stroke)
    }
}

@Composable
private fun CameraPreview(
    activity: MainActivity,
    modifier: Modifier = Modifier,
    lensFacing: Int,
    torchEnabled: Boolean,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lifecycleOwner, cameraProviderFuture, lensFacing, torchEnabled) {
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                cameraProvider.unbindAll()
                val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                activity.currentImageCapture = capture
                val hasFlash = camera.cameraInfo.hasFlashUnit()
                onFlashAvailabilityChanged(hasFlash)
                try {
                    camera.cameraControl.enableTorch(hasFlash && torchEnabled)
                } catch (_: Exception) {
                    onFlashAvailabilityChanged(false)
                }
            } catch (e: Exception) {
                Log.d("CameraPreview", "Camera bind failed", e)
                activity.currentImageCapture = null
                onFlashAvailabilityChanged(false)
                onError(context.getString(R.string.camera_bind_error))
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (_: Exception) {
            } finally {
                activity.currentImageCapture = null
            }
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

@Composable
private fun RadialPaletteMenu(
    modifier: Modifier,
    center: androidx.compose.ui.geometry.Offset,
    colors: List<Int>,
    selectedIndex: Int,
    touchedColor: Int
) {
    val highlightColor = AtelierPrimaryFixed.copy(alpha = 0.3f)
    val strokeColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
    Canvas(modifier = modifier) {
        if (colors.isEmpty()) return@Canvas
        val ringRadius = 112.dp.toPx()
        val ringStroke = 28.dp.toPx()
        val highlightStroke = ringStroke + 10.dp.toPx()
        val sweep = 360f / colors.size
        val topLeft = androidx.compose.ui.geometry.Offset(center.x - ringRadius, center.y - ringRadius)
        val size = androidx.compose.ui.geometry.Size(ringRadius * 2, ringRadius * 2)

        if (selectedIndex in colors.indices) {
            drawArc(
                color = highlightColor,
                startAngle = -90f + selectedIndex * sweep,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = highlightStroke, cap = StrokeCap.Round)
            )
        }
        colors.forEachIndexed { index, colorValue ->
            drawArc(
                color = Color(colorValue),
                startAngle = -90f + index * sweep,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = ringStroke, cap = StrokeCap.Round)
            )
        }
        drawCircle(color = Color(touchedColor), radius = 22.dp.toPx(), center = center)
        drawCircle(
            color = strokeColor,
            radius = 24.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun ColorDetailScreen(
    modifier: Modifier,
    paletteColor: PaletteColor,
    onCopy: (String) -> Unit,
    onAddToPalette: () -> Unit,
    onBack: () -> Unit
) {
    val cmyk = AtelierData.rgbToCmyk(paletteColor.red, paletteColor.green, paletteColor.blue)
    val backLabel = stringResource(R.string.back_to_app)
    val copyHexLabel = stringResource(R.string.copy_hex)
    Box(modifier = modifier.background(Color(paletteColor.color))) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AtelierSurface.copy(alpha = 0.82f))
                    .semantics {
                        role = Role.Button
                        contentDescription = backLabel
                    }
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.West, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Box(modifier = Modifier.fillMaxHeight(0.4f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                    .background(AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.94f))
                    .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 28.dp)
            ) {
                AtelierLabelTag(stringResource(R.string.detail_tag))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = paletteColor.hexCode,
                    style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .semantics {
                            role = Role.Button
                            contentDescription = copyHexLabel
                        }
                        .clickable { onCopy(paletteColor.hexCode) }
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    TechnicalValue("RGB", "${paletteColor.red}, ${paletteColor.green}, ${paletteColor.blue}")
                    TechnicalValue(
                        "CMYK",
                        "${AtelierData.run { cmyk[0].format0() }} ${AtelierData.run { cmyk[1].format0() }} ${AtelierData.run { cmyk[2].format0() }} ${AtelierData.run { cmyk[3].format0() }}"
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = paletteColor.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
                GradientPrimaryButton(
                    text = stringResource(R.string.add_to_palette),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAddToPalette
                )
            }
        }
    }
}
