package com.vishnu.campalette.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.content.Intent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.RadialMenuState
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.components.AppScreen

import com.vishnu.campalette.ui.HarmonyMode
import com.vishnu.campalette.ui.screens.ColorDetailScreen
import com.vishnu.campalette.ui.screens.ColorBlindnessPreviewScreen
import com.vishnu.campalette.ui.screens.LiveCameraScreen
import com.vishnu.campalette.ui.screens.OnboardingPermissionScreen
import com.vishnu.campalette.ui.screens.PaletteEditorScreen
import com.vishnu.campalette.ui.screens.PaletteLibraryScreen
import com.vishnu.campalette.ui.screens.SettingsScreen
import com.vishnu.campalette.ui.screens.ShareExportSheet
import com.vishnu.campalette.ui.theme.DynamicThemeProvider
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import java.util.concurrent.ExecutorService
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

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
    val hazeState = rememberHazeState()
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasCameraPermission = isGranted }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Live) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var colorPalette by rememberSaveable(stateSaver = paletteColorListSaver()) { mutableStateOf<List<PaletteColor>>(emptyList()) }
    var radialMenuState by remember { mutableStateOf<RadialMenuState?>(null) }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedColorDetails by rememberSaveable(stateSaver = paletteColorSaver()) { mutableStateOf<PaletteColor?>(null) }
    var paletteName by rememberSaveable { mutableStateOf("") }
    var paletteSource by rememberSaveable { mutableStateOf("") }
    var selectedEditorHex by rememberSaveable { mutableStateOf<String?>(null) }
    var harmonyMode by rememberSaveable { mutableStateOf(HarmonyMode.Analogous) }
    var flashPreferred by rememberSaveable { mutableStateOf(false) }
    var hapticsEnabled by rememberSaveable { mutableStateOf(true) }
    var paletteTintEnabled by rememberSaveable { mutableStateOf(true) }
    var librarySearch by rememberSaveable { mutableStateOf("") }
    var libraryFilter by rememberSaveable { mutableStateOf("all") }
    var libraryGrid by rememberSaveable { mutableStateOf(false) }
    var lensFacing by rememberSaveable { mutableStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var gridEnabled by rememberSaveable { mutableStateOf(false) }
    var cameraHasFlash by rememberSaveable { mutableStateOf(false) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }
    var showColorBlindness by rememberSaveable { mutableStateOf(false) }
    var colorBlindnessType by rememberSaveable { mutableStateOf(AtelierData.ColorBlindnessType.Deuteranopia) }
    val haptics = rememberAtelierHaptics(enabled = hapticsEnabled)
    val hasCapturedPalette = capturedImage != null && colorPalette.isNotEmpty()
    val dominantSeedColor = if (paletteTintEnabled) colorPalette.firstOrNull()?.color else null

    LaunchedEffect(currentScreen, hasCameraPermission, selectedColorDetails) {
        val useDarkSystemIcons = selectedColorDetails?.let {
            ColorUtils.calculateLuminance(it.color) > 0.45
        } ?: (currentScreen != AppScreen.Live || !hasCameraPermission)
        val systemBarStyle = if (useDarkSystemIcons) {
            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        }
        activity.enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle
        )
        @Suppress("DEPRECATION")
        activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        controller.isAppearanceLightStatusBars = useDarkSystemIcons
        controller.isAppearanceLightNavigationBars = useDarkSystemIcons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isStatusBarContrastEnforced = false
            activity.window.isNavigationBarContrastEnforced = false
        }
    }

    val workingPalette = remember(colorPalette) { colorPalette }
    val activeEditorColor = workingPalette.firstOrNull { it.hexCode == selectedEditorHex } ?: workingPalette.firstOrNull()
    var savedPalettes by rememberSaveable(stateSaver = paletteStudyListSaver()) { mutableStateOf<List<PaletteStudy>>(emptyList()) }
    var historyPalettes by rememberSaveable(stateSaver = paletteStudyListSaver()) { mutableStateOf<List<PaletteStudy>>(emptyList()) }
    var latestCapturedStudy by rememberSaveable(stateSaver = paletteStudySaver()) { mutableStateOf<PaletteStudy?>(null) }
    var storageLoaded by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) { decodeBitmap(context, uri) }
                }.onSuccess { bitmap ->
                    val displayBitmap = withContext(Dispatchers.Default) { AtelierData.downscaleBitmap(bitmap, 1080) }
                    val extracted = withContext(Dispatchers.Default) {
                        AtelierData.extractColorPalette(activity, displayBitmap).withoutDuplicateColors()
                    }
                    val importName = activity.getString(R.string.capture_name, AtelierData.timestampLabel())
                    capturedImage?.recycle()
                    capturedImage = displayBitmap
                    colorPalette = extracted
                    paletteTintEnabled = true
                    paletteName = importName
                    paletteSource = "Gallery import"
                    selectedEditorHex = extracted.firstOrNull()?.hexCode
                    val study = AtelierData.createCapturedStudy(importName, extracted, paletteSource)
                    historyPalettes = listOf(study) + historyPalettes
                    haptics.perform(AtelierHapticEvent.GalleryImport)
                }.onFailure {
                    snackbarHostState.showSnackbar(activity.getString(R.string.capture_failed))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("campalette", android.content.Context.MODE_PRIVATE)
        val savedJson = prefs.getString("saved_json", null)
        val historyJson = prefs.getString("history_json", null)
        if (savedJson != null) savedPalettes = AtelierData.decodeStudyJson(savedJson)
        if (historyJson != null) historyPalettes = AtelierData.decodeStudyJson(historyJson)
        storageLoaded = true
    }
    LaunchedEffect(savedPalettes, storageLoaded) {
        if (storageLoaded) {
            context.getSharedPreferences("campalette", android.content.Context.MODE_PRIVATE)
                .edit().putString("saved_json", AtelierData.encodeStudyJson(savedPalettes)).apply()
        }
    }
    LaunchedEffect(historyPalettes, storageLoaded) {
        if (storageLoaded) {
            context.getSharedPreferences("campalette", android.content.Context.MODE_PRIVATE)
                .edit().putString("history_json", AtelierData.encodeStudyJson(historyPalettes)).apply()
        }
    }
    val libraryPalettes = remember(savedPalettes, historyPalettes, librarySearch, libraryFilter) {
        val savedNames = savedPalettes.mapTo(mutableSetOf()) { it.name }
        (savedPalettes + historyPalettes)
            .distinctBy { "${it.name}|${it.capturedAt}|${it.source}" }
            .filter { study ->
                librarySearch.isBlank() || study.name.contains(librarySearch, ignoreCase = true) ||
                    study.colors.any { it.hexCode.contains(librarySearch, ignoreCase = true) }
            }
            .filter { study ->
                when (libraryFilter) {
                    "camera" -> study.source.contains("camera", ignoreCase = true) || study.source.contains("capture", ignoreCase = true)
                    "harmony" -> study.source.contains("harmony", ignoreCase = true)
                    "saved" -> study.name in savedNames
                    else -> true
                }
            }
    }

    var reducedMotion by remember { mutableStateOf(false) }

    val harmonyLabel = remember(harmonyMode) {
        when (harmonyMode) {
            HarmonyMode.Analogous -> activity.getString(R.string.harmony_analogous)
            HarmonyMode.Complementary -> activity.getString(R.string.harmony_complementary)
            HarmonyMode.Tonal -> activity.getString(R.string.harmony_tonal)
        }
    }

    fun performCapture() {
        if (isCapturing) return
        isCapturing = true
        AtelierData.takePhoto(
            activity = activity,
            cameraExecutor = cameraExecutor,
            imageCapture = activity.currentImageCapture,
            onImageCaptured = { bitmap ->
                scope.launch(Dispatchers.Default) {
                    val displayBmp = AtelierData.downscaleBitmap(bitmap, 1080)
                    val extracted = AtelierData.extractColorPalette(activity, displayBmp).withoutDuplicateColors()
                    val captureName = activity.getString(R.string.capture_name, AtelierData.timestampLabel())
                    val capturedStudy = AtelierData.createCapturedStudy(
                        name = captureName, colors = extracted, source = activity.getString(R.string.source_live_camera)
                    )
                    withContext(Dispatchers.Main) {
                        haptics.perform(AtelierHapticEvent.CaptureSuccess)
                        if (bitmap !== displayBmp) bitmap.recycle()
                        capturedImage?.recycle()
                        capturedImage = displayBmp
                        colorPalette = extracted
                        paletteTintEnabled = true
                        paletteName = captureName
                        paletteSource = activity.getString(R.string.source_live_camera)
                        selectedEditorHex = extracted.firstOrNull()?.hexCode
                        latestCapturedStudy = capturedStudy
                        historyPalettes = listOf(capturedStudy) + historyPalettes
                        radialMenuState = null
                        currentScreen = AppScreen.Live
                        isCapturing = false
                    }
                }
            },
            onError = { message ->
                isCapturing = false
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    val clearCapture: () -> Unit = {
        capturedImage?.recycle()
        capturedImage = null
        colorPalette = emptyList()
        paletteName = ""
        paletteSource = ""
        selectedEditorHex = null
        radialMenuState = null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        DynamicThemeProvider(dominantColor = dominantSeedColor) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                AnimatedContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(state = hazeState),
                        targetState = currentScreen,
                        transitionSpec = {
                            if (reducedMotion) {
                                fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                            } else if (targetState == AppScreen.Editor || initialState == AppScreen.Editor) {
                                (slideInHorizontally(tween(220)) { it / 3 } + fadeIn(tween(160)))
                                    .togetherWith(slideOutHorizontally(tween(200)) { -it / 5 } + fadeOut(tween(130)))
                            } else {
                                fadeIn(tween(150)).togetherWith(fadeOut(tween(120)))
                            }
                        },
                        label = "screenTransition"
                    ) { screen ->
                        val liveCameraState = remember(colorPalette, selectedColorDetails, previewSize, hasCapturedPalette, paletteName, paletteSource, harmonyLabel, isCapturing) {
                            com.vishnu.campalette.ui.screens.LiveCameraState(
                                palette = colorPalette,
                                selectedColor = selectedColorDetails ?: colorPalette.firstOrNull(),
                                previewSize = previewSize,
                                hasCapturedPalette = hasCapturedPalette,
                                paletteName = paletteName,
                                harmonyLabel = harmonyLabel,
                                isCapturing = isCapturing
                            )
                        }
                        when (screen) {
                            AppScreen.Live -> if (!hasCameraPermission) {
                                OnboardingPermissionScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    onRequestPermission = {
                                        haptics.perform(AtelierHapticEvent.PermissionPrompt)
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                )
                            } else {
                                LiveCameraScreen(
                                state = liveCameraState,
                                activity = activity,
                                capturedImage = capturedImage,
                                radialMenuState = radialMenuState,
                                gridEnabled = gridEnabled,
                                flashEnabled = flashPreferred && cameraHasFlash,
                                flashAvailable = cameraHasFlash,
                                lensFacing = lensFacing,
                                largeTouchTargets = false,
                                onCapture = {
                                    haptics.perform(AtelierHapticEvent.CapturePress)
                                    performCapture()
                                },
                                onClearCapture = clearCapture,
                                onMenuClick = { haptics.perform(AtelierHapticEvent.Navigation); currentScreen = AppScreen.History },
                                onEditPalette = {
                                    haptics.perform(AtelierHapticEvent.Navigation)
                                    currentScreen = AppScreen.Editor
                                },
                                onPreviewMeasured = { previewSize = it },
                                onFlipCamera = {
                                    haptics.perform(if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) AtelierHapticEvent.ToggleOn else AtelierHapticEvent.ToggleOff)
                                    lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) androidx.camera.core.CameraSelector.LENS_FACING_FRONT else androidx.camera.core.CameraSelector.LENS_FACING_BACK
                                },
                                onToggleFlash = {
                                    if (cameraHasFlash) {
                                        haptics.perform(if (flashPreferred) AtelierHapticEvent.ToggleOff else AtelierHapticEvent.ToggleOn)
                                        flashPreferred = !flashPreferred
                                    } else scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.settings_capture_flash_unavailable)) }
                                },
                                onToggleGrid = { haptics.perform(if (gridEnabled) AtelierHapticEvent.ToggleOff else AtelierHapticEvent.ToggleOn); gridEnabled = !gridEnabled },
                                onFlashAvailabilityChanged = { cameraHasFlash = it; if (!it) flashPreferred = false },
                                onSampleTap = { bitmap, offset, size ->
                                    val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
                                    if (sampledColor != null) {
                                        haptics.perform(AtelierHapticEvent.Sample)
                                        val generated = AtelierData.generatePaletteFromSeedColor(activity, sampledColor).withoutDuplicateColors()
                                        colorPalette = generated
                                        paletteTintEnabled = true
                                        paletteSource = activity.getString(R.string.source_sampled_capture, latestCapturedStudy?.name ?: activity.getString(R.string.source_live_camera))
                                        selectedEditorHex = generated.firstOrNull()?.hexCode
                                        radialMenuState = null
                                    }
                                },
                                onSampleLongPress = { bitmap, offset, size ->
                                    val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
                                    if (sampledColor != null) {
                                        haptics.perform(AtelierHapticEvent.SampleExplore)
                                        val generated = AtelierData.generatePaletteFromSeedColor(activity, sampledColor).withoutDuplicateColors()
                                        colorPalette = generated
                                        paletteTintEnabled = true
                                        paletteSource = activity.getString(R.string.source_sampled_capture, latestCapturedStudy?.name ?: activity.getString(R.string.source_live_camera))
                                        selectedEditorHex = generated.firstOrNull()?.hexCode
                                        radialMenuState = RadialMenuState(center = offset, touchedColor = sampledColor, selectedIndex = AtelierData.nearestPaletteIndex(sampledColor, generated))
                                    }
                                },
                                onColorSelected = { haptics.perform(AtelierHapticEvent.Selection); selectedColorDetails = it },
                                onCameraError = { scope.launch { snackbarHostState.showSnackbar(it) } },
                                    onImportGallery = { galleryLauncher.launch("image/*") }
                                )
                            }



                            AppScreen.History -> PaletteLibraryScreen(
                                modifier = Modifier.fillMaxSize(),
                                state = com.vishnu.campalette.ui.screens.LibraryState(
                                    palettes = libraryPalettes,
                                    searchQuery = librarySearch,
                                    selectedFilter = libraryFilter,
                                    selectedSort = "Recent",
                                    isGridView = libraryGrid,
                                    favorites = emptySet()
                                ),
                                onSearchChange = { librarySearch = it },
                                onFilterChange = { libraryFilter = it },
                                onToggleGrid = { libraryGrid = !libraryGrid },
                                onPaletteSelect = { selected ->
                                    haptics.perform(AtelierHapticEvent.Selection)
                                    val cleanedColors = selected.colors.withoutDuplicateColors()
                                    paletteName = selected.name; paletteSource = selected.source; colorPalette = cleanedColors
                                    paletteTintEnabled = true
                                    latestCapturedStudy = selected; selectedEditorHex = cleanedColors.firstOrNull()?.hexCode; currentScreen = AppScreen.Editor
                                },
                                onDeletePalette = { selected ->
                                    savedPalettes = savedPalettes.filterNot { it.name == selected.name }
                                    historyPalettes = historyPalettes.filterNot { it.name == selected.name && it.capturedAt == selected.capturedAt }
                                },
                                onNavigate = {
                                    haptics.perform(AtelierHapticEvent.Navigation)
                                    currentScreen = it
                                }
                            )

                            AppScreen.Editor -> PaletteEditorScreen(
                                modifier = Modifier.fillMaxSize(),
                                state = com.vishnu.campalette.ui.screens.EditorState(
                                    name = paletteName,
                                    source = paletteSource,
                                    palette = workingPalette,
                                    harmonyMode = harmonyMode,
                                    selectedHex = selectedEditorHex
                                ),
                                onNameChange = { paletteName = it },
                                onHarmonyModeChange = { requestedMode ->
                                    activeEditorColor?.let { selected ->
                                        haptics.perform(AtelierHapticEvent.Selection)
                                        harmonyMode = requestedMode
                                        val harmonized = AtelierData.harmonyPalette(activity, selected.color, requestedMode).withoutDuplicateColors()
                                        colorPalette = harmonized; paletteSource = activity.getString(R.string.source_harmony_study); selectedEditorHex = harmonized.firstOrNull()?.hexCode
                                        paletteTintEnabled = true
                                    }
                                },
                                onSelectColor = { selectedEditorHex = it.hexCode },
                                onInspectColor = {
                                    haptics.perform(AtelierHapticEvent.Inspect)
                                    selectedColorDetails = it
                                },
                                onDeleteColor = { color ->
                                    val newPalette = workingPalette.filterNot { it.hexCode == color.hexCode }
                                    if (newPalette.isNotEmpty()) {
                                        colorPalette = newPalette
                                        if (selectedEditorHex == color.hexCode) {
                                            selectedEditorHex = newPalette.first().hexCode
                                        }
                                    }
                                },
                                onReorder = { from, to ->
                                    val list = workingPalette.toMutableList()
                                    val item = list.removeAt(from)
                                    list.add(to, item)
                                    colorPalette = list
                                },
                                onSave = {
                                    haptics.perform(AtelierHapticEvent.Save)
                                    val study = AtelierData.createCapturedStudy(name = paletteName, colors = workingPalette, source = activity.getString(R.string.builder_palette_source))
                                    savedPalettes = listOf(study) + savedPalettes.filterNot { it.name == study.name }
                                    currentScreen = AppScreen.History
                                },
                                onShare = { showShareSheet = true },
                                onNavigate = {
                                    haptics.perform(AtelierHapticEvent.Navigation)
                                    currentScreen = it
                                }
                            )

                            AppScreen.Settings -> SettingsScreen(
                                modifier = Modifier.fillMaxSize(),
                                paletteTintEnabled = paletteTintEnabled,
                                onPaletteTintChange = { paletteTintEnabled = it },
                                reducedMotion = reducedMotion,
                                onReducedMotionChange = { reducedMotion = it },
                                hapticsEnabled = hapticsEnabled,
                                onHapticsChange = { hapticsEnabled = it }
                            )
                        }
                }

                if (
                    currentScreen != AppScreen.Editor &&
                    selectedColorDetails == null &&
                    !showShareSheet
                ) {
                    com.vishnu.campalette.ui.components.BottomBar(
                        currentScreen = currentScreen,
                        hazeState = hazeState,
                        modifier = Modifier.align(Alignment.BottomCenter),
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
                        onCopy = { v -> haptics.perform(AtelierHapticEvent.Copy); clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(v)); scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.copied)) } },
                        onAddToPalette = {
                            haptics.perform(AtelierHapticEvent.AddToPalette)
                            if (workingPalette.none { it.hexCode == paletteColor.hexCode }) colorPalette = (workingPalette + paletteColor).take(8)
                            selectedEditorHex = paletteColor.hexCode; selectedColorDetails = null; currentScreen = AppScreen.Editor
                        },
                        onRemoveFromPalette = {
                            haptics.perform(AtelierHapticEvent.Delete)
                            colorPalette = workingPalette.filterNot { it.hexCode == paletteColor.hexCode }
                            selectedColorDetails = null
                        },
                        onShowColorBlindness = { showColorBlindness = true },
                        onBack = { haptics.perform(AtelierHapticEvent.Navigation); selectedColorDetails = null }
                    )
                }

                if (showColorBlindness) {
                    selectedColorDetails?.let { color ->
                        ColorBlindnessPreviewScreen(
                            modifier = Modifier.fillMaxSize(),
                            paletteColor = color,
                            initialType = colorBlindnessType,
                            onTypeChanged = { colorBlindnessType = it },
                            onClose = { showColorBlindness = false }
                        )
                    }
                }

                if (showShareSheet) {
                    ShareExportSheet(
                        modifier = Modifier.fillMaxSize(),
                        palette = workingPalette,
                        paletteName = paletteName,
                        onShareImage = {
                            haptics.perform(AtelierHapticEvent.Share)
                            sharePaletteImage(context, workingPalette, paletteName.ifBlank { "Campalette palette" })
                        },
                        onCopyAllHex = {
                            clipboardManager.setText(AnnotatedString(workingPalette.joinToString("\n") { it.hexCode }))
                            scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.copied)) }
                        },
                        onExportCss = { copyExport(clipboardManager, AtelierData.exportCss(workingPalette), scope, snackbarHostState, activity) },
                        onExportSwift = { copyExport(clipboardManager, AtelierData.exportSwift(workingPalette), scope, snackbarHostState, activity) },
                        onExportAndroid = { copyExport(clipboardManager, AtelierData.exportAndroidRes(workingPalette), scope, snackbarHostState, activity) },
                        onExportFigma = { copyExport(clipboardManager, AtelierData.exportFigma(workingPalette), scope, snackbarHostState, activity) },
                        onClose = { showShareSheet = false }
                    )
                }
            }
        }
        }
    }
}

private fun List<PaletteColor>.withoutDuplicateColors(): List<PaletteColor> =
    distinctBy { it.color }.take(8)

private fun paletteColorSaver(): Saver<PaletteColor?, Any> = Saver(
    save = { color -> color?.let { listOf(it.name, it.color, it.hexCode, it.red, it.green, it.blue) } },
    restore = { saved -> val v = saved as List<*>; PaletteColor(v[0] as String, v[1] as Int, v[2] as String, v[3] as Int, v[4] as Int, v[5] as Int) }
)

private fun paletteColorListSaver(): Saver<List<PaletteColor>, Any> = Saver(
    save = { list -> list.flatMap { listOf(it.name, it.color, it.hexCode, it.red, it.green, it.blue) } },
    restore = { saved -> (saved as List<*>).chunked(6).map { PaletteColor(it[0] as String, it[1] as Int, it[2] as String, it[3] as Int, it[4] as Int, it[5] as Int) } }
)

private fun paletteStudyListSaver(): Saver<List<PaletteStudy>, Any> = Saver(
    save = { studies ->
        studies.flatMap { s ->
            val colors = s.colors.flatMap { listOf(it.name, it.color, it.hexCode, it.red, it.green, it.blue) }
            listOf(s.name, s.note, s.capturedAt, s.source, s.colors.size) + colors
        }
    },
    restore = { saved ->
        val p = saved as List<*>
        val r = mutableListOf<PaletteStudy>()
        var i = 0
        while (i < p.size) {
            val n = p[i++] as String; val nt = p[i++] as String; val ca = p[i++] as String; val s = p[i++] as String; val cc = p[i++] as Int
            val c = buildList { repeat(cc) { add(PaletteColor(p[i++] as String, p[i++] as Int, p[i++] as String, p[i++] as Int, p[i++] as Int, p[i++] as Int)) } }
            r += PaletteStudy(name = n, colors = c, note = nt, capturedAt = ca, source = s)
        }
        r
    }
)

private fun paletteStudySaver(): Saver<PaletteStudy?, Any> = Saver(
    save = { study ->
        study?.let { s ->
            val colors = s.colors.flatMap { listOf(it.name, it.color, it.hexCode, it.red, it.green, it.blue) }
            listOf(s.name, s.note, s.capturedAt, s.source, s.colors.size) + colors
        }
    },
    restore = { saved ->
        val p = saved as List<*>; var i = 0
        val n = p[i++] as String; val nt = p[i++] as String; val ca = p[i++] as String; val s = p[i++] as String; val cc = p[i++] as Int
        val c = buildList { repeat(cc) { add(PaletteColor(p[i++] as String, p[i++] as Int, p[i++] as String, p[i++] as Int, p[i++] as Int, p[i++] as Int)) } }
        PaletteStudy(name = n, colors = c, note = nt, capturedAt = ca, source = s)
    }
)

private fun decodeBitmap(context: android.content.Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            .copy(Bitmap.Config.ARGB_8888, false)
    } else {
        context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input) ?: error("Unable to decode selected image")
        }
    }
}

private fun sharePaletteImage(
    context: android.content.Context,
    palette: List<PaletteColor>,
    title: String
) {
    if (palette.isEmpty()) return
    val bitmap = AtelierData.generateShareBitmap(palette, title)
    val directory = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val file = File(directory, "campalette-${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    bitmap.recycle()
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share palette"))
}

private fun copyExport(
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    value: String,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    activity: MainActivity
) {
    clipboardManager.setText(AnnotatedString(value))
    scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.copied)) }
}
