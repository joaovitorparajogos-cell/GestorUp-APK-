package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ==========================================
// PALETA OFICIAL DO GESTORUP - CONSTANTES BRUTAS
// ==========================================
val GestorUpGreen = Color(0xFF20A779)          // Verde principal da marca
val GestorUpGreenDark = Color(0xFF3CD19C)      // Verde vibrante para modo escuro
val GestorUpGreenSoft = Color(0xFFB9E8D5)      // Verde suave para badges e chips

// Constantes brutas para Tema Claro
val GestorUpBackgroundLightConst = Color(0xFFF7F9F8)
val GestorUpSurfaceLightConst = Color(0xFFFFFFFF)
val GestorUpTextPrimaryLightConst = Color(0xFF17211D)
val GestorUpTextSecondaryLightConst = Color(0xFF66736C)
val GestorUpBorderLightConst = Color(0xFFE1E8E3)
val GestorUpGreenDeepLightConst = Color(0xFF102820)
val GestorUpGreenLightConst = Color(0xFFE6F7F0)
val GestorUpGreenSurfaceConst = Color(0xFFF0F9F5)

// Constantes brutas para Tema Escuro Profundo (Verdadeiro Modo Escuro)
val GestorUpBackgroundDarkConst = Color(0xFF0F1412)     // Fundo escuro profundo
val GestorUpSurfaceDarkConst = Color(0xFF18201C)        // Superfície escura de cartões
val GestorUpSurfaceVariantDarkConst = Color(0xFF222C27) // Variação de cartões no escuro
val GestorUpTextPrimaryDarkConst = Color(0xFFF1F5F3)   // Texto claro de alto contraste
val GestorUpTextSecondaryDarkConst = Color(0xFFA5B8AD) // Texto secundário claro
val GestorUpBorderDarkConst = Color(0xFF26352E)        // Bordas discretas no escuro

// Status e Ações
val GestorUpSuccess = Color(0xFF16845B)
val GestorUpWarning = Color(0xFFA96508)
val GestorUpError = Color(0xFFC03939)
val GestorUpInfo = Color(0xFF356AC3)
val GestorUpCardBorder = Color(0xFFE5ECE8)

// =========================================================================
// TOKENS DINÂMICOS SENSÍVEIS AO TEMA (CLARO / ESCURO)
// Todas as telas e componentes usando estes tokens responderão automaticamente
// e de forma abrangente em todo o aplicativo sem deixar partes brancas no escuro.
// =========================================================================
val GestorUpBackground: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.background

val GestorUpSurface: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.surface

val GestorUpSurfaceVariant: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.surfaceVariant

val GestorUpTextPrimary: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.onSurface

val GestorUpTextSecondary: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.onSurfaceVariant

val GestorUpGreenDeep: Color
  @Composable
  @ReadOnlyComposable
  get() {
    val isDark = MaterialTheme.colorScheme.surface == GestorUpSurfaceDarkConst ||
                 MaterialTheme.colorScheme.background == GestorUpBackgroundDarkConst
    return if (isDark) Color(0xFFE8EFEA) else GestorUpGreenDeepLightConst
  }

val GestorUpBorder: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.outlineVariant

val GestorUpGreenLight: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.surfaceVariant

val GestorUpOutline: Color
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.colorScheme.outline

// Aliases para retrocompatibilidade
val GestorUpBackgroundDark = GestorUpBackgroundDarkConst
val GestorUpSurfaceDark = GestorUpSurfaceDarkConst
val GestorUpBorderDark = GestorUpBorderDarkConst
val GestorUpGreenPrimary = GestorUpGreen
val GestorUpGreenSecondary = GestorUpGreenDeepLightConst
val GestorUpGreenVibrant = GestorUpGreen
val GestorUpGreenContainer = GestorUpGreenSoft
val GestorUpOnGreenContainer = GestorUpGreenDeepLightConst
val GestorUpGreenSurface = GestorUpGreenSurfaceConst
