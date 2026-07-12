package com.vishnu.campalette.ui.components

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.R

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
    activity: MainActivity? = null
) {
    val context = LocalContext.current
    val cameraBindError = stringResource(R.string.camera_bind_error)
    val lifecycleOwner = LocalLifecycleOwner.current
    val boundCamera = remember { mutableStateOf<Camera?>(null) }
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val currentOnSampleTap = rememberUpdatedState(onSampleTap)
    val currentOnSampleLongPress = rememberUpdatedState(onSampleLongPress)
    val gestureDetector = remember(context, previewView) {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                previewView.bitmap?.let { bitmap ->
                    currentOnSampleTap.value(bitmap, Offset(e.x, e.y), IntSize(previewView.width, previewView.height))
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                previewView.bitmap?.let { bitmap ->
                    currentOnSampleLongPress.value(bitmap, Offset(e.x, e.y), IntSize(previewView.width, previewView.height))
                }
            }
        })
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
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                cameraProvider.unbindAll()
                val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                boundCamera.value = camera
                activity?.currentImageCapture = capture
                val hasFlash = camera.cameraInfo.hasFlashUnit()
                onFlashAvailabilityChanged(hasFlash)
            } catch (e: Exception) {
                Log.d("CameraPreview", "Camera bind failed", e)
                boundCamera.value = null
                activity?.currentImageCapture = null
                onFlashAvailabilityChanged(false)
                onCameraError(cameraBindError)
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
                onFlashAvailabilityChanged(false)
            }
        }
        onDispose { }
    }

    Box(modifier = modifier.onSizeChanged(onPreviewMeasured)) {
        AndroidView(
            factory = {
                previewView.apply {
                    setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        if (gridEnabled) {
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
