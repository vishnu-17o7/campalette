package com.vishnu.campalette.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AtelierRoundedDefault = 10.dp
val AtelierRoundedLarge = 16.dp
val AtelierRoundedExtra = 20.dp
val AtelierRoundedFull = 9999.dp

val Shapes = Shapes(
    small = RoundedCornerShape(AtelierRoundedDefault),
    medium = RoundedCornerShape(AtelierRoundedLarge),
    large = RoundedCornerShape(AtelierRoundedExtra),
    extraLarge = RoundedCornerShape(AtelierRoundedExtra)
)
