package com.vishnu.campalette.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vishnu.campalette.R
import com.vishnu.campalette.ui.components.AtelierTag
import com.vishnu.campalette.ui.components.PrimaryButton
import com.vishnu.campalette.ui.components.SecondaryButton
import com.vishnu.campalette.ui.theme.AtelierPrimaryContainer

@Composable
fun OnboardingFlowScreen(
    modifier: Modifier = Modifier,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPageData(
            icon = Icons.Rounded.CameraAlt,
            tagRes = R.string.camera_permission_tag,
            titleRes = R.string.onboarding_page1_title,
            bodyRes = R.string.onboarding_page1_body
        ),
        OnboardingPageData(
            icon = Icons.Rounded.AutoAwesome,
            tagRes = R.string.builder_tag,
            titleRes = R.string.onboarding_page2_title,
            bodyRes = R.string.onboarding_page2_body
        ),
        OnboardingPageData(
            icon = Icons.Rounded.Palette,
            tagRes = R.string.palette_library_tag,
            titleRes = R.string.onboarding_page3_title,
            bodyRes = R.string.onboarding_page3_body
        )
    )

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, AtelierPrimaryContainer)
            )
        )
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(250)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(200)))
            },
            label = "onboardingPage"
        ) { currentPage ->
            val data = pages[currentPage]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        when (currentPage) {
                            0 -> ScanningAnimation(modifier = Modifier.fillMaxSize())
                            1 -> TappingAnimation(modifier = Modifier.fillMaxSize())
                            2 -> SharingAnimation(modifier = Modifier.fillMaxSize())
                            else -> Icon(
                                imageVector = data.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    AtelierTag(
                        text = stringResource(data.tagRes),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(data.titleRes),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(data.bodyRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Page indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pages.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentPage) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == currentPage)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    if (currentPage == pages.size - 1) {
                        PrimaryButton(
                            text = stringResource(R.string.onboarding_finish),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onComplete
                        )
                    } else {
                        PrimaryButton(
                            text = stringResource(R.string.onboarding_next),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { page = currentPage + 1 }
                        )
                    }
                    SecondaryButton(
                        text = stringResource(R.string.onboarding_skip),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onSkip
                    )
                }
            }
        }
    }
}

private data class OnboardingPageData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tagRes: Int,
    val titleRes: Int,
    val bodyRes: Int
)

@Composable
fun ScanningAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanningAnimation")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val sweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 3f * pulse
        drawCircle(
            color = onPrimaryColor.copy(alpha = 0.2f),
            radius = radius
        )
        drawCircle(
            color = onPrimaryColor,
            radius = radius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        val lineY = size.height * sweep
        drawLine(
            color = onPrimaryColor.copy(alpha = 0.7f),
            start = androidx.compose.ui.geometry.Offset(size.width / 2f - radius, lineY),
            end = androidx.compose.ui.geometry.Offset(size.width / 2f + radius, lineY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun TappingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "tappingAnimation")
    val tapProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tapProgress"
    )
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radiusMax = size.minDimension / 2.2f
        val coreRadius = 14.dp.toPx() * (1f - 0.1f * tapProgress)
        drawCircle(
            color = onPrimaryColor,
            radius = coreRadius,
            center = center
        )
        drawCircle(
            color = onPrimaryColor.copy(alpha = 1f - tapProgress),
            radius = coreRadius + (radiusMax - coreRadius) * tapProgress,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun SharingAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sharingAnimation")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowProgress"
    )
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2.2f)
        val nodeRadius = 10.dp.toPx()
        val leftNode = androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.7f)
        val rightNode = androidx.compose.ui.geometry.Offset(size.width * 0.75f, size.height * 0.7f)

        drawLine(color = onPrimaryColor.copy(alpha = 0.4f), start = center, end = leftNode, strokeWidth = 2.dp.toPx())
        drawLine(color = onPrimaryColor.copy(alpha = 0.4f), start = center, end = rightNode, strokeWidth = 2.dp.toPx())

        drawCircle(color = onPrimaryColor, radius = nodeRadius, center = center)
        drawCircle(color = onPrimaryColor, radius = nodeRadius, center = leftNode)
        drawCircle(color = onPrimaryColor, radius = nodeRadius, center = rightNode)

        val particle1 = center + (leftNode - center) * flowProgress
        val particle2 = center + (rightNode - center) * flowProgress
        drawCircle(color = onPrimaryColor, radius = 4.dp.toPx(), center = particle1)
        drawCircle(color = onPrimaryColor, radius = 4.dp.toPx(), center = particle2)
    }
}
