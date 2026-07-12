package com.vishnu.campalette.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.theme.AtelierTheme

@Composable
fun ShareExportSheet(
    modifier: Modifier = Modifier,
    palette: List<PaletteColor>,
    paletteName: String,
    onShareImage: () -> Unit,
    onCopyAllHex: () -> Unit,
    onExportCss: () -> Unit,
    onExportSwift: () -> Unit,
    onExportAndroid: () -> Unit,
    onExportFigma: () -> Unit,
    onClose: () -> Unit
) {
    val sheetInteractionSource = remember { MutableInteractionSource() }
    val resolvedPaletteName = paletteName.ifBlank { stringResource(R.string.current_palette) }
    val formats = listOf(
        ExportFormat(
            label = stringResource(R.string.export_format_css),
            format = "CSS",
            onClick = onExportCss
        ),
        ExportFormat(
            label = stringResource(R.string.export_format_swift),
            format = "Swift",
            onClick = onExportSwift
        ),
        ExportFormat(
            label = stringResource(R.string.export_format_android),
            format = "XML",
            onClick = onExportAndroid
        ),
        ExportFormat(
            label = stringResource(R.string.export_format_figma),
            format = "JSON",
            onClick = onExportFigma
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.38f))
            .clickable(role = Role.Button, onClickLabel = "Close export sheet", onClick = onClose)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 18.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                ExportSheetDragHandle()
                ExportSheetHeader(
                    title = "Export palette",
                    subtitle = resolvedPaletteName,
                    onDone = onClose
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        top = 10.dp,
                        end = 16.dp,
                        bottom = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        PalettePreviewStrip(palette = palette)
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = AtelierTheme.colors.surfaceContainerLow
                        ) {
                            Column {
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

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Copy for",
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

                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
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
