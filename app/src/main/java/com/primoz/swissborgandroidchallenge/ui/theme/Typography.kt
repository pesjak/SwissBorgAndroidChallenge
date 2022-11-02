package com.primoz.swissborgandroidchallenge.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.primoz.swissborgandroidchallenge.R

private val light = Font(R.font.roboto_light, FontWeight.W300)
private val regular = Font(R.font.roboto_regular, FontWeight.W400)
private val medium = Font(R.font.roboto_medium, FontWeight.W500)
private val bold = Font(R.font.roboto_bold, FontWeight.W800)

private val robotoFontFamily = FontFamily(fonts = listOf(light, regular, medium, bold))

// Set of Material typography styles to start with
val Typography = Typography(
    h1 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 96.sp,
        fontWeight = FontWeight.Light,
    ),
    h2 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 60.sp,
        fontWeight = FontWeight.Light,
    ),
    h3 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 48.sp,
        fontWeight = FontWeight.Normal,
    ),
    h4 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 30.sp,
        fontWeight = FontWeight.Normal,
    ),
    h5 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal,
    ),
    h6 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
    ),
    subtitle1 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    ),
    subtitle2 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    ),
    body1 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
    ),
    body2 = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
    ),
    button = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    ),
    caption = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    ),
    overline = TextStyle(
        fontFamily = robotoFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
    )
)
