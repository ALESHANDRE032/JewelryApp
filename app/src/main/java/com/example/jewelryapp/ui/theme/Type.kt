package com.example.jewelryapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CormorantFamily = FontFamily.Serif
val ManropeFamily   = FontFamily.Default

val DisplayXl = TextStyle(
    fontFamily = CormorantFamily, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic,
    fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = (-1.2).sp
)
val DisplayLg = TextStyle(
    fontFamily = CormorantFamily, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic,
    fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1).sp
)
val DisplayMd = TextStyle(
    fontFamily = CormorantFamily, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic,
    fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.6).sp
)
val NumberLg = TextStyle(
    fontFamily = CormorantFamily, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic,
    fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp
)
val Eyebrow = TextStyle(
    fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 1.6.sp
)

val Typography = Typography(
    displaySmall = TextStyle(
        fontFamily = CormorantFamily, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic,
        fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = (-0.1).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ManropeFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ManropeFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    )
)
