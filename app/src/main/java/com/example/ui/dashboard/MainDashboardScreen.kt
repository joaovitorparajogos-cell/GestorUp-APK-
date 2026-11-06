package com.example.ui.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GestorUpBottomNavigation
import com.example.ui.components.GestorUpNavTab
import com.example.ui.components.GestorUpTopBar
import com.example.ui.screens.DailyScheduleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyScheduleScreen
import com.example.ui.screens.PendingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TeamScreen
import com.example.ui.theme.GestorUpBackground
import com.example.ui.viewmodel.GestorUpViewModel

import androidx.activity.compose.BackHandler

/**
 * Painel Principal Redesenhado do GestorUp
 *
 * Navegação Oficial de 5 Abas:
 * 1. Início
 * 2. Escala diária (Funcionalidade principal)
 * 3. Escala mensal (Anexo de imagem da planilha)
 * 4. Equipe (Colaboradores do depósito)
 * 5. Pendências (Lembretes e encaminhamentos para tesouraria/documentos)
 *
 * Configurações acessíveis exclusivamente pelo ícone de engrenagem na barra superior.
 */
@Composable
fun MainDashboardScreen(
  userName: String = "Gestor",
  onLogout: () -> Unit,
  onReplaySplash: (durationMs: Int) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: GestorUpViewModel = viewModel(),
) {
  var selectedTab by remember { mutableStateOf(GestorUpNavTab.INICIO) }
  var isSettingsOpen by remember { mutableStateOf(false) }
  val contagemPendencias by viewModel.contagemPendenciasAbertas.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // Retornar das configurações pelo botão físico/gesto de voltar do Android
  BackHandler(enabled = isSettingsOpen) {
    isSettingsOpen = false
  }

  // Exibir feedbacks da ViewModel via Snackbar
  LaunchedEffect(viewModel) {
    viewModel.uiEvents.collect { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      GestorUpTopBar(
        userName = userName,
        subtitle = "Depósito do Supermercado",
        isSettingsOpen = isSettingsOpen,
        onReplaySplash = { onReplaySplash(1250) },
        onOpenSettings = { isSettingsOpen = true },
        onBackFromSettings = { isSettingsOpen = false },
        onLogout = onLogout
      )
    },
    bottomBar = {
      // A barra inferior é ocultada na tela de configurações conforme solicitado pelo usuário
      if (!isSettingsOpen) {
        GestorUpBottomNavigation(
          currentTab = selectedTab,
          onSelectTab = { selectedTab = it },
          contagemPendencias = contagemPendencias
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      if (isSettingsOpen) {
        SettingsScreen(
          viewModel = viewModel,
          onBack = { isSettingsOpen = false },
          onReplaySplash = { onReplaySplash(1250) }
        )
      } else {
        Crossfade(
          targetState = selectedTab,
          animationSpec = tween(durationMillis = 200),
          label = "DashboardTabTransition"
        ) { tab ->
          when (tab) {
            GestorUpNavTab.INICIO -> {
              HomeScreen(
                viewModel = viewModel,
                onNavigateTab = { selectedTab = it }
              )
            }

            GestorUpNavTab.ESCALA_DIARIA -> {
              DailyScheduleScreen(viewModel = viewModel)
            }

            GestorUpNavTab.ESCALA_MENSAL -> {
              MonthlyScheduleScreen(viewModel = viewModel)
            }

            GestorUpNavTab.EQUIPE -> {
              TeamScreen(viewModel = viewModel)
            }

            GestorUpNavTab.PENDENCIAS -> {
              PendingScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
