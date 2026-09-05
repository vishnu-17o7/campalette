package com.vishnu.campalette.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.content.Intent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vishnu.campalette.MainActivity
import com.vishnu.campalette.BuildConfig
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.components.AppScreen
import com.vishnu.campalette.ui.components.CampaletteWindowSizeClass
import com.vishnu.campalette.ui.components.LocalWindowSizeClass
import com.vishnu.campalette.ui.components.dockClearance
import com.vishnu.campalette.ui.components.windowSizeClassFor

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
import com.vishnu.campalette.ui.state.PaletteViewModel
import com.vishnu.campalette.ui.state.SettingsUiState
import com.vishnu.campalette.data.ThemeMode
import java.util.concurrent.ExecutorService
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

@Composable
@Suppress("DEPRECATION")
fun AppShell(
    activity: MainActivity,
    cameraExecutor: ExecutorService,
    settingsState: SettingsUiState,
    isDarkTheme: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onPaletteTintChange: (Boolean) -> Unit,
    onReducedMotionChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onPaletteColorCountChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = remember(context) {
        checkNotNull(context.getSystemService(ClipboardManager::class.java))
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    val haptics = rememberAtelierHaptics(enabled = settingsState.hapticsEnabled)
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        haptics.perform(
            if (isGranted) AtelierHapticEvent.PermissionGranted else AtelierHapticEvent.PermissionDenied
        )
    }

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
    var editorOrigin by rememberSaveable { mutableStateOf(AppScreen.Live) }
    val paletteViewModel: PaletteViewModel = viewModel()
    val workspace by paletteViewModel.workspace.collectAsStateWithLifecycle()
    val libraryState by paletteViewModel.library.collectAsStateWithLifecycle()
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    val liveSampleSession = remember { LiveSampleSession() }
    var previewSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedColorDetails by rememberSaveable(stateSaver = paletteColorSaver()) { mutableStateOf<PaletteColor?>(null) }
    var flashPreferred by rememberSaveable { mutableStateOf(false) }
    var lensFacing by rememberSaveable { mutableIntStateOf(androidx.camera.core.CameraSelector.LENS_FACING_BACK) }
    var gridEnabled by rememberSaveable { mutableStateOf(false) }
    var cameraHasFlash by rememberSaveable { mutableStateOf(false) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }
    var showColorBlindness by rememberSaveable { mutableStateOf(false) }
    var colorBlindnessType by rememberSaveable { mutableStateOf(AtelierData.ColorBlindnessType.Deuteranopia) }
    var lastCameraErrorMessage by remember { mutableStateOf<String?>(null) }
    var lastCameraErrorAt by remember { mutableLongStateOf(0L) }
    val hasCapturedPalette = capturedImage != null && workspace.palette.isNotEmpty()
    val dominantSeedColor = if (settingsState.paletteTintEnabled) workspace.palette.firstOrNull()?.color else null

    LaunchedEffect(currentScreen, hasCameraPermission, selectedColorDetails, isDarkTheme) {
        val detailColor = selectedColorDetails
        val useDarkSystemIcons = when {
            detailColor != null -> ColorUtils.calculateLuminance(detailColor.color) > 0.179
            currentScreen == AppScreen.Live && hasCameraPermission -> false
            else -> !isDarkTheme
        }
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

    val workingPalette = workspace.palette
    val activeEditorColor = workingPalette.firstOrNull { it.hexCode == workspace.selectedHex }
        ?: workingPalette.firstOrNull()

    DisposableEffect(capturedImage) {
        val ownedBitmap = capturedImage
        onDispose {
            ownedBitmap?.takeUnless(Bitmap::isRecycled)?.recycle()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val result = runCatching {
                    val bitmap = withContext(Dispatchers.IO) { decodeBitmap(context, uri) }
                    val displayBitmap = withContext(Dispatchers.Default) { AtelierData.downscaleBitmap(bitmap, 1080) }
                    if (displayBitmap !== bitmap) bitmap.recycle()
                    try {
                        val extracted = withContext(Dispatchers.Default) {
                            AtelierData.extractColorPalette(activity, displayBitmap, settingsState.paletteColorCount)
                                .withoutDuplicateColors()
                        }
                        displayBitmap to extracted
                    } catch (error: Throwable) {
                        displayBitmap.recycle()
                        throw error
                    }
                }
                result.onSuccess { (displayBitmap, extracted) ->
                    val importName = activity.getString(R.string.capture_name, AtelierData.timestampLabel())
                    val source = activity.getString(R.string.source_gallery_import)
                    val study = AtelierData.createCapturedStudy(importName, extracted, source)
                    capturedImage = displayBitmap
                    paletteViewModel.replaceWorkspace(
                        palette = extracted,
                        name = importName,
                        source = source,
                        latestCapturedStudy = study
                    )
                    onPaletteTintChange(true)
                    paletteViewModel.recordHistory(study)
                    haptics.perform(AtelierHapticEvent.GalleryImport)
                }.onFailure {
                    haptics.perform(AtelierHapticEvent.Error)
                    snackbarHostState.showSnackbar(activity.getString(R.string.gallery_import_error))
                }
            }
        }
    }

    fun saveStudy(study: PaletteStudy, message: Int = R.string.palette_saved) {
        if (study.colors.isEmpty()) return
        val cleanedColors = study.colors.withoutDuplicateColors()
        val resolvedName = study.name.ifBlank { activity.getString(R.string.capture_name, AtelierData.timestampLabel()) }
        val savedStudy = AtelierData.createCapturedStudy(
            name = resolvedName,
            colors = cleanedColors,
            source = study.source.ifBlank { activity.getString(R.string.current_palette_source) }
        )
        paletteViewModel.saveStudy(savedStudy)
        haptics.perform(AtelierHapticEvent.Save)
        scope.launch { snackbarHostState.showSnackbar(activity.getString(message)) }
    }

    fun saveCurrentPalette() {
        if (workingPalette.isNotEmpty()) {
            val resolvedName = workspace.name.ifBlank {
                activity.getString(R.string.capture_name, AtelierData.timestampLabel())
            }
            val resolvedSource = workspace.source.ifBlank {
                activity.getString(R.string.current_palette_source)
            }
            paletteViewModel.replaceWorkspace(
                palette = workingPalette,
                name = resolvedName,
                source = resolvedSource
            )
            saveStudy(
                PaletteStudy(
                    name = resolvedName,
                    colors = workingPalette,
                    source = resolvedSource
                )
            )
        }
    }

    fun saveColor(color: PaletteColor) {
        val savedStudy = AtelierData.createCapturedStudy(
            name = AtelierData.guessColorName(color.color),
            colors = listOf(color),
            source = activity.getString(R.string.source_saved_color)
        )
        paletteViewModel.saveSingleColor(savedStudy)
        haptics.perform(AtelierHapticEvent.Save)
        scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.color_saved)) }
    }

    fun openEditor(origin: AppScreen) {
        editorOrigin = origin
        haptics.perform(AtelierHapticEvent.Navigation)
        currentScreen = AppScreen.Editor
    }

    val isCurrentPaletteSaved = remember(libraryState.savedPalettes, workingPalette, workspace.name) {
        workingPalette.isNotEmpty() && libraryState.savedPalettes.any { study ->
            study.name == workspace.name &&
                study.colors.map(PaletteColor::hexCode) == workingPalette.map(PaletteColor::hexCode)
        }
    }

    val harmonyLabel = remember(workspace.harmonyMode) {
        when (workspace.harmonyMode) {
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
                scope.launch {
                    var displayBmp: Bitmap? = null
                    var extractBmp: Bitmap? = null
                    try {
                        val prepared = withContext(Dispatchers.Default) {
                            val display = AtelierData.downscaleBitmap(bitmap, 1080)
                            val extract = AtelierData.downscaleBitmap(display, 256)
                            if (bitmap !== display && bitmap !== extract) {
                                bitmap.takeUnless(Bitmap::isRecycled)?.recycle()
                            }
                            display to extract
                        }
                        displayBmp = prepared.first
                        extractBmp = prepared.second
                        capturedImage = prepared.first
                        isCapturing = false
                        haptics.perform(AtelierHapticEvent.CaptureSuccess)
                        liveSampleSession.clear()
                        currentScreen = AppScreen.Live

                        val extracted = withContext(Dispatchers.Default) {
                            AtelierData.extractColorPalette(
                                activity,
                                prepared.second,
                                settingsState.paletteColorCount
                            ).withoutDuplicateColors()
                        }
                        check(extracted.isNotEmpty()) { "No colors could be extracted from the capture." }
                        if (capturedImage !== prepared.first) return@launch
                        val captureName = activity.getString(R.string.capture_name, AtelierData.timestampLabel())
                        val source = activity.getString(R.string.source_live_camera)
                        val capturedStudy = AtelierData.createCapturedStudy(
                            name = captureName,
                            colors = extracted,
                            source = source
                        )
                        paletteViewModel.replaceWorkspace(
                            palette = extracted,
                            name = captureName,
                            source = source,
                            latestCapturedStudy = capturedStudy
                        )
                        onPaletteTintChange(true)
                        paletteViewModel.recordHistory(capturedStudy)
                    } catch (error: Throwable) {
                        if (error is CancellationException) {
                            if (displayBmp == null) {
                                bitmap.takeUnless(Bitmap::isRecycled)?.recycle()
                            }
                            throw error
                        }
                        haptics.perform(AtelierHapticEvent.Error)
                        snackbarHostState.showSnackbar(activity.getString(R.string.capture_failed))
                        if (displayBmp == null) {
                            bitmap.takeUnless(Bitmap::isRecycled)?.recycle()
                        }
                    } finally {
                        val extract = extractBmp
                        val display = displayBmp
                        if (extract != null && extract !== display) {
                            extract.takeUnless(Bitmap::isRecycled)?.recycle()
                        }
                        isCapturing = false
                    }
                }
            },
            onError = { message ->
                isCapturing = false
                haptics.perform(AtelierHapticEvent.Error)
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    val clearCapture: () -> Unit = remember {
        {
            capturedImage = null
            paletteViewModel.clearWorkspace()
            liveSampleSession.clear()
        }
    }
    val latestPerformCapture = rememberUpdatedState { performCapture() }
    val latestSaveCurrentPalette = rememberUpdatedState { saveCurrentPalette() }
    val latestOpenLiveEditor = rememberUpdatedState { openEditor(AppScreen.Live) }
    val latestOnPaletteTintChange = rememberUpdatedState(onPaletteTintChange)
    val latestWorkspace = rememberUpdatedState(workspace)
    val onCapture: () -> Unit = remember {
        {
            latestPerformCapture.value()
        }
    }
    val onCapturePress: () -> Unit = remember {
        {
            haptics.perform(AtelierHapticEvent.CapturePress)
        }
    }
    val onMenuClick: () -> Unit = remember {
        {
            haptics.perform(AtelierHapticEvent.Navigation)
            currentScreen = AppScreen.History
        }
    }
    val onEditPalette: () -> Unit = remember {
        { latestOpenLiveEditor.value() }
    }
    val onSavePalette: () -> Unit = remember {
        { latestSaveCurrentPalette.value() }
    }
    val onPreviewMeasured: (IntSize) -> Unit = remember {
        { size -> previewSize = size }
    }
    val onFlipCamera: () -> Unit = remember {
        {
            haptics.perform(AtelierHapticEvent.Selection)
            lensFacing = if (lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_BACK) {
                androidx.camera.core.CameraSelector.LENS_FACING_FRONT
            } else {
                androidx.camera.core.CameraSelector.LENS_FACING_BACK
            }
        }
    }
    val onToggleFlash: () -> Unit = remember {
        {
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
        }
    }
    val onToggleGrid: () -> Unit = remember {
        {
            haptics.perform(
                if (gridEnabled) AtelierHapticEvent.ToggleOff else AtelierHapticEvent.ToggleOn
            )
            gridEnabled = !gridEnabled
        }
    }
    val onFlashAvailabilityChanged: (Boolean) -> Unit = remember {
        { available ->
            cameraHasFlash = available
            if (!available) flashPreferred = false
        }
    }
    val onSampleTap: (Bitmap, Offset, IntSize) -> Unit = remember {
        { bitmap, offset, size ->
            val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
            if (sampledColor != null) {
                haptics.perform(AtelierHapticEvent.Sample)
                val generated = AtelierData.generatePaletteFromSeedColor(activity, sampledColor)
                    .withoutDuplicateColors()
                paletteViewModel.setPalette(
                    generated,
                    activity.getString(
                        R.string.source_sampled_capture,
                        latestWorkspace.value.latestCapturedStudy?.name
                            ?: activity.getString(R.string.source_live_camera)
                    )
                )
                latestOnPaletteTintChange.value(true)
                liveSampleSession.clear()
            }
        }
    }
    val onSampleLongPress: (Bitmap, Offset, IntSize) -> Unit = remember {
        { bitmap, offset, size ->
            val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
            if (sampledColor != null) {
                haptics.perform(AtelierHapticEvent.SampleExplore)
                liveSampleSession.update(sampledColor)
            }
        }
    }
    val onSampleDrag: (Bitmap, Offset, IntSize) -> Unit = remember {
        { bitmap, offset, size ->
            val sampledColor = AtelierData.sampleColorFromBitmap(bitmap, offset, size)
            if (sampledColor != null) {
                liveSampleSession.update(sampledColor)
            }
        }
    }
    val onSampleDragEnd: () -> Unit = remember {
        {
            val sampledColor = liveSampleSession.color
            liveSampleSession.clear()
            if (sampledColor != null) {
                val generated = AtelierData.generatePaletteFromSeedColor(activity, sampledColor)
                    .withoutDuplicateColors()
                paletteViewModel.setPalette(
                    generated,
                    activity.getString(
                        R.string.source_sampled_capture,
                        latestWorkspace.value.latestCapturedStudy?.name
                            ?: activity.getString(R.string.source_live_camera)
                    )
                )
                latestOnPaletteTintChange.value(true)
            }
        }
    }
    val onColorSelected: (PaletteColor) -> Unit = remember {
        { color ->
            haptics.perform(AtelierHapticEvent.Selection)
            selectedColorDetails = color
        }
    }
    val onCameraError: (String) -> Unit = remember {
        { message ->
            val now = SystemClock.elapsedRealtime()
            if (message != lastCameraErrorMessage || now - lastCameraErrorAt > 5_000L) {
                lastCameraErrorMessage = message
                lastCameraErrorAt = now
                haptics.perform(AtelierHapticEvent.Error)
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        }
    }
    val onImportGallery: () -> Unit = remember {
        {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    BackHandler {
        when {
            showColorBlindness -> {
                haptics.perform(AtelierHapticEvent.SheetDismiss)
                showColorBlindness = false
            }
            showShareSheet -> {
                haptics.perform(AtelierHapticEvent.SheetDismiss)
                showShareSheet = false
            }
            selectedColorDetails != null -> {
                haptics.perform(AtelierHapticEvent.Navigation)
                selectedColorDetails = null
            }
            liveSampleSession.color != null -> liveSampleSession.clear()
            currentScreen == AppScreen.Editor -> {
                haptics.perform(AtelierHapticEvent.Navigation)
                currentScreen = editorOrigin
            }
            currentScreen == AppScreen.Live && capturedImage != null -> clearCapture()
            currentScreen != AppScreen.Live -> {
                haptics.perform(AtelierHapticEvent.Navigation)
                currentScreen = AppScreen.Live
            }
            else -> activity.finish()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = dockClearance(extraAboveIsland = 0.dp))
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        CompositionLocalProvider(LocalReducedMotion provides settingsState.reducedMotion) {
        DynamicThemeProvider(dominantColor = dominantSeedColor) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                val windowSizeClass = windowSizeClassFor(maxWidth)
                val compactHeight = maxHeight < 480.dp
                val horizontalMotionSign = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
                CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
            Box(modifier = Modifier.fillMaxSize()) {
                val dockVisible = currentScreen != AppScreen.Editor &&
                    selectedColorDetails == null &&
                    !showShareSheet &&
                    !showColorBlindness
                val reserveDockSpace = compactHeight &&
                    dockVisible &&
                    (currentScreen != AppScreen.Live || !hasCameraPermission)
                val cameraOnLive = currentScreen == AppScreen.Live && hasCameraPermission
                val showForeground = !cameraOnLive
                val liveCameraState = remember(workspace.palette, selectedColorDetails, previewSize, hasCapturedPalette, workspace.name, harmonyLabel, isCapturing, isCurrentPaletteSaved) {
                    com.vishnu.campalette.ui.screens.LiveCameraState(
                        palette = workspace.palette,
                        selectedColor = selectedColorDetails ?: workspace.palette.firstOrNull(),
                        previewSize = previewSize,
                        hasCapturedPalette = hasCapturedPalette,
                        paletteName = workspace.name,
                        harmonyLabel = harmonyLabel,
                        isCapturing = isCapturing,
                        isPaletteSaved = isCurrentPaletteSaved
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState)
                ) {
                if (hasCameraPermission) {
                    LiveCameraScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = if (cameraOnLive) 1f else 0f }
                            .then(if (cameraOnLive) Modifier else Modifier.clearAndSetSemantics { }),
                        state = liveCameraState,
                        activity = activity,
                        capturedImage = capturedImage,
                        samplingLoupe = null,
                        gridEnabled = gridEnabled,
                        flashEnabled = flashPreferred && cameraHasFlash,
                        flashAvailable = cameraHasFlash,
                        lensFacing = lensFacing,
                        largeTouchTargets = false,
                        onCapture = onCapture,
                        onCapturePress = onCapturePress,
                        onClearCapture = clearCapture,
                        onMenuClick = onMenuClick,
                        onEditPalette = onEditPalette,
                        onSavePalette = onSavePalette,
                        onPreviewMeasured = onPreviewMeasured,
                        onFlipCamera = onFlipCamera,
                        onToggleFlash = onToggleFlash,
                        onToggleGrid = onToggleGrid,
                        onFlashAvailabilityChanged = onFlashAvailabilityChanged,
                        onSampleTap = onSampleTap,
                        onSampleLongPress = onSampleLongPress,
                        onSampleDrag = onSampleDrag,
                        onSampleDragEnd = onSampleDragEnd,
                        onColorSelected = onColorSelected,
                        onCameraError = onCameraError,
                        onImportGallery = onImportGallery
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showForeground,
                    modifier = Modifier.fillMaxSize(),
                    enter = if (settingsState.reducedMotion) {
                        fadeIn(tween(90))
                    } else if (currentScreen == AppScreen.Editor) {
                        fadeIn(tween(200)) + slideInHorizontally(tween(200)) { horizontalMotionSign * it / 5 }
                    } else {
                        fadeIn(tween(140))
                    },
                    exit = if (settingsState.reducedMotion) {
                        fadeOut(tween(70))
                    } else if (currentScreen == AppScreen.Live) {
                        fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { -horizontalMotionSign * it / 8 }
                    } else {
                        fadeOut(tween(120))
                    }
                ) {
                AnimatedContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (reserveDockSpace) {
                                    dockClearance(extraAboveIsland = 0.dp)
                                } else {
                                    0.dp
                                }
                            ),
                        targetState = currentScreen,
                        transitionSpec = {
                            if (settingsState.reducedMotion) {
                                fadeIn(tween(90)).togetherWith(fadeOut(tween(70)))
                            } else if (targetState == AppScreen.Editor) {
                                (
                                    fadeIn(tween(200)) +
                                        slideInHorizontally(tween(200)) { horizontalMotionSign * it / 5 }
                                ).togetherWith(
                                    fadeOut(tween(160)) +
                                        slideOutHorizontally(tween(160)) { -horizontalMotionSign * it / 8 }
                                )
                            } else if (initialState == AppScreen.Editor) {
                                (
                                    fadeIn(tween(200)) +
                                        slideInHorizontally(tween(200)) { -horizontalMotionSign * it / 8 }
                                ).togetherWith(
                                    fadeOut(tween(160)) +
                                        slideOutHorizontally(tween(160)) { horizontalMotionSign * it / 5 }
                                )
                            } else {
                                val primaryOrder = mapOf(
                                    AppScreen.History to 0,
                                    AppScreen.Live to 1,
                                    AppScreen.Settings to 2
                                )
                                val logicalDirection = if (
                                    (primaryOrder[targetState] ?: 0) >= (primaryOrder[initialState] ?: 0)
                                ) 1 else -1
                                val direction = logicalDirection * horizontalMotionSign
                                (
                                    fadeIn(tween(140)) +
                                        slideInHorizontally(tween(140)) { direction * it / 10 }
                                ).togetherWith(
                                    fadeOut(tween(120)) +
                                        slideOutHorizontally(tween(120)) { -direction * it / 10 }
                                )
                            }
                        },
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.Live -> OnboardingPermissionScreen(
                                modifier = Modifier.fillMaxSize(),
                                onRequestPermission = {
                                    haptics.perform(AtelierHapticEvent.PermissionPrompt)
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                onImportGallery = onImportGallery
                            )



                            AppScreen.History -> PaletteLibraryScreen(
                                modifier = Modifier.fillMaxSize(),
                                state = com.vishnu.campalette.ui.screens.LibraryState(
                                    palettes = libraryState.palettes,
                                    searchQuery = libraryState.searchQuery,
                                    selectedFilter = libraryState.selectedFilter,
                                    selectedSort = "Recent",
                                    isGridView = libraryState.isGridView,
                                    favorites = libraryState.favorites
                                ),
                                onSearchChange = paletteViewModel::setSearchQuery,
                                onFilterChange = { filter ->
                                    if (filter != libraryState.selectedFilter) {
                                        haptics.perform(AtelierHapticEvent.Filter)
                                        paletteViewModel.setLibraryFilter(filter)
                                    }
                                },
                                onToggleGrid = {
                                    haptics.perform(
                                        if (libraryState.isGridView) {
                                            AtelierHapticEvent.ToggleOff
                                        } else {
                                            AtelierHapticEvent.ToggleOn
                                        }
                                    )
                                    paletteViewModel.toggleLibraryGrid()
                                },
                                onPaletteSelect = { selected ->
                                    val cleanedColors = selected.colors.withoutDuplicateColors()
                                    paletteViewModel.replaceWorkspace(
                                        palette = cleanedColors,
                                        name = selected.name,
                                        source = selected.source,
                                        latestCapturedStudy = selected
                                    )
                                    onPaletteTintChange(true)
                                    openEditor(AppScreen.History)
                                },
                                onDeletePalette = { study ->
                                    haptics.perform(AtelierHapticEvent.Delete)
                                    paletteViewModel.deleteStudy(study)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = activity.getString(R.string.palette_deleted),
                                            actionLabel = activity.getString(R.string.undo),
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            haptics.perform(AtelierHapticEvent.Undo)
                                            paletteViewModel.undoDeleteStudy()
                                        }
                                    }
                                },
                                onNavigate = {
                                    haptics.perform(AtelierHapticEvent.Navigation)
                                    currentScreen = it
                                },
                                onSavePalette = { selected -> saveStudy(selected) }
                            )

                            AppScreen.Editor -> {
                                val inlineEditorDetail = windowSizeClass.isExpanded && selectedColorDetails != null
                                val editorContent = @Composable { editorModifier: Modifier ->
                                    PaletteEditorScreen(
                                        modifier = editorModifier,
                                        state = com.vishnu.campalette.ui.screens.EditorState(
                                            name = workspace.name,
                                            source = workspace.source,
                                            palette = workingPalette,
                                            harmonyMode = workspace.harmonyMode,
                                            selectedHex = workspace.selectedHex
                                        ),
                                        onNameChange = paletteViewModel::setPaletteName,
                                        onHarmonyModeChange = { requestedMode ->
                                            activeEditorColor?.let { selected ->
                                                haptics.perform(AtelierHapticEvent.Selection)
                                                val harmonized = AtelierData.harmonyPalette(activity, selected.color, requestedMode).withoutDuplicateColors()
                                                paletteViewModel.applyHarmony(
                                                    requestedMode,
                                                    harmonized,
                                                    activity.getString(R.string.source_harmony_study)
                                                )
                                                onPaletteTintChange(true)
                                            }
                                        },
                                        onSelectColor = {
                                            if (workspace.selectedHex != it.hexCode) {
                                                haptics.perform(AtelierHapticEvent.Selection)
                                                paletteViewModel.selectEditorColor(it.hexCode)
                                            }
                                        },
                                        onInspectColor = {
                                            haptics.perform(AtelierHapticEvent.Inspect)
                                            selectedColorDetails = it
                                        },
                                        onDeleteColor = { color ->
                                            haptics.perform(AtelierHapticEvent.Delete)
                                            paletteViewModel.removeColor(color, keepAtLeastOne = true)
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = activity.getString(R.string.color_removed),
                                                    actionLabel = activity.getString(R.string.undo),
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    haptics.perform(AtelierHapticEvent.Undo)
                                                    paletteViewModel.undoRemoveColor()
                                                }
                                            }
                                        },
                                        onReorder = { from, to ->
                                            haptics.perform(AtelierHapticEvent.Reorder)
                                            paletteViewModel.reorderColor(from, to)
                                        },
                                        onReorderStart = {
                                            haptics.perform(AtelierHapticEvent.ReorderStart)
                                        },
                                        onReorderDrop = {
                                            haptics.perform(AtelierHapticEvent.ReorderDrop)
                                        },
                                        onSave = {
                                            saveCurrentPalette()
                                            currentScreen = AppScreen.History
                                        },
                                        onShare = {
                                            haptics.perform(AtelierHapticEvent.SheetPresent)
                                            showShareSheet = true
                                        },
                                        onNavigate = {
                                            haptics.perform(AtelierHapticEvent.Navigation)
                                            currentScreen = it
                                        },
                                        onBack = {
                                            haptics.perform(AtelierHapticEvent.Navigation)
                                            currentScreen = editorOrigin
                                        }
                                    )
                                }
                                if (inlineEditorDetail) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .windowInsetsPadding(
                                                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)
                                            )
                                    ) {
                                        editorContent(
                                            Modifier
                                                .widthIn(max = 420.dp)
                                                .fillMaxHeight()
                                                .weight(1f, fill = false)
                                        )
                                        selectedColorDetails?.let { paletteColor ->
                                            ColorDetailScreen(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                paletteColor = paletteColor,
                                                isColorSaved = paletteColor.hexCode in libraryState.savedColorHexes,
                                                isPaletteSaved = isCurrentPaletteSaved,
                                                onCopy = { value ->
                                                    haptics.perform(AtelierHapticEvent.Copy)
                                                    copyText(
                                                        clipboardManager,
                                                        activity.getString(R.string.clipboard_color_label),
                                                        value
                                                    )
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(activity.getString(R.string.copied))
                                                    }
                                                },
                                                onSaveColor = ::saveColor,
                                                onSavePalette = { saveCurrentPalette() },
                                                onAddToPalette = {
                                                    haptics.perform(AtelierHapticEvent.AddToPalette)
                                                    paletteViewModel.addColor(paletteColor)
                                                    selectedColorDetails = null
                                                },
                                                onRemoveFromPalette = {
                                                    haptics.perform(AtelierHapticEvent.Delete)
                                                    paletteViewModel.removeColor(paletteColor)
                                                    selectedColorDetails = null
                                                },
                                                onShowColorBlindness = {
                                                    haptics.perform(AtelierHapticEvent.SheetPresent)
                                                    showColorBlindness = true
                                                },
                                                onBack = {
                                                    haptics.perform(AtelierHapticEvent.Navigation)
                                                    selectedColorDetails = null
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    editorContent(Modifier.fillMaxSize())
                                }
                            }

                            AppScreen.Settings -> SettingsScreen(
                                modifier = Modifier.fillMaxSize(),
                                themeMode = settingsState.themeMode,
                                onThemeModeChange = { mode ->
                                    if (mode != settingsState.themeMode) {
                                        haptics.perform(AtelierHapticEvent.Selection)
                                        onThemeModeChange(mode)
                                    }
                                },
                                paletteTintEnabled = settingsState.paletteTintEnabled,
                                onPaletteTintChange = { enabled ->
                                    haptics.perform(
                                        if (enabled) AtelierHapticEvent.ToggleOn else AtelierHapticEvent.ToggleOff
                                    )
                                    onPaletteTintChange(enabled)
                                },
                                reducedMotion = settingsState.reducedMotion,
                                onReducedMotionChange = { enabled ->
                                    haptics.perform(
                                        if (enabled) AtelierHapticEvent.ToggleOn else AtelierHapticEvent.ToggleOff
                                    )
                                    onReducedMotionChange(enabled)
                                },
                                paletteColorCount = settingsState.paletteColorCount,
                                onPaletteColorCountChange = { count ->
                                    haptics.perform(AtelierHapticEvent.Selection)
                                    onPaletteColorCountChange(count)
                                },
                                hapticsEnabled = settingsState.hapticsEnabled,
                                onHapticsChange = { enabled ->
                                    haptics.perform(
                                        if (enabled) AtelierHapticEvent.ToggleOn else AtelierHapticEvent.ToggleOff,
                                        force = enabled
                                    )
                                    onHapticsChange(enabled)
                                },
                                versionName = BuildConfig.VERSION_NAME,
                                onOpenPrivacyPolicy = {
                                    haptics.perform(AtelierHapticEvent.Navigation)
                                    openExternalUrl(
                                        context = context,
                                        url = activity.getString(R.string.privacy_policy_url),
                                        errorMessage = activity.getString(R.string.settings_privacy_policy_error),
                                        snackbarHostState = snackbarHostState,
                                        scope = scope
                                    )
                                },
                                onOpenTerms = {
                                    haptics.perform(AtelierHapticEvent.Navigation)
                                    openExternalUrl(
                                        context = context,
                                        url = activity.getString(R.string.terms_url),
                                        errorMessage = activity.getString(R.string.settings_terms_error),
                                        snackbarHostState = snackbarHostState,
                                        scope = scope
                                    )
                                }
                            )
                        }
                }
                }
                }

                val dockScreen = if (currentScreen == AppScreen.Editor) editorOrigin else currentScreen
                AnimatedVisibility(
                    visible = dockVisible,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    enter = if (settingsState.reducedMotion) {
                        fadeIn(tween(90))
                    } else {
                        fadeIn(tween(160)) + slideInVertically(tween(180)) { it / 3 }
                    },
                    exit = if (settingsState.reducedMotion) {
                        fadeOut(tween(70))
                    } else {
                        fadeOut(tween(140)) + slideOutVertically(tween(160)) { it / 3 }
                    }
                ) {
                    com.vishnu.campalette.ui.components.BottomBar(
                        currentScreen = dockScreen,
                        useCameraGlass = dockScreen == AppScreen.Live && hasCameraPermission,
                        hazeState = hazeState,
                        onScreenSelected = {
                            if (it != currentScreen) {
                                haptics.perform(AtelierHapticEvent.Navigation)
                                currentScreen = it
                            }
                        }
                    )
                }

                val inlineEditorDetail = windowSizeClass.isExpanded &&
                    currentScreen == AppScreen.Editor &&
                    selectedColorDetails != null
                if (!inlineEditorDetail) {
                    AnimatedContent(
                        targetState = selectedColorDetails,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            if (settingsState.reducedMotion) {
                                fadeIn(tween(90)).togetherWith(fadeOut(tween(70)))
                            } else if (windowSizeClass == CampaletteWindowSizeClass.Compact) {
                                when {
                                    initialState == null && targetState != null ->
                                        (slideInHorizontally(tween(200)) { horizontalMotionSign * it / 4 } +
                                            fadeIn(tween(180)))
                                            .togetherWith(fadeOut(tween(120)))
                                    initialState != null && targetState == null ->
                                        fadeIn(tween(120)).togetherWith(
                                            slideOutHorizontally(tween(180)) { horizontalMotionSign * it / 4 } +
                                                fadeOut(tween(160))
                                        )
                                    else -> fadeIn(tween(140)).togetherWith(fadeOut(tween(120)))
                                }
                            } else {
                                (fadeIn(tween(160)) + scaleIn(tween(180), initialScale = 0.97f))
                                    .togetherWith(
                                        fadeOut(tween(140)) + scaleOut(tween(160), targetScale = 0.97f)
                                    )
                            }
                        },
                        label = "colorDetailTransition"
                    ) { paletteColor ->
                    paletteColor?.let {
                        val detailScreen = @Composable { detailModifier: Modifier ->
                            ColorDetailScreen(
                                modifier = detailModifier,
                                paletteColor = paletteColor,
                                isColorSaved = paletteColor.hexCode in libraryState.savedColorHexes,
                                isPaletteSaved = isCurrentPaletteSaved,
                                onCopy = { value ->
                                    haptics.perform(AtelierHapticEvent.Copy)
                                    copyText(
                                        clipboardManager,
                                        activity.getString(R.string.clipboard_color_label),
                                        value
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar(activity.getString(R.string.copied))
                                    }
                                },
                                onSaveColor = ::saveColor,
                                onSavePalette = { saveCurrentPalette() },
                                onAddToPalette = {
                                    haptics.perform(AtelierHapticEvent.AddToPalette)
                                    paletteViewModel.addColor(paletteColor)
                                    selectedColorDetails = null
                                    currentScreen = AppScreen.Editor
                                },
                                onRemoveFromPalette = {
                                    haptics.perform(AtelierHapticEvent.Delete)
                                    paletteViewModel.removeColor(paletteColor)
                                    selectedColorDetails = null
                                },
                                onShowColorBlindness = {
                                    haptics.perform(AtelierHapticEvent.SheetPresent)
                                    showColorBlindness = true
                                },
                                onBack = { haptics.perform(AtelierHapticEvent.Navigation); selectedColorDetails = null }
                            )
                        }
                        if (windowSizeClass == CampaletteWindowSizeClass.Compact) {
                            detailScreen(Modifier.fillMaxSize())
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.38f))
                                    .clickable {
                                        haptics.perform(AtelierHapticEvent.Navigation)
                                        selectedColorDetails = null
                                    }
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .widthIn(max = 640.dp)
                                        .fillMaxHeight(0.92f)
                                        .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {},
                                    shape = RoundedCornerShape(28.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    shadowElevation = 18.dp
                                ) {
                                    detailScreen(Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                    }
                }

                AnimatedVisibility(
                    visible = showColorBlindness && selectedColorDetails != null,
                    modifier = Modifier.fillMaxSize(),
                    enter = EnterTransition.None,
                    exit = ExitTransition.None
                ) {
                    selectedColorDetails?.let { color ->
                        ColorBlindnessPreviewScreen(
                            modifier = Modifier.fillMaxSize(),
                            paletteColor = color,
                            initialType = colorBlindnessType,
                            onTypeChanged = {
                                if (it != colorBlindnessType) {
                                    haptics.perform(AtelierHapticEvent.ColorBlindnessPreview)
                                    colorBlindnessType = it
                                }
                            },
                            onClose = {
                                haptics.perform(AtelierHapticEvent.SheetDismiss)
                                showColorBlindness = false
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showShareSheet,
                    modifier = Modifier.fillMaxSize(),
                    enter = EnterTransition.None,
                    exit = ExitTransition.None
                ) {
                    ShareExportSheet(
                        modifier = Modifier.fillMaxSize(),
                        palette = workingPalette,
                        paletteName = workspace.name,
                        onSavePalette = { saveCurrentPalette() },
                        onShareImage = {
                            scope.launch {
                                try {
                                    val shareIntent = createPaletteShareIntent(
                                        context = context,
                                        palette = workingPalette,
                                        title = workspace.name.ifBlank {
                                            activity.getString(R.string.share_palette_default_title)
                                        }
                                    )
                                    context.startActivity(
                                        Intent.createChooser(
                                            shareIntent,
                                            activity.getString(R.string.share_palette_chooser)
                                        )
                                    )
                                    haptics.perform(AtelierHapticEvent.Share)
                                } catch (_: Exception) {
                                    haptics.perform(AtelierHapticEvent.Error)
                                    snackbarHostState.showSnackbar(
                                        activity.getString(R.string.share_palette_error)
                                    )
                                }
                            }
                        },
                        onCopyAllHex = {
                            haptics.perform(AtelierHapticEvent.Copy)
                            copyText(
                                clipboardManager,
                                activity.getString(R.string.clipboard_palette_label),
                                workingPalette.joinToString("\n") { it.hexCode }
                            )
                            scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.copied)) }
                        },
                        onExportCss = {
                            haptics.perform(AtelierHapticEvent.Export)
                            copyExport(clipboardManager, AtelierData.exportCss(workingPalette), scope, snackbarHostState, activity)
                        },
                        onExportSwift = {
                            haptics.perform(AtelierHapticEvent.Export)
                            copyExport(clipboardManager, AtelierData.exportSwift(workingPalette), scope, snackbarHostState, activity)
                        },
                        onExportAndroid = {
                            haptics.perform(AtelierHapticEvent.Export)
                            copyExport(clipboardManager, AtelierData.exportAndroidRes(workingPalette), scope, snackbarHostState, activity)
                        },
                        onExportFigma = {
                            haptics.perform(AtelierHapticEvent.Export)
                            copyExport(clipboardManager, AtelierData.exportFigma(workingPalette), scope, snackbarHostState, activity)
                        },
                        onClose = {
                            haptics.perform(AtelierHapticEvent.SheetDismiss)
                            showShareSheet = false
                        }
                    )
                }
            }
            }
        }
        }
        }
    }
}

private fun openExternalUrl(
    context: Context,
    url: String,
    errorMessage: String,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (_: ActivityNotFoundException) {
        scope.launch { snackbarHostState.showSnackbar(errorMessage) }
    }
}

private fun List<PaletteColor>.withoutDuplicateColors(): List<PaletteColor> =
    distinctBy { it.color }.take(AtelierData.MAX_PALETTE_COLOR_COUNT)

private class LiveSampleSession {
    var color: Int? = null
        private set

    fun update(sampledColor: Int) {
        color = sampledColor
    }

    fun clear() {
        color = null
    }
}

private fun paletteColorSaver(): Saver<PaletteColor?, Any> = Saver(
    save = { color -> color?.let { listOf(it.name, it.color, it.hexCode, it.red, it.green, it.blue) } },
    restore = { saved -> val v = saved as List<*>; PaletteColor(v[0] as String, v[1] as Int, v[2] as String, v[3] as Int, v[4] as Int, v[5] as Int) }
)

private fun decodeBitmap(
    context: android.content.Context,
    uri: Uri,
    maxDimension: Int = 1080
): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val width = info.size.width
            val height = info.size.height
            val largestDimension = maxOf(width, height)
            if (largestDimension > maxDimension) {
                val scale = maxDimension.toFloat() / largestDimension
                decoder.setTargetSize(
                    (width * scale).toInt().coerceAtLeast(1),
                    (height * scale).toInt().coerceAtLeast(1)
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri).use { input -> BitmapFactory.decodeStream(input, null, bounds) }
        var sampleSize = 1
        while (maxOf(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) > maxDimension * 2) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input, null, options) ?: error("Unable to decode selected image")
        }
    }
}

private suspend fun createPaletteShareIntent(
    context: android.content.Context,
    palette: List<PaletteColor>,
    title: String
): Intent {
    require(palette.isNotEmpty()) { "A palette is required for sharing." }
    val bitmap = withContext(Dispatchers.Default) {
        AtelierData.generateShareBitmap(palette, title)
    }
    val file = try {
        withContext(Dispatchers.IO) {
            val directory = File(context.cacheDir, "shared_images")
            check(directory.exists() || directory.mkdirs()) { "Could not create the share cache." }
            directory.listFiles()
                ?.sortedByDescending(File::lastModified)
                ?.drop(10)
                ?.forEach(File::delete)
            File(directory, "campalette-${System.currentTimeMillis()}.png").also { output ->
                FileOutputStream(output).use { stream ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        "Could not encode the shared palette."
                    }
                }
            }
        }
    } finally {
        bitmap.takeUnless(Bitmap::isRecycled)?.recycle()
    }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}

private fun copyExport(
    clipboardManager: ClipboardManager,
    value: String,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    activity: MainActivity
) {
    copyText(
        clipboardManager,
        activity.getString(R.string.clipboard_export_label),
        value
    )
    scope.launch { snackbarHostState.showSnackbar(activity.getString(R.string.copied)) }
}

private fun copyText(
    clipboardManager: ClipboardManager,
    label: String,
    value: String
) {
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, value))
}
