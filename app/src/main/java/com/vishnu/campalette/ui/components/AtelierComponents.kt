package com.vishnu.campalette.ui.components

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick as semanticsOnClick
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.AtelierDarkPrimary
import com.vishnu.campalette.ui.theme.AtelierPrimary
import com.vishnu.campalette.ui.theme.ExpressiveEffectsColorSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsSpring
import com.vishnu.campalette.ui.theme.ExpressiveSpatialSpring
import com.vishnu.campalette.ui.theme.LocalDynamicThemeColors
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * App Screens
 */
enum class AppScreen {
    Live, History, Editor, Settings
}

@Composable
fun rememberPressScale(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true
): State<Float> {
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    return animateFloatAsState(
        targetValue = if (!reducedMotion && isPressed && enabled) 0.97f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Press scale"
    )
}

/**
 * Shared Utilities
 */

fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit,
    enabled: Boolean = true,
    dismissThreshold: Float = 0.4f,
    onRevealChange: (Boolean) -> Unit = {}
): Modifier = composed {
    if (!enabled) return@composed this

    val reducedMotion = LocalReducedMotion.current
    var componentWidth by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val settleJob = remember { object { var job: Job? = null } }

    this
        .onGloballyPositioned { componentWidth = it.size.width.toFloat() }
        .pointerInput(reducedMotion) {
            val velocityTracker = VelocityTracker()
            detectHorizontalDragGestures(
                onDragStart = {
                    settleJob.job?.cancel()
                    velocityTracker.resetTracking()
                },
                onDragEnd = {
                    val width = componentWidth.takeIf { it > 0f } ?: size.width.toFloat().coerceAtLeast(1f)
                    val current = offsetX.floatValue
                    val velocityX = velocityTracker.calculateVelocity().x
                    val threshold = width * dismissThreshold
                    val projected = if (reducedMotion) {
                        current
                    } else {
                        projectNavigationOffset(current, velocityX, 0.998f)
                    }
                    val shouldDismiss = abs(projected) > threshold
                    val target = when {
                        !shouldDismiss -> 0f
                        projected > 0f -> width
                        else -> -width
                    }
                    settleJob.job = scope.launch {
                        if (reducedMotion) {
                            offsetX.floatValue = target
                        } else {
                            animate(
                                initialValue = current,
                                targetValue = target,
                                initialVelocity = velocityX,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                            ) { value, _ ->
                                offsetX.floatValue = value
                            }
                        }
                        onRevealChange(shouldDismiss)
                        if (shouldDismiss) onDismiss()
                    }
                },
                onDragCancel = {
                    settleJob.job = scope.launch {
                        val current = offsetX.floatValue
                        if (reducedMotion) {
                            offsetX.floatValue = 0f
                        } else {
                            animate(
                                initialValue = current,
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                            ) { value, _ ->
                                offsetX.floatValue = value
                            }
                        }
                        onRevealChange(false)
                    }
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    offsetX.floatValue += dragAmount
                    onRevealChange(abs(offsetX.floatValue) > 0.5f)
                }
            )
        }
        .absoluteOffset { IntOffset(offsetX.floatValue.roundToInt(), 0) }
        .graphicsLayer {
            val width = componentWidth.takeIf { it > 0f } ?: 1f
            alpha = 1f - (abs(offsetX.floatValue) / width).coerceIn(0f, 1f)
        }
}

enum class CampaletteWindowSizeClass {
    Compact,
    Medium,
    Expanded;

    val isWide: Boolean get() = this != Compact
    val isExpanded: Boolean get() = this == Expanded
}

val LocalWindowSizeClass = staticCompositionLocalOf { CampaletteWindowSizeClass.Compact }

fun windowSizeClassFor(width: Dp): CampaletteWindowSizeClass = when {
    width < 600.dp -> CampaletteWindowSizeClass.Compact
    width < 840.dp -> CampaletteWindowSizeClass.Medium
    else -> CampaletteWindowSizeClass.Expanded
}

@Composable
fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable (isTablet: Boolean) -> Unit
) {
    Box(modifier = modifier) {
        content(LocalWindowSizeClass.current.isWide)
    }
}

@Composable
fun AdaptiveSheetScaffold(
    onDismiss: () -> Unit,
    dismissLabel: String,
    sheetTitle: String,
    compactHeightFraction: Float,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val sizeClass = LocalWindowSizeClass.current
    val compactHeight = LocalConfiguration.current.screenHeightDp < 480
    val compact = sizeClass == CampaletteWindowSizeClass.Compact || compactHeight
    val safeSides = if (compact) {
        WindowInsetsSides.Horizontal + WindowInsetsSides.Top
    } else {
        WindowInsetsSides.Horizontal + WindowInsetsSides.Vertical
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
                .background(Color.Black.copy(alpha = 0.38f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        contentDescription = dismissLabel
                        role = Role.Button
                    }
                    .clickable(
                        role = Role.Button,
                        onClickLabel = dismissLabel,
                        onClick = onDismiss
                    )
            )
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
                                    .swipeDownToDismiss(onDismiss = onDismiss)
                            } else {
                                Modifier
                                    .align(Alignment.Center)
                                    .widthIn(max = 560.dp)
                                    .wrapContentHeight()
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
                        modifier = if (compact) Modifier.fillMaxSize() else Modifier.wrapContentHeight(),
                        content = content
                    )
                }
            }
        }
    }
}

object CampaletteDock {
    val IslandHeight = 60.dp
    val IslandVerticalPadding = 8.dp
    val ContentGap = 36.dp
    val ShutterSize = 76.dp
    val ShutterGap = 12.dp
}

@Composable
fun dockClearance(extraAboveIsland: Dp = CampaletteDock.ContentGap): Dp {
    val navigationInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return navigationInset +
        CampaletteDock.IslandVerticalPadding +
        CampaletteDock.IslandHeight +
        CampaletteDock.IslandVerticalPadding +
        extraAboveIsland
}

@Composable
fun shutterClearance(): Dp =
    dockClearance() + CampaletteDock.ShutterSize + CampaletteDock.ShutterGap

fun Modifier.swipeDownToDismiss(
    onDismiss: () -> Unit,
    enabled: Boolean = true,
    minimumDismissDistance: Dp = 72.dp
): Modifier = composed {
    if (!enabled) return@composed this
    val density = LocalDensity.current
    val reducedMotion = LocalReducedMotion.current
    val offsetY = remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val settleJob = remember { object { var job: Job? = null } }
    this
        .pointerInput(reducedMotion, minimumDismissDistance) {
            val velocityTracker = VelocityTracker()
            val minDismissPx = with(density) { minimumDismissDistance.toPx() }
            detectVerticalDragGestures(
                onDragStart = {
                    settleJob.job?.cancel()
                    velocityTracker.resetTracking()
                },
                onDragEnd = {
                    val current = offsetY.floatValue
                    val velocityY = velocityTracker.calculateVelocity().y
                    val dismissThreshold = maxOf(minDismissPx, size.height * 0.30f)
                    val projected = if (reducedMotion) {
                        current
                    } else {
                        projectNavigationOffset(current, velocityY, 0.998f)
                    }
                    val fling = !reducedMotion && abs(velocityY) > 800f
                    val shouldDismiss = if (fling) velocityY > 0f else projected > dismissThreshold
                    val target = if (shouldDismiss) {
                        size.height.toFloat().coerceAtLeast(dismissThreshold * 2f)
                    } else {
                        0f
                    }
                    settleJob.job = scope.launch {
                        if (reducedMotion) {
                            offsetY.floatValue = target
                        } else {
                            animate(
                                initialValue = current,
                                targetValue = target,
                                initialVelocity = velocityY,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                            ) { value, _ ->
                                offsetY.floatValue = value
                            }
                        }
                        if (shouldDismiss) onDismiss()
                    }
                },
                onDragCancel = {
                    settleJob.job = scope.launch {
                        val current = offsetY.floatValue
                        if (reducedMotion) {
                            offsetY.floatValue = 0f
                        } else {
                            animate(
                                initialValue = current,
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                            ) { value, _ ->
                                offsetY.floatValue = value
                            }
                        }
                    }
                },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    offsetY.floatValue = (offsetY.floatValue + dragAmount).coerceAtLeast(0f)
                }
            )
        }
        .offset { IntOffset(0, offsetY.floatValue.roundToInt()) }
}

/**
 * Typography & Labels
 */

@Composable
fun AtelierTag(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}

@Composable
fun ValueLabel(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Cards & Containers
 */

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AtelierTheme.colors.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content
        )
    }
}

/**
 * Buttons & Inputs
 */

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = rememberPressScale(interactionSource, enabled)
    val reducedMotion = LocalReducedMotion.current
    val pressAlpha = if (!enabled) 0.5f else if (!reducedMotion && isPressed) 0.88f else 1f

    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
                alpha = pressAlpha
            }
            .clip(RoundedCornerShape(14.dp))
            .background(dynamicPrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
                role = Role.Button
            )
            .heightIn(min = 50.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = rememberPressScale(interactionSource)
    val reducedMotion = LocalReducedMotion.current
    val pressAlpha = if (!reducedMotion && isPressed) 0.88f else 1f

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
                alpha = pressAlpha
            }
            .clip(RoundedCornerShape(14.dp))
            .background(AtelierTheme.colors.surfaceContainer.copy(alpha = 0.86f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                role = Role.Button
            )
            .heightIn(min = 50.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AtelierTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label }
                .background(
                    AtelierTheme.colors.surfaceContainer.copy(alpha = 0.82f),
                    RoundedCornerShape(14.dp)
                )
                .heightIn(min = 52.dp)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun AppleSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AtelierTheme.colors.surfaceContainerHigh.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = placeholder },
            decorationBox = { inner ->
                if (value.isBlank()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                    )
                }
                inner()
            }
        )
        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { onValueChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
fun SegmentedControl(
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    itemRole: Role = Role.Tab
) {
    val reducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val lastIndex = (options.size - 1).coerceAtLeast(0)
    val selectedIndex = options.indexOfFirst { it.first == selectedKey }.coerceIn(0, lastIndex)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AtelierTheme.colors.surfaceContainerHighest.copy(alpha = 0.52f))
            .padding(2.dp)
            .selectableGroup()
    ) {
        val itemWidthPx = with(density) { maxWidth.toPx() } / options.size.coerceAtLeast(1)
        val pillOffset = remember { Animatable(selectedIndex * itemWidthPx) }
        var dragOffsetPx by remember { mutableFloatStateOf(selectedIndex * itemWidthPx) }
        var isDragging by remember { mutableStateOf(false) }
        var releaseVelocityPx by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(selectedIndex, itemWidthPx, isDragging, reducedMotion) {
            if (itemWidthPx <= 0f || isDragging) return@LaunchedEffect
            val target = selectedIndex * itemWidthPx
            if (reducedMotion) {
                pillOffset.snapTo(target)
            } else {
                val momentum = abs(releaseVelocityPx) > itemWidthPx * 2f
                pillOffset.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = if (momentum) 0.82f else 1f,
                        stiffness = 400f
                    ),
                    initialVelocity = releaseVelocityPx
                )
            }
            dragOffsetPx = target
            releaseVelocityPx = 0f
        }

        val visualOffsetPx = if (isDragging) dragOffsetPx else pillOffset.value
        Box(
            modifier = Modifier
                .offset { IntOffset(visualOffsetPx.roundToInt(), 0) }
                .width(with(density) { itemWidthPx.toDp() })
                .fillMaxHeight()
                .shadow(1.dp, RoundedCornerShape(8.dp), clip = false)
                .background(AtelierTheme.colors.surfaceContainerLowest, RoundedCornerShape(8.dp))
        )

        val dragState = rememberDraggableState { delta ->
            val maximum = lastIndex * itemWidthPx
            dragOffsetPx = resistedNavigationOffset(
                proposed = dragOffsetPx + delta,
                minimum = 0f,
                maximum = maximum,
                resistanceDimension = itemWidthPx
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = options.size > 1,
                    onDragStarted = {
                        pillOffset.stop()
                        dragOffsetPx = pillOffset.value
                        isDragging = true
                    },
                    onDragStopped = { velocity ->
                        val projection = if (reducedMotion) 0f else velocity
                        val projected = projectNavigationOffset(dragOffsetPx, projection)
                        val index = if (itemWidthPx <= 0f) {
                            selectedIndex
                        } else {
                            (projected / itemWidthPx).roundToInt().coerceIn(0, lastIndex)
                        }
                        releaseVelocityPx = projection
                        isDragging = false
                        val key = options[index].first
                        if (key != selectedKey) onSelected(key)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { (key, label) ->
                val selected = key == selectedKey
                val labelColor by animateColorAsState(
                    targetValue = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = if (reducedMotion) snap() else ExpressiveEffectsColorSpring,
                    label = "Segment label"
                )
                val interactionSource = remember { MutableInteractionSource() }
                val pressScale = rememberPressScale(interactionSource)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = pressScale.value
                            scaleY = pressScale.value
                        }
                        .clearAndSetSemantics {
                            contentDescription = label
                            role = itemRole
                            this.selected = selected
                            semanticsOnClick {
                                onSelected(key)
                                true
                            }
                        }
                        .selectable(
                            selected = selected,
                            onClick = { onSelected(key) },
                            role = itemRole,
                            interactionSource = interactionSource,
                            indication = null
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = labelColor,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AtelierToggle(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val minThumbPx = with(density) { 2.dp.toPx() }
    val maxThumbPx = with(density) { 22.dp.toPx() }
    val travelPx = (maxThumbPx - minThumbPx).coerceAtLeast(1f)
    val latestChecked = rememberUpdatedState(checked)
    val latestOnCheckedChange = rememberUpdatedState(onCheckedChange)
    val thumbPx = remember { Animatable(if (checked) maxThumbPx else minThumbPx) }
    var dragThumbPx by remember { mutableFloatStateOf(if (checked) maxThumbPx else minThumbPx) }
    var dragging by remember { mutableStateOf(false) }
    var held by remember { mutableStateOf(false) }
    val toggleStateDescription = stringResource(
        if (checked) R.string.toggle_state_on else R.string.toggle_state_off
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else AtelierTheme.colors.surfaceContainerHighest,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsColorSpring,
        label = "Track"
    )
    val stretch by animateFloatAsState(
        targetValue = if (!reducedMotion && (held || dragging)) 1.06f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Thumb stretch"
    )

    LaunchedEffect(checked, minThumbPx, maxThumbPx, reducedMotion, dragging) {
        if (dragging) return@LaunchedEffect
        val target = if (checked) maxThumbPx else minThumbPx
        if (abs(thumbPx.value - dragThumbPx) > 0.5f) {
            thumbPx.snapTo(dragThumbPx)
        }
        if (reducedMotion) {
            thumbPx.snapTo(target)
        } else {
            thumbPx.animateTo(target, ExpressiveEffectsSpring)
        }
        dragThumbPx = target
    }

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 48.dp)
            .clearAndSetSemantics {
                contentDescription = label
                role = Role.Switch
                stateDescription = toggleStateDescription
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                semanticsOnClick {
                    onCheckedChange(!checked)
                    true
                }
            }
            .pointerInput(minThumbPx, maxThumbPx, reducedMotion) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    held = true
                    val tracker = VelocityTracker()
                    tracker.addPosition(down.uptimeMillis, down.position)
                    var dragged = false
                    var current = thumbPx.value
                    val slop = viewConfiguration.touchSlop
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) {
                            if (dragged) change.consume()
                            break
                        }
                        tracker.addPosition(change.uptimeMillis, change.position)
                        val dx = change.position.x - change.previousPosition.x
                        if (!dragged && abs(change.position.x - down.position.x) > slop) {
                            dragged = true
                            dragging = true
                        }
                        if (dragged) {
                            change.consume()
                            current = resistedNavigationOffset(
                                proposed = current + dx,
                                minimum = minThumbPx,
                                maximum = maxThumbPx,
                                resistanceDimension = travelPx
                            )
                            dragThumbPx = current
                        }
                    }
                    held = false
                    if (!dragged) {
                        latestOnCheckedChange.value(!latestChecked.value)
                    } else {
                        val velocity = tracker.calculateVelocity().x
                        val turnOn = if (abs(velocity) > 250f) velocity > 0f else current > (minThumbPx + maxThumbPx) / 2f
                        dragging = false
                        if (turnOn != latestChecked.value) {
                            latestOnCheckedChange.value(turnOn)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 51.dp, height = 31.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (if (dragging) dragThumbPx else thumbPx.value).roundToInt(),
                            y = with(density) { 2.dp.roundToPx() }
                        )
                    }
                    .size(27.dp)
                    .graphicsLayer {
                        scaleX = stretch
                        scaleY = 1f
                    }
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Color Swatches & Palettes
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    label: String? = null,
    onLongClick: (() -> Unit)? = null,
    entryDelay: Int = 0
) {
    val reducedMotion = LocalReducedMotion.current

    val scale by animateFloatAsState(
        targetValue = if (selected && !reducedMotion) 1.04f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "SwatchScale"
    )

    val ringWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        animationSpec = if (reducedMotion) snap() else spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "Ring"
    )

    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift
    val swatchDescription = label?.let { stringResource(R.string.color_named, it) }
        ?: stringResource(R.string.color_swatch)
    val swatchState = stringResource(
        if (selected) R.string.swatch_state_selected else R.string.swatch_state_not_selected
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(size + 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .drawBehind {
                    if (ringWidth > 0.dp) {
                        drawCircle(
                            color = dynamicPrimary,
                            radius = (size.toPx() / 2f) + 6.dp.toPx(),
                            style = Stroke(width = ringWidth.toPx())
                        )
                    }
                }
                .clip(RoundedCornerShape(14.dp))
                .background(color)
                .semantics {
                    role = Role.Button
                    contentDescription = swatchDescription
                    stateDescription = swatchState
                }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        )
        if (label != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PaletteListCard(
    title: String,
    swatches: List<PaletteColor>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isFavorite: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
) {
    val swatchDescription = stringResource(
        R.string.accessibility_palette_colors,
        swatches.take(6).joinToString(separator = ", ") { it.hexCode }
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = shape,
        color = AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = stringResource(R.string.favorite),
                        tint = LocalDynamicThemeColors.current.primaryShift,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .semantics { contentDescription = swatchDescription }
            ) {
                swatches.take(6).forEach { pc ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color(pc.color))
                    )
                }
            }
        }
    }
}

/**
 * Navigation & Progress
 */

@Composable
fun BottomBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
    useCameraGlass: Boolean = currentScreen == AppScreen.Live
) {
    val destinations = remember {
        listOf(AppScreen.History, AppScreen.Live, AppScreen.Settings)
    }
    val selectedIndex = destinations.indexOf(currentScreen).takeIf { it >= 0 } ?: 1
    val reducedMotion = LocalReducedMotion.current
    val layoutDirection = LocalLayoutDirection.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val useDarkGlass = isDark || useCameraGlass
    val glassBase = if (useDarkGlass) Color.Black else Color.White
    val dockBorderColor = if (useDarkGlass) {
        Color(0xFFE1E7F0).copy(alpha = 0.28f)
    } else {
        Color.White.copy(alpha = 0.60f)
    }
    val glassFill = glassBase.copy(alpha = if (useDarkGlass) 0.58f else 0.72f)
    val glassStyle = remember(useDarkGlass, glassBase) {
        HazeStyle(
            blurRadius = 32.dp,
            backgroundColor = glassBase.copy(alpha = if (useDarkGlass) 0.52f else 0.64f),
            tints = listOf(HazeTint(glassBase.copy(alpha = if (useDarkGlass) 0.38f else 0.48f)))
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val dockShape = RoundedCornerShape(30.dp)
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 252.dp)
                .fillMaxWidth()
                .height(60.dp)
                .shadow(if (useDarkGlass) 4.dp else 4.dp, dockShape, clip = false)
                .clip(dockShape)
                .background(glassFill)
                .hazeEffect(
                    state = hazeState,
                    style = glassStyle
                ) {
                    noiseFactor = 0.04f
                }
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = if (useDarkGlass) 0.34f else 0.68f),
                                Color.White.copy(alpha = if (useDarkGlass) 0.08f else 0.10f)
                            )
                        ),
                        cornerRadius = CornerRadius(30.dp.toPx()),
                        style = Stroke(width = 0.75.dp.toPx())
                    )
                }
                .border(
                    width = if (useDarkGlass) 1.dp else 0.75.dp,
                    color = dockBorderColor,
                    shape = dockShape
                )
                .testTag("bottom_navigation_island"),
            contentAlignment = Alignment.CenterStart
        ) {
            val density = LocalDensity.current
            val horizontalInset = 4.dp
            val indicatorInset = 2.dp
            val itemWidth = (maxWidth - horizontalInset * 2) / destinations.size
            val itemWidthPx = with(density) { itemWidth.toPx() }
            val minimumAnchorPx = with(density) { (horizontalInset + indicatorInset).toPx() }
            val indicatorWidth = itemWidth - indicatorInset * 2
            fun anchorFor(logicalIndex: Int): Float =
                minimumAnchorPx + logicalIndex * itemWidthPx

            val lastLogicalIndex = destinations.lastIndex
            val maximumAnchorPx = minimumAnchorPx + lastLogicalIndex * itemWidthPx
            val selectedAnchorPx = anchorFor(selectedIndex)
            val indicatorOffset = remember { Animatable(selectedAnchorPx) }
            var dragOffsetPx by remember { mutableFloatStateOf(selectedAnchorPx) }
            var isDragging by remember { mutableStateOf(false) }
            var releaseVelocityPx by remember { mutableFloatStateOf(0f) }
            var settleRequest by remember { mutableIntStateOf(0) }

            LaunchedEffect(
                selectedIndex,
                itemWidthPx,
                layoutDirection,
                isDragging,
                settleRequest,
                reducedMotion
            ) {
                if (!isDragging) {
                    val target = anchorFor(selectedIndex)
                    if (reducedMotion) {
                        indicatorOffset.snapTo(target)
                    } else {
                        val carriesMomentum = abs(releaseVelocityPx) > itemWidthPx * 2f
                        indicatorOffset.animateTo(
                            targetValue = target,
                            animationSpec = spring(
                                dampingRatio = if (carriesMomentum) 0.82f else Spring.DampingRatioNoBouncy,
                                stiffness = 520f
                            ),
                            initialVelocity = releaseVelocityPx
                        )
                    }
                    dragOffsetPx = target
                    releaseVelocityPx = 0f
                }
            }

            val dragState = rememberDraggableState { delta ->
                val startRelativeDelta = if (layoutDirection == LayoutDirection.Rtl) -delta else delta
                val proposed = dragOffsetPx + startRelativeDelta
                dragOffsetPx = resistedNavigationOffset(
                    proposed = proposed,
                    minimum = minimumAnchorPx,
                    maximum = maximumAnchorPx,
                    resistanceDimension = itemWidthPx
                )
            }
            val visuallyActiveIndex = if (isDragging && itemWidthPx > 0f) {
                ((dragOffsetPx - minimumAnchorPx) / itemWidthPx)
                    .roundToInt()
                    .coerceIn(0, lastLogicalIndex)
            } else {
                selectedIndex
            }
            val navigationAccent = if (useDarkGlass) AtelierDarkPrimary else AtelierPrimary
            val indicatorColor = if (useDarkGlass) {
                Color.White.copy(alpha = 0.16f)
            } else {
                navigationAccent.copy(alpha = 0.13f)
            }
            val indicatorBorder = if (useDarkGlass) {
                Color.White.copy(alpha = 0.14f)
            } else {
                navigationAccent.copy(alpha = 0.10f)
            }
            val inactiveColor = if (useDarkGlass) {
                Color.White.copy(alpha = 0.78f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f)
            }

            Box(
                modifier = Modifier
                    .offset {
                        val x = if (isDragging) dragOffsetPx else indicatorOffset.value
                        IntOffset(x.roundToInt(), 0)
                    }
                    .width(indicatorWidth)
                    .height(48.dp)
                    .graphicsLayer {
                        val dragScale = if (isDragging && !reducedMotion) 1.035f else 1f
                        scaleX = dragScale
                        scaleY = dragScale
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(indicatorColor)
                    .border(0.75.dp, indicatorBorder, RoundedCornerShape(24.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalInset)
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                        onDragStarted = {
                            indicatorOffset.stop()
                            dragOffsetPx = indicatorOffset.value
                            isDragging = true
                        },
                        onDragStopped = { velocity ->
                            val startRelativeVelocity = if (layoutDirection == LayoutDirection.Rtl) -velocity else velocity
                            val projectionVelocity = if (reducedMotion) 0f else startRelativeVelocity
                            val projected = projectNavigationOffset(dragOffsetPx, projectionVelocity)
                            val targetLogicalIndex = ((projected - minimumAnchorPx) / itemWidthPx)
                                .roundToInt()
                                .coerceIn(0, lastLogicalIndex)
                            releaseVelocityPx = projectionVelocity
                            indicatorOffset.snapTo(dragOffsetPx)
                            settleRequest++
                            isDragging = false
                            val destination = destinations[targetLogicalIndex]
                            if (destination != currentScreen) onScreenSelected(destination)
                        }
                    )
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    icon = { Icon(Icons.Rounded.PhotoLibrary, null) },
                    label = stringResource(R.string.bottom_nav_library),
                    selected = currentScreen == AppScreen.History,
                    visuallyActive = visuallyActiveIndex == 0,
                    activeColor = navigationAccent,
                    inactiveColor = inactiveColor,
                    onClick = { onScreenSelected(AppScreen.History) },
                    modifier = Modifier.weight(1f)
                )

                BottomBarItem(
                    icon = { Icon(Icons.Rounded.CameraAlt, null) },
                    label = stringResource(R.string.bottom_nav_camera),
                    selected = currentScreen == AppScreen.Live,
                    visuallyActive = visuallyActiveIndex == 1,
                    activeColor = navigationAccent,
                    inactiveColor = inactiveColor,
                    onClick = { onScreenSelected(AppScreen.Live) },
                    modifier = Modifier.weight(1f)
                )

                BottomBarItem(
                    icon = { Icon(Icons.Rounded.Settings, null) },
                    label = stringResource(R.string.bottom_nav_settings),
                    selected = currentScreen == AppScreen.Settings,
                    visuallyActive = visuallyActiveIndex == 2,
                    activeColor = navigationAccent,
                    inactiveColor = inactiveColor,
                    onClick = { onScreenSelected(AppScreen.Settings) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun projectNavigationOffset(
    currentOffset: Float,
    velocity: Float,
    decelerationRate: Float = 0.99f
): Float = currentOffset + (velocity / 1000f) * decelerationRate / (1f - decelerationRate)

private fun resistedNavigationOffset(
    proposed: Float,
    minimum: Float,
    maximum: Float,
    resistanceDimension: Float
): Float = when {
    proposed < minimum -> minimum - rubberBandDistance(minimum - proposed, resistanceDimension)
    proposed > maximum -> maximum + rubberBandDistance(proposed - maximum, resistanceDimension)
    else -> proposed
}

private fun rubberBandDistance(
    overshoot: Float,
    dimension: Float,
    constant: Float = 0.55f
): Float = if (dimension <= 0f) {
    0f
} else {
    (overshoot * dimension * constant) / (dimension + constant * abs(overshoot))
}

@Composable
private fun BottomBarItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    visuallyActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalReducedMotion.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color by animateColorAsState(
        targetValue = if (visuallyActive) activeColor else inactiveColor,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsColorSpring,
        label = "Color"
    )
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && !reducedMotion) 0.97f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Bottom navigation press"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxHeight()
            .clearAndSetSemantics {
                contentDescription = label
                role = Role.Tab
                this.selected = selected
                semanticsOnClick {
                    onClick()
                    true
                }
            }
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 26.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides color
            ) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
fun CaptureProgress(modifier: Modifier = Modifier) {
    val reducedMotion = LocalReducedMotion.current
    val findingColors = stringResource(R.string.finding_colors)
    val rotation = if (reducedMotion) {
        0f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "Capture spinner")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(750, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Spinner rotation"
        ).value
    }

    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(12.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.52f))
            .border(0.5.dp, Color.White.copy(alpha = 0.22f), CircleShape)
            .semantics { contentDescription = findingColors },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { rotationZ = rotation }
        ) {
            val stroke = Stroke(width = 2.25.dp.toPx(), cap = StrokeCap.Round)
            drawArc(
                color = Color.White.copy(alpha = 0.22f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 88f,
                useCenter = false,
                style = stroke
            )
        }
    }
}

private fun fillCenterScale(
    containerSize: IntSize,
    bitmapWidth: Int,
    bitmapHeight: Int
): Float = max(
    containerSize.width.toFloat() / bitmapWidth.toFloat(),
    containerSize.height.toFloat() / bitmapHeight.toFloat()
)

private fun fillCenterBitmapCoord(
    touch: Float,
    container: Int,
    bitmapSize: Int,
    scale: Float
): Float = (touch + (bitmapSize * scale - container) / 2f) / scale

@Composable
fun MagnifierLoupe(
    bitmap: Bitmap,
    touchPoint: Offset,
    containerSize: IntSize,
    modifier: Modifier = Modifier,
    hexCode: String = "",
    tempMode: String = ""
) {
    val magnifierRadiusDp = 48.dp
    val zoomFactor = 2f
    val srcRect = remember { android.graphics.Rect() }
    val destRect = remember { android.graphics.Rect() }

    Box(
        modifier = modifier.offset {
            val radiusPx = magnifierRadiusDp.toPx()
            val diameterPx = radiusPx * 2f
            val fingerGapPx = 32.dp.toPx()
            val hexOverflowPx = if (hexCode.isNotEmpty()) 12.dp.toPx() else 0f
            val maxX = (containerSize.width - diameterPx).coerceAtLeast(0f)
            val maxY = (containerSize.height - diameterPx - hexOverflowPx).coerceAtLeast(0f)
            IntOffset(
                x = (touchPoint.x - radiusPx).coerceIn(0f, maxX).roundToInt(),
                y = (touchPoint.y - diameterPx - fingerGapPx).coerceIn(0f, maxY).roundToInt()
            )
        }
    ) {
        Box(
            modifier = Modifier
                .size(magnifierRadiusDp * 2)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .drawBehind {
                    if (containerSize.width == 0 || containerSize.height == 0 ||
                        bitmap.width == 0 || bitmap.height == 0
                    ) {
                        return@drawBehind
                    }
                    val scale = fillCenterScale(containerSize, bitmap.width, bitmap.height)
                    val mappedX = fillCenterBitmapCoord(
                        touchPoint.x,
                        containerSize.width,
                        bitmap.width,
                        scale
                    )
                    val mappedY = fillCenterBitmapCoord(
                        touchPoint.y,
                        containerSize.height,
                        bitmap.height,
                        scale
                    )
                    val magnifierRadiusPx = magnifierRadiusDp.toPx()
                    val sampleRadius = magnifierRadiusPx / zoomFactor / scale
                    val srcLeft = mappedX - sampleRadius
                    val srcTop = mappedY - sampleRadius
                    val srcRight = mappedX + sampleRadius
                    val srcBottom = mappedY + sampleRadius
                    val srcSpan = sampleRadius * 2f
                    val destSize = magnifierRadiusPx * 2f
                    val clampedLeft = srcLeft.coerceAtLeast(0f)
                    val clampedTop = srcTop.coerceAtLeast(0f)
                    val clampedRight = srcRight.coerceAtMost(bitmap.width.toFloat())
                    val clampedBottom = srcBottom.coerceAtMost(bitmap.height.toFloat())
                    if (clampedLeft < clampedRight && clampedTop < clampedBottom) {
                        srcRect.set(
                            clampedLeft.toInt(),
                            clampedTop.toInt(),
                            clampedRight.toInt(),
                            clampedBottom.toInt()
                        )
                        destRect.set(
                            ((clampedLeft - srcLeft) / srcSpan * destSize).roundToInt(),
                            ((clampedTop - srcTop) / srcSpan * destSize).roundToInt(),
                            ((clampedRight - srcLeft) / srcSpan * destSize).roundToInt(),
                            ((clampedBottom - srcTop) / srcSpan * destSize).roundToInt()
                        )
                        if (srcRect.width() > 0 && srcRect.height() > 0) {
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                bitmap,
                                srcRect,
                                destRect,
                                null
                            )
                        }
                    }

                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(magnifierRadiusPx, magnifierRadiusPx - 10.dp.toPx()),
                        end = Offset(magnifierRadiusPx, magnifierRadiusPx + 10.dp.toPx()),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(magnifierRadiusPx - 10.dp.toPx(), magnifierRadiusPx),
                        end = Offset(magnifierRadiusPx + 10.dp.toPx(), magnifierRadiusPx),
                        strokeWidth = 3f
                    )
                }
        )

        if (hexCode.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp),
                shape = RoundedCornerShape(8.dp),
                color = AtelierTheme.colors.surfaceContainerHighest,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = hexCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
