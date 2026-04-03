package com.vishnu.campalette.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.RadialMenuState
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.components.AppScreen
import com.vishnu.campalette.ui.components.FloatingBottomNav
import com.vishnu.campalette.ui.HarmonyMode
import com.vishnu.campalette.ui.screens.ColorDetailScreen
import com.vishnu.campalette.ui.screens.LiveCameraScreen
import com.vishnu.campalette.ui.screens.OnboardingPermissionScreen
import com.vishnu.campalette.ui.screens.PaletteHistoryScreen
import com.vishnu.campalette.ui.screens.PaletteEditorScreen
import com.vishnu.campalette.ui.screens.PaletteLibraryScreen
import com.vishnu.campalette.ui.screens.SettingsScreen
import java.util.concurrent.ExecutorService
import kotlinx.coroutines.launch

@Composable
fun AppShell(
    activity: MainActivity,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
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

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Live) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var colorPalette by rememberSaveable(stateSaver = paletteColorListSaver()) { mutableStateOf<List<PaletteColor>>(emptyList()) }
    var radialMenuState by remember { mutableStateOf<RadialMenuState?>(null) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedColorDetails by rememberSaveable(stateSaver = paletteColorSaver()) { mutableStateOf<PaletteColor?>(null) }
    var paletteName by rememberSaveable { mutableStateOf("") }
    var paletteSource by rememberSaveable { mutableStateOf("") }
    var selectedEditorHex by rememberSaveable { mutableStateOf<String?>(null) }
    var harmonyMode by rememberSaveable { mutableStateOf(HarmonyMode.Analogous) }
    var keepReticle by rememberSaveable { mutableStateOf(true) }
    var flashPreferred by rememberSaveable { mutableStateOf(false) }
    var hapticsEnabled by rememberSaveable { mutableStateOf(true) }
    var lensFacing by rememberSaveable { mutableStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var gridEnabled by rememberSaveable { mutableStateOf(false) }
    var cameraHasFlash by rememberSaveable { mutableStateOf(false) }
    val haptics = rememberAtelierHaptics(enabled = hapticsEnabled)
    val hasCapturedPalette = capturedImage != null && colorPalette.isNotEmpty()

    val workingPalette = remember(colorPalette) { colorPalette }
    val activeEditorColor = workingPalette.firstOrNull { it.hexCode == selectedEditorHex } ?: workingPalette.firstOrNull()
    var savedPalettes by rememberSaveable(stateSaver = paletteStudyListSaver()) {
        mutableStateOf<List<PaletteStudy>>(emptyList())
    }
    var historyPalettes by rememberSaveable(stateSaver = paletteStudyListSaver()) { mutableStateOf<List<PaletteStudy>>(emptyList()) }
    var latestCapturedStudy by rememberSaveable(stateSaver = paletteStudySaver()) { mutableStateOf<PaletteStudy?>(null) }
    val libraryPalettes = remember(savedPalettes, workingPalette, paletteName, paletteSource) {
        if (workingPalette.isNotEmpty() && paletteName.isNotBlank()) {
            val currentStudy = AtelierData.createCapturedStudy(
                name = paletteName,
                colors = workingPalette,
                source = paletteSource.ifBlank { activity.getString(R.string.current_palette_source) }
            )
            listOf(currentStudy) + savedPalettes
        } else {
            savedPalettes
        }
    }
    val performCapture = {
        AtelierData.takePhoto(
            activity = activity,
            cameraExecutor = cameraExecutor,
            imageCapture = activity.currentImageCapture,
            onImageCaptured = { bitmap ->
                val extracted = AtelierData.extractColorPalette(activity, bitmap)
                val captureName = activity.getString(R.string.capture_name, AtelierData.timestampLabel())
                val capturedStudy = AtelierData.createCapturedStudy(
                    name = captureName,
                    colors = extracted,
                    source = activity.getString(R.string.source_live_camera)
                )
                haptics.perform(AtelierHapticEvent.CaptureSuccess)
                capturedImage = bitmap
                colorPalette = extracted
                paletteName = captureName
                paletteSource = activity.getString(R.string.source_live_camera)
                selectedEditorHex = extracted.firstOrNull()?.hexCode
                latestCapturedStudy = capturedStudy
                historyPalettes = listOf(capturedStudy) + historyPalettes
                radialMenuState = null
                currentScreen = AppScreen.Live
            },
            onError = { message ->
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!hasCameraPermission) {
                OnboardingPermissionScreen(
                    modifier = Modifier.fillMaxSize(),
                    onRequestPermission = {
                        haptics.perform(AtelierHapticEvent.PermissionPrompt)
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            } else {
                when (currentScreen) {
                    AppScreen.Live -> LiveCameraScreen(
                        activity = activity,
                        modifier = Modifier.fillMaxSize(),
                        capturedImage = capturedImage,
                        palette = workingPalette,
                        selectedColor = selectedColorDetails ?: workingPalette.firstOrNull(),
                        radialMenuState = radialMenuState,
                        keepReticle = keepReticle,
                        gridEnabled = gridEnabled,
                        flashEnabled = flashPreferred && cameraHasFlash,
                        flashAvailable = cameraHasFlash,
                        lensFacing = lensFacing,
                        previewSize = previewSize,
                        hasCapturedPalette = hasCapturedPalette,
                        paletteName = paletteName,
                        paletteSource = paletteSource,
                        harmonyLabel = when (harmonyMode) {
                            HarmonyMode.Analogous -> activity.getString(R.string.harmony_analogous)
                            HarmonyMode.Complementary -> activity.getString(R.string.harmony_complementary)
                            HarmonyMode.Tonal -> activity.getString(R.string.harmony_tonal)
                        },
                        onMenuClick = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Library
                        },
                        onProfileClick = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Settings
                        },
                        onPreviewMeasured = { previewSize = it },
                        onFlipCamera = {
                            haptics.perform(
                                if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
                                    AtelierHapticEvent.ToggleOn
                                } else {
                                    AtelierHapticEvent.ToggleOff
                                }
                            )
                            lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
                                androidx.camera.core.CameraSelector.LENS_FACING_FRONT
                            } else {
                                androidx.camera.core.CameraSelector.LENS_FACING_BACK
                            }
                        },
                        onToggleFlash = {
                            if (cameraHasFlash) {
                                haptics.perform(
                                    if (flashPreferred) AtelierHapticEvent.ToggleOff else AtelierHapticEvent.ToggleOn
                                )
                                flashPreferred = !flashPreferred
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(activity.getString(R.string.settings_capture_flash_unavailable))
                                }
                            }
                        },
                        onToggleGrid = {
                            haptics.perform(
                                if (gridEnabled) AtelierHapticEvent.ToggleOff else AtelierHapticEvent.ToggleOn
                            )
                            gridEnabled = !gridEnabled
                        },
                        onFlashAvailabilityChanged = { available ->
                            cameraHasFlash = available
                            if (!available) {
                                flashPreferred = false
                            }
                        },
                        onSampleTap = { bitmap, offset, size ->
                            val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
                            if (sampledColor != null) {
                                haptics.perform(AtelierHapticEvent.Sample)
                                val generated = AtelierData.generatePaletteFromSeedColor(activity, sampledColor)
                                colorPalette = generated
                                paletteSource = activity.getString(
                                    R.string.source_sampled_capture,
                                    latestCapturedStudy?.name ?: activity.getString(R.string.source_live_camera)
                                )
                                selectedEditorHex = generated.firstOrNull()?.hexCode
                                radialMenuState = null
                            }
                        },
                        onSampleLongPress = { bitmap, offset, size ->
                            val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
                            if (sampledColor != null) {
                                haptics.perform(AtelierHapticEvent.SampleExplore)
                                val generated = AtelierData.generatePaletteFromSeedColor(activity, sampledColor)
                                colorPalette = generated
                                paletteSource = activity.getString(
                                    R.string.source_sampled_capture,
                                    latestCapturedStudy?.name ?: activity.getString(R.string.source_live_camera)
                                )
                                selectedEditorHex = generated.firstOrNull()?.hexCode
                                radialMenuState = RadialMenuState(
                                    center = offset,
                                    touchedColor = sampledColor,
                                    selectedIndex = AtelierData.nearestPaletteIndex(sampledColor, generated)
                                )
                            }
                        },
                        onColorSelected = {
                            haptics.perform(AtelierHapticEvent.Selection)
                            selectedColorDetails = it
                        },
                        onCapture = {
                            haptics.perform(AtelierHapticEvent.CapturePress)
                            performCapture()
                        },
                        onCameraError = { message ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                    )

                    AppScreen.Library -> PaletteLibraryScreen(
                        modifier = Modifier.fillMaxSize(),
                        palettes = libraryPalettes,
                        onOpenCamera = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Live
                        },
                        onOpenBuilder = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Editor
                        },
                        onPaletteSelected = { selected ->
                            haptics.perform(AtelierHapticEvent.Selection)
                            paletteName = selected.name
                            paletteSource = selected.source
                            colorPalette = selected.colors
                            selectedEditorHex = selected.colors.firstOrNull()?.hexCode
                            currentScreen = AppScreen.Editor
                        }
                    )

                    AppScreen.History -> PaletteHistoryScreen(
                        modifier = Modifier.fillMaxSize(),
                        palettes = historyPalettes,
                        onPaletteSelected = { selected ->
                            haptics.perform(AtelierHapticEvent.Selection)
                            paletteName = selected.name
                            paletteSource = selected.source
                            colorPalette = selected.colors
                            latestCapturedStudy = selected
                            selectedEditorHex = selected.colors.firstOrNull()?.hexCode
                            currentScreen = AppScreen.Editor
                        },
                        onOpenCamera = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Live
                        }
                    )

                    AppScreen.Editor -> PaletteEditorScreen(
                        modifier = Modifier.fillMaxSize(),
                        paletteName = paletteName,
                        paletteSource = paletteSource,
                        palette = workingPalette,
                        selectedHex = activeEditorColor?.hexCode,
                        harmonyLabel = when (harmonyMode) {
                            HarmonyMode.Analogous -> activity.getString(R.string.harmony_analogous)
                            HarmonyMode.Complementary -> activity.getString(R.string.harmony_complementary)
                            HarmonyMode.Tonal -> activity.getString(R.string.harmony_tonal)
                        },
                        onPaletteNameChange = { paletteName = it },
                        onColorSelected = {
                            haptics.perform(AtelierHapticEvent.Selection)
                            selectedEditorHex = it.hexCode
                        },
                        onInspectSelected = {
                            haptics.perform(AtelierHapticEvent.Inspect)
                            activeEditorColor?.let { selectedColorDetails = it }
                        },
                        onAnalyzeHarmony = {
                            activeEditorColor?.let { selected ->
                                haptics.perform(AtelierHapticEvent.Selection)
                                val nextMode = when (harmonyMode) {
                                    HarmonyMode.Analogous -> HarmonyMode.Complementary
                                    HarmonyMode.Complementary -> HarmonyMode.Tonal
                                    HarmonyMode.Tonal -> HarmonyMode.Analogous
                                }
                                harmonyMode = nextMode
                                val harmonized = AtelierData.harmonyPalette(activity, selected.color, nextMode)
                                colorPalette = harmonized
                                paletteSource = activity.getString(R.string.source_harmony_study)
                                selectedEditorHex = harmonized.firstOrNull()?.hexCode
                            }
                        },
                        onSavePalette = {
                            haptics.perform(AtelierHapticEvent.Save)
                            val study = AtelierData.createCapturedStudy(
                                name = paletteName,
                                colors = workingPalette,
                                source = activity.getString(R.string.builder_palette_source)
                            )
                            savedPalettes = listOf(study) + savedPalettes.filterNot { it.name == study.name }
                            currentScreen = AppScreen.Library
                        },
                        onOpenCamera = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Live
                        },
                        onOpenLibrary = {
                            haptics.perform(AtelierHapticEvent.Navigation)
                            currentScreen = AppScreen.Library
                        }
                    )

                    AppScreen.Settings -> SettingsScreen(
                        modifier = Modifier.fillMaxSize(),
                        keepReticle = keepReticle,
                        flashPreferred = flashPreferred,
                        flashAvailable = cameraHasFlash,
                        hapticsEnabled = hapticsEnabled,
                        onReticleChanged = {
                            haptics.perform(
                                if (keepReticle) AtelierHapticEvent.ToggleOff else AtelierHapticEvent.ToggleOn
                            )
                            keepReticle = it
                        },
                        onFlashChanged = {
                            if (cameraHasFlash) {
                                haptics.perform(
                                    if (it) AtelierHapticEvent.ToggleOn else AtelierHapticEvent.ToggleOff
                                )
                                flashPreferred = it
                            }
                        },
                        onHapticsChanged = {
                            haptics.perform(
                                if (it) AtelierHapticEvent.ToggleOn else AtelierHapticEvent.ToggleOff,
                                force = true
                            )
                            hapticsEnabled = it
                        }
                    )
                }
            }

            if (hasCameraPermission && selectedColorDetails == null) {
                FloatingBottomNav(
                    currentScreen = currentScreen,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .fillMaxWidth(0.9f)
                        .navigationBarsPadding(),
                    onLiveAction = if (hasCapturedPalette || currentScreen == AppScreen.Live) {
                        {
                            haptics.perform(AtelierHapticEvent.CapturePress)
                            performCapture()
                        }
                    } else {
                        null
                    },
                    onScreenSelected = {
                        haptics.perform(AtelierHapticEvent.Navigation)
                        currentScreen = it
                    }
                )
            }

            selectedColorDetails?.let { paletteColor ->
                ColorDetailScreen(
                    modifier = Modifier.fillMaxSize(),
                    paletteColor = paletteColor,
                    onCopy = { value ->
                        haptics.perform(AtelierHapticEvent.Copy)
                        clipboardManager.setText(AnnotatedString(value))
                        scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.copied)) }
                    },
                    onAddToPalette = {
                        haptics.perform(AtelierHapticEvent.AddToPalette)
                        if (workingPalette.none { it.hexCode == paletteColor.hexCode }) {
                            colorPalette = (workingPalette + paletteColor).take(8)
                            selectedEditorHex = paletteColor.hexCode
                        }
                        selectedColorDetails = null
                        currentScreen = AppScreen.Editor
                    },
                    onBack = {
                        haptics.perform(AtelierHapticEvent.Navigation)
                        selectedColorDetails = null
                    }
                )
            }
        }
    }
}

private fun paletteColorSaver(): Saver<PaletteColor?, Any> = Saver(
    save = { color ->
        color?.let { listOf(it.name, it.color, it.hexCode, it.red, it.green, it.blue) }
    },
    restore = { saved ->
        val values = saved as List<*>
        PaletteColor(
            name = values[0] as String,
            color = values[1] as Int,
            hexCode = values[2] as String,
            red = values[3] as Int,
            green = values[4] as Int,
            blue = values[5] as Int
        )
    }
)

private fun paletteColorListSaver(): Saver<List<PaletteColor>, Any> = Saver(
    save = { list ->
        list.flatMap { color ->
            listOf(color.name, color.color, color.hexCode, color.red, color.green, color.blue)
        }
    },
    restore = { saved ->
        (saved as List<*>).chunked(6).map { values ->
            PaletteColor(
                name = values[0] as String,
                color = values[1] as Int,
                hexCode = values[2] as String,
                red = values[3] as Int,
                green = values[4] as Int,
                blue = values[5] as Int
            )
        }
    }
)

private fun paletteStudyListSaver(): Saver<List<PaletteStudy>, Any> = Saver(
    save = { studies ->
        studies.flatMap { study ->
            val colors = study.colors.flatMap { color ->
                listOf(color.name, color.color, color.hexCode, color.red, color.green, color.blue)
            }
            listOf(study.name, study.note, study.capturedAt, study.source, study.colors.size) + colors
        }
    },
    restore = { saved ->
        val payload = saved as List<*>
        val restored = mutableListOf<PaletteStudy>()
        var index = 0
        while (index < payload.size) {
            val name = payload[index++] as String
            val note = payload[index++] as String
            val capturedAt = payload[index++] as String
            val source = payload[index++] as String
            val colorCount = payload[index++] as Int
            val colors = buildList {
                repeat(colorCount) {
                    add(
                        PaletteColor(
                            name = payload[index++] as String,
                            color = payload[index++] as Int,
                            hexCode = payload[index++] as String,
                            red = payload[index++] as Int,
                            green = payload[index++] as Int,
                            blue = payload[index++] as Int
                        )
                    )
                }
            }
            restored += PaletteStudy(
                name = name,
                colors = colors,
                note = note,
                capturedAt = capturedAt,
                source = source
            )
        }
        restored
    }
)

private fun paletteStudySaver(): Saver<PaletteStudy?, Any> = Saver(
    save = { study ->
        study?.let {
            val colors = it.colors.flatMap { color ->
                listOf(color.name, color.color, color.hexCode, color.red, color.green, color.blue)
            }
            listOf(it.name, it.note, it.capturedAt, it.source, it.colors.size) + colors
        }
    },
    restore = { saved ->
        val payload = saved as List<*>
        var index = 0
        val name = payload[index++] as String
        val note = payload[index++] as String
        val capturedAt = payload[index++] as String
        val source = payload[index++] as String
        val colorCount = payload[index++] as Int
        val colors = buildList {
            repeat(colorCount) {
                add(
                    PaletteColor(
                        name = payload[index++] as String,
                        color = payload[index++] as Int,
                        hexCode = payload[index++] as String,
                        red = payload[index++] as Int,
                        green = payload[index++] as Int,
                        blue = payload[index++] as Int
                    )
                )
            }
        }
        PaletteStudy(
            name = name,
            colors = colors,
            note = note,
            capturedAt = capturedAt,
            source = source
        )
    }
)
