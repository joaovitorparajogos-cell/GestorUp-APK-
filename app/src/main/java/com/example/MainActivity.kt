package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.brand.BrandAssets
import com.example.ui.dashboard.MainDashboardScreen
import com.example.ui.splash.GestorUpSplashScreen
import com.example.ui.theme.GestorUpTheme
import com.example.ui.viewmodel.GestorUpViewModel

enum class AppDestination {
  SPLASH,
  DASHBOARD
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: GestorUpViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()
      val fontSizeOption by viewModel.fontSizeOption.collectAsState()

      GestorUpTheme(
        themeMode = themeMode,
        fontSizeOption = fontSizeOption
      ) {
        Surface(modifier = Modifier.fillMaxSize()) {
          GestorUpAppRoot(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun GestorUpAppRoot(viewModel: GestorUpViewModel = viewModel()) {
  var currentDestination by remember { mutableStateOf(AppDestination.SPLASH) }
  var splashDurationMs by remember {
    mutableIntStateOf(BrandAssets.UArrowGeometry.DEFAULT_ANIMATION_DURATION_MS.toInt())
  }
  
  // Handle system back button gracefully
  BackHandler(enabled = currentDestination != AppDestination.SPLASH) {
    if (currentDestination == AppDestination.DASHBOARD) {
        // Handle exit or do nothing
    }
  }

  Crossfade(
    targetState = currentDestination,
    animationSpec = tween(durationMillis = 300),
    label = "GestorUpScreenNavigation"
  ) { destination ->
    when (destination) {
      AppDestination.SPLASH -> {
        GestorUpSplashScreen(
          durationMs = splashDurationMs,
          onSplashFinished = {
            currentDestination = AppDestination.DASHBOARD
          }
        )
      }

      AppDestination.DASHBOARD -> {
        MainDashboardScreen(
          userName = "Gestor",
          viewModel = viewModel,
          onLogout = { /* Nenhuma ação de logout necessária sem autenticação */ },
          onReplaySplash = { customDuration ->
            splashDurationMs = customDuration
            currentDestination = AppDestination.SPLASH
          }
        )
      }
    }
  }
}
