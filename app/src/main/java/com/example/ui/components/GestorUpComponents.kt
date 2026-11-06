package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.brand.BrandAssets
import com.example.ui.theme.GestorUpBackgroundDarkConst
import com.example.ui.theme.GestorUpBorder

import com.example.ui.theme.GestorUpGreen
import com.example.ui.theme.GestorUpGreenDeep
import com.example.ui.theme.GestorUpGreenSoft
import com.example.ui.theme.GestorUpSurface
import com.example.ui.theme.GestorUpTextPrimary
import com.example.ui.theme.GestorUpTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 5 Abas Oficiais da Nova Navegação Principal do GestorUp
 */
enum class GestorUpNavTab(val title: String, val icon: ImageVector) {
  INICIO("Início", Icons.Default.Home),
  ESCALA_DIARIA("Diária", Icons.Default.Schedule),
  ESCALA_MENSAL("Mensal", Icons.Default.CalendarMonth),
  EQUIPE("Equipe", Icons.Default.Group),
  PENDENCIAS("Pendências", Icons.Default.AssignmentTurnedIn),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestorUpTopBar(
  userName: String = "Gestor",
  subtitle: String = "Depósito • Supermercado",
  isSettingsOpen: Boolean = false,
  onReplaySplash: () -> Unit,
  onOpenSettings: (() -> Unit)? = null,
  onBackFromSettings: (() -> Unit)? = null,
  onLogout: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    modifier = modifier.border(BorderStroke(1.dp, GestorUpBorder)),
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = GestorUpSurface,
      titleContentColor = GestorUpTextPrimary,
      navigationIconContentColor = GestorUpGreen,
      actionIconContentColor = GestorUpGreen
    ),
    navigationIcon = {
      if (isSettingsOpen && onBackFromSettings != null) {
        IconButton(
          onClick = onBackFromSettings,
          modifier = Modifier.testTag("action_back_from_settings")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Voltar ao Início",
            tint = GestorUpGreen
          )
        }
      }
    },
    title = {
      if (isSettingsOpen) {
        Column {
          Text(
            text = "Configurações",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = GestorUpGreenDeep
          )
          Text(
            text = "Temas e tamanho de leitura",
            fontSize = 11.sp,
            color = GestorUpTextSecondary
          )
        }
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (MaterialTheme.colorScheme.background == GestorUpBackgroundDarkConst) Color(0xFF1E2823) else Color.White
            ),
            border = BorderStroke(1.dp, GestorUpBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Image(
              painter = painterResource(id = BrandAssets.SYMBOL_RES),
              contentDescription = "Ícone GestorUp",
              modifier = Modifier
                .size(36.dp)
                .padding(2.dp),
              contentScale = ContentScale.Fit
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = BrandAssets.APP_NAME,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep
            )
            Text(
              text = subtitle,
              fontSize = 11.sp,
              color = GestorUpTextSecondary
            )
          }
        }
      }
    },
    actions = {
      IconButton(
        onClick = onReplaySplash,
        modifier = Modifier.testTag("action_replay_splash")
      ) {
        Icon(
          imageVector = Icons.Default.PlayCircleOutline,
          contentDescription = "Ver Animação de Abertura",
          tint = GestorUpGreen
        )
      }
      if (!isSettingsOpen && onOpenSettings != null) {
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("action_open_settings")
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Configurações",
            tint = GestorUpGreen
          )
        }
      }
    }
  )
}

@Composable
fun GestorUpBottomNavigation(
  currentTab: GestorUpNavTab,
  onSelectTab: (GestorUpNavTab) -> Unit,
  contagemPendencias: Int = 0,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier.border(BorderStroke(1.dp, GestorUpBorder)),
    containerColor = GestorUpSurface,
    tonalElevation = 2.dp
  ) {
    GestorUpNavTab.entries.forEach { tab ->
      val isSelected = currentTab == tab
      NavigationBarItem(
        selected = isSelected,
        onClick = { onSelectTab(tab) },
        alwaysShowLabel = true,
        icon = {
          if (tab == GestorUpNavTab.PENDENCIAS && contagemPendencias > 0) {
            BadgedBox(
              badge = {
                Badge(
                  containerColor = Color(0xFFA96508),
                  contentColor = Color.White
                ) {
                  Text("$contagemPendencias")
                }
              }
            ) {
              Icon(imageVector = tab.icon, contentDescription = tab.title)
            }
          } else {
            Icon(imageVector = tab.icon, contentDescription = tab.title)
          }
        },
        label = {
          Text(
            text = tab.title,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = GestorUpGreen,
          selectedTextColor = GestorUpGreen,
          indicatorColor = GestorUpGreenSoft,
          unselectedIconColor = GestorUpTextSecondary,
          unselectedTextColor = GestorUpTextSecondary
        )
      )
    }
  }
}

/**
 * Seletor de Horário no Formato 24 Horas
 */
@Composable
fun TimePicker24hDialog(
  initialTime: String = "07:00",
  title: String = "Selecionar Horário",
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val parts = initialTime.split(":")
  var hour by remember { mutableIntStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 7) }
  var minute by remember { mutableIntStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 0) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = title,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = GestorUpGreenDeep
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display do horário selecionado
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxWidth()
            .background(GestorUpGreenSoft.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
        ) {
          Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = GestorUpGreenDeep
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Atalhos rápidos frequentes para o depósito
        Text(
          text = "Horários frequentes do depósito:",
          fontSize = 12.sp,
          color = GestorUpTextSecondary,
          modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf("06:00", "07:00", "08:00", "13:40").forEach { preset ->
            OutlinedButton(
              onClick = {
                val p = preset.split(":")
                hour = p[0].toInt()
                minute = p[1].toInt()
              },
              shape = RoundedCornerShape(8.dp),
              contentPadding = ButtonDefaults.ContentPadding
            ) {
              Text(text = preset, fontSize = 11.sp, color = GestorUpGreen)
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          listOf("14:20", "15:20", "16:20", "22:00").forEach { preset ->
            OutlinedButton(
              onClick = {
                val p = preset.split(":")
                hour = p[0].toInt()
                minute = p[1].toInt()
              },
              shape = RoundedCornerShape(8.dp),
              contentPadding = ButtonDefaults.ContentPadding
            ) {
              Text(text = preset, fontSize = 11.sp, color = GestorUpGreen)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ajustes finos de hora e minuto
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Hora", fontSize = 12.sp, color = GestorUpTextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { hour = (hour - 1 + 24) % 24 }) {
                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
              }
              Text(String.format(Locale.getDefault(), "%02d", hour), fontSize = 18.sp, fontWeight = FontWeight.Bold)
              IconButton(onClick = { hour = (hour + 1) % 24 }) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Minuto", fontSize = 12.sp, color = GestorUpTextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = { minute = (minute - 1 + 60) % 60 }) {
                Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
              }
              Text(String.format(Locale.getDefault(), "%02d", minute), fontSize = 18.sp, fontWeight = FontWeight.Bold)
              IconButton(onClick = { minute = (minute + 1) % 60 }) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
            Text("Cancelar", color = GestorUpTextSecondary)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = {
              onConfirm(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
            },
            colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Diálogo do Calendário Mensal para navegar entre dias e meses
 */
@Composable
fun CalendarPickerModal(
  currentSelectedDate: String,
  datesWithScale: Set<String>,
  datesWithNotes: Set<String>,
  onDateSelected: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  val initDate = try { sdf.parse(currentSelectedDate) ?: Date() } catch (e: Exception) { Date() }

  var calendarMonth by remember {
    mutableStateOf(Calendar.getInstance().apply {
      time = initDate
      set(Calendar.DAY_OF_MONTH, 1)
    })
  }

  val monthYearTitle = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR"))
    .format(calendarMonth.time)
    .replaceFirstChar { it.uppercase() }

  val daysInMonth = calendarMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
  val firstDayOfWeek = calendarMonth.get(Calendar.DAY_OF_WEEK) // 1 = Dom, 2 = Seg...

  val todayStr = sdf.format(Date())

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        // Cabeçalho de Navegação do Mês
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              calendarMonth = (calendarMonth.clone() as Calendar).apply {
                add(Calendar.MONTH, -1)
              }
            }
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior", tint = GestorUpGreen)
          }

          Text(
            text = monthYearTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = GestorUpGreenDeep
          )

          IconButton(
            onClick = {
              calendarMonth = (calendarMonth.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
              }
            }
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês", tint = GestorUpGreen)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dias da semana
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB").forEach { d ->
            Text(
              text = d,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpTextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.width(36.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de dias
        val totalSlots = ((firstDayOfWeek - 1) + daysInMonth)
        val rows = (totalSlots + 6) / 7

        Column {
          for (r in 0 until rows) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              for (c in 0..6) {
                val dayIndex = r * 7 + c - (firstDayOfWeek - 1) + 1
                if (dayIndex in 1..daysInMonth) {
                  val slotCal = (calendarMonth.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, dayIndex)
                  }
                  val slotDateStr = sdf.format(slotCal.time)
                  val isSelected = slotDateStr == currentSelectedDate
                  val isToday = slotDateStr == todayStr
                  val hasScale = datesWithScale.contains(slotDateStr)
                  val hasNote = datesWithNotes.contains(slotDateStr)

                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(
                        when {
                          isSelected -> GestorUpGreen
                          isToday -> GestorUpGreenSoft
                          else -> Color.Transparent
                        }
                      )
                      .clickable {
                        onDateSelected(slotDateStr)
                        onDismiss()
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                        text = "$dayIndex",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                          isSelected -> Color.White
                          isToday -> GestorUpGreenDeep
                          else -> GestorUpTextPrimary
                        }
                      )
                      // Marcador discreto de escala ou anotação
                      if (hasScale || hasNote) {
                        Box(
                          modifier = Modifier
                            .size(4.dp)
                            .background(
                              if (isSelected) Color.White else GestorUpGreen,
                              CircleShape
                            )
                        )
                      }
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.size(38.dp))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(
            onClick = {
              onDateSelected(todayStr)
              onDismiss()
            }
          ) {
            Text("Ir para Hoje", color = GestorUpGreen, fontWeight = FontWeight.Bold)
          }

          OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
            Text("Fechar", color = GestorUpTextSecondary)
          }
        }
      }
    }
  }
}
