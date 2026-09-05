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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.components.CampaletteWindowSizeClass
import com.vishnu.campalette.ui.components.LocalWindowSizeClass
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ColorBlindnessPreviewScreen(
    modifier: Modifier = Modifier,
    paletteColor: PaletteColor,
    initialType: AtelierData.ColorBlindnessType,
    onTypeChanged: (AtelierData.ColorBlindnessType) -> Unit,
    onClose: () -> Unit
) {
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    val protanopiaLabel = stringResource(R.string.colorblind_protanopia)
    val deuteranopiaLabel = stringResource(R.string.colorblind_deuteranopia)
    val tritanopiaLabel = stringResource(R.string.colorblind_tritanopia)
    val types = remember(protanopiaLabel, deuteranopiaLabel, tritanopiaLabel) {
        listOf(
            AtelierData.ColorBlindnessType.Protanopia to protanopiaLabel,
            AtelierData.ColorBlindnessType.Deuteranopia to deuteranopiaLabel,
            AtelierData.ColorBlindnessType.Tritanopia to tritanopiaLabel
        )
    }
    val simulatedColor = remember(selectedType, paletteColor.color) {
        AtelierData.applyColorBlindness(paletteColor.color, selectedType)
    }
    val selectedLabel = remember(selectedType, types) {
        types.first { it.first == selectedType }.second
    }
    val simulatedHex = remember(simulatedColor) {
        String.format(Locale.US, "#%06X", 0xFFFFFF and simulatedColor)
    }
    val originalColor = remember(paletteColor.color) { Color(paletteColor.color) }
    val previewColor = remember(simulatedColor) { Color(simulatedColor) }

    FluidColorVisionSheet(
        modifier = modifier,
        onDismiss = onClose,
        dismissLabel = stringResource(R.string.close_color_vision),
        sheetTitle = stringResource(R.string.colorblind_title),
        compactHeightFraction = 0.72f
    ) { dismiss ->
        SheetDragHandle()
        SheetHeader(
            title = stringResource(R.string.colorblind_title),
            subtitle = paletteColor.name,
            onDone = dismiss
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            VisionTypeSelector(
                types = types,
                selectedType = selectedType,
                onSelect = { type ->
                    selectedType = type
                    onTypeChanged(type)
                }
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = AtelierTheme.colors.surfaceContainerLow
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ColorComparison(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.colorblind_original),
                        hex = paletteColor.hexCode,
                        color = originalColor
                    )
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(184.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    )
                    ColorComparison(
                        modifier = Modifier.weight(1f),
                        label = selectedLabel,
                        hex = simulatedHex,
                        color = previewColor
                    )
                }
            }

            Text(
                text = stringResource(R.string.colorblind_warning),
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FluidColorVisionSheet(
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
            -colorVisionSheetRubberBand(-proposed, sheetHeightPx)
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
                                val projected = projectColorVisionSheetOffset(dragOffset, releaseVelocity)
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

private fun projectColorVisionSheetOffset(current: Float, velocity: Float): Float {
    val decelerationRate = 0.99f
    return current + (velocity / 1000f) * decelerationRate / (1f - decelerationRate)
}

private fun colorVisionSheetRubberBand(
    overshoot: Float,
    dimension: Float,
    constant: Float = 0.55f
): Float {
    if (dimension <= 0f) return 0f
    return (overshoot * dimension * constant) / (dimension + constant * abs(overshoot))
}

@Composable
private fun SheetDragHandle() {
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
private fun SheetHeader(
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
private fun VisionTypeSelector(
    types: List<Pair<AtelierData.ColorBlindnessType, String>>,
    selectedType: AtelierData.ColorBlindnessType,
    onSelect: (AtelierData.ColorBlindnessType) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = AtelierTheme.colors.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            types.forEach { (type, label) ->
                val selected = type == selectedType
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onSelect(type) }
                        ),
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) {
                        AtelierTheme.colors.surfaceContainerLow
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (selected) 1.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorComparison(
    label: String,
    hex: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val comparisonDescription = remember(label, hex) { "$label, $hex" }
    Column(
        modifier = modifier
            .semantics { contentDescription = comparisonDescription }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = hex,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
