package com.example.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.brand.BrandAssets

/**
 * GestorUp Official Splash Screen.
 *
 * Displays IMAGEM 1 (Full brand logo + symbol + slogan "Sua equipe. Sua gestão. Tudo em ordem.")
 * centered on a clean white surface with 1:1 aspect ratio preservation and safe-area margins.
 * Executes the official U-arrow trajectory animation and automatically transitions to the
 * target destination (Auth or Dashboard).
 */
@Composable
fun GestorUpSplashScreen(
  modifier: Modifier = Modifier,
  durationMs: Int = BrandAssets.UArrowGeometry.DEFAULT_ANIMATION_DURATION_MS.toInt(),
  onSplashFinished: () -> Unit,
) {
  var isAnimationComplete by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
      .systemBarsPadding()
      .testTag("splash_screen_container"),
    contentAlignment = Alignment.Center
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center
    ) {
      // Calculate square container fitting the available screen dimension
      val maxAllowed = minOf(maxWidth, maxHeight, 500.dp)

      Box(
        modifier = Modifier
          .size(maxAllowed)
          .aspectRatio(1f)
          .testTag("brand_logo_box"),
        contentAlignment = Alignment.Center
      ) {
        // IMAGEM 1: Official Full GestorUp Logo (Symbol, Name & Slogan)
        Image(
          painter = painterResource(id = BrandAssets.LOGO_FULL_RES),
          contentDescription = "${BrandAssets.APP_NAME} - ${BrandAssets.SLOGAN}",
          modifier = Modifier
            .fillMaxSize()
            .testTag("brand_logo_image"),
          contentScale = ContentScale.Fit
        )

        // Animated Trajectory Layer over the 'U'
        GestorUpArrowAnimationLayer(
          modifier = Modifier
            .fillMaxSize()
            .testTag("u_arrow_animation_layer"),
          durationMs = durationMs,
          startDelayMs = 150,
          onAnimationFinished = {
            isAnimationComplete = true
            onSplashFinished()
          }
        )
      }
    }
  }
}
