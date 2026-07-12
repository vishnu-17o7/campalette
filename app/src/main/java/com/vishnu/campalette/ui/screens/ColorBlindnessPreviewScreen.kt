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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vishnu.campalette.PaletteColor
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.AtelierData
import com.vishnu.campalette.ui.theme.AtelierTheme
import java.util.Locale

@Composable
fun ColorBlindnessPreviewScreen(
    modifier: Modifier = Modifier,
    paletteColor: PaletteColor,
    initialType: AtelierData.ColorBlindnessType,
    onTypeChanged: (AtelierData.ColorBlindnessType) -> Unit,
    onClose: () -> Unit
) {
    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    val types = listOf(
        AtelierData.ColorBlindnessType.Protanopia to stringResource(R.string.colorblind_protanopia),
        AtelierData.ColorBlindnessType.Deuteranopia to stringResource(R.string.colorblind_deuteranopia),
        AtelierData.ColorBlindnessType.Tritanopia to stringResource(R.string.colorblind_tritanopia)
    )
    val simulatedColor = remember(selectedType, paletteColor.color) {
        AtelierData.applyColorBlindness(paletteColor.color, selectedType)
    }
    val selectedLabel = types.first { it.first == selectedType }.second
    val simulatedHex = remember(simulatedColor) {
        String.format(Locale.US, "#%06X", 0xFFFFFF and simulatedColor)
    }
    val sheetInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.38f))
            .clickable(role = Role.Button, onClickLabel = "Close preview", onClick = onClose)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clickable(
                    interactionSource = sheetInteractionSource,
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                SheetDragHandle()
                SheetHeader(
                    title = stringResource(R.string.colorblind_title),
                    subtitle = paletteColor.name,
                    onDone = onClose
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                                label = "Original",
                                hex = paletteColor.hexCode,
                                color = Color(paletteColor.color)
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
                                color = Color(simulatedColor)
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
    }
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
    Column(
        modifier = modifier
            .semantics { contentDescription = "$label, $hex" }
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
