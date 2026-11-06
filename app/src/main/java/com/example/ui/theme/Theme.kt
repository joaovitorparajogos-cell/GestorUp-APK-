package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = GestorUpGreen,
    onPrimary = Color.White,
    primaryContainer = GestorUpGreenSoft,
    onPrimaryContainer = GestorUpGreenDeepLightConst,
    secondary = GestorUpGreenDeepLightConst,
    onSecondary = Color.White,
    secondaryContainer = GestorUpGreenSurfaceConst,
    onSecondaryContainer = GestorUpGreenDeepLightConst,
    tertiary = GestorUpInfo,
    onTertiary = Color.White,
    background = GestorUpBackgroundLightConst,
    onBackground = GestorUpTextPrimaryLightConst,
    surface = GestorUpSurfaceLightConst,
    onSurface = GestorUpTextPrimaryLightConst,
    surfaceVariant = GestorUpGreenLightConst,
    onSurfaceVariant = GestorUpTextSecondaryLightConst,
    outline = GestorUpBorderLightConst,
    outlineVariant = GestorUpBorderLightConst.copy(alpha = 0.5f),
    error = GestorUpError,
    onError = Color.White,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = GestorUpGreenDark,
    onPrimary = Color(0xFF0C1F17),
    primaryContainer = Color(0xFF1B4F3D),
    onPrimaryContainer = GestorUpGreenSoft,
    secondary = GestorUpGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E2B25),
    onSecondaryContainer = Color(0xFFD3E7DC),
    tertiary = Color(0xFF64B5F6),
    onTertiary = Color.Black,
    background = GestorUpBackgroundDarkConst,
    onBackground = GestorUpTextPrimaryDarkConst,
    surface = GestorUpSurfaceDarkConst,
    onSurface = GestorUpTextPrimaryDarkConst,
    surfaceVariant = GestorUpSurfaceVariantDarkConst,
    onSurfaceVariant = GestorUpTextSecondaryDarkConst,
    outline = GestorUpBorderDarkConst,
    outlineVariant = Color(0xFF202A24),
    error = Color(0xFFFF6B6B),
    onError = Color.Black,
  )

@Composable
fun GestorUpTheme(
  themeMode: String = "ESCURO",
  fontSizeOption: String = "PEQUENO",
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isDark = when (themeMode.uppercase()) {
    "ESCURO" -> true
    "CLARO" -> false
    "PADRAO" -> isSystemInDarkTheme()
    else -> isSystemInDarkTheme()
  }

  val fontScale = when (fontSizeOption.uppercase()) {
    "PEQUENO" -> 0.88f
    "GRANDE" -> 1.16f
    else -> 1.0f
  }

  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      isDark -> DarkColorScheme
      else -> LightColorScheme
    }

  val scaledTypography = getGestorUpTypography(fontScale)

  MaterialTheme(
    colorScheme = colorScheme,
    typography = scaledTypography,
    content = content
  )
}

@Composable
fun GestorUpTheme(
  darkTheme: Boolean,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = GestorUpTheme(
  themeMode = if (darkTheme) "ESCURO" else "CLARO",
  fontSizeOption = "MEDIO",
  dynamicColor = dynamicColor,
  content = content
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) = GestorUpTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
