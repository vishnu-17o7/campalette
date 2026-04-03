package com.vishnu.campalette.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.PaletteStudy
import com.vishnu.campalette.R
import com.vishnu.campalette.ToggleRowState
import com.vishnu.campalette.ui.components.AtelierLabelTag
import com.vishnu.campalette.ui.components.AtelierSwatch
import com.vishnu.campalette.ui.components.EditorialInputField
import com.vishnu.campalette.ui.components.GradientPrimaryButton
import com.vishnu.campalette.ui.components.PaletteCard
import com.vishnu.campalette.ui.components.SoftActionButton
import com.vishnu.campalette.ui.theme.AtelierPrimaryContainer
import com.vishnu.campalette.ui.theme.AtelierTheme

@Composable
fun PaletteLibraryScreen(
    modifier: Modifier,
    palettes: List<PaletteStudy>,
    onOpenCamera: () -> Unit,
    onOpenBuilder: () -> Unit,
    onPaletteSelected: (PaletteStudy) -> Unit
) {
    val featured = palettes.firstOrNull()
    val archive = palettes.drop(1)

    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        item {
            if (palettes.isEmpty()) {
                EmptyStudioState(
                    tag = stringResource(R.string.palette_library_tag),
                    title = stringResource(R.string.palette_library_empty_title),
                    body = stringResource(R.string.palette_library_empty_body),
                    primaryText = stringResource(R.string.palette_library_empty_primary),
                    secondaryText = stringResource(R.string.palette_library_empty_secondary),
                    onPrimaryClick = onOpenCamera,
                    onSecondaryClick = onOpenBuilder
                )
            } else {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AtelierLabelTag(text = stringResource(R.string.palette_library_tag))
                    Text(
                        text = stringResource(R.string.palette_library_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.palette_library_body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        featured?.let { study ->
            item {
                PaletteCard(
                    title = study.name,
                    subtitle = study.note.ifBlank {
                        stringResource(
                            R.string.palette_card_meta,
                            stringResource(R.string.palette_card_colors, study.colors.size),
                            study.source.ifBlank { stringResource(R.string.current_palette_source) }
                        )
                    },
                    swatches = study.colors,
                    tag = stringResource(R.string.palette_featured_tag),
                    featured = true,
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    onClick = { onPaletteSelected(study) }
                )
            }
        }
        if (archive.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AtelierLabelTag(text = stringResource(R.string.palette_archive_tag))
                    Text(
                        text = stringResource(R.string.saved_palettes),
                        style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            itemsIndexed(archive) { index, palette ->
                val staggerPadding = if (index % 2 == 0) 24.dp else 40.dp
                PaletteCard(
                    title = palette.name,
                    subtitle = listOf(
                        palette.source.ifBlank { stringResource(R.string.current_palette_source) },
                        palette.capturedAt.ifBlank { stringResource(R.string.palette_card_colors, palette.colors.size) }
                    ).joinToString(" · "),
                    swatches = palette.colors,
                    modifier = Modifier.padding(start = staggerPadding, end = 16.dp),
                    onClick = { onPaletteSelected(palette) }
                )
            }
        }
    }
}

@Composable
fun PaletteHistoryScreen(
    modifier: Modifier,
    palettes: List<PaletteStudy>,
    onOpenCamera: () -> Unit,
    onPaletteSelected: (PaletteStudy) -> Unit
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AtelierLabelTag(text = stringResource(R.string.history))
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (palettes.isEmpty()) stringResource(R.string.history_empty) else stringResource(R.string.history_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (palettes.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SoftActionButton(
                        text = stringResource(R.string.palette_library_empty_primary),
                        onClick = onOpenCamera
                    )
                }
            }
        }
        itemsIndexed(palettes) { index, palette ->
            val tag = if (index == 0) {
                stringResource(R.string.history_section_latest)
            } else {
                stringResource(R.string.history_section_archive)
            }
            PaletteCard(
                title = palette.name,
                subtitle = listOf(palette.source, palette.capturedAt).filter { it.isNotBlank() }.joinToString(" · "),
                swatches = palette.colors,
                tag = tag,
                featured = index == 0,
                modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                onClick = { onPaletteSelected(palette) }
            )
        }
    }
}

@Composable
fun PaletteEditorScreen(
    modifier: Modifier,
    paletteName: String,
    paletteSource: String,
    palette: List<PaletteColor>,
    selectedHex: String?,
    harmonyLabel: String,
    onPaletteNameChange: (String) -> Unit,
    onColorSelected: (PaletteColor) -> Unit,
    onInspectSelected: () -> Unit,
    onAnalyzeHarmony: () -> Unit,
    onSavePalette: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    val analyzeHarmonyLabel = stringResource(R.string.editor_harmony_chip)
    val inspectLabel = stringResource(R.string.editor_inspect)
    val isCompact = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 390 ||
        androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.1f
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            if (palette.isEmpty()) {
                EmptyStudioState(
                    tag = stringResource(R.string.builder_tag),
                    title = stringResource(R.string.builder_empty_title),
                    body = stringResource(R.string.builder_empty_body),
                    primaryText = stringResource(R.string.builder_empty_primary),
                    secondaryText = stringResource(R.string.builder_empty_secondary),
                    onPrimaryClick = onOpenCamera,
                    onSecondaryClick = onOpenLibrary
                )
            } else {
                Column(
                    modifier = Modifier.padding(start = 24.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    AtelierLabelTag(text = stringResource(R.string.builder_tag))
                    if (paletteSource.isNotBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AtelierLabelTag(text = stringResource(R.string.builder_source_label))
                            Text(
                                text = paletteSource,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    EditorialInputField(
                        value = paletteName,
                        label = stringResource(R.string.editor_name_label),
                        placeholder = stringResource(R.string.editor_name_placeholder),
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = onPaletteNameChange
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(AtelierPrimaryContainer)
                            .semantics {
                                role = Role.Button
                                contentDescription = analyzeHarmonyLabel
                            }
                            .clickable(onClick = onAnalyzeHarmony)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = analyzeHarmonyLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AtelierLabelTag(text = stringResource(R.string.editor_harmony_result))
                        Text(
                            text = harmonyLabel,
                            style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(AtelierTheme.colors.surfaceContainerLow)
                            .semantics {
                                role = Role.Button
                                contentDescription = inspectLabel
                            }
                            .clickable(onClick = onInspectSelected)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = inspectLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        itemsIndexed(palette) { index, swatch ->
            EditorSwatchCard(
                paletteColor = swatch,
                selected = selectedHex == swatch.hexCode,
                tall = if (isCompact) false else index % 2 == 0,
                compact = isCompact,
                modifier = Modifier.padding(
                    start = if (isCompact || index % 2 == 0) 24.dp else 44.dp,
                    end = 16.dp
                ),
                onClick = { onColorSelected(swatch) }
            )
        }
        if (palette.isNotEmpty()) {
            item {
                GradientPrimaryButton(
                    text = stringResource(R.string.editor_save_palette),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    onClick = onSavePalette
                )
            }
        }
    }
}

@Composable
private fun EditorSwatchCard(
    paletteColor: PaletteColor,
    selected: Boolean,
    tall: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(AtelierTheme.colors.surfaceContainerLowest)
            .semantics {
                role = Role.Button
                contentDescription = paletteColor.name
            }
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    when {
                        compact -> 132.dp
                        tall -> 188.dp
                        else -> 140.dp
                    }
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color(paletteColor.color)),
            contentAlignment = Alignment.TopStart
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AtelierTheme.colors.primaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        AtelierSwatch(
            color = Color(paletteColor.color),
            label = paletteColor.hexCode,
            selected = selected,
            onClick = onClick
        )
        Text(
            text = paletteColor.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyStudioState(
    tag: String,
    title: String,
    body: String,
    primaryText: String,
    secondaryText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AtelierLabelTag(text = tag)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GradientPrimaryButton(
                text = primaryText,
                modifier = Modifier.fillMaxWidth(),
                onClick = onPrimaryClick
            )
            SoftActionButton(
                text = secondaryText,
                modifier = Modifier.fillMaxWidth(),
                onClick = onSecondaryClick
            )
        }
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier,
    keepReticle: Boolean,
    flashPreferred: Boolean,
    flashAvailable: Boolean,
    hapticsEnabled: Boolean,
    onReticleChanged: (Boolean) -> Unit,
    onFlashChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier.background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(start = 24.dp, end = 16.dp, top = 32.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AtelierLabelTag(text = stringResource(R.string.settings_tag))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic)
                )
                Text(
                    text = stringResource(R.string.settings_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SettingsSection(
                title = stringResource(R.string.settings_section_capture),
                rows = listOf(
                    ToggleRowState(stringResource(R.string.settings_capture_grid), checked = keepReticle, onCheckedChange = onReticleChanged),
                    ToggleRowState(
                        stringResource(R.string.settings_capture_flash),
                        enabled = flashAvailable,
                        checked = flashPreferred,
                        onCheckedChange = onFlashChanged
                    ),
                    ToggleRowState(stringResource(R.string.settings_capture_haptics), checked = hapticsEnabled, onCheckedChange = onHapticsChanged)
                ),
                footer = if (flashAvailable) null else stringResource(R.string.settings_capture_flash_unavailable)
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AtelierLabelTag(text = stringResource(R.string.settings_section_about))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(AtelierTheme.colors.surfaceContainerLow)
                        .padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_about_version),
                        style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, rows: List<ToggleRowState>, footer: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AtelierLabelTag(text = title)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(AtelierTheme.colors.surfaceContainerLow)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AtelierTheme.colors.surfaceContainerLowest)
                        .semantics {
                            if (!row.enabled) disabled()
                        }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (row.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = row.checked,
                        enabled = row.enabled,
                        onCheckedChange = row.onCheckedChange
                    )
                }
            }
        }
        footer?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun OnboardingPermissionScreen(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    val permissionLabel = stringResource(R.string.permission_cta)
    Column(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, AtelierPrimaryContainer)
                )
            )
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AtelierLabelTag(text = stringResource(R.string.camera_permission_tag), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.displayLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = stringResource(R.string.atelier_tagline),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                text = stringResource(R.string.onboarding_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(48.dp))
                    .background(AtelierTheme.colors.surfaceContainerLowest)
                    .semantics {
                        role = Role.Button
                        contentDescription = permissionLabel
                    }
                    .clickable(onClick = onRequestPermission)
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = permissionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
