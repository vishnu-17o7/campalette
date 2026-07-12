package com.vishnu.campalette.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.components.PrimaryButton
import com.vishnu.campalette.ui.components.SecondaryButton
import com.vishnu.campalette.ui.theme.AtelierTheme

@Composable
fun GalleryImportScreen(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    onConfirm: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    var rotationCount by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.inverseSurface)) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.gallery_picker_title),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotation)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 48.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.82f))
                .clickable(onClick = onCancel)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = stringResource(R.string.back_to_app),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 48.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(AtelierTheme.colors.surfaceContainerLowest.copy(alpha = 0.82f))
                .clickable {
                    rotationCount = (rotationCount + 1) % 4
                    rotation = rotationCount * 90f
                }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.RotateRight,
                contentDescription = stringResource(R.string.gallery_crop_rotate),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.gallery_picker_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
            PrimaryButton(
                text = stringResource(R.string.gallery_confirm),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val rotated = if (rotationCount % 4 != 0) {
                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(rotation)
                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    } else bitmap
                    onConfirm(rotated)
                }
            )
            SecondaryButton(
                text = stringResource(R.string.back_to_app),
                modifier = Modifier.fillMaxWidth(),
                onClick = onCancel
            )
        }
    }
}
