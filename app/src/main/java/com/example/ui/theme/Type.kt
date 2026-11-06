package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val LexendFamily = FontFamily(
  Font(R.font.lexend, FontWeight.Normal)
)

/**
 * Creates dynamic Material 3 Typography scaled according to user preference (Pequeno, Médio, Grande).
 * Lexend provides optimal legibility, warmth, and visual comfort across the entire interface.
 */
fun getGestorUpTypography(fontScale: Float = 1.0f): Typography {
  fun scaledSp(base: Float) = (base * fontScale).sp

  return Typography(
    displayLarge = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Bold,
      fontSize = scaledSp(30f),
      lineHeight = scaledSp(36f)
    ),
    displayMedium = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Bold,
      fontSize = scaledSp(26f),
      lineHeight = scaledSp(32f)
    ),
    displaySmall = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = scaledSp(22f),
      lineHeight = scaledSp(28f)
    ),
    headlineLarge = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Bold,
      fontSize = scaledSp(20f),
      lineHeight = scaledSp(26f)
    ),
    headlineMedium = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = scaledSp(18f),
      lineHeight = scaledSp(24f)
    ),
    headlineSmall = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = scaledSp(16f),
      lineHeight = scaledSp(22f)
    ),
    titleLarge = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Bold,
      fontSize = scaledSp(18f),
      lineHeight = scaledSp(24f)
    ),
    titleMedium = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = scaledSp(15f),
      lineHeight = scaledSp(21f)
    ),
    titleSmall = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Medium,
      fontSize = scaledSp(13f),
      lineHeight = scaledSp(19f)
    ),
    bodyLarge = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Normal,
      fontSize = scaledSp(15f),
      lineHeight = scaledSp(22f),
      letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Normal,
      fontSize = scaledSp(13f),
      lineHeight = scaledSp(19f),
      letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Normal,
      fontSize = scaledSp(11f),
      lineHeight = scaledSp(16f),
      letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.SemiBold,
      fontSize = scaledSp(13f),
      lineHeight = scaledSp(18f)
    ),
    labelMedium = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Medium,
      fontSize = scaledSp(11f),
      lineHeight = scaledSp(15f)
    ),
    labelSmall = TextStyle(
      fontFamily = LexendFamily,
      fontWeight = FontWeight.Medium,
      fontSize = scaledSp(10f),
      lineHeight = scaledSp(13f)
    )
  )
}

val Typography = getGestorUpTypography(1.0f)
