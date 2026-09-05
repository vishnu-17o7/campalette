package com.vishnu.campalette.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Remove
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.R
import com.vishnu.campalette.data.ThemeMode
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.DiscreteTickGate
import com.vishnu.campalette.ui.PaletteSizeSliderMath
import com.vishnu.campalette.ui.HarmonyMode
import com.vishnu.campalette.ui.components.AdaptiveLayout
import com.vishnu.campalette.ui.components.AppScreen
import com.vishnu.campalette.ui.components.CampaletteWindowSizeClass
import com.vishnu.campalette.ui.components.LocalWindowSizeClass
import com.vishnu.campalette.ui.components.AppleSearchField
import com.vishnu.campalette.ui.components.AtelierTag
import com.vishnu.campalette.ui.components.AtelierTextField
import com.vishnu.campalette.ui.components.AtelierToggle
import com.vishnu.campalette.ui.components.ColorSwatch
import com.vishnu.campalette.ui.components.PaletteListCard
import com.vishnu.campalette.ui.components.dockClearance
import com.vishnu.campalette.ui.components.PrimaryButton
import com.vishnu.campalette.ui.components.rememberPressScale
import com.vishnu.campalette.ui.components.SecondaryButton
import com.vishnu.campalette.ui.components.SegmentedControl
import com.vishnu.campalette.ui.components.SurfaceCard
import com.vishnu.campalette.ui.components.ValueLabel
import com.vishnu.campalette.ui.components.swipeToDismiss
import com.vishnu.campalette.ui.theme.AtelierTheme
import com.vishnu.campalette.ui.theme.CampaletteTheme
import com.vishnu.campalette.ui.theme.ExpressiveEffectsColorSpring
import com.vishnu.campalette.ui.theme.ExpressiveEffectsSpring
import com.vishnu.campalette.ui.theme.LocalReducedMotion
import kotlin.math.roundToInt
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
    onSavePalette: (PaletteStudy) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPalette by remember { mutableStateOf<PaletteStudy?>(null) }
    val gridRows = remember(state.palettes) { state.palettes.chunked(2) }
    val listBottomPadding = dockClearance()

    AdaptiveLayout(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) { isTablet ->
        Row(modifier = Modifier.fillMaxSize()) {
            // Main List Pane
            LazyColumn(
                modifier = Modifier
                    .then(
                        if (isTablet) {
                            Modifier.widthIn(min = 320.dp, max = 420.dp)
                        } else {
                            Modifier.weight(1f)
                        }
                    )
                    .fillMaxHeight()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = listBottomPadding)
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsPadding().height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.library_title),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        PressIconButton(onClick = onToggleGrid) {
                            Icon(
                                imageVector = if (state.isGridView) Icons.AutoMirrored.Rounded.List else Icons.Rounded.GridView,
                                contentDescription = stringResource(R.string.library_toggle_view),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Search and Filters
                item {
                    AppleSearchField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = stringResource(R.string.library_search_placeholder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SegmentedControl(
                        options = listOf(
                            "all" to stringResource(R.string.library_filter_all),
                            "camera" to stringResource(R.string.library_filter_camera),
                            "harmony" to stringResource(R.string.library_filter_harmony),
                            "saved" to stringResource(R.string.library_filter_saved)
                        ),
                        selectedKey = state.selectedFilter,
                        onSelected = onFilterChange
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Palette List
                if (state.palettes.isEmpty()) {
                    item {
                        EmptyStudioState(
                            tag = stringResource(R.string.palette_library_title),
                            title = if (state.searchQuery.isBlank()) {
                                stringResource(R.string.library_empty_title)
                            } else {
                                stringResource(R.string.library_empty_search)
                            },
                            body = if (state.searchQuery.isBlank()) {
                                stringResource(R.string.library_empty_body)
                            } else {
                                stringResource(R.string.library_empty_search_body)
                            },
                            primaryActionText = stringResource(R.string.library_capture_action),
                            onPrimaryAction = { onNavigate(AppScreen.Live) }
                        )
                    }
                } else {
                    if (state.isGridView) {
                        // Grid View (2 items per row)
                        itemsIndexed(
                            items = gridRows,
                            key = { _, rowItems -> rowItems.first().stableUiKey() }
                        ) { _, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { study ->
                                    var isDeleteRevealVisible by remember(study.stableUiKey()) {
                                        mutableStateOf(false)
                                    }
                                    val paletteName = study.name.ifEmpty {
                                        stringResource(R.string.library_unnamed_palette)
                                    }
                                    val deleteActionLabel = stringResource(
                                        R.string.accessibility_delete_palette,
                                        paletteName
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isDeleteRevealVisible) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                }
                                            )
                                    ) {
                                        PaletteListCard(
                                            title = paletteName,
                                            subtitle = pluralStringResource(
                                                R.plurals.library_color_count,
                                                study.colors.size,
                                                study.colors.size
                                            ),
                                            swatches = study.colors,
                                            onClick = {
                                                if (isTablet) selectedPalette = study else onPaletteSelect(study)
                                            },
                                            isFavorite = state.favorites.contains(study.name),
                                            shape = androidx.compose.ui.graphics.RectangleShape,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .semantics {
                                                    customActions = listOf(
                                                        CustomAccessibilityAction(deleteActionLabel) {
                                                            onDeletePalette(study)
                                                            true
                                                        }
                                                    )
                                                }
                                                .swipeToDismiss(
                                                    onDismiss = { onDeletePalette(study) },
                                                    onRevealChange = { isDeleteRevealVisible = it }
                                                )
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = state.palettes,
                            key = { _, study -> study.stableUiKey() }
                        ) { index, study ->
                            var isDeleteRevealVisible by remember(study.stableUiKey()) {
                                mutableStateOf(false)
                            }
                            val paletteName = study.name.ifEmpty {
                                stringResource(R.string.library_unnamed_palette)
                            }
                            val deleteActionLabel = stringResource(
                                R.string.accessibility_delete_palette,
                                paletteName
                            )
                            val rowShape = when {
                                state.palettes.size == 1 -> RoundedCornerShape(18.dp)
                                index == 0 -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                                index == state.palettes.lastIndex -> RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                                else -> RoundedCornerShape(0.dp)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(rowShape)
                                    .background(
                                        if (isDeleteRevealVisible) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                            ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .swipeToDismiss(
                                        onDismiss = { onDeletePalette(study) },
                                        onRevealChange = { isDeleteRevealVisible = it }
                                    ),
                                shape = androidx.compose.ui.graphics.RectangleShape,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column {
                                    NativePaletteRow(
                                        study = study,
                                        onClick = {
                                            if (isTablet) selectedPalette = study else onPaletteSelect(study)
                                        },
                                        isSaved = state.favorites.contains(study.name),
                                        onSave = { onSavePalette(study) },
                                        modifier = Modifier.semantics {
                                            customActions = listOf(
                                                CustomAccessibilityAction(deleteActionLabel) {
                                                    onDeletePalette(study)
                                                    true
                                                }
                                            )
                                        }
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
            }

            // Detail Pane (Tablet only)
            if (isTablet) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                Box(
                    modifier = Modifier.weight(1.2f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    selectedPalette?.let { selected ->
                        PaletteLibraryDetailPane(
                            study = selected,
                            onEdit = { onPaletteSelect(selected) },
                            isSaved = state.favorites.contains(selected.name),
                            onSave = { onSavePalette(selected) }
                        )
                    } ?: run {
                        Text(
                            text = stringResource(R.string.library_select_palette),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun PaletteStudy.stableUiKey(): String = buildString {
    append(capturedAt)
    append('|')
    append(source)
    append('|')
    append(name)
    colors.forEach { append('|').append(it.hexCode) }
}

@Composable
private fun rememberRowHighlight(
    interactionSource: MutableInteractionSource
): State<Color> {
    val isPressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = LocalReducedMotion.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val pressed = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.06f else 0.04f)
    return animateColorAsState(
        targetValue = if (isPressed) pressed else Color.Transparent,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsColorSpring,
        label = "Row highlight"
    )
}

@Composable
private fun PressIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interactionSource)
    IconButton(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun NativePaletteRow(
    study: PaletteStudy,
    onClick: () -> Unit,
    isSaved: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paletteName = study.name.ifEmpty { stringResource(R.string.library_unnamed_palette) }
    val swatchDescription = stringResource(
        R.string.accessibility_palette_colors,
        study.colors.take(6).joinToString(separator = ", ") { it.hexCode }
    )
    val saveDescription = stringResource(
        if (isSaved) {
            R.string.accessibility_save_palette_again
        } else {
            R.string.accessibility_save_palette
        },
        paletteName
    )
    val interactionSource = remember { MutableInteractionSource() }
    val highlight = rememberRowHighlight(interactionSource)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(highlight.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .heightIn(min = 88.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .size(width = 84.dp, height = 58.dp)
                .clip(RoundedCornerShape(12.dp))
                .semantics { contentDescription = swatchDescription }
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
                text = paletteName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${
                    pluralStringResource(
                        R.plurals.library_color_count,
                        study.colors.size,
                        study.colors.size
                    )
                } · ${study.source}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        PressIconButton(onClick = onSave, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = saveDescription,
                tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp)
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PaletteLibraryDetailPane(
    study: PaletteStudy,
    onEdit: () -> Unit,
    isSaved: Boolean,
    onSave: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val stats = remember(study.colors) { AtelierData.paletteStats(study.colors) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = study.name.ifEmpty { stringResource(R.string.library_unnamed_palette) },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.library_captured_via, study.source),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        SurfaceCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ValueLabel(stringResource(R.string.label_colors), "${study.colors.size}")
                ValueLabel(stringResource(R.string.label_warmth), stats.warmth)
                ValueLabel(
                    stringResource(R.string.label_contrast),
                    String.format(locale, "%.1f", stats.contrastRatio)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        AtelierTag(stringResource(R.string.label_swatches))
        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            gridItemsIndexed(study.colors, key = { _, color -> color.hexCode }) { _, color ->
                ColorSwatch(
                    color = Color(color.color),
                    selected = false,
                    onClick = {},
                    label = color.hexCode,
                    size = 56.dp,
                    entryDelay = 0
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton(
                text = if (isSaved) {
                    stringResource(R.string.library_filter_saved)
                } else {
                    stringResource(R.string.save_palette)
                },
                onClick = onSave,
                modifier = Modifier.weight(1f),
                icon = { Icon(if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, null) }
            )
            PrimaryButton(
                text = stringResource(R.string.library_edit_palette),
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Rounded.Edit, null) }
            )
        }
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
    onBack: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier,
    onReorderStart: () -> Unit = {},
    onReorderDrop: () -> Unit = {}
) {
    val locale = LocalConfiguration.current.locales[0]
    val navigationBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    if (state.palette.isEmpty()) {
        EmptyStudioState(
            tag = stringResource(R.string.editor),
            title = stringResource(R.string.editor_empty_title),
            body = stringResource(R.string.editor_empty_body),
            primaryActionText = stringResource(R.string.editor_open_camera),
            onPrimaryAction = { onNavigate(AppScreen.Live) },
            secondaryActionText = stringResource(R.string.editor_browse_library),
            onSecondaryAction = { onNavigate(AppScreen.History) }
        )
        return
    }

    val sizeClass = LocalWindowSizeClass.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
    LazyColumn(
        modifier = Modifier
            .widthIn(max = if (sizeClass == CampaletteWindowSizeClass.Compact) Dp.Unspecified else 720.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 32.dp + navigationBottomPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.statusBarsPadding().height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                PressIconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        stringResource(R.string.back_to_app),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.editor_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PressIconButton(onClick = onShare, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Rounded.Share,
                            stringResource(R.string.editor_share),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    val saveInteraction = remember { MutableInteractionSource() }
                    val saveScale = rememberPressScale(saveInteraction)
                    Text(
                        text = stringResource(R.string.editor_save),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = saveScale.value
                                scaleY = saveScale.value
                            }
                            .heightIn(min = 48.dp)
                            .clickable(
                                interactionSource = saveInteraction,
                                indication = LocalIndication.current,
                                onClick = onSave,
                                role = Role.Button
                            )
                            .padding(horizontal = 12.dp)
                            .wrapContentHeight()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            AtelierTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.editor_palette_name),
                placeholder = stringResource(R.string.editor_palette_name_hint)
            )
        }

        // Action Toolbar
        item {
            AtelierTag(stringResource(R.string.editor_harmony))
            Spacer(modifier = Modifier.height(8.dp))
            SegmentedControl(
                options = listOf(
                    HarmonyMode.Analogous.name to stringResource(R.string.harmony_analogous_short),
                    HarmonyMode.Complementary.name to stringResource(R.string.harmony_complement_short),
                    HarmonyMode.Tonal.name to stringResource(R.string.harmony_tonal_short)
                ),
                selectedKey = state.harmonyMode.name,
                onSelected = { key ->
                    HarmonyMode.entries.firstOrNull { it.name == key }?.let(onHarmonyModeChange)
                },
                itemRole = Role.RadioButton
            )
        }

        // Stats Card
        item {
            val stats = remember(state.palette) { AtelierData.paletteStats(state.palette) }
            SurfaceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ValueLabel(stringResource(R.string.label_colors), "${state.palette.size}")
                    ValueLabel(stringResource(R.string.label_warmth), stats.warmth)
                    ValueLabel(
                        stringResource(R.string.label_saturation),
                        String.format(locale, "%.0f%%", stats.avgSaturation)
                    )
                }
            }
        }

        item {
            SurfaceCard(
                modifier = Modifier.selectableGroup(),
                padding = PaddingValues(0.dp)
            ) {
                state.palette.forEachIndexed { index, color ->
                    key(color.hexCode) {
                        var isDeleteRevealVisible by remember(color.hexCode) { mutableStateOf(false) }
                        var dragOffsetY by remember { mutableFloatStateOf(0f) }
                        var rowHeightPx by remember { mutableFloatStateOf(0f) }
                        var isDragging by remember { mutableStateOf(false) }
                        val indexState = rememberUpdatedState(index)
                        val lastIndexState = rememberUpdatedState(state.palette.lastIndex)
                        val onReorderState = rememberUpdatedState(onReorder)
                        val onReorderStartState = rememberUpdatedState(onReorderStart)
                        val onReorderDropState = rememberUpdatedState(onReorderDrop)
                        val deleteActionLabel = stringResource(
                            R.string.accessibility_delete_color,
                            color.hexCode
                        )
                        val moveUpActionLabel = stringResource(
                            R.string.accessibility_move_color_up,
                            color.hexCode
                        )
                        val moveDownActionLabel = stringResource(
                            R.string.accessibility_move_color_down,
                            color.hexCode
                        )
                        val accessibilityActions = buildList {
                            add(
                                CustomAccessibilityAction(deleteActionLabel) {
                                    onDeleteColor(color)
                                    true
                                }
                            )
                            if (index > 0) {
                                add(
                                    CustomAccessibilityAction(moveUpActionLabel) {
                                        onReorder(index, index - 1)
                                        true
                                    }
                                )
                            }
                            if (index < state.palette.lastIndex) {
                                add(
                                    CustomAccessibilityAction(moveDownActionLabel) {
                                        onReorder(index, index + 1)
                                        true
                                    }
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .background(
                                    if (isDeleteRevealVisible) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                        ) {
                        EditorSwatchRow(
                            color = color,
                            selected = state.selectedHex == color.hexCode,
                            onSelect = { onSelectColor(color) },
                            onEdit = { onInspectColor(color) },
                            modifier = Modifier
                                .semantics { customActions = accessibilityActions }
                                .onSizeChanged { rowHeightPx = it.height.toFloat() }
                                .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                                .swipeToDismiss(
                                    onDismiss = { onDeleteColor(color) },
                                    onRevealChange = { isDeleteRevealVisible = it }
                                )
                                .pointerInput(color.hexCode) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            isDragging = true
                                            onReorderStartState.value()
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y
                                            val itemHeight = size.height.toFloat().takeIf { it > 0f }
                                                ?: rowHeightPx
                                            if (itemHeight > 0f) {
                                                val fromIndex = indexState.value
                                                val targetIndex = (fromIndex + (dragOffsetY / itemHeight).roundToInt())
                                                    .coerceIn(0, lastIndexState.value)
                                                if (targetIndex != fromIndex) {
                                                    onReorderState.value(fromIndex, targetIndex)
                                                    dragOffsetY -= (targetIndex - fromIndex) * itemHeight
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            dragOffsetY = 0f
                                            isDragging = false
                                            onReorderDropState.value()
                                        },
                                        onDragCancel = {
                                            dragOffsetY = 0f
                                            isDragging = false
                                            onReorderDropState.value()
                                        }
                                    )
                                }
                        )
                        }
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
}

@Composable
private fun EditorSwatchRow(
    color: PaletteColor,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inspectDescription = stringResource(R.string.inspect_color, color.hexCode)
    val colorName = remember(color.color) { AtelierData.guessColorName(color.color) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressScale = rememberPressScale(interactionSource)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale.value
                scaleY = pressScale.value
            }
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                        .copy(alpha = 0.36f)
                        .compositeOver(MaterialTheme.colorScheme.surface)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onSelect,
                role = Role.RadioButton
            )
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
                text = colorName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        PressIconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.Rounded.Visibility,
                inspectDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Settings Screen
 */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    paletteTintEnabled: Boolean,
    onPaletteTintChange: (Boolean) -> Unit,
    reducedMotion: Boolean,
    onReducedMotionChange: (Boolean) -> Unit,
    paletteColorCount: Int,
    onPaletteColorCountChange: (Int) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    versionName: String,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
    LazyColumn(
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = dockClearance()),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            Spacer(modifier = Modifier.statusBarsPadding().height(12.dp))
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            SettingsSectionHeader(stringResource(R.string.settings_appearance))
            SurfaceCard(padding = PaddingValues(0.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SegmentedControl(
                        options = listOf(
                            ThemeMode.System.storageValue to stringResource(R.string.settings_theme_system),
                            ThemeMode.Light.storageValue to stringResource(R.string.settings_theme_light),
                            ThemeMode.Dark.storageValue to stringResource(R.string.settings_theme_dark)
                        ),
                        selectedKey = themeMode.storageValue,
                        onSelected = { onThemeModeChange(ThemeMode.fromStorageValue(it)) },
                        itemRole = Role.RadioButton
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                )
                SettingsToggleRow(
                    stringResource(R.string.settings_palette_accent),
                    paletteTintEnabled,
                    onPaletteTintChange
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFootnote(stringResource(R.string.settings_appearance_description))
        }

        item {
            SettingsSectionHeader(stringResource(R.string.settings_capture))
            SurfaceCard(padding = PaddingValues(0.dp)) {
                SettingsPaletteSizeSlider(
                    count = paletteColorCount,
                    onCountChange = onPaletteColorCountChange
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFootnote(stringResource(R.string.settings_palette_size_description))
        }

        item {
            SettingsSectionHeader(stringResource(R.string.settings_interaction))
            SurfaceCard(padding = PaddingValues(0.dp)) {
                SettingsToggleRow(
                    stringResource(R.string.settings_reduced_motion),
                    reducedMotion,
                    onReducedMotionChange
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                )
                SettingsToggleRow(
                    stringResource(R.string.settings_haptics),
                    hapticsEnabled,
                    onHapticsChange
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFootnote(stringResource(R.string.settings_interaction_description))
        }

        item {
            SettingsSectionHeader(stringResource(R.string.settings_section_about))
            SurfaceCard(padding = PaddingValues(0.dp)) {
                SettingsValueRow(
                    title = stringResource(R.string.settings_about_version, versionName)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                )
                SettingsLinkRow(
                    title = stringResource(R.string.settings_privacy_policy),
                    onClick = onOpenPrivacyPolicy
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
                )
                SettingsLinkRow(
                    title = stringResource(R.string.settings_terms),
                    onClick = onOpenTerms
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SettingsFootnote(stringResource(R.string.settings_about_credits))
        }

    }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.15.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .semantics { heading() }
    )
}

@Composable
private fun SettingsValueRow(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingsLinkRow(
    title: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val highlight = rememberRowHighlight(interactionSource)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlight.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .heightIn(min = 54.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val highlight = rememberRowHighlight(interactionSource)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlight.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
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
        AtelierToggle(
            checked = checked,
            label = title,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsPaletteSizeSlider(
    count: Int,
    onCountChange: (Int) -> Unit
) {
    val min = AtelierData.MIN_PALETTE_COLOR_COUNT
    val max = AtelierData.MAX_PALETTE_COLOR_COUNT
    val range = (max - min).toFloat()
    val reducedMotion = LocalReducedMotion.current
    val density = LocalDensity.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var dragging by remember { mutableStateOf(false) }
    var rawValue by remember { mutableFloatStateOf(count.toFloat()) }
    val latestCount = rememberUpdatedState(count)
    val latestOnCountChange = rememberUpdatedState(onCountChange)
    val tickGate = remember { DiscreteTickGate(count) }
    SideEffect {
        if (!dragging && tickGate.last != count) {
            tickGate.reset(count)
        }
    }
    val displayCount = if (dragging) {
        rawValue.roundToInt().coerceIn(min, max)
    } else {
        count
    }
    val targetFraction = ((if (dragging) rawValue else count.toFloat()) - min) / range
    val thumbFraction by animateFloatAsState(
        targetValue = targetFraction.coerceIn(0f, 1f),
        animationSpec = if (dragging || reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Palette size thumb"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (!reducedMotion && dragging) 1.08f else 1f,
        animationSpec = if (reducedMotion) snap() else ExpressiveEffectsSpring,
        label = "Palette size press"
    )
    val valueLabel = stringResource(R.string.settings_palette_size_value, displayCount)
    val trackFill = if (isDark) Color.White.copy(alpha = 0.78f) else Color(0xFF007AFF)
    val trackRest = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
    val thumbFill = if (isDark) Color(0xFFF4F4F7) else Color.White

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_palette_size),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .semantics {
                    contentDescription = valueLabel
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = (displayCount - min).toFloat(),
                        range = 0f..range,
                        steps = max - min - 1
                    )
                    setProgress { value ->
                        val next = PaletteSizeSliderMath.tick(min + value * range, min, max)
                        tickGate.offer(next)?.let { latestOnCountChange.value(it) }
                        true
                    }
                }
                .pointerInput(min, max) {
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    val thumbRadius = with(density) { 14.dp.toPx() }
                    fun emitTick(raw: Float) {
                        tickGate.offerRaw(raw, min, max)?.let { latestOnCountChange.value(it) }
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        dragging = true
                        val startCount = latestCount.value
                        tickGate.reset(startCount)
                        val thumbX = PaletteSizeSliderMath.thumbCenterX(
                            startCount, min, max, width, thumbRadius
                        )
                        val grabbingThumb = PaletteSizeSliderMath.isThumbGrab(
                            down.position.x, thumbX, thumbRadius
                        )
                        val grabOffsetX = if (grabbingThumb) down.position.x - thumbX else 0f
                        rawValue = if (grabbingThumb) {
                            startCount.toFloat()
                        } else {
                            PaletteSizeSliderMath.rawFromX(
                                down.position.x, width, thumbRadius, min, max
                            )
                        }
                        if (!grabbingThumb) emitTick(rawValue)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                change.consume()
                                break
                            }
                            val moved = change.positionChanged()
                            change.consume()
                            if (!moved) continue
                            rawValue = PaletteSizeSliderMath.rawFromX(
                                change.position.x - grabOffsetX,
                                width,
                                thumbRadius,
                                min,
                                max
                            )
                            emitTick(rawValue)
                        }
                        dragging = false
                    }
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val thumbRadiusPx = with(density) { 14.dp.toPx() }
            val inner = (constraints.maxWidth.toFloat() - thumbRadiusPx * 2f).coerceAtLeast(1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(trackRest)
                    .border(0.5.dp, Color.White.copy(alpha = if (isDark) 0.16f else 0.40f), RoundedCornerShape(999.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(thumbFraction.coerceAtLeast(0.04f))
                    .height(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(trackFill)
            )
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (thumbRadiusPx + thumbFraction * inner - thumbRadiusPx).roundToInt(),
                            y = 0
                        )
                    }
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                    }
                    .shadow(10.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(thumbFill)
                    .border(0.5.dp, Color.White.copy(alpha = if (isDark) 0.55f else 0.85f), CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = min.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = max.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    val initialHsv = remember(initialColor.color) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.color, it) }
    }
    var h by remember { mutableFloatStateOf(initialHsv[0]) }
    var s by remember { mutableFloatStateOf(initialHsv[1]) }
    var v by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentColor = Color.hsv(h, s, v)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = AtelierTheme.colors.surfaceContainerLow
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.editor_color_picker_title),
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

                Text(stringResource(R.string.color_picker_hue), style = MaterialTheme.typography.labelSmall)
                Slider(value = h, onValueChange = { h = it }, valueRange = 0f..360f)

                Text(
                    stringResource(R.string.color_picker_saturation),
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(value = s, onValueChange = { s = it })

                Text(
                    stringResource(R.string.color_picker_value),
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(value = v, onValueChange = { v = it })

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cancelInteraction = remember { MutableInteractionSource() }
                    val cancelScale = rememberPressScale(cancelInteraction)
                    Text(
                        text = stringResource(R.string.color_picker_cancel),
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = cancelScale.value
                                scaleY = cancelScale.value
                            }
                            .clickable(
                                interactionSource = cancelInteraction,
                                indication = LocalIndication.current,
                                onClick = onDismiss,
                                role = Role.Button
                            )
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PrimaryButton(
                        text = stringResource(R.string.color_picker_apply),
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
    onImportGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val compactHeight = LocalConfiguration.current.screenHeightDp < 480
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(if (compactHeight) Modifier.statusBarsPadding() else Modifier)
            .padding(
                horizontal = 32.dp,
                vertical = if (compactHeight) 8.dp else 32.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(if (compactHeight) 48.dp else 84.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(if (compactHeight) 24.dp else 38.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 28.dp))
        Text(
            text = stringResource(R.string.permission_title),
            style = if (compactHeight) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.headlineLarge
            },
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(if (compactHeight) 6.dp else 16.dp))
        Text(
            text = stringResource(R.string.permission_body),
            style = if (compactHeight) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(if (compactHeight) 12.dp else 32.dp))
        PrimaryButton(
            text = stringResource(R.string.permission_allow_camera),
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))
        SecondaryButton(
            text = stringResource(R.string.permission_import_gallery),
            onClick = onImportGallery,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Library tablet", showBackground = true, widthDp = 840, heightDp = 1280)
@Composable
private fun PaletteLibraryScreenTabletPreview() {
    PaletteLibraryScreenPreview()
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
            onSavePalette = {},
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
            onBack = {},
            onNavigate = {}
        )
    }
}

@Preview(name = "Settings dark", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SettingsScreenPreview() {
    CampaletteTheme(darkTheme = true) {
        SettingsScreen(
            themeMode = ThemeMode.Dark,
            onThemeModeChange = {},
            paletteTintEnabled = true,
            onPaletteTintChange = {},
            reducedMotion = false,
            onReducedMotionChange = {},
            paletteColorCount = AtelierData.DEFAULT_PALETTE_COLOR_COUNT,
            onPaletteColorCountChange = {},
            hapticsEnabled = true,
            onHapticsChange = {},
            versionName = "1.0.0",
            onOpenPrivacyPolicy = {},
            onOpenTerms = {}
        )
    }
}
