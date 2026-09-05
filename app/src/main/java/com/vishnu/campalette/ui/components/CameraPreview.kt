package com.vishnu.campalette.ui.components

import android.graphics.Bitmap
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.vishnu.campalette.BuildConfig
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import kotlin.math.roundToInt

private const val SAMPLE_BUFFER_MAX_DIMENSION = 480

// Palette extraction and the review screen never need more than ~1080px on the
// long edge, so we ask the sensor for a much smaller capture up front. This
// avoids encoding/decoding a full 12MP+ JPEG just to immediately downscale it,
// which was the dominant source of capture latency.
private const val CAPTURE_TARGET_LONG_EDGE = 1600
private const val CAPTURE_TARGET_SHORT_EDGE = 1200

private class PreviewSampleBuffer {
    private var bitmap: Bitmap? = null

    fun capture(previewView: PreviewView): Bitmap? {
        if (previewView.width <= 0 || previewView.height <= 0) return null
        val largestDimension = maxOf(previewView.width, previewView.height)
        val scale = minOf(1f, SAMPLE_BUFFER_MAX_DIMENSION.toFloat() / largestDimension)
        val targetWidth = (previewView.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (previewView.height * scale).roundToInt().coerceAtLeast(1)
        val current = bitmap
        val target = if (
            current == null || current.isRecycled ||
            current.width != targetWidth || current.height != targetHeight
        ) {
            current?.recycle()
            createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888).also { bitmap = it }
        } else {
            current
        }

        val textureView = previewView.findDescendantTextureView()
        textureView?.getBitmap(target)?.let { return it }
        val fallback = previewView.bitmap ?: return null
        return try {
            android.graphics.Canvas(target).drawBitmap(
                fallback,
                null,
                android.graphics.Rect(0, 0, target.width, target.height),
                android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
            )
            target
        } finally {
            fallback.takeUnless(Bitmap::isRecycled)?.recycle()
        }
    }

    fun recycle() {
        bitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        bitmap = null
    }
}

private fun View.findDescendantTextureView(): TextureView? {
    if (this is TextureView) return this
    if (this !is ViewGroup) return null
    repeat(childCount) { index ->
        getChildAt(index).findDescendantTextureView()?.let { return it }
    }
    return null
}

private class ExploreSession {
    var active: Boolean = false
    var pointerId: Int = MotionEvent.INVALID_POINTER_ID
    var frozen: Bitmap? = null

    fun end(): Boolean {
        if (!active) return false
        active = false
        pointerId = MotionEvent.INVALID_POINTER_ID
        frozen = null
        return true
    }
}

private fun startFocusAndMeteringAt(camera: Camera?, previewView: PreviewView, x: Float, y: Float) {
    if (camera == null) return
    try {
        val point = previewView.meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
        ).build()
        // Metering is async; never Future.get() on the UI/sample path.
        camera.cameraControl.startFocusAndMetering(action)
    } catch (_: Exception) {
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    flashEnabled: Boolean,
    gridEnabled: Boolean,
    onPreviewMeasured: (IntSize) -> Unit,
    onFlashAvailabilityChanged: (Boolean) -> Unit,
    onCameraError: (String) -> Unit,
    onSampleTap: (android.graphics.Bitmap, Offset, IntSize) -> Unit,
    onSampleLongPress: (android.graphics.Bitmap, Offset, IntSize) -> Unit,
    onSampleDrag: (android.graphics.Bitmap, Offset, IntSize) -> Unit = { _, _, _ -> },
    onSampleDragEnd: () -> Unit = {},
    activity: MainActivity? = null
) {
    val context = LocalContext.current
    val cameraBindError = stringResource(R.string.camera_bind_error)
    val cameraPreviewDescription = stringResource(R.string.camera_preview_sample)
    val lifecycleOwner = LocalLifecycleOwner.current
    val boundCamera = remember { mutableStateOf<Camera?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val sampleBuffer = remember { PreviewSampleBuffer() }
    val currentOnFlashAvailabilityChanged = rememberUpdatedState(onFlashAvailabilityChanged)
    val currentOnCameraError = rememberUpdatedState(onCameraError)
    val currentOnSampleTap = rememberUpdatedState(onSampleTap)
    val currentOnSampleLongPress = rememberUpdatedState(onSampleLongPress)
    val currentOnSampleDrag = rememberUpdatedState(onSampleDrag)
    val currentOnSampleDragEnd = rememberUpdatedState(onSampleDragEnd)
    val exploreSession = remember { ExploreSession() }
    val gestureDetector = remember(context, previewView) {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                if (exploreSession.active) return true
                val x = e.x
                val y = e.y
                sampleBuffer.capture(previewView)?.let { bitmap ->
                    currentOnSampleTap.value(bitmap, Offset(x, y), IntSize(previewView.width, previewView.height))
                }
                startFocusAndMeteringAt(boundCamera.value, previewView, x, y)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val bitmap = sampleBuffer.capture(previewView) ?: return
                exploreSession.active = true
                exploreSession.frozen = bitmap
                currentOnSampleLongPress.value(
                    bitmap,
                    Offset(e.x, e.y),
                    IntSize(previewView.width, previewView.height)
                )
            }
        }).apply { setOnDoubleTapListener(null) }
    }

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
                val captureResolutionSelector = ResolutionSelector.Builder()
                    .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                    .setResolutionStrategy(
                        ResolutionStrategy(
                            Size(CAPTURE_TARGET_LONG_EDGE, CAPTURE_TARGET_SHORT_EDGE),
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                        )
                    )
                    .build()
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setResolutionSelector(captureResolutionSelector)
                    .build()
                cameraProvider.unbindAll()
                val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                boundCamera.value = camera
                activity?.currentImageCapture = capture
                val hasFlash = camera.cameraInfo.hasFlashUnit()
                currentOnFlashAvailabilityChanged.value(hasFlash)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.d("CameraPreview", "Camera bind failed", e)
                }
                boundCamera.value = null
                activity?.currentImageCapture = null
                currentOnFlashAvailabilityChanged.value(false)
                currentOnCameraError.value(cameraBindError)
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            isDisposed = true
            if (exploreSession.end()) {
                currentOnSampleDragEnd.value()
            }
            sampleBuffer.recycle()
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (_: Exception) {
            } finally {
                boundCamera.value = null
                activity?.currentImageCapture = null
            }
        }
    }

    DisposableEffect(boundCamera.value, flashEnabled) {
        val camera = boundCamera.value
        if (camera != null) {
            val hasFlash = camera.cameraInfo.hasFlashUnit()
            try {
                camera.cameraControl.enableTorch(hasFlash && flashEnabled)
            } catch (_: Exception) {
                currentOnFlashAvailabilityChanged.value(false)
            }
        }
        onDispose { }
    }

    val reducedMotion = LocalReducedMotion.current
    Box(modifier = modifier.onSizeChanged(onPreviewMeasured)) {
        AndroidView(
            factory = {
                previewView.apply {
                    contentDescription = cameraPreviewDescription
                    setOnTouchListener { view, event ->
                        val action = event.actionMasked
                        if (action == MotionEvent.ACTION_DOWN) {
                            exploreSession.pointerId = event.getPointerId(0)
                            view.parent?.requestDisallowInterceptTouchEvent(true)
                        }

                        if (exploreSession.active) {
                            when (action) {
                                MotionEvent.ACTION_POINTER_DOWN -> return@setOnTouchListener true
                                MotionEvent.ACTION_POINTER_UP -> {
                                    if (event.getPointerId(event.actionIndex) == exploreSession.pointerId) {
                                        if (exploreSession.end()) currentOnSampleDragEnd.value()
                                        view.parent?.requestDisallowInterceptTouchEvent(false)
                                    }
                                    return@setOnTouchListener true
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    val index = event.findPointerIndex(exploreSession.pointerId)
                                    val frozen = exploreSession.frozen
                                    if (index >= 0 && frozen != null) {
                                        currentOnSampleDrag.value(
                                            frozen,
                                            Offset(event.getX(index), event.getY(index)),
                                            IntSize(previewView.width, previewView.height)
                                        )
                                    }
                                    return@setOnTouchListener true
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                    gestureDetector.onTouchEvent(event)
                                    if (exploreSession.end()) currentOnSampleDragEnd.value()
                                    view.parent?.requestDisallowInterceptTouchEvent(false)
                                    if (action == MotionEvent.ACTION_UP) view.performClick()
                                    return@setOnTouchListener true
                                }
                            }
                        }

                        val handled = gestureDetector.onTouchEvent(event)
                        if (action == MotionEvent.ACTION_UP) view.performClick()
                        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                            view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        handled || exploreSession.active
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        AnimatedVisibility(
            visible = gridEnabled,
            enter = if (reducedMotion) fadeIn(snap()) else fadeIn(tween(150)),
            exit = if (reducedMotion) fadeOut(snap()) else fadeOut(tween(120))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color.White.copy(alpha = 0.42f)
                val stroke = 1f
                drawLine(gridColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), stroke)
                drawLine(gridColor, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), stroke)
                drawLine(gridColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), stroke)
                drawLine(gridColor, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), stroke)
            }
        }
    }
}
