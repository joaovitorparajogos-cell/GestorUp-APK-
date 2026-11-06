package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brand.BrandAssets
import com.example.ui.theme.GestorUpBorder
import com.example.ui.theme.GestorUpGreen
import com.example.ui.theme.GestorUpGreenDeep
import com.example.ui.theme.GestorUpGreenSoft
import com.example.ui.theme.GestorUpTextPrimary
import com.example.ui.theme.GestorUpTextSecondary
import com.example.ui.viewmodel.GestorUpViewModel

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.BuildConfig
import com.example.updater.UpdateInfo
import com.example.updater.UpdateState
import com.example.ui.components.ManageFuncoesDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
  viewModel: GestorUpViewModel,
  onBack: () -> Unit = {},
  onReplaySplash: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val themeMode by viewModel.themeMode.collectAsState()
  val fontSizeOption by viewModel.fontSizeOption.collectAsState()
  val updateState by viewModel.updateState.collectAsState()
  val todasFuncoes by viewModel.todasFuncoes.collectAsState()
  val todosColaboradores by viewModel.todosColaboradores.collectAsState()
  var showManageFuncoesModal by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
  ) {
    // Botão Voltar para o Início / Escala
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("btn_back_settings")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Voltar",
          tint = GestorUpGreen
        )
      }
      Text(
        text = "Voltar para as Telas do GestorUp",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = GestorUpGreen,
        modifier = Modifier.clickable { onBack() }
      )
    }

    // Cabeçalho da Tela
    Text(
      text = "Configurações",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Text(
      text = "Personalize o tema visual e o tamanho de leitura do GestorUp.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
    )

    // 1. SEÇÃO DE TEMA DO APLICATIVO
    SettingsSectionHeader(
      icon = Icons.Default.Palette,
      title = "Tema do Aplicativo",
      subtitle = "Escolha o esquema de cores para sua rotina no depósito"
    )

    Spacer(modifier = Modifier.height(10.dp))

    val themes = listOf(
      ThemeChoice("CLARO", "Tema Claro", "Fundo limpo e verde-esmeralda oficial", Icons.Default.LightMode),
      ThemeChoice("ESCURO", "Tema Escuro", "Superfícies escuras que descansam a visão", Icons.Default.DarkMode),
      ThemeChoice("SISTEMA", "Padrão do Sistema", "Segue o modo claro/escuro do seu aparelho", Icons.Default.BrightnessAuto)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      themes.forEach { choice ->
        val isSelected = themeMode.equals(choice.id, ignoreCase = true)
        ThemeOptionCard(
          choice = choice,
          isSelected = isSelected,
          onSelect = { viewModel.setThemeMode(choice.id) }
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 2. SEÇÃO DE TAMANHO DE FONTE (LEXEND)
    SettingsSectionHeader(
      icon = Icons.Default.FormatSize,
      title = "Tamanho do Texto",
      subtitle = "Tipografia Lexend: visual moderno, aconchegante e de alta legibilidade"
    )

    Spacer(modifier = Modifier.height(10.dp))

    val fontSizes = listOf(
      FontSizeChoice("PEQUENO", "Pequeno", "88%", "Mais informações visíveis na tela"),
      FontSizeChoice("MEDIO", "Médio", "100%", "Tamanho padrão equilibrado e confortável"),
      FontSizeChoice("GRANDE", "Grande", "116%", "Letras maiores para leitura rápida e descansada")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      fontSizes.forEach { fontChoice ->
        val isSelected = fontSizeOption.equals(fontChoice.id, ignoreCase = true)
        FontSizeOptionCard(
          choice = fontChoice,
          isSelected = isSelected,
          onSelect = { viewModel.setFontSizeOption(fontChoice.id) }
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 3. PRÉVIA EM TEMPO REAL DA LEITURA
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = CircleShape,
            color = GestorUpGreenSoft,
            modifier = Modifier.size(28.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Prévia de Leitura no GestorUp",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Carlos Silva • Empilhadeira",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = GestorUpGreenSoft
              ) {
                Text(
                  text = "07:00 - 15:20",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Anotação do dia: Caminhão com paletes de bebidas descarrega na Doca 3 às 08:30.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 4. FUNÇÕES E CARGOS DA EQUIPE
    SettingsSectionHeader(
      icon = Icons.Default.WorkOutline,
      title = "Funções e Cargos da Equipe",
      subtitle = "Adicione, edite nomes ou exclua funções disponíveis no depósito"
    )

    Spacer(modifier = Modifier.height(10.dp))

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Funções Cadastradas",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${todasFuncoes.size} funções ativas configuradas",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          OutlinedButton(
            onClick = { showManageFuncoesModal = true },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, GestorUpGreen),
            modifier = Modifier.testTag("btn_gerenciar_funcoes_settings")
          ) {
            Icon(Icons.Default.WorkOutline, contentDescription = null, tint = GestorUpGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Gerenciar", color = GestorUpGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 5. ATUALIZAÇÕES DO APLICATIVO (GITHUB RELEASES)
    SettingsSectionHeader(
      icon = Icons.Default.SystemUpdate,
      title = "Atualizações do Aplicativo",
      subtitle = "Atualizador nativo integrado ao repositório oficial do GitHub"
    )

    Spacer(modifier = Modifier.height(10.dp))

    UpdateSectionCard(
      updateState = updateState,
      lastCheckedTime = viewModel.updateManager.lastCheckedTime,
      onCheckUpdates = { viewModel.verificarAtualizacoes(isManual = true) },
      onStartDownload = { info -> viewModel.baixarAtualizacao(info) },
      onCancelDownload = { viewModel.cancelarDownloadAtualizacao() },
      onInstall = { file -> viewModel.instalarAtualizacao(file) }
    )

    Spacer(modifier = Modifier.height(24.dp))

    // 6. IDENTIDADE VISUAL E ANIMAÇÃO
    SettingsSectionHeader(
      icon = Icons.Default.Info,
      title = "Sobre o GestorUp",
      subtitle = "Identidade oficial da aplicação"
    )

    Spacer(modifier = Modifier.height(10.dp))

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = BrandAssets.APP_NAME,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreen
            )
            Text(
              text = BrandAssets.SLOGAN,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = GestorUpGreenSoft,
            border = BorderStroke(1.dp, GestorUpGreen)
          ) {
            Text(
              text = "v${BuildConfig.VERSION_NAME} (b${BuildConfig.VERSION_CODE})",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onReplaySplash,
          colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_replay_splash_settings")
        ) {
          Icon(Icons.Default.PlayCircleOutline, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Reproduzir Animação da Marca",
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(30.dp))
  }

  // DIÁLOGO DE GERENCIAMENTO DE FUNÇÕES
  if (showManageFuncoesModal) {
    ManageFuncoesDialog(
      funcoes = todasFuncoes,
      colaboradores = todosColaboradores,
      onAddFuncao = { nome -> viewModel.salvarFuncao(nome = nome) },
      onEditFuncao = { funcao, novoNome -> viewModel.salvarFuncao(id = funcao.id, nome = novoNome) },
      onDeleteFuncao = { funcao -> viewModel.deletarFuncao(funcao) },
      onDismiss = { showManageFuncoesModal = false }
    )
  }
}

private data class ThemeChoice(
  val id: String,
  val title: String,
  val description: String,
  val icon: ImageVector
)

private data class FontSizeChoice(
  val id: String,
  val title: String,
  val scalePercent: String,
  val description: String
)

@Composable
private fun SettingsSectionHeader(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = GestorUpGreen,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun ThemeOptionCard(
  choice: ThemeChoice,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  val borderColor = if (isSelected) GestorUpGreen else MaterialTheme.colorScheme.outlineVariant
  val containerColor = if (isSelected) {
    MaterialTheme.colorScheme.surfaceVariant
  } else {
    MaterialTheme.colorScheme.surface
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onSelect() }
      .testTag("theme_option_${choice.id.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = if (isSelected) GestorUpGreen else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(38.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = choice.icon,
            contentDescription = null,
            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = choice.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = choice.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (isSelected) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Selecionado",
          tint = GestorUpGreen,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun FontSizeOptionCard(
  choice: FontSizeChoice,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  val borderColor = if (isSelected) GestorUpGreen else MaterialTheme.colorScheme.outlineVariant
  val containerColor = if (isSelected) {
    MaterialTheme.colorScheme.surfaceVariant
  } else {
    MaterialTheme.colorScheme.surface
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onSelect() }
      .testTag("font_option_${choice.id.lowercase()}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) GestorUpGreen else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(38.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = choice.scalePercent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = choice.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = choice.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (isSelected) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = "Selecionado",
          tint = GestorUpGreen,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun UpdateSectionCard(
  updateState: UpdateState,
  lastCheckedTime: Long,
  onCheckUpdates: () -> Unit,
  onStartDownload: (UpdateInfo) -> Unit,
  onCancelDownload: () -> Unit,
  onInstall: (java.io.File) -> Unit,
) {
  val lastCheckedStr = remember(lastCheckedTime) {
    if (lastCheckedTime <= 0) "Nunca verificado"
    else {
      val sdf = SimpleDateFormat("dd/MM 'às' HH:mm", Locale("pt", "BR"))
      sdf.format(Date(lastCheckedTime))
    }
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("card_atualizacoes_app")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Versão Instalada",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = GestorUpGreen
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Text(
            text = "Checagem: $lastCheckedStr",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(14.dp))

      when (updateState) {
        is UpdateState.Idle -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Consulte se há novas versões publicadas no repositório GitHub.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
              onClick = onCheckUpdates,
              colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              modifier = Modifier.testTag("btn_verificar_atualizacoes")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Verificar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }

        is UpdateState.Checking -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator(
              color = GestorUpGreen,
              strokeWidth = 2.5.dp,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Consultando última versão no GitHub Releases...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        is UpdateState.UpToDate -> {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = GestorUpGreenSoft,
            border = BorderStroke(1.dp, GestorUpGreen),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Aplicativo Atualizado!",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep
                )
                Text(
                  text = "Você já está utilizando a versão mais recente (${updateState.versionName}).",
                  style = MaterialTheme.typography.bodySmall,
                  color = GestorUpGreenDeep.copy(alpha = 0.85f)
                )
              }
              OutlinedButton(
                onClick = onCheckUpdates,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GestorUpGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("Checar", fontSize = 12.sp, color = GestorUpGreenDeep, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        is UpdateState.UpdateAvailable -> {
          val info = updateState.info
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            border = BorderStroke(1.5.dp, GestorUpGreen),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = GestorUpGreen,
                    modifier = Modifier.size(22.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = "Nova Versão Disponível!",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = GestorUpGreenDeep
                    )
                    Text(
                      text = "v${info.remoteVersionName} (Build ${info.remoteVersionCode}) • ${info.publishedAt}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = GestorUpGreenSoft
                ) {
                  Text(
                    text = formatBytes(info.apkSize),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GestorUpGreenDeep,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(
                    text = "Notas da Atualização:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = info.releaseNotes,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (!info.expectedSha256.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "SHA-256 verificado: ${info.expectedSha256.take(12)}...${info.expectedSha256.takeLast(6)}",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = { onStartDownload(info) },
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_atualizar_agora")
              ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Baixar e Atualizar Agora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }

        is UpdateState.Downloading -> {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, GestorUpGreen),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Baixando Atualização...",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (updateState.progressPercent >= 0) "${updateState.progressPercent}%" else "Baixando...",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreen
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              if (updateState.progressPercent >= 0) {
                LinearProgressIndicator(
                  progress = { updateState.progressPercent / 100f },
                  color = GestorUpGreen,
                  trackColor = MaterialTheme.colorScheme.outlineVariant,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                )
              } else {
                LinearProgressIndicator(
                  color = GestorUpGreen,
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "${formatBytes(updateState.downloadedBytes)} de ${formatBytes(updateState.totalBytes)}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                  onClick = onCancelDownload,
                  shape = RoundedCornerShape(6.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("btn_cancelar_download")
                ) {
                  Text("Cancelar", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        is UpdateState.Downloaded -> {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GestorUpGreenSoft,
            border = BorderStroke(1.5.dp, GestorUpGreen),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = GestorUpGreen,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Download Concluído e Verificado!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GestorUpGreenDeep
                  )
                  Text(
                    text = "APK validado com sucesso e pronto para ser instalado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GestorUpGreenDeep.copy(alpha = 0.85f)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Button(
                onClick = { onInstall(updateState.apkFile) },
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_instalar_agora")
              ) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Instalar Atualização Agora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }

        is UpdateState.Installing -> {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            CircularProgressIndicator(
              color = GestorUpGreen,
              strokeWidth = 2.5.dp,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Abrindo instalador de pacotes do Android...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        is UpdateState.Error -> {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Não foi possível atualizar",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = updateState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              if (updateState.canRetry) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                  onClick = onCheckUpdates,
                  shape = RoundedCornerShape(6.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text("Tentar Novamente", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun formatBytes(bytes: Long): String {
  if (bytes <= 0) return "0 MB"
  val mb = bytes.toDouble() / (1024 * 1024)
  return "%.1f MB".format(Locale.US, mb)
}

