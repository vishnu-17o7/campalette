package com.vishnu.campalette.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.sin
import com.vishnu.campalette.ui.theme.ExpressiveSpatialSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsColorSpring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.theme.AtelierPrimary
import com.vishnu.campalette.ui.theme.AtelierPrimaryContainer
import com.vishnu.campalette.ui.theme.AtelierRoundedExtra
import com.vishnu.campalette.ui.theme.AtelierRoundedLarge
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.LocalDynamicThemeColors

enum class AppScreen(
    @StringRes val labelRes: Int,
    @StringRes val actionRes: Int
) {
    Live(R.string.live_view, R.string.bottom_nav_live),
    Library(R.string.library, R.string.bottom_nav_library),
    History(R.string.history, R.string.bottom_nav_history),
    Editor(R.string.editor, R.string.bottom_nav_builder),
    Settings(R.string.settings, R.string.bottom_nav_settings)
}

@Composable
fun AtelierLabelTag(
    text: String,
    color: Color = MaterialTheme.colorScheme.outline
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.12f),
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(AtelierRoundedLarge))
            .background(background)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GradientPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dynamicColors = LocalDynamicThemeColors.current
    val primary = if (dynamicColors.blendFactor > 0f) dynamicColors.primaryShift else AtelierPrimary
    val container = if (dynamicColors.blendFactor > 0f) dynamicColors.primaryContainerShift else AtelierPrimaryContainer

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = ExpressiveSpatialSpring,
        label = "ctaScale"
    )
    val gradientTint by animateFloatAsState(
        targetValue = if (pressed) 0.15f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "ctaTint"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(AtelierRoundedLarge))
            .background(
                Brush.linearGradient(
                    colors = listOf(container, primary),
                    start = Offset.Zero,
                    end = Offset(520f, 140f)
                )
            )
            .background(Color.White.copy(alpha = gradientTint), RoundedCornerShape(AtelierRoundedLarge))
            .semantics {
                role = Role.Button
                contentDescription = text
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun SoftActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = ExpressiveSpatialSpring,
        label = "softButtonScale"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(AtelierRoundedLarge))
            .background(AtelierTheme.colors.surfaceContainerLow)
            .semantics {
                role = Role.Button
                contentDescription = text
            }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun EditorialInputField(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onValueChange: (String) -> Unit
) {
    val hasContent = value.isNotBlank()
    val underlineHeight by animateDpAsState(
        targetValue = if (hasContent) 2.5.dp else 1.5.dp,
        animationSpec = tween(durationMillis = 180),
        label = "underlineHeight"
    )
    val underlineAlpha by animateFloatAsState(
        targetValue = if (hasContent) 0.9f else 0.45f,
        animationSpec = tween(durationMillis = 180),
        label = "underlineAlpha"
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontStyle = FontStyle.Italic
        ),
        modifier = modifier.semantics {
            contentDescription = label
            editableText = AnnotatedString(value)
        },
        decorationBox = { innerTextField ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AtelierLabelTag(text = label, color = MaterialTheme.colorScheme.outline)
                Box {
                    if (value.isBlank() && placeholder.isNotBlank()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                    innerTextField()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(underlineHeight)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = underlineAlpha))
                )
            }
        }
    )
}

@Composable
fun AtelierSwatch(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null
) {
    val stateLabel = if (selected) {
        stringResource(R.string.selected_state)
    } else {
        stringResource(R.string.not_selected_state)
    }
    val description = stringResource(R.string.swatch_state, label)

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "swatchScale"
    )

    val ringWidth by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = ExpressiveEffectsSpring,
        label = "swatchRingWidth"
    )

    val borderColor = AtelierTheme.colors.primaryFixed

    Column(
        modifier = modifier.widthIn(min = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size((size + 10.dp) * scale)
                .clip(CircleShape)
                .background(
                    if (selected) borderColor.copy(alpha = 0.7f * scale)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size * scale)
                    .clip(CircleShape)
                    .background(color)
                    .drawWithContent {
                        drawContent()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.02f), Color.Black.copy(alpha = 0.1f)),
                                center = center,
                                radius = this.size.minDimension / 1.6f
                            )
                        )
                    }
                    .then(
                        if (selected) Modifier.drawBehind {
                            drawCircle(
                                color = borderColor,
                                radius = this.size.minDimension / 2f + 2.dp.toPx(),
                                style = Stroke(width = (2.5f * ringWidth).dp.toPx())
                            )
                        } else Modifier
                    )
                    .semantics {
                        this.selected = selected
                        role = Role.Button
                        contentDescription = description
                        stateDescription = stateLabel
                    }
                    .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 12.sp),
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PaletteStrip(
    palette: List<PaletteColor>,
    selectedHex: String?,
    onColorSelected: (PaletteColor) -> Unit,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingAdd: (() -> Unit)? = null
) {
    val captureLabel = stringResource(R.string.capture_button)
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(palette) { paletteColor ->
            AtelierSwatch(
                color = Color(paletteColor.color),
                label = paletteColor.hexCode,
                selected = paletteColor.hexCode == selectedHex,
                labelColor = labelColor,
                onClick = { onColorSelected(paletteColor) }
            )
        }
        if (trailingAdd != null) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(56.dp)
                            .semantics {
                                role = Role.Button
                                contentDescription = captureLabel
                            }
                            .clickable(onClick = trailingAdd)
                    ) {
                        drawCircle(
                            color = labelColor.copy(alpha = 0.3f),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        )
                        drawLine(
                            color = labelColor.copy(alpha = 0.45f),
                            start = Offset(size.width / 2f, size.height * 0.3f),
                            end = Offset(size.width / 2f, size.height * 0.7f),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = labelColor.copy(alpha = 0.45f),
                            start = Offset(size.width * 0.3f, size.height / 2f),
                            end = Offset(size.width * 0.7f, size.height / 2f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    Text(
                        text = stringResource(R.string.capture_color_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingBottomNav(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier,
    isCapturing: Boolean = false,
    isCaptured: Boolean = false,
    onLiveAction: (() -> Unit)? = null
) {
    val dynamicColors = LocalDynamicThemeColors.current
    val dynamicPrimary = if (dynamicColors.blendFactor > 0f) dynamicColors.primaryShift else AtelierPrimary
    val dynamicContainer = if (dynamicColors.blendFactor > 0f) dynamicColors.primaryContainerShift else AtelierPrimaryContainer
    val items = listOf(
        Triple(AppScreen.History, Icons.Rounded.History, AppScreen.History.actionRes),
        Triple(AppScreen.Live, Icons.Rounded.CameraAlt, AppScreen.Live.actionRes),
        Triple(AppScreen.Settings, Icons.Rounded.Settings, AppScreen.Settings.actionRes)
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AtelierTheme.colors.surfaceContainerLow.copy(alpha = 0.92f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (screen, icon, actionRes) ->
            androidx.compose.runtime.key(screen) {
            val isLiveCaptureAction = screen == AppScreen.Live && currentScreen == AppScreen.Live && onLiveAction != null
            val action = if (isLiveCaptureAction) {
                stringResource(R.string.bottom_nav_capture)
            } else {
                stringResource(actionRes)
            }
            val state = if (!isLiveCaptureAction && currentScreen == screen) {
                stringResource(R.string.selected_state)
            } else {
                stringResource(R.string.not_selected_state)
            }
            if (icon == Icons.Rounded.CameraAlt) {
                val displayIcon = if (isCaptured) Icons.Rounded.Refresh else Icons.Rounded.CameraAlt
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 0.94f else 1f,
                    animationSpec = ExpressiveSpatialSpring,
                    label = "cameraScale"
                )

                val shouldPulse = isLiveCaptureAction && !isCapturing && !isCaptured
                val pulseTransition = rememberInfiniteTransition(label = "cameraBreathe")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                Box(
                    modifier = Modifier
                        .scale(if (shouldPulse) scale * pulseScale else scale)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(dynamicPrimary, dynamicContainer),
                                start = Offset.Zero,
                                end = Offset(220f, 120f)
                            )
                        )
                        .semantics {
                            role = if (isLiveCaptureAction) Role.Button else Role.Tab
                            if (!isLiveCaptureAction) {
                                selected = currentScreen == screen
                                stateDescription = state
                            }
                            contentDescription = if (isCapturing) "Capturing" else action
                        }
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = !isCapturing
                        ) {
                            if (isLiveCaptureAction) {
                                onLiveAction?.invoke()
                            } else {
                                onScreenSelected(screen)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCapturing && isLiveCaptureAction) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(displayIcon, contentDescription = action, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                val isSelected = currentScreen == screen
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val targetTint by animateColorAsState(
                    targetValue = if (isSelected) {
                        dynamicPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    },
                    animationSpec = ExpressiveEffectsColorSpring,
                    label = "navIconTint"
                )
                val activeIndicatorScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = ExpressiveSpatialSpring,
                    label = "indicatorScale"
                )
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .semantics {
                            role = Role.Tab
                            selected = currentScreen == screen
                            contentDescription = action
                            stateDescription = state
                        }
                        .clickable(interactionSource = interaction, indication = null) { onScreenSelected(screen) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .scale(activeIndicatorScale)
                                .background(dynamicContainer.copy(alpha = 0.22f), shape = CircleShape)
                        )
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = action,
                        tint = targetTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            } // key(screen)
        }
    }
}

@Composable
fun PaletteCard(
    title: String,
    subtitle: String,
    swatches: List<PaletteColor>,
    modifier: Modifier = Modifier,
    tag: String? = null,
    featured: Boolean = false,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onActionClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = ExpressiveSpatialSpring,
        label = "paletteCardScale"
    )
    val shape = RoundedCornerShape(AtelierRoundedLarge)
    Column(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                if (pressed) AtelierTheme.colors.surfaceContainerLow
                else if (featured) AtelierTheme.colors.surfaceContainerLow
                else AtelierTheme.colors.surfaceContainerLowest
            )
            .semantics {
                role = Role.Button
                contentDescription = title
            }
            .let { base -> if (onClick != null) base.clickable(interactionSource = interaction, indication = null, onClick = onClick) else base }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (featured) 148.dp else 120.dp)
        ) {
            swatches.take(5).forEach { swatch ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(swatch.color))
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!tag.isNullOrBlank()) {
                    AtelierLabelTag(
                        text = tag,
                        color = if (featured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                Text(
                    text = title,
                    style = if (featured) {
                        MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
                    } else {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (actionIcon != null && onActionClick != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .clickable(onClick = onActionClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TechnicalValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AtelierLabelTag(label, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun WavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp,
    waveLength: Dp = 24.dp,
    amplitude: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseShift"
    )

    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f
        val pxWaveLength = with(density) { waveLength.toPx() }
        val pxAmplitude = with(density) { amplitude.toPx() }
        val pxStrokeWidth = with(density) { strokeWidth.toPx() }

        val path = Path().apply {
            moveTo(0f, midY)
            var x = 0f
            while (x < width) {
                val relativeX = x / pxWaveLength
                val y = midY + pxAmplitude * sin(relativeX * 2f * Math.PI.toFloat() - phaseShift)
                lineTo(x, y)
                x += 2f
            }
            lineTo(width, midY)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = pxStrokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun ExpressiveSplitButton(
    primaryText: String,
    secondaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    val dynamicColors = LocalDynamicThemeColors.current
    val dynamicPrimary = if (dynamicColors.blendFactor > 0f) dynamicColors.primaryShift else AtelierPrimary
    val dynamicContainer = if (dynamicColors.blendFactor > 0f) dynamicColors.primaryContainerShift else AtelierPrimaryContainer

    val interactionPrimary = remember { MutableInteractionSource() }
    val pressedPrimary by interactionPrimary.collectIsPressedAsState()
    val scalePrimary by animateFloatAsState(
        targetValue = if (pressedPrimary) 0.94f else 1f,
        animationSpec = ExpressiveSpatialSpring,
        label = "splitPrimaryScale"
    )

    val interactionSecondary = remember { MutableInteractionSource() }
    val pressedSecondary by interactionSecondary.collectIsPressedAsState()
    val scaleSecondary by animateFloatAsState(
        targetValue = if (pressedSecondary) 0.94f else 1f,
        animationSpec = ExpressiveSpatialSpring,
        label = "splitSecondaryScale"
    )

    Row(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .scale(scalePrimary)
                .clip(RoundedCornerShape(topStart = AtelierRoundedLarge, bottomStart = AtelierRoundedLarge, topEnd = 4.dp, bottomEnd = 4.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(dynamicPrimary, dynamicContainer),
                        start = Offset.Zero,
                        end = Offset(240f, 100f)
                    )
                )
                .clickable(interactionSource = interactionPrimary, indication = null, onClick = onPrimaryClick)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(2.dp))

        Box(
            modifier = Modifier
                .scale(scaleSecondary)
                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = AtelierRoundedLarge, bottomEnd = AtelierRoundedLarge))
                .background(dynamicPrimary)
                .clickable(interactionSource = interactionSecondary, indication = null, onClick = onSecondaryClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = secondaryIcon,
                contentDescription = primaryText,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
