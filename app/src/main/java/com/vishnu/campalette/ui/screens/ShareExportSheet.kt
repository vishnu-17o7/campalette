package com.vishnu.campalette.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.components.CampaletteWindowSizeClass
import com.vishnu.campalette.ui.components.LocalWindowSizeClass
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ShareExportSheet(
    modifier: Modifier = Modifier,
    palette: List<PaletteColor>,
    paletteName: String,
    onSavePalette: () -> Unit,
    onShareImage: () -> Unit,
    onCopyAllHex: () -> Unit,
    onExportCss: () -> Unit,
    onExportSwift: () -> Unit,
    onExportAndroid: () -> Unit,
    onExportFigma: () -> Unit,
    onClose: () -> Unit
) {
    val resolvedPaletteName = paletteName.ifBlank { stringResource(R.string.current_palette) }
    val cssLabel = stringResource(R.string.export_format_css)
    val swiftLabel = stringResource(R.string.export_format_swift)
    val androidLabel = stringResource(R.string.export_format_android)
    val figmaLabel = stringResource(R.string.export_format_figma)
    val formats = remember(
        cssLabel,
        swiftLabel,
        androidLabel,
        figmaLabel,
        onExportCss,
        onExportSwift,
        onExportAndroid,
        onExportFigma
    ) {
        listOf(
            ExportFormat(label = cssLabel, format = "CSS", onClick = onExportCss),
            ExportFormat(label = swiftLabel, format = "Swift", onClick = onExportSwift),
            ExportFormat(label = androidLabel, format = "XML", onClick = onExportAndroid),
            ExportFormat(label = figmaLabel, format = "JSON", onClick = onExportFigma)
        )
    }
    val listPadding = remember {
        PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 20.dp)
    }

    FluidSheetScaffold(
        modifier = modifier,
        onDismiss = onClose,
        dismissLabel = stringResource(R.string.close_export_sheet),
        sheetTitle = stringResource(R.string.export_palette),
        compactHeightFraction = 0.86f
    ) { dismiss ->
        ExportSheetDragHandle()
        ExportSheetHeader(
            title = stringResource(R.string.export_palette),
            subtitle = resolvedPaletteName,
            onDone = dismiss
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .heightIn(max = 640.dp)
                .navigationBarsPadding(),
            contentPadding = listPadding,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "preview") {
                PalettePreviewStrip(palette = palette)
            }

            item(key = "actions") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = AtelierTheme.colors.surfaceContainerLow
                ) {
                    Column {
                        ExportActionRow(
                            label = stringResource(R.string.save_palette),
                            icon = Icons.Rounded.Bookmark,
                            onClick = onSavePalette
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 54.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                        ExportActionRow(
                            label = stringResource(R.string.share_as_image),
                            icon = Icons.Rounded.Share,
                            onClick = onShareImage
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 54.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        )
                        ExportActionRow(
                            label = stringResource(R.string.copy_all_hex),
                            icon = Icons.Rounded.ContentCopy,
                            onClick = onCopyAllHex
                        )
                    }
                }
            }

            item(key = "formats") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.copy_for),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = AtelierTheme.colors.surfaceContainerLow
                    ) {
                        Column {
                            formats.forEachIndexed { index, format ->
                                key(format.format) {
                                    ExportFormatRow(
                                        label = format.label,
                                        format = format.format,
                                        onClick = format.onClick
                                    )
                                    if (index < formats.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 16.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(key = "bottom_space") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

private data class ExportFormat(
    val label: String,
    val format: String,
    val onClick: () -> Unit
)

@Composable
private fun FluidSheetScaffold(
    onDismiss: () -> Unit,
    dismissLabel: String,
    sheetTitle: String,
    compactHeightFraction: Float,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    val sizeClass = LocalWindowSizeClass.current
    val compactHeight = LocalConfiguration.current.screenHeightDp < 480
    val compact = sizeClass == CampaletteWindowSizeClass.Compact || compactHeight
    val reducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val onDismissState = rememberUpdatedState(onDismiss)
    val offsetY = remember { Animatable(0f) }
    val scrim = remember { Animatable(0f) }
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    var hasPresented by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var hostVisible by remember { mutableStateOf(true) }

    val finishDismiss: () -> Unit = {
        hostVisible = false
        onDismissState.value()
    }

    val requestDismiss: () -> Unit = {
        scope.launch {
            if (reducedMotion) {
                offsetY.snapTo(if (compact) sheetHeightPx else 0f)
                scrim.animateTo(0f, tween(90))
            } else {
                coroutineScope {
                    launch { scrim.animateTo(0f, tween(180)) }
                    if (compact && sheetHeightPx > 0f) {
                        offsetY.animateTo(
                            targetValue = sheetHeightPx,
                            animationSpec = spring(dampingRatio = 1f, stiffness = 600f)
                        )
                    }
                }
            }
            finishDismiss()
        }
    }

    val dragState = rememberDraggableState { delta ->
        val proposed = dragOffset + delta
        dragOffset = if (proposed < 0f) {
            -sheetRubberBand(-proposed, sheetHeightPx)
        } else {
            proposed
        }
    }

    LaunchedEffect(hasPresented) {
        if (!hasPresented) return@LaunchedEffect
        if (reducedMotion) {
            offsetY.snapTo(0f)
            scrim.animateTo(1f, tween(90))
        } else if (compact) {
            offsetY.snapTo(sheetHeightPx)
            coroutineScope {
                launch { scrim.animateTo(1f, tween(180)) }
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 1f, stiffness = 600f)
                )
            }
        } else {
            offsetY.snapTo(0f)
            scrim.animateTo(1f, tween(180))
        }
    }

    if (!hostVisible) return

    Dialog(
        onDismissRequest = requestDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = if (isDragging && sheetHeightPx > 0f) {
                            (1f - dragOffset / sheetHeightPx).coerceIn(0f, 1f)
                        } else {
                            scrim.value
                        }
                    }
                    .background(Color.Black.copy(alpha = 0.38f))
                    .semantics {
                        contentDescription = dismissLabel
                        role = Role.Button
                    }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = dismissLabel,
                        onClick = requestDismiss
                    )
            )
            val safeSides = if (compact) {
                WindowInsetsSides.Horizontal + WindowInsetsSides.Top
            } else {
                WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(safeSides))
            ) {
                Surface(
                    modifier = Modifier
                        .then(
                            if (compact) {
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .fillMaxHeight(compactHeightFraction)
                            } else {
                                Modifier
                                    .align(Alignment.Center)
                                    .widthIn(max = 560.dp)
                                    .wrapContentHeight()
                            }
                        )
                        .onSizeChanged { size ->
                            if (size.height > 0) {
                                sheetHeightPx = size.height.toFloat()
                                if (!hasPresented) hasPresented = true
                            }
                        }
                        .offset {
                            val y = if (isDragging) dragOffset else offsetY.value
                            IntOffset(0, y.roundToInt())
                        }
                        .graphicsLayer {
                            alpha = when {
                                !hasPresented && !reducedMotion -> 0f
                                reducedMotion -> if (isDragging && sheetHeightPx > 0f) {
                                    (1f - dragOffset / sheetHeightPx).coerceIn(0f, 1f)
                                } else {
                                    scrim.value
                                }
                                else -> 1f
                            }
                        }
                        .draggable(
                            state = dragState,
                            orientation = Orientation.Vertical,
                            enabled = compact,
                            startDragImmediately = false,
                            onDragStarted = {
                                offsetY.stop()
                                scrim.stop()
                                dragOffset = offsetY.value
                                isDragging = true
                            },
                            onDragStopped = { velocity ->
                                offsetY.snapTo(dragOffset)
                                isDragging = false
                                val releaseVelocity = if (reducedMotion) 0f else velocity
                                val threshold = maxOf(
                                    sheetHeightPx * 0.30f,
                                    with(density) { 72.dp.toPx() }
                                )
                                val projected = projectSheetOffset(dragOffset, releaseVelocity)
                                val shouldDismiss = if (abs(releaseVelocity) > 700f) {
                                    releaseVelocity > 0f
                                } else {
                                    projected > threshold
                                }
                                scope.launch {
                                    if (reducedMotion) {
                                        if (shouldDismiss) {
                                            offsetY.snapTo(sheetHeightPx)
                                            scrim.snapTo(0f)
                                            finishDismiss()
                                        } else {
                                            offsetY.snapTo(0f)
                                            scrim.snapTo(1f)
                                        }
                                    } else {
                                        coroutineScope {
                                            launch {
                                                scrim.animateTo(
                                                    targetValue = if (shouldDismiss) 0f else 1f,
                                                    animationSpec = tween(180)
                                                )
                                            }
                                            offsetY.animateTo(
                                                targetValue = if (shouldDismiss) sheetHeightPx else 0f,
                                                animationSpec = spring(
                                                    dampingRatio = if (abs(releaseVelocity) > 700f) 0.82f else 1f,
                                                    stiffness = 520f
                                                ),
                                                initialVelocity = releaseVelocity
                                            )
                                        }
                                        if (shouldDismiss) finishDismiss()
                                    }
                                }
                            }
                        )
                        .pointerInput(Unit) { detectTapGestures(onTap = {}) }
                        .semantics {
                            paneTitle = sheetTitle
                            isTraversalGroup = true
                        },
                    shape = if (compact) {
                        RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    } else {
                        RoundedCornerShape(28.dp)
                    },
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = if (compact) Modifier.fillMaxSize() else Modifier.wrapContentHeight()
                    ) {
                        content(requestDismiss)
                    }
                }
            }
        }
    }
}

private fun projectSheetOffset(current: Float, velocity: Float): Float {
    val decelerationRate = 0.99f
    return current + (velocity / 1000f) * decelerationRate / (1f - decelerationRate)
}

private fun sheetRubberBand(overshoot: Float, dimension: Float, constant: Float = 0.55f): Float {
    if (dimension <= 0f) return 0f
    return (overshoot * dimension * constant) / (dimension + constant * abs(overshoot))
}

@Composable
private fun ExportSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 36.dp, height = 5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f))
        )
    }
}

@Composable
private fun ExportSheetHeader(
    title: String,
    subtitle: String,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onDone),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.editor_color_picker_done),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PalettePreviewStrip(palette: List<PaletteColor>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AtelierTheme.colors.surfaceContainerLow)
    ) {
        if (palette.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.current_palette),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            palette.forEach { color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(color.color))
                )
            }
        }
    }
}

@Composable
private fun ExportActionRow(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun ExportFormatRow(
    label: String,
    format: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = format,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        )
    }
}
