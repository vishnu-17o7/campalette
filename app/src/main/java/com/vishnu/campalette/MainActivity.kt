package com.vishnu.campalette

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.palette.graphics.Palette
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.vishnu.campalette.ui.theme.CampaletteTheme

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        setContent {
            CampaletteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen()
                }
            }
        }
    }

    @Composable
    fun CameraScreen() {
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
        var colorPalette by remember { mutableStateOf<List<PaletteColor>>(emptyList()) }

        if (!hasCameraPermission) {
            PermissionScreen {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Camera Preview - takes 60% of screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                ) {
                    CameraPreview(
                        onImageCaptured = { bitmap ->
                            capturedImage = bitmap
                            colorPalette = extractColorPalette(bitmap)
                        }
                    )
                }

                // Color Palette Display - takes 40% of screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (colorPalette.isNotEmpty()) {
                        ColorPaletteDisplay(colorPalette)
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Take a photo to see the color palette",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PermissionScreen(onRequestPermission: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getString(R.string.camera_permission_required),
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onRequestPermission) {
                    Text(getString(R.string.grant_permission))
                }
            }
        }
    }

    @Composable
    fun CameraPreview(onImageCaptured: (Bitmap) -> Unit) {
        val context = LocalContext.current
        val lifecycleOwner = context as LifecycleOwner
        var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = androidx.camera.view.PreviewView(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            imageCapture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Log.e("CameraPreview", "Use case binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Capture Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = {
                        imageCapture?.let { capture ->
                            capture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(image)
                                        onImageCaptured(bitmap)
                                        image.close()
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        Log.e("CameraPreview", "Photo capture failed", exception)
                                    }
                                }
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = getString(R.string.take_photo),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun ColorPaletteDisplay(palette: List<PaletteColor>) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = getString(R.string.color_palette),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(palette) { paletteColor ->
                ColorCard(paletteColor)
            }
        }
    }

    @Composable
    fun ColorCard(paletteColor: PaletteColor) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color(paletteColor.color),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = paletteColor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = paletteColor.hexCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    private fun extractColorPalette(bitmap: Bitmap): List<PaletteColor> {
        val palette = Palette.from(bitmap).generate()
        val colors = mutableListOf<PaletteColor>()

        palette.dominantSwatch?.let {
            colors.add(PaletteColor(getString(R.string.dominant_color), it.rgb, it.rgb.toHexCode()))
        }
        
        palette.vibrantSwatch?.let {
            colors.add(PaletteColor(getString(R.string.vibrant_color), it.rgb, it.rgb.toHexCode()))
        }
        
        palette.lightVibrantSwatch?.let {
            colors.add(PaletteColor(getString(R.string.light_vibrant), it.rgb, it.rgb.toHexCode()))
        }
        
        palette.darkVibrantSwatch?.let {
            colors.add(PaletteColor(getString(R.string.dark_vibrant), it.rgb, it.rgb.toHexCode()))
        }
        
        palette.mutedSwatch?.let {
            colors.add(PaletteColor(getString(R.string.muted_color), it.rgb, it.rgb.toHexCode()))
        }
        
        palette.lightMutedSwatch?.let {
            colors.add(PaletteColor(getString(R.string.light_muted), it.rgb, it.rgb.toHexCode()))
        }
        
        palette.darkMutedSwatch?.let {
            colors.add(PaletteColor(getString(R.string.dark_muted), it.rgb, it.rgb.toHexCode()))
        }

        return colors
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        
        // Rotate the bitmap if needed
        val rotationDegrees = image.imageInfo.rotationDegrees
        return if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    private fun Int.toHexCode(): String {
        return String.format("#%06X", 0xFFFFFF and this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

data class PaletteColor(
    val name: String,
    val color: Int,
    val hexCode: String
)
