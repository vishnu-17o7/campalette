package com.vishnu.campalette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.core.content.edit
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vishnu.campalette.ui.AppShell
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.state.SettingsViewModel
import com.vishnu.campalette.ui.theme.CampaletteTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    var currentImageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seedBenchmarkLibraryIfRequested()
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            CampaletteRoot(activity = this, cameraExecutor = cameraExecutor)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun seedBenchmarkLibraryIfRequested() {
        if (!BuildConfig.BENCHMARK_MODE || !intent.getBooleanExtra(BENCHMARK_SEED_EXTRA, false)) return
        val templates = AtelierData.buildLibraryPalettes(
            current = AtelierData.defaultPalette(),
            paletteName = "Live study"
        ).drop(1)
        val studies = List(36) { index ->
            val template = templates[index % templates.size]
            template.copy(
                name = "${template.name} ${index + 1}",
                capturedAt = "Benchmark ${index + 1}"
            )
        }
        getSharedPreferences("campalette", MODE_PRIVATE).edit {
            putString("saved_json", AtelierData.encodeStudyJson(studies.take(18)))
            putString("history_json", AtelierData.encodeStudyJson(studies))
        }
    }

    private companion object {
        const val BENCHMARK_SEED_EXTRA = "campalette.benchmark.SEED_LIBRARY"
    }
}

@Composable
private fun CampaletteRoot(
    activity: MainActivity,
    cameraExecutor: ExecutorService
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = settingsState.themeMode.resolve(isSystemInDarkTheme())

    CampaletteTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppShell(
                activity = activity,
                cameraExecutor = cameraExecutor,
                settingsState = settingsState,
                isDarkTheme = isDarkTheme,
                onThemeModeChange = settingsViewModel::setThemeMode,
                onPaletteTintChange = settingsViewModel::setPaletteTintEnabled,
                onReducedMotionChange = settingsViewModel::setReducedMotion,
                onHapticsChange = settingsViewModel::setHapticsEnabled,
                onPaletteColorCountChange = settingsViewModel::setPaletteColorCount
            )
        }
    }
}

@Immutable
data class PaletteColor(
    val name: String,
    val color: Int,
    val hexCode: String,
    val red: Int,
    val green: Int,
    val blue: Int
)

@Immutable
data class RadialMenuState(
    val center: androidx.compose.ui.geometry.Offset,
    val touchedColor: Int,
    val selectedIndex: Int
)

@Immutable
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
