package com.primoz.swissborgandroidchallenge.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)

val ShapesTopOnly = Shapes(
    small = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
    medium = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    large = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
)
