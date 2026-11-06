package com.example.brand

import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.R

/**
 * GestorUp Official Brand Resources & Identity Configuration.
 *
 * Centralizes all references to the official brand assets:
 * - IMAGEM 1: Complete GestorUp logo (Symbol + "GestorUp" + Slogan) for the Splash Screen.
 * - IMAGEM 2: Isolated GestorUp Symbol for the Android App Icon.
 * - Geometry & trajectories for the official U-arrow animation.
 */
object BrandAssets {
  /** Official App Name */
  const val APP_NAME = "GestorUp"

  /** Official Brand Slogan */
  const val SLOGAN = "Sua equipe. Sua gestão. Tudo em ordem."

  /**
   * IMAGEM 1: Complete official logo with symbol, name and slogan.
   * Dedicated exclusively to the application launch / Splash Screen.
   */
  @DrawableRes
  val LOGO_FULL_RES: Int = R.drawable.ic_gestorup_logo_full

  /**
   * IMAGEM 2: Isolated official symbol.
   * Dedicated exclusively to the Android launcher adaptive icon and in-app avatar/badge.
   */
  @DrawableRes
  val SYMBOL_RES: Int = R.drawable.ic_gestorup_symbol

  /** Official Brand Colors extracted from the vector brand master */
  object Colors {
    val EmeraldDark = Color(0xFF012823)      // "Gestor" text & dark emerald accent
    val EmeraldPrimary = Color(0xFF0A5C43)   // Symbol calendar header & brand core
    val EmeraldVibrant = Color(0xFF10B981)   // Arrow body & "Up" text vibrant gradient
    val EmeraldBright = Color(0xFF34D399)    // Arrow ascending highlights
    val EmeraldLight = Color(0xFF5BDD62)     // Arrowhead peak glow
    val SurfaceWhite = Color(0xFFFFFFFF)     // Clean splash & icon surface
  }

  /**
   * Trajectory of the arrow incorporated into the letter 'U' of GestorUp.
   * Normalized relative to the 1254x1254 square canvas of the full logo.
   *
   * Path follows:
   * 1. Top of left stem of 'U'
   * 2. Downwards along left stem
   * 3. Curve through the bottom base of 'U'
   * 4. Upwards along right stem
   * 5. Culminating at the arrowhead peak pointing directly upwards
   */
  object UArrowGeometry {
    val StartLeftStem = Offset(0.6770f, 0.6900f)
    val MidLeftStem = Offset(0.6770f, 0.7800f)
    val BottomLeftCurve = Offset(0.6800f, 0.8250f)
    val BottomCenter = Offset(0.7233f, 0.8350f)
    val BottomRightCurve = Offset(0.7650f, 0.8250f)
    val MidRightStem = Offset(0.7783f, 0.7700f)
    val ArrowBase = Offset(0.7783f, 0.6950f)
    val ArrowPeak = Offset(0.7783f, 0.6579f)

    /** Standard recommended animation duration in milliseconds (1.2s) */
    const val DEFAULT_ANIMATION_DURATION_MS = 1250L

    /** Settle pause after arrow reaches the peak before navigating (400ms) */
    const val POST_ANIMATION_HOLD_MS = 350L
  }
}
