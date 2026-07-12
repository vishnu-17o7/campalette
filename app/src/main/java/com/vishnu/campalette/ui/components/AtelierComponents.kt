package com.vishnu.campalette.ui.components

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.ExpressiveEffectsColorSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsSpring
import com.vishnu.campalette.ui.theme.ExpressiveSpatialSpring
import com.vishnu.campalette.ui.theme.LocalDynamicThemeColors
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
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

/**
 * Shared Utilities
 */

fun Modifier.swipeToDismiss(
    onDismiss: () -> Unit,
    enabled: Boolean = true,
    dismissThreshold: Float = 0.4f
): Modifier = composed {
    if (!enabled) return@composed this

    val haptic = LocalHapticFeedback.current
    var componentWidth by remember { mutableStateOf(0f) }

    val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
    val scope = rememberCoroutineScope()

    this
        .onGloballyPositioned { componentWidth = it.size.width.toFloat() }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val threshold = componentWidth * dismissThreshold
                    if (kotlin.math.abs(offsetX.value) > threshold) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = if (offsetX.value > 0) componentWidth else -componentWidth,
                                animationSpec = androidx.compose.animation.core.tween(200)
                            )
                            onDismiss()
                        }
                    } else {
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = 0.8f,
                                    stiffness = 400f
                                )
                            )
                        }
                    }
                },
                onDragCancel = {
                    scope.launch {
                        offsetX.animateTo(0f)
                    }
                },
                onHorizontalDrag = { change: androidx.compose.ui.input.pointer.PointerInputChange, dragAmount: Float ->
                    change.consume()
                    scope.launch {
                        offsetX.snapTo(offsetX.value + dragAmount)
                    }
                }
            )
        }
        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
        .alpha(1f - (kotlin.math.abs(offsetX.value) / (componentWidth.takeIf { it > 0 } ?: 1f)).coerceIn(0f, 1f))
}

@Composable
fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable (isTablet: Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    Box(modifier = modifier) {
        content(isTablet)
    }
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
        color = AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.92f),
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
    val reducedMotion = LocalReducedMotion.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = if (reducedMotion) spring() else ExpressiveSpatialSpring,
        label = "ButtonScale"
    )

    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift
    val alpha = if (enabled) 1f else 0.5f

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(RoundedCornerShape(14.dp))
            .background(dynamicPrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
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
    val reducedMotion = LocalReducedMotion.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = if (reducedMotion) spring() else ExpressiveSpatialSpring,
        label = "ButtonScale"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(AtelierTheme.colors.surfaceContainer.copy(alpha = 0.86f))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
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
                    contentDescription = "Clear search",
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AtelierTheme.colors.surfaceContainerHighest.copy(alpha = 0.52f))
            .padding(2.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (key, label) ->
            val selected = key == selectedKey
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (selected) {
                            Modifier
                                .shadow(1.dp, RoundedCornerShape(8.dp), clip = false)
                                .background(AtelierTheme.colors.surfaceContainerLowest, RoundedCornerShape(8.dp))
                        } else Modifier
                    )
                    .selectable(
                        selected = selected,
                        onClick = { onSelected(key) },
                        role = Role.Tab
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AtelierToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF34C759) else AtelierTheme.colors.surfaceContainerHighest,
        animationSpec = ExpressiveEffectsColorSpring, label = "Track"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f), label = "Thumb"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else Color.White,
        animationSpec = ExpressiveEffectsColorSpring, label = "ThumbColor"
    )

    Box(
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .semantics {
                role = Role.Switch
                stateDescription = if (checked) "On" else "Off"
            }
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 2.dp)
                .size(27.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift
    val bgColor by animateColorAsState(
        targetValue = if (selected) dynamicPrimary else AtelierTheme.colors.surfaceContainerHigh,
        animationSpec = ExpressiveEffectsColorSpring, label = "bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = ExpressiveEffectsColorSpring, label = "text"
    )

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
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
    var appeared by remember { mutableStateOf(reducedMotion) }

    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            delay(entryDelay.toLong())
            appeared = true
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (appeared) {
            if (selected) 1.15f else 1f
        } else 0f,
        animationSpec = if (reducedMotion) spring() else ExpressiveSpatialSpring,
        label = "SwatchScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = if (reducedMotion) spring() else ExpressiveEffectsSpring,
        label = "SwatchAlpha"
    )

    val ringWidth by animateDpAsState(
        targetValue = if (selected) 3.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "Ring"
    )

    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift

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
                    this.alpha = alpha
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}

@Composable
fun PaletteRow(
    palette: List<PaletteColor>,
    selectedColor: PaletteColor?,
    onColorSelected: (PaletteColor) -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        itemsIndexed(palette) { index, pc ->
            ColorSwatch(
                color = Color(pc.color),
                selected = selectedColor == pc,
                onClick = { onColorSelected(pc) },
                label = pc.hexCode,
                entryDelay = index * 60
            )
        }
        if (onAddClick != null) {
            item {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AtelierTheme.colors.surfaceContainerHighest)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Color",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
    isFavorite: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
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
                        contentDescription = "Favorite",
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
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cameraChrome = currentScreen == AppScreen.Live
    val useDarkGlass = isDark || cameraChrome
    val glassBase = if (useDarkGlass) Color.Black else Color.White
    val glassStyle = HazeStyle(
        blurRadius = 30.dp,
        backgroundColor = glassBase.copy(alpha = if (useDarkGlass) 0.18f else 0.10f),
        tints = listOf(HazeTint(glassBase.copy(alpha = if (useDarkGlass) 0.26f else 0.18f)))
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val dockShape = RoundedCornerShape(32.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 350.dp)
                .height(64.dp)
                .shadow(3.dp, dockShape, clip = false)
                .clip(dockShape)
                .hazeEffect(
                    state = hazeState,
                    style = glassStyle
                ) {
                    noiseFactor = 0.025f
                }
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = if (useDarkGlass) 0.34f else 0.68f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx()),
                        style = Stroke(width = 0.75.dp.toPx())
                    )
                }
                .padding(horizontal = 8.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = { Icon(Icons.Rounded.PhotoLibrary, null) },
                label = "Library",
                selected = currentScreen == AppScreen.History,
                darkChrome = cameraChrome,
                onClick = { onScreenSelected(AppScreen.History) },
                modifier = Modifier.weight(1f)
            )

            BottomBarItem(
                icon = { Icon(Icons.Rounded.CameraAlt, null) },
                label = "Camera",
                selected = currentScreen == AppScreen.Live,
                darkChrome = cameraChrome,
                onClick = { onScreenSelected(AppScreen.Live) },
                modifier = Modifier.weight(1f)
            )

            BottomBarItem(
                icon = { Icon(Icons.Rounded.Settings, null) },
                label = "Settings",
                selected = currentScreen == AppScreen.Settings,
                darkChrome = cameraChrome,
                onClick = { onScreenSelected(AppScreen.Settings) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    darkChrome: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift
    val color by animateColorAsState(
        targetValue = if (selected) {
            dynamicPrimary
        } else if (darkChrome) {
            Color.White.copy(alpha = 0.82f)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
        },
        animationSpec = ExpressiveEffectsColorSpring, label = "Color"
    )
    val selectionColor by animateColorAsState(
        targetValue = if (selected) dynamicPrimary.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = ExpressiveEffectsColorSpring,
        label = "Selection"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .semantics { contentDescription = label }
            .heightIn(min = 56.dp)
            .padding(vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(selectionColor),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides color
            ) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun CaptureProgress(modifier: Modifier = Modifier) {
    val dynamicPrimary = LocalDynamicThemeColors.current.primaryShift
    val dynamicContainer = LocalDynamicThemeColors.current.primaryContainerShift

    val infiniteTransition = rememberInfiniteTransition(label = "Dots")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Canvas(modifier = modifier.size(64.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 3

        // Draw 3 orbiting dots
        for (i in 0 until 3) {
            val offsetAngle = angle + (i * 120f)
            val rad = offsetAngle * (PI / 180f).toFloat()
            val x = center.x + radius * cos(rad)
            val y = center.y + radius * sin(rad)

            drawCircle(
                color = if (i == 0) dynamicPrimary else dynamicContainer,
                radius = 6.dp.toPx() * scale,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun MagnifierLoupe(
    bitmap: Bitmap,
    touchPoint: Offset,
    containerSize: IntSize,
    modifier: Modifier = Modifier,
    hexCode: String = "",
    tempMode: String = ""
) {
    val density = LocalDensity.current
    val magnifierRadiusDp = 48.dp
    val magnifierRadiusPx = with(density) { magnifierRadiusDp.toPx() }
    val zoomFactor = 2f

    val posX = touchPoint.x.coerceIn(
        magnifierRadiusPx,
        (containerSize.width - magnifierRadiusPx).coerceAtLeast(magnifierRadiusPx)
    )
    val posY = (touchPoint.y - magnifierRadiusPx - with(density) { 32.dp.toPx() }).coerceIn(
        magnifierRadiusPx,
        (containerSize.height - magnifierRadiusPx).coerceAtLeast(magnifierRadiusPx)
    )

    val animatedPosX by animateFloatAsState(targetValue = posX, label = "X")
    val animatedPosY by animateFloatAsState(targetValue = posY, label = "Y")

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (animatedPosX - magnifierRadiusPx).roundToInt(),
                    (animatedPosY - magnifierRadiusPx).roundToInt()
                )
            }
    ) {
        // Shadow and Border
        Box(
            modifier = Modifier
                .size(magnifierRadiusDp * 2)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .drawBehind {
                    // Draw magnified bitmap section
                    val srcRect = android.graphics.Rect(
                        (touchPoint.x - (magnifierRadiusPx / zoomFactor)).toInt().coerceAtLeast(0),
                        (touchPoint.y - (magnifierRadiusPx / zoomFactor)).toInt().coerceAtLeast(0),
                        (touchPoint.x + (magnifierRadiusPx / zoomFactor)).toInt().coerceAtMost(bitmap.width),
                        (touchPoint.y + (magnifierRadiusPx / zoomFactor)).toInt().coerceAtMost(bitmap.height)
                    )
                    val destRect = android.graphics.Rect(0, 0, (magnifierRadiusPx * 2).toInt(), (magnifierRadiusPx * 2).toInt())
                    drawContext.canvas.nativeCanvas.drawBitmap(bitmap, srcRect, destRect, null)

                    // Draw Crosshair
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

        // Hex Label
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
