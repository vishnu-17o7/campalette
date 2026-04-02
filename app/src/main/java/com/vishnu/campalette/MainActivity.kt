package com.vishnu.campalette

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.palette.graphics.Palette
import com.vishnu.campalette.ui.theme.CampaletteTheme
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CampaletteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen(cameraExecutor = cameraExecutor)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CameraScreen(cameraExecutor: ExecutorService) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val clipboardManager = LocalClipboardManager.current
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasCameraPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            if (!hasCameraPermission) {
                PermissionScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
                return@Scaffold
            }

            var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
            var colorPalette by remember { mutableStateOf<List<PaletteColor>>(emptyList()) }
            var radialMenuState by remember { mutableStateOf<RadialMenuState?>(null) }
            var previewSize by remember { mutableStateOf(IntSize.Zero) }
            var selectedColorDetails by remember { mutableStateOf<PaletteColor?>(null) }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val panelHeight = maxHeight * 0.24f

                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        tonalElevation = 2.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                onError = { message ->
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            )

                            capturedImage?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .onSizeChanged { previewSize = it }
                                        .pointerInput(bitmap, colorPalette, previewSize) {
                                            detectTapGestures(
                                                onTap = { offset ->
                                                    val sampledColor = sampleColorFromBitmap(
                                                        bitmap = bitmap,
                                                        touchPoint = offset,
                                                        containerSize = previewSize
                                                    ) ?: return@detectTapGestures
                                                    colorPalette = generatePaletteFromSeedColor(sampledColor)
                                                    radialMenuState = null
                                                },
                                                onLongPress = { offset ->
                                                    val sampledColor = sampleColorFromBitmap(
                                                        bitmap = bitmap,
                                                        touchPoint = offset,
                                                        containerSize = previewSize
                                                    ) ?: return@detectTapGestures
                                                    val selectedIndex = nearestPaletteIndex(
                                                        targetColor = sampledColor,
                                                        palette = colorPalette
                                                    )
                                                    radialMenuState = RadialMenuState(
                                                        center = offset,
                                                        touchedColor = sampledColor,
                                                        selectedIndex = selectedIndex
                                                    )
                                                }
                                            )
                                        }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.live_view),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = capturedImage == null,
                                enter = fadeIn(animationSpec = tween(220)) + scaleIn(
                                    animationSpec = tween(220),
                                    initialScale = 0.98f
                                ),
                                exit = fadeOut(animationSpec = tween(160)) + scaleOut(
                                    animationSpec = tween(160),
                                    targetScale = 0.98f
                                ),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ColorLens,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.capture_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = radialMenuState != null && colorPalette.isNotEmpty(),
                                enter = fadeIn(animationSpec = tween(140)) + scaleIn(
                                    animationSpec = tween(180),
                                    initialScale = 0.96f
                                ),
                                exit = fadeOut(animationSpec = tween(140)) + scaleOut(
                                    animationSpec = tween(160),
                                    targetScale = 0.98f
                                )
                            ) {
                                radialMenuState?.let { state ->
                                    val highlightColor =
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    val ringOutlineColor =
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    RadialPaletteMenu(
                                        modifier = Modifier.fillMaxSize(),
                                        center = state.center,
                                        colors = colorPalette.map { it.color },
                                        selectedIndex = state.selectedIndex,
                                        touchedColor = state.touchedColor,
                                        highlightColor = highlightColor,
                                        ringOutlineColor = ringOutlineColor
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(panelHeight + 60.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f),
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    PalettePanel(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(panelHeight)
                            .align(Alignment.BottomCenter),
                        palette = colorPalette,
                        onColorSelected = { paletteColor ->
                            val detailString = buildColorDetailString(paletteColor)
                            clipboardManager.setText(AnnotatedString(detailString))
                            scope.launch {
                                snackbarHostState.showSnackbar(getString(R.string.copied))
                            }
                            selectedColorDetails = paletteColor
                        }
                    )

                    CaptureButton(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = panelHeight + 14.dp),
                        onClick = {
                            takePhoto(
                                cameraExecutor = cameraExecutor,
                                imageCapture = currentImageCapture,
                                onImageCaptured = { bitmap ->
                                    capturedImage = bitmap
                                    colorPalette = extractColorPalette(bitmap)
                                    radialMenuState = null
                                },
                                onError = { message ->
                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                }
                            )
                        }
                    )
                }
            }

            selectedColorDetails?.let { paletteColor ->
                ColorDetailDialog(
                    paletteColor = paletteColor,
                    onCopy = { value ->
                        clipboardManager.setText(AnnotatedString(value))
                        scope.launch {
                            snackbarHostState.showSnackbar(getString(R.string.copied))
                        }
                    },
                    onDismiss = { selectedColorDetails = null }
                )
            }
        }
    }

    private var currentImageCapture: ImageCapture? = null

    @Composable
    fun PermissionScreen(
        modifier: Modifier = Modifier,
        onRequestPermission: () -> Unit
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.camera_permission_required),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onRequestPermission) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }

    @Composable
    fun CameraPreview(
        modifier: Modifier = Modifier,
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

        DisposableEffect(lifecycleOwner, cameraProviderFuture) {
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

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        capture
                    )
                    currentImageCapture = capture
                } catch (e: Exception) {
                    Log.d("CameraPreview", "Camera bind failed", e)
                    currentImageCapture = null
                    onError(context.getString(R.string.camera_bind_error))
                }
            }
            cameraProviderFuture.addListener(listener, executor)

            onDispose {
                try {
                    if (cameraProviderFuture.isDone) {
                        cameraProviderFuture.get().unbindAll()
                    }
                } catch (e: Exception) {
                    Log.d("CameraPreview", "Camera unbind failed", e)
                } finally {
                    currentImageCapture = null
                }
            }
        }

        AndroidView(
            factory = { previewView },
            modifier = modifier
        )
    }

    @Composable
    fun CaptureButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.take_photo),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    @Composable
    fun PalettePanel(
        modifier: Modifier = Modifier,
        palette: List<PaletteColor>,
        onColorSelected: (PaletteColor) -> Unit
    ) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 10.dp)
                        .size(width = 48.dp, height = 4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.color_palette),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.palette_ready),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = palette.isEmpty(),
                    enter = fadeIn(animationSpec = tween(240)) + scaleIn(
                        animationSpec = tween(240),
                        initialScale = 0.98f
                    ),
                    exit = fadeOut(animationSpec = tween(180)) + scaleOut(
                        animationSpec = tween(160),
                        targetScale = 0.98f
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.palette_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.palette_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = palette.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(240)) + scaleIn(
                        animationSpec = tween(240),
                        initialScale = 0.98f
                    ),
                    exit = fadeOut(animationSpec = tween(180)) + scaleOut(
                        animationSpec = tween(160),
                        targetScale = 0.98f
                    )
                ) {
                    Column {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(palette) { paletteColor ->
                                PaletteSwatch(
                                    paletteColor = paletteColor,
                                    onClick = { onColorSelected(paletteColor) }
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(palette) { paletteColor ->
                                ColorCard(
                                    paletteColor = paletteColor,
                                    onClick = { onColorSelected(paletteColor) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PaletteSwatch(
        paletteColor: PaletteColor,
        onClick: () -> Unit
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(paletteColor.color))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable(onClick = onClick)
            )
            Text(
                text = paletteColor.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    fun ColorCard(
        paletteColor: PaletteColor,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(paletteColor.color), RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = paletteColor.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.hex_color, paletteColor.hexCode),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            R.string.rgb_color,
                            paletteColor.red,
                            paletteColor.green,
                            paletteColor.blue
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    @Composable
    fun RadialPaletteMenu(
        modifier: Modifier = Modifier,
        center: Offset,
        colors: List<Int>,
        selectedIndex: Int,
        touchedColor: Int,
        highlightColor: Color,
        ringOutlineColor: Color
    ) {
        if (colors.isEmpty()) return

        Canvas(modifier = modifier) {
            val ringRadius = 110.dp.toPx()
            val ringStroke = 28.dp.toPx()
            val highlightStroke = ringStroke + 10.dp.toPx()
            val sweep = 360f / colors.size
            val topLeft = Offset(center.x - ringRadius, center.y - ringRadius)
            val size = Size(ringRadius * 2, ringRadius * 2)

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

            drawCircle(
                color = Color(touchedColor),
                radius = 22.dp.toPx(),
                center = center
            )
            drawCircle(
                color = ringOutlineColor,
                radius = 24.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    private fun takePhoto(
        cameraExecutor: ExecutorService,
        imageCapture: ImageCapture?,
        onImageCaptured: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        if (imageCapture == null) {
            onError(getString(R.string.capture_unavailable))
            return
        }

        val mainExecutor = ContextCompat.getMainExecutor(this)
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val bitmap = imageProxyToBitmap(image)
                        mainExecutor.execute { onImageCaptured(bitmap) }
                    } catch (e: Exception) {
                        Log.d("CameraCapture", "Photo capture failed", e)
                        mainExecutor.execute { onError(getString(R.string.capture_failed)) }
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d("CameraCapture", "Photo capture failed", exception)
                    mainExecutor.execute { onError(getString(R.string.capture_failed)) }
                }
            }
        )
    }

    private fun extractColorPalette(bitmap: Bitmap): List<PaletteColor> {
        val palette = Palette.from(bitmap).generate()
        val colors = mutableListOf<PaletteColor>()

        fun addColor(nameRes: Int, rgb: Int?) {
            rgb?.let {
                val red = android.graphics.Color.red(it)
                val green = android.graphics.Color.green(it)
                val blue = android.graphics.Color.blue(it)
                colors.add(
                    PaletteColor(
                        name = getString(nameRes),
                        color = it,
                        hexCode = it.toHexCode(),
                        red = red,
                        green = green,
                        blue = blue
                    )
                )
            }
        }

        addColor(R.string.dominant_color, palette.dominantSwatch?.rgb)
        addColor(R.string.vibrant_color, palette.vibrantSwatch?.rgb)
        addColor(R.string.light_vibrant, palette.lightVibrantSwatch?.rgb)
        addColor(R.string.dark_vibrant, palette.darkVibrantSwatch?.rgb)
        addColor(R.string.muted_color, palette.mutedSwatch?.rgb)
        addColor(R.string.light_muted, palette.lightMutedSwatch?.rgb)
        addColor(R.string.dark_muted, palette.darkMutedSwatch?.rgb)

        return colors
    }

    private fun generatePaletteFromSeedColor(seedColor: Int): List<PaletteColor> {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(seedColor, hsv)

        fun shiftHue(degrees: Float): Int {
            val shifted = floatArrayOf((hsv[0] + degrees) % 360f, hsv[1], hsv[2])
            return android.graphics.Color.HSVToColor(shifted)
        }

        fun tint(): Int {
            val tinted = floatArrayOf(
                hsv[0],
                (hsv[1] * 0.6f).coerceIn(0f, 1f),
                (hsv[2] * 1.18f).coerceIn(0f, 1f)
            )
            return android.graphics.Color.HSVToColor(tinted)
        }

        fun shade(): Int {
            val shaded = floatArrayOf(
                hsv[0],
                (hsv[1] * 1.05f).coerceIn(0f, 1f),
                (hsv[2] * 0.6f).coerceIn(0f, 1f)
            )
            return android.graphics.Color.HSVToColor(shaded)
        }

        val generated = listOf(
            Pair(R.string.palette_seed, seedColor),
            Pair(R.string.palette_complement, shiftHue(180f)),
            Pair(R.string.palette_analog_a, shiftHue(30f)),
            Pair(R.string.palette_analog_b, shiftHue(330f)),
            Pair(R.string.palette_tint, tint()),
            Pair(R.string.palette_shade, shade())
        )

        return generated.map { (nameRes, color) ->
            PaletteColor(
                name = getString(nameRes),
                color = color,
                hexCode = color.toHexCode(),
                red = android.graphics.Color.red(color),
                green = android.graphics.Color.green(color),
                blue = android.graphics.Color.blue(color)
            )
        }
    }

    private fun sampleColorFromBitmap(
        bitmap: Bitmap,
        touchPoint: Offset,
        containerSize: IntSize
    ): Int? {
        if (containerSize.width == 0 || containerSize.height == 0) return null

        val scale = max(
            containerSize.width.toFloat() / bitmap.width.toFloat(),
            containerSize.height.toFloat() / bitmap.height.toFloat()
        )
        val displayedWidth = bitmap.width * scale
        val displayedHeight = bitmap.height * scale
        val offsetX = (displayedWidth - containerSize.width) / 2f
        val offsetY = (displayedHeight - containerSize.height) / 2f
        val bitmapX = ((touchPoint.x + offsetX) / scale).toInt()
        val bitmapY = ((touchPoint.y + offsetY) / scale).toInt()

        if (bitmapX !in 0 until bitmap.width || bitmapY !in 0 until bitmap.height) {
            return null
        }
        return bitmap.getPixel(bitmapX, bitmapY)
    }

    private fun nearestPaletteIndex(targetColor: Int, palette: List<PaletteColor>): Int {
        if (palette.isEmpty()) return -1
        val targetR = android.graphics.Color.red(targetColor)
        val targetG = android.graphics.Color.green(targetColor)
        val targetB = android.graphics.Color.blue(targetColor)
        var bestIndex = 0
        var bestDistance = Int.MAX_VALUE
        palette.forEachIndexed { index, paletteColor ->
            val dr = targetR - paletteColor.red
            val dg = targetG - paletteColor.green
            val db = targetB - paletteColor.blue
            val distance = dr * dr + dg * dg + db * db
            if (distance < bestDistance) {
                bestDistance = distance
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        if (image.format == ImageFormat.JPEG || image.planes.size == 1) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Bitmap decode failed")
        }

        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val yuvBytes = out.toByteArray()
        val decoded = BitmapFactory.decodeByteArray(yuvBytes, 0, yuvBytes.size)
            ?: throw IllegalStateException("Bitmap decode failed")

        val rotationDegrees = image.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        } else {
            decoded
        }
    }

    private fun Int.toHexCode(): String {
        return String.format("#%06X", 0xFFFFFF and this)
    }

    @Composable
    fun ColorDetailDialog(
        paletteColor: PaletteColor,
        onCopy: (String) -> Unit,
        onDismiss: () -> Unit
    ) {
        val colorValue = paletteColor.color
        val hsl = FloatArray(3)
        val hsv = FloatArray(3)
        ColorUtils.colorToHSL(colorValue, hsl)
        android.graphics.Color.colorToHSV(colorValue, hsv)

        val r = paletteColor.red
        val g = paletteColor.green
        val b = paletteColor.blue
        val cmyk = rgbToCmyk(r, g, b)
        val luminance = ColorUtils.calculateLuminance(colorValue)
        val detailString = buildColorDetailString(paletteColor)

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCopy(detailString)
                    }
                ) {
                    Text(stringResource(R.string.copy))
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.color_details),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(colorValue))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(18.dp)
                                )
                                .clickable { onCopy(paletteColor.hexCode) }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = paletteColor.name.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = paletteColor.hexCode,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { onCopy(paletteColor.hexCode) }
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.tap_to_copy),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

                    ColorDetailRow(
                        label = stringResource(R.string.rgb_label),
                        value = "${r}, ${g}, ${b}",
                        onCopy = onCopy
                    )
                    ColorDetailRow(
                        label = stringResource(R.string.hsl_label),
                        value = "${hsl[0].format0()}°, ${(hsl[1] * 100f).format0()}%, ${(hsl[2] * 100f).format0()}%",
                        onCopy = onCopy
                    )
                    ColorDetailRow(
                        label = stringResource(R.string.hsv_label),
                        value = "${hsv[0].format0()}°, ${(hsv[1] * 100f).format0()}%, ${(hsv[2] * 100f).format0()}%",
                        onCopy = onCopy
                    )
                    ColorDetailRow(
                        label = stringResource(R.string.cmyk_label),
                        value = "${cmyk[0].format0()}%, ${cmyk[1].format0()}%, ${cmyk[2].format0()}%, ${cmyk[3].format0()}%",
                        onCopy = onCopy
                    )
                    ColorDetailRow(
                        label = stringResource(R.string.luminance_label),
                        value = luminance.format2(),
                        onCopy = onCopy
                    )
                }
            }
        )
    }

    @Composable
    fun ColorDetailRow(
        label: String,
        value: String,
        onCopy: (String) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCopy(value) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(90.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    private fun rgbToCmyk(red: Int, green: Int, blue: Int): FloatArray {
        val r = red / 255f
        val g = green / 255f
        val b = blue / 255f
        val k = 1f - max(r, max(g, b))
        if (k >= 1f) {
            return floatArrayOf(0f, 0f, 0f, 100f)
        }
        val c = (1f - r - k) / (1f - k)
        val m = (1f - g - k) / (1f - k)
        val y = (1f - b - k) / (1f - k)
        return floatArrayOf(c * 100f, m * 100f, y * 100f, k * 100f)
    }

    private fun Float.format0(): String {
        return String.format("%.0f", this)
    }

    private fun Double.format2(): String {
        return String.format("%.2f", this)
    }

    private fun buildColorDetailString(paletteColor: PaletteColor): String {
        val hsl = FloatArray(3)
        val hsv = FloatArray(3)
        ColorUtils.colorToHSL(paletteColor.color, hsl)
        android.graphics.Color.colorToHSV(paletteColor.color, hsv)
        val cmyk = rgbToCmyk(paletteColor.red, paletteColor.green, paletteColor.blue)
        val luminance = ColorUtils.calculateLuminance(paletteColor.color)
        return buildString {
            appendLine(getString(R.string.hex_color, paletteColor.hexCode))
            appendLine("${getString(R.string.rgb_label)}: ${paletteColor.red}, ${paletteColor.green}, ${paletteColor.blue}")
            appendLine(
                "${getString(R.string.hsl_label)}: ${hsl[0].format0()}°, ${(hsl[1] * 100f).format0()}%, ${(hsl[2] * 100f).format0()}%"
            )
            appendLine(
                "${getString(R.string.hsv_label)}: ${hsv[0].format0()}°, ${(hsv[1] * 100f).format0()}%, ${(hsv[2] * 100f).format0()}%"
            )
            appendLine(
                "${getString(R.string.cmyk_label)}: ${cmyk[0].format0()}%, ${cmyk[1].format0()}%, ${cmyk[2].format0()}%, ${cmyk[3].format0()}%"
            )
            appendLine("${getString(R.string.luminance_label)}: ${luminance.format2()}")
        }.trimEnd()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

data class PaletteColor(
    val name: String,
    val color: Int,
    val hexCode: String,
    val red: Int,
    val green: Int,
    val blue: Int
)

data class RadialMenuState(
    val center: Offset,
    val touchedColor: Int,
    val selectedIndex: Int
)
