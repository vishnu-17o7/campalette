package com.vishnu.campalette.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.theme.AtelierPrimary
import com.vishnu.campalette.ui.theme.AtelierPrimaryContainer
import com.vishnu.campalette.ui.theme.AtelierTheme

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
            .clip(RoundedCornerShape(32.dp))
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
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "ctaScale"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(48.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(AtelierPrimary, AtelierPrimaryContainer),
                    start = Offset.Zero,
                    end = Offset(520f, 140f)
                )
            )
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
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(AtelierTheme.colors.surfaceContainerLow)
            .semantics {
                role = Role.Button
                contentDescription = text
            }
            .clickable(onClick = onClick)
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
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
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
    Column(
        modifier = modifier.widthIn(min = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (selected) size + 10.dp else size)
                .clip(CircleShape)
                .background(
                    if (selected) AtelierTheme.colors.primaryFixed.copy(alpha = 0.7f)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size)
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
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    onColorSelected: (PaletteColor) -> Unit,
    trailingAdd: (() -> Unit)? = null
) {
    val captureLabel = stringResource(R.string.capture_button)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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
    modifier: Modifier = Modifier,
    onLiveAction: (() -> Unit)? = null,
    onScreenSelected: (AppScreen) -> Unit
) {
    val items = listOf(
        Triple(AppScreen.Library, Icons.Rounded.Palette, AppScreen.Library.actionRes),
        Triple(AppScreen.Editor, Icons.Rounded.AutoAwesome, AppScreen.Editor.actionRes),
        Triple(AppScreen.Live, Icons.Rounded.CameraAlt, AppScreen.Live.actionRes),
        Triple(AppScreen.History, Icons.Rounded.History, AppScreen.History.actionRes),
        Triple(AppScreen.Settings, Icons.Rounded.Settings, AppScreen.Settings.actionRes)
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(AtelierTheme.colors.surfaceContainerLow.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (screen, icon, actionRes) ->
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
                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (pressed) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = 0.68f, stiffness = 480f),
                    label = "cameraScale"
                )
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(if (isLiveCaptureAction) RoundedCornerShape(28.dp) else CircleShape)
                        .background(
                            if (isLiveCaptureAction) {
                                Brush.linearGradient(
                                    colors = listOf(AtelierPrimary, AtelierPrimaryContainer),
                                    start = Offset.Zero,
                                    end = Offset(220f, 120f)
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                                    start = Offset.Zero,
                                    end = Offset(1f, 1f)
                                )
                            }
                        )
                        .semantics {
                            role = if (isLiveCaptureAction) Role.Button else Role.Tab
                            if (!isLiveCaptureAction) {
                                selected = currentScreen == screen
                                stateDescription = state
                            }
                            contentDescription = action
                        }
                        .clickable(interactionSource = interaction, indication = null) {
                            if (isLiveCaptureAction) {
                                onLiveAction?.invoke()
                            } else {
                                onScreenSelected(screen)
                            }
                        }
                        .padding(horizontal = if (isLiveCaptureAction) 18.dp else 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .semantics {
                            role = Role.Tab
                            selected = currentScreen == screen
                            contentDescription = action
                            stateDescription = state
                        }
                        .clickable { onScreenSelected(screen) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (currentScreen == screen) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        }
                    )
                }
            }
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
    onClick: (() -> Unit)? = null
) {
    val shape = if (featured) RoundedCornerShape(40.dp) else RoundedCornerShape(32.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                if (featured) AtelierTheme.colors.surfaceContainerLow
                else AtelierTheme.colors.surfaceContainerLowest
            )
            .semantics {
                role = Role.Button
                contentDescription = title
            }
            .let { base -> if (onClick != null) base.clickable(onClick = onClick) else base }
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
        Column(
            modifier = Modifier.padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 24.dp),
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
                    MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic)
                } else {
                    MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic)
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
