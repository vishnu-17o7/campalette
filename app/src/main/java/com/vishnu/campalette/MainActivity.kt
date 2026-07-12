package com.vishnu.campalette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vishnu.campalette.ui.AppShell
import com.vishnu.campalette.ui.theme.CampaletteTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    var currentImageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CampaletteTheme {
                CampaletteRoot(activity = this, cameraExecutor = cameraExecutor)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
private fun CampaletteRoot(
    activity: MainActivity,
    cameraExecutor: ExecutorService
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AppShell(
            activity = activity,
            cameraExecutor = cameraExecutor
        )
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
    val center: androidx.compose.ui.geometry.Offset,
    val touchedColor: Int,
    val selectedIndex: Int
)

data class PaletteStudy(
    val name: String,
    val colors: List<PaletteColor>,
    val note: String = "",
    val capturedAt: String = "",
    val source: String = ""
)

data class ToggleRowState(
    val label: String,
    val enabled: Boolean = true,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)
