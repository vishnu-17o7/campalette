package com.vishnu.campalette.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.HarmonyMode
import com.vishnu.campalette.ui.components.AdaptiveLayout
import com.vishnu.campalette.ui.components.AppScreen
import com.vishnu.campalette.ui.components.AppleSearchField
import com.vishnu.campalette.ui.components.AtelierTag
import com.vishnu.campalette.ui.components.AtelierTextField
import com.vishnu.campalette.ui.components.AtelierToggle
import com.vishnu.campalette.ui.components.ColorSwatch
import com.vishnu.campalette.ui.components.FilterChip
import com.vishnu.campalette.ui.components.PaletteListCard
import com.vishnu.campalette.ui.components.PrimaryButton
import com.vishnu.campalette.ui.components.SecondaryButton
import com.vishnu.campalette.ui.components.SegmentedControl
import com.vishnu.campalette.ui.components.SurfaceCard
import com.vishnu.campalette.ui.components.ValueLabel
import com.vishnu.campalette.ui.components.swipeToDismiss
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.ExpressiveSpatialSpring
import com.vishnu.campalette.ui.theme.CampaletteTheme
import kotlin.math.roundToInt

/**
 * State Holders
 */
data class LibraryState(
    val palettes: List<PaletteStudy>,
    val searchQuery: String,
    val selectedFilter: String,
    val selectedSort: String,
    val isGridView: Boolean,
    val favorites: Set<String>
)

data class EditorState(
    val palette: List<PaletteColor>,
    val name: String,
    val source: String,
    val harmonyMode: HarmonyMode,
    val selectedHex: String? = null
)

/**
 * Palette Library Screen
 */
@Composable
fun PaletteLibraryScreen(
    state: LibraryState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onToggleGrid: () -> Unit,
    onPaletteSelect: (PaletteStudy) -> Unit,
    onDeletePalette: (PaletteStudy) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPalette by remember { mutableStateOf<PaletteStudy?>(null) }

    AdaptiveLayout(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { isTablet ->
        Row(modifier = Modifier.fillMaxSize()) {
            // Main List Pane
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsPadding().height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Library",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isTablet) {
                            IconButton(onClick = onToggleGrid) {
                                Icon(
                                    imageVector = if (state.isGridView) Icons.Rounded.List else Icons.Rounded.GridView,
                                    contentDescription = "Toggle view",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Search and Filters
                item {
                    AppleSearchField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = "Search palettes or hex"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SegmentedControl(
                        options = listOf(
                            "all" to "All",
                            "camera" to "Camera",
                            "harmony" to "Harmony",
                            "saved" to "Saved"
                        ),
                        selectedKey = state.selectedFilter,
                        onSelected = onFilterChange
                    )
                }

                // Palette List
                if (state.palettes.isEmpty()) {
                    item {
                        EmptyStudioState(
                            tag = "Palette library",
                            title = "No palettes yet",
                            body = "Capture a scene or import a photo. Your palettes will appear here.",
                            primaryActionText = "Capture colors",
                            onPrimaryAction = { onNavigate(AppScreen.Live) }
                        )
                    }
                } else {
                    if (state.isGridView) {
                        // Grid View (2 items per row)
                        val chunked = state.palettes.chunked(2)
                        itemsIndexed(chunked) { _, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { study ->
                                    PaletteListCard(
                                        title = study.name.ifEmpty { "Unnamed Palette" },
                                        subtitle = "${study.colors.size} colors",
                                        swatches = study.colors,
                                        onClick = {
                                            if (isTablet) selectedPalette = study else onPaletteSelect(study)
                                        },
                                        isFavorite = state.favorites.contains(study.name),
                                        modifier = Modifier
                                            .weight(1f)
                                            .swipeToDismiss(onDismiss = { onDeletePalette(study) })
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        item {
                            SurfaceCard(padding = PaddingValues(0.dp)) {
                                state.palettes.forEachIndexed { index, study ->
                                    NativePaletteRow(
                                        study = study,
                                        onClick = {
                                            if (isTablet) selectedPalette = study else onPaletteSelect(study)
                                        },
                                        modifier = Modifier.swipeToDismiss(onDismiss = { onDeletePalette(study) })
                                    )
                                    if (index != state.palettes.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 112.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Detail Pane (Tablet only)
            if (isTablet) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                Box(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPalette != null) {
                        PaletteLibraryDetailPane(
                            study = selectedPalette!!,
                            onEdit = { onPaletteSelect(selectedPalette!!) }
                        )
                    } else {
                        Text(
                            text = "Select a palette to view details",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NativePaletteRow(
    study: PaletteStudy,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 88.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(width = 84.dp, height = 58.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            study.colors.take(6).forEach { swatch ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(swatch.color))
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = study.name.ifEmpty { "Untitled palette" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${study.colors.size} colors · ${study.source}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PaletteLibraryDetailPane(
    study: PaletteStudy,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = study.name.ifEmpty { "Unnamed Palette" },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Captured via ${study.source}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        SurfaceCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val stats = AtelierData.paletteStats(study.colors)
                ValueLabel("Colors", "${study.colors.size}")
                ValueLabel("Warmth", stats.warmth)
                ValueLabel("Contrast", String.format("%.1f", stats.contrastRatio))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        AtelierTag("Swatches")
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(study.colors) { _, color ->
                ColorSwatch(color = Color(color.color), selected = false, onClick = {}, label = color.hexCode, size = 64.dp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(
            text = "Edit Palette",
            onClick = onEdit,
            icon = { Icon(Icons.Rounded.Edit, null) }
        )
    }
}

/**
 * Palette Editor Screen
 */
@Composable
fun PaletteEditorScreen(
    state: EditorState,
    onNameChange: (String) -> Unit,
    onHarmonyModeChange: (HarmonyMode) -> Unit,
    onSelectColor: (PaletteColor) -> Unit,
    onInspectColor: (PaletteColor) -> Unit,
    onDeleteColor: (PaletteColor) -> Unit,
    onReorder: (from: Int, to: Int) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    if (state.palette.isEmpty()) {
        EmptyStudioState(
            tag = "Editor",
            title = "No palette to edit",
            body = "Capture a new palette or open one from your library to start editing.",
            primaryActionText = "Open Camera",
            onPrimaryAction = { onNavigate(AppScreen.Live) },
            secondaryActionText = "Browse Library",
            onSecondaryAction = { onNavigate(AppScreen.History) }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.statusBarsPadding().height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                IconButton(
                    onClick = { onNavigate(AppScreen.History) },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Rounded.ArrowBack, "Back to library", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "Edit Palette",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onShare, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.Share, "Share palette", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onSave).padding(start = 4.dp, top = 12.dp, bottom = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AtelierTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Palette name",
                placeholder = "Give this palette a name"
            )
        }

        // Action Toolbar
        item {
            AtelierTag("Harmony")
            Spacer(modifier = Modifier.height(8.dp))
            SegmentedControl(
                options = listOf(
                    HarmonyMode.Analogous.name to "Analogous",
                    HarmonyMode.Complementary.name to "Complement",
                    HarmonyMode.Tonal.name to "Tonal"
                ),
                selectedKey = state.harmonyMode.name,
                onSelected = { key ->
                    HarmonyMode.entries.firstOrNull { it.name == key }?.let(onHarmonyModeChange)
                }
            )
        }

        // Stats Card
        item {
            SurfaceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val stats = AtelierData.paletteStats(state.palette)
                    ValueLabel("Colors", "${state.palette.size}")
                    ValueLabel("Warmth", stats.warmth)
                    ValueLabel("Saturation", String.format("%.0f%%", stats.avgSaturation))
                }
            }
        }

        item {
            SurfaceCard(padding = PaddingValues(0.dp)) {
                state.palette.forEachIndexed { index, color ->
                    key("${color.hexCode}-$index") {
                        var dragOffsetY by remember { mutableFloatStateOf(0f) }
                        val animatedDragOffsetY by animateFloatAsState(
                            targetValue = dragOffsetY,
                            animationSpec = ExpressiveSpatialSpring,
                            label = "DragY"
                        )

                        EditorSwatchRow(
                            color = color,
                            selected = state.selectedHex == color.hexCode,
                            onSelect = { onSelectColor(color) },
                            onEdit = { onInspectColor(color) },
                            modifier = Modifier
                                .offset { IntOffset(0, animatedDragOffsetY.roundToInt()) }
                                .swipeToDismiss(onDismiss = { onDeleteColor(color) })
                                .pointerInput(color.hexCode) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                            val itemHeight = 77.dp.toPx()
                                            val targetIndex = (index + (dragOffsetY / itemHeight).roundToInt())
                                                .coerceIn(0, state.palette.lastIndex)
                                            if (targetIndex != index) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onReorder(index, targetIndex)
                                                dragOffsetY -= (targetIndex - index) * itemHeight
                                            }
                                        },
                                        onDragEnd = { dragOffsetY = 0f },
                                        onDragCancel = { dragOffsetY = 0f }
                                    )
                                }
                        )
                        if (index != state.palette.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 84.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun EditorSwatchRow(
    color: PaletteColor,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.36f) else Color.Transparent
            )
            .clickable(onClick = onSelect)
            .heightIn(min = 76.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(color.color))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            Text(
                text = color.hexCode,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = AtelierData.guessColorName(color.color),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected harmony seed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Rounded.Visibility, "Inspect color", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Settings Screen
 */
@Composable
fun SettingsScreen(
    paletteTintEnabled: Boolean,
    onPaletteTintChange: (Boolean) -> Unit,
    reducedMotion: Boolean,
    onReducedMotionChange: (Boolean) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.statusBarsPadding().height(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            AtelierTag("Appearance")
            Spacer(modifier = Modifier.height(10.dp))
            SurfaceCard(padding = PaddingValues(0.dp)) {
                SettingsToggleRow(
                    "Palette accent",
                    paletteTintEnabled,
                    onPaletteTintChange
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFootnote("Tint selected controls with colors from the active palette.")
        }

        item {
            AtelierTag("Interaction")
            Spacer(modifier = Modifier.height(10.dp))
            SurfaceCard(padding = PaddingValues(0.dp)) {
                SettingsToggleRow("Reduce motion", reducedMotion, onReducedMotionChange)
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                )
                SettingsToggleRow("Haptics", hapticsEnabled, onHapticsChange)
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFootnote("Use quieter transitions or turn off tactile confirmation.")
        }

    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        AtelierToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsFootnote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * Color Picker Dialog
 */
@Composable
fun ColorPickerDialog(
    initialColor: PaletteColor,
    onDismiss: () -> Unit,
    onColorSelected: (PaletteColor) -> Unit
) {
    val hsvArray = FloatArray(3)
    android.graphics.Color.colorToHSV(initialColor.color, hsvArray)
    var h by remember { mutableFloatStateOf(hsvArray[0]) }
    var s by remember { mutableFloatStateOf(hsvArray[1]) }
    var v by remember { mutableFloatStateOf(hsvArray[2]) }

    val currentColor = Color.hsv(h, s, v)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AtelierTheme.colors.surfaceContainerLow
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Adjust Color",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentColor)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Hue", style = MaterialTheme.typography.labelSmall)
                Slider(value = h, onValueChange = { h = it }, valueRange = 0f..360f)

                Text("Saturation", style = MaterialTheme.typography.labelSmall)
                Slider(value = s, onValueChange = { s = it })

                Text("Value (Brightness)", style = MaterialTheme.typography.labelSmall)
                Slider(value = v, onValueChange = { v = it })

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cancel",
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PrimaryButton(
                        text = "Apply",
                        onClick = {
                            val intColor = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
                            val newHex = String.format("#%06X", 0xFFFFFF and intColor)
                            val r = android.graphics.Color.red(intColor)
                            val g = android.graphics.Color.green(intColor)
                            val b = android.graphics.Color.blue(intColor)
                            onColorSelected(
                                initialColor.copy(
                                    color = intColor,
                                    hexCode = newHex,
                                    red = r,
                                    green = g,
                                    blue = b
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Empty States & Overlays
 */
@Composable
private fun EmptyStudioState(
    tag: String,
    title: String,
    body: String,
    primaryActionText: String,
    onPrimaryAction: () -> Unit,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = AtelierTheme.colors.surfaceContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = tag, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        PrimaryButton(text = primaryActionText, onClick = onPrimaryAction, modifier = Modifier.fillMaxWidth())
        if (secondaryActionText != null && onSecondaryAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            SecondaryButton(text = secondaryActionText, onClick = onSecondaryAction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun OnboardingPermissionScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(84.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Capture colors around you",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Camera access lets Campalette sample a scene and build a reusable palette. Photos stay on this device.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(
            text = "Allow camera access",
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Library phone", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun PaletteLibraryScreenPreview() {
    val palette = AtelierData.defaultPalette()
    CampaletteTheme {
        PaletteLibraryScreen(
            state = LibraryState(
                palettes = listOf(
                    PaletteStudy("Forest Walk", palette, source = "Live camera"),
                    PaletteStudy("Desert Dawn", palette.reversed(), source = "Saved")
                ),
                searchQuery = "",
                selectedFilter = "all",
                selectedSort = "Recent",
                isGridView = false,
                favorites = emptySet()
            ),
            onSearchChange = {},
            onFilterChange = {},
            onToggleGrid = {},
            onPaletteSelect = {},
            onDeletePalette = {},
            onNavigate = {}
        )
    }
}

@Preview(name = "Editor phone", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun PaletteEditorScreenPreview() {
    CampaletteTheme {
        PaletteEditorScreen(
            state = EditorState(AtelierData.defaultPalette(), "Morning Atelier", "Live camera", HarmonyMode.Analogous, AtelierData.defaultPalette().first().hexCode),
            onNameChange = {},
            onHarmonyModeChange = {},
            onSelectColor = {},
            onInspectColor = {},
            onDeleteColor = {},
            onReorder = { _, _ -> },
            onSave = {},
            onShare = {},
            onNavigate = {}
        )
    }
}

@Preview(name = "Settings dark", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SettingsScreenPreview() {
    CampaletteTheme(darkTheme = true) {
        SettingsScreen(
            paletteTintEnabled = true,
            onPaletteTintChange = {},
            reducedMotion = false,
            onReducedMotionChange = {},
            hapticsEnabled = true,
            onHapticsChange = {}
        )
    }
}
