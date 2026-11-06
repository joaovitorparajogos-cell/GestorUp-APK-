package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brand.BrandAssets
import com.example.ui.components.CalendarPickerModal
import com.example.ui.components.GestorUpNavTab
import com.example.ui.theme.GestorUpBackground
import com.example.ui.theme.GestorUpBorder
import com.example.ui.theme.GestorUpGreen
import com.example.ui.theme.GestorUpGreenDeep
import com.example.ui.theme.GestorUpGreenLight
import com.example.ui.theme.GestorUpGreenSoft
import com.example.ui.theme.GestorUpSurface
import com.example.ui.theme.GestorUpTextPrimary
import com.example.ui.theme.GestorUpTextSecondary
import com.example.ui.viewmodel.GestorUpViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: GestorUpViewModel,
  onNavigateTab: (GestorUpNavTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val escalaHoje by viewModel.escalaDoDia.collectAsState()
  val anotacaoHoje by viewModel.anotacaoDoDia.collectAsState()
  val pendenciasAbertas by viewModel.pendenciasAbertas.collectAsState()
  val datesWithScale by viewModel.datasComEscala.collectAsState()
  val datesWithNotes by viewModel.datasComAnotacao.collectAsState()

  var showCalendarModal by remember { mutableStateOf(false) }

  val todayFormatted = remember {
    val now = Date()
    val diaSemana = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(now).replaceFirstChar { it.uppercase() }
    val dataCompleta = SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")).format(now)
    "$diaSemana, $dataCompleta"
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(GestorUpBackground),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. CABEÇALHO DO DIA
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
        border = BorderStroke(1.dp, GestorUpBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Hoje no Depósito",
                fontSize = 13.sp,
                color = GestorUpTextSecondary,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = todayFormatted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpGreenDeep
              )
            }
            IconButtonWithBackground(
              icon = Icons.Default.CalendarMonth,
              description = "Calendário",
              onClick = { showCalendarModal = true }
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 2. RESUMO DA ESCALA DO DIA
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GestorUpGreenLight),
            border = BorderStroke(1.dp, GestorUpGreenSoft),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Escala de Hoje (${escalaHoje.size} colaboradores)",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep,
                  modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = GestorUpGreenLight,
                  border = BorderStroke(1.dp, GestorUpGreen),
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      viewModel.selecionarHoje()
                      onNavigateTab(GestorUpNavTab.ESCALA_DIARIA)
                    }
                ) {
                  Text(
                    text = "Editar",
                    color = GestorUpGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  )
                }
              }

              if (escalaHoje.isEmpty()) {
                Text(
                  text = "Nenhum horário cadastrado para hoje. Toque em 'Editar' para organizar a escala.",
                  fontSize = 12.sp,
                  color = GestorUpTextSecondary,
                  modifier = Modifier.padding(vertical = 4.dp)
                )
              } else {
                Spacer(modifier = Modifier.height(6.dp))
                escalaHoje.take(4).forEach { item ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "• ${item.colaboradorNome}",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Medium,
                      color = GestorUpTextPrimary
                    )
                    Text(
                      text = if (item.isFolga || item.horarioEntrada.equals("FOLGA", ignoreCase = true)) "FOLGA" else "Entrada: ${item.horarioEntrada}",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = if (item.isFolga || item.horarioEntrada.equals("FOLGA", ignoreCase = true)) GestorUpTextSecondary else GestorUpGreenDeep
                    )
                  }
                }
                if (escalaHoje.size > 4) {
                  Text(
                    text = "+ mais ${escalaHoje.size - 4} colaboradores...",
                    fontSize = 11.sp,
                    color = GestorUpTextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Botão Compartilhar WhatsApp
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                Button(
                  onClick = {
                    val msg = viewModel.gerarTextoMensagemWhatsApp(escalaHoje, anotacaoHoje)
                    viewModel.compartilharNoWhatsApp(context, msg)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                  Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Enviar Escala no WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // 3. AÇÕES RÁPIDAS
    item {
      Text(
        text = "Ações Rápidas",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = GestorUpGreenDeep
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickActionCard(
          icon = Icons.Default.Schedule,
          title = "Escala Diária",
          subtitle = "Horários 24h",
          modifier = Modifier.weight(1f),
          onClick = { onNavigateTab(GestorUpNavTab.ESCALA_DIARIA) }
        )
        QuickActionCard(
          icon = Icons.Default.CalendarMonth,
          title = "Calendário",
          subtitle = "Navegar datas",
          modifier = Modifier.weight(1f),
          onClick = { showCalendarModal = true }
        )
        QuickActionCard(
          icon = Icons.Default.Image,
          title = "Escala Mensal",
          subtitle = "Ver planilha",
          modifier = Modifier.weight(1f),
          onClick = { onNavigateTab(GestorUpNavTab.ESCALA_MENSAL) }
        )
      }
    }

    // 4. PENDÊNCIAS EM ABERTO
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
        border = BorderStroke(1.dp, GestorUpBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Pendências em Aberto",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpGreenDeep
              )
              if (pendenciasAbertas.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .background(Color(0xFFA96508), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${pendenciasAbertas.size}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            TextButton(
              onClick = { onNavigateTab(GestorUpNavTab.PENDENCIAS) },
              contentPadding = PaddingValues(0.dp)
            ) {
              Text("Ver todas", color = GestorUpGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (pendenciasAbertas.isEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 8.dp)
            ) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Tudo em ordem! Nenhuma pendência aberta no momento.",
                fontSize = 13.sp,
                color = GestorUpTextSecondary
              )
            }
          } else {
            Spacer(modifier = Modifier.height(6.dp))
            pendenciasAbertas.take(3).forEach { pendencia ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp)
                  .clickable { onNavigateTab(GestorUpNavTab.PENDENCIAS) },
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.WarningAmber,
                  contentDescription = null,
                  tint = if (pendencia.prioridade == "Urgente" || pendencia.prioridade == "Alta")
                    Color(0xFFC03939) else Color(0xFFA96508),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = pendencia.titulo,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GestorUpTextPrimary,
                    maxLines = 1
                  )
                  Text(
                    text = "${pendencia.categoria} • Prioridade ${pendencia.prioridade}",
                    fontSize = 11.sp,
                    color = GestorUpTextSecondary
                  )
                }
                Icon(
                  Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  tint = GestorUpTextSecondary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // MODAL CALENDÁRIO
  if (showCalendarModal) {
    CalendarPickerModal(
      currentSelectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
      datesWithScale = datesWithScale,
      datesWithNotes = datesWithNotes,
      onDateSelected = { date ->
        viewModel.selecionarData(date)
        onNavigateTab(GestorUpNavTab.ESCALA_DIARIA)
      },
      onDismiss = { showCalendarModal = false }
    )
  }
}

@Composable
private fun QuickActionCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Card(
    modifier = modifier
      .height(108.dp)
      .clickable { onClick() },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
    border = BorderStroke(1.dp, GestorUpBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 6.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .background(GestorUpGreenSoft, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = GestorUpGreenDeep, modifier = Modifier.size(18.dp))
      }
      Spacer(modifier = Modifier.height(6.dp))
      Box(
        modifier = Modifier.height(30.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = title,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = GestorUpGreenDeep,
          textAlign = TextAlign.Center,
          lineHeight = 14.sp,
          maxLines = 2
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 10.sp,
        color = GestorUpTextSecondary,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun IconButtonWithBackground(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  description: String,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(CircleShape)
      .background(GestorUpGreenSoft)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Icon(icon, contentDescription = description, tint = GestorUpGreenDeep, modifier = Modifier.size(20.dp))
  }
}
