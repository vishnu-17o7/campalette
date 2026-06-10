package com.vishnu.campalette.ui.screens

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlipCameraIos
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.West
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.vishnu.campalette.ui.components.WavyProgressIndicator
import com.vishnu.campalette.ui.theme.ExpressiveSpatialSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsSpring
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.Dp
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
import com.vishnu.campalette.ui.theme.AtelierRoundedExtra
import com.vishnu.campalette.ui.theme.AtelierSurface
import com.vishnu.campalette.ui.theme.AtelierTheme
import kotlinx.coroutines.delay

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
    isCapturing: Boolean = false,
    onClearCapture: (() -> Unit)? = null,
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
    onCameraError: (String) -> Unit
) {
    val activeColor = selectedColor ?: palette.firstOrNull()
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var sampledPoint by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }
    val overlayWhite = MaterialTheme.colorScheme.onPrimary
    val overlayPanel = AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.14f)

    BoxWithConstraints(modifier = modifier.background(MaterialTheme.colorScheme.inverseSurface)) {
        val sidePadding = 18.dp
        val bottomStackPadding = navInset + 98.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged(onPreviewMeasured)
        ) {
            CameraPreview(
                activity = activity,
                modifier = Modifier.fillMaxSize(),
                lensFacing = lensFacing,
                torchEnabled = flashEnabled,
                onFlashAvailabilityChanged = onFlashAvailabilityChanged,
                onError = onCameraError
            )

            capturedImage?.let { bitmap ->
                AnimatedVisibility(
                    visible = capturedImage != null,
                    enter = fadeIn(tween(180)) + scaleIn(tween(200, delayMillis = 20), initialScale = 0.96f),
                    exit = fadeOut(tween(150))
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.capture_button),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(bitmap, previewSize) {
                                detectTapGestures(
                                    onTap = {
                                        sampledPoint = it
                                        onSampleTap(bitmap, it, previewSize)
                                    },
                                    onLongPress = {
                                        sampledPoint = it
                                        onSampleLongPress(bitmap, it, previewSize)
                                    }
                                )
                            }
                    )
                }
            }

            sampledPoint?.let { point ->
                SampledPointIndicator(
                    point = point,
                    onFinish = { sampledPoint = null }
                )
            }

            if (gridEnabled) {
                GridOverlay(modifier = Modifier.fillMaxSize())
            }

            LiveTopBar(
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick
            )

            if (isCapturing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Analyzing Colors...",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        WavyProgressIndicator(
                            color = MaterialTheme.colorScheme.inversePrimary,
                            strokeWidth = 3.5.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                        )
                    }
                }
            }

            if (capturedImage != null && onClearCapture != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 14.dp, top = 64.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.82f))
                        .clickable(onClick = onClearCapture),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.back_to_app),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

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
                    .padding(end = sidePadding, bottom = bottomStackPadding + 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

            if (hasCapturedPalette) {
                AnimatedVisibility(
                    visible = hasCapturedPalette,
                    enter = fadeIn(tween(180)) + scaleIn(tween(200, delayMillis = 60), initialScale = 0.94f),
                    exit = fadeOut(tween(160))
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 16.dp, end = 16.dp, bottom = bottomStackPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlassPanel(
                            modifier = Modifier.fillMaxWidth(0.92f),
                            background = overlayPanel,
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = paletteName,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = harmonyLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = overlayWhite.copy(alpha = 0.8f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (activeColor != null) {
                                    PaletteStrip(
                                        palette = palette.take(5),
                                        selectedHex = activeColor.hexCode,
                                        labelColor = overlayWhite,
                                        onColorSelected = onColorSelected,
                                        trailingAdd = null,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
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
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "cameraCtrlScale"
    )
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

    val ringAlpha by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "ctrlRingAlpha"
    )
    val ringColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .scale(scale)
            .size(52.dp)
            .clip(CircleShape)
            .then(
                if (active) Modifier.drawBehind {
                    drawCircle(
                        color = ringColor.copy(alpha = ringAlpha),
                        radius = this.size.minDimension / 2f + 2.dp.toPx(),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else Modifier
            )
            .background(background)
            .semantics {
                role = Role.Button
                contentDescription = label
                stateDescription = state
            }
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = if (enabled) 1f else 0.4f)
        )
    }
}

@Composable
private fun LiveTopBar(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val openHistoryLabel = stringResource(R.string.open_library)
    val openSettingsLabel = stringResource(R.string.open_settings)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AtelierTheme.colors.surfaceContainerLow.copy(alpha = 0.72f))
                    .semantics {
                        role = Role.Button
                        contentDescription = openHistoryLabel
                    }
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = openHistoryLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = stringResource(R.string.atelier_brand),
                style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AtelierTheme.colors.surfaceContainerLow.copy(alpha = 0.72f))
                    .semantics {
                        role = Role.Button
                        contentDescription = openSettingsLabel
                    }
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = openSettingsLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
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
    val boundCamera = remember { mutableStateOf<Camera?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lensFacing) {
        var isDisposed = false
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            if (isDisposed) return@Runnable
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
                boundCamera.value = camera
                activity.currentImageCapture = capture
                val hasFlash = camera.cameraInfo.hasFlashUnit()
                onFlashAvailabilityChanged(hasFlash)
            } catch (e: Exception) {
                Log.d("CameraPreview", "Camera bind failed", e)
                boundCamera.value = null
                activity.currentImageCapture = null
                onFlashAvailabilityChanged(false)
                onError(context.getString(R.string.camera_bind_error))
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            isDisposed = true
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (_: Exception) {
            } finally {
                boundCamera.value = null
                activity.currentImageCapture = null
            }
        }
    }

    DisposableEffect(boundCamera.value, torchEnabled) {
        val camera = boundCamera.value
        if (camera != null) {
            val hasFlash = camera.cameraInfo.hasFlashUnit()
            try {
                camera.cameraControl.enableTorch(hasFlash && torchEnabled)
            } catch (_: Exception) {
                onFlashAvailabilityChanged(false)
            }
        }
        onDispose { }
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
private fun SampledPointIndicator(
    point: androidx.compose.ui.geometry.Offset,
    onFinish: () -> Unit
) {
    val alphaTarget = remember { mutableFloatStateOf(1f) }
    val scaleTarget = remember { mutableFloatStateOf(0.4f) }
    LaunchedEffect(point) {
        scaleTarget.value = 1.3f
        alphaTarget.value = 0f
        delay(400)
        onFinish()
    }
    val indicatorAlpha by animateFloatAsState(
        targetValue = alphaTarget.value,
        animationSpec = ExpressiveEffectsSpring,
        label = "sampleAlpha"
    )
    val indicatorScale by animateFloatAsState(
        targetValue = scaleTarget.value,
        animationSpec = ExpressiveSpatialSpring,
        label = "sampleScale"
    )
    Box(
        modifier = Modifier
            .offset { IntOffset((point.x - 24.dp.toPx()).toInt(), (point.y - 24.dp.toPx()).toInt()) }
            .graphicsLayer(
                alpha = indicatorAlpha,
                scaleX = indicatorScale,
                scaleY = indicatorScale
            )
            .size(48.dp)
            .drawBehind {
                drawCircle(
                    color = Color.White,
                    radius = 20.dp.toPx(),
                    style = Stroke(width = 2.5.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = 6.dp.toPx()
                )
            }
    )
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
                Icon(Icons.Rounded.West, contentDescription = backLabel, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
            Box(modifier = Modifier.fillMaxHeight(0.4f))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = AtelierRoundedExtra, topEnd = AtelierRoundedExtra))
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
