package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Colaborador
import com.example.data.model.EscalaItem
import com.example.ui.components.CalendarPickerModal
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
import com.example.ui.viewmodel.InterjornadaConflito
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formata os dígitos numéricos digitados no formato HH:mm automaticamente.
 */
fun formatTimeDigits(input: String): String {
  val digits = input.filter { it.isDigit() }.take(4)
  return when {
    digits.isEmpty() -> ""
    digits.length == 1 -> digits
    digits.length == 2 -> {
      val h = digits.toIntOrNull() ?: 0
      if (h > 23) "23" else digits
    }
    digits.length == 3 -> {
      val h = digits.substring(0, 2).toIntOrNull() ?: 0
      val validH = if (h > 23) "23" else digits.substring(0, 2)
      val m1 = digits[2]
      if (m1 > '5') "$validH:5" else "$validH:$m1"
    }
    else -> {
      val h = digits.substring(0, 2).toIntOrNull() ?: 0
      val validH = if (h > 23) "23" else digits.substring(0, 2)
      val m = digits.substring(2, 4).toIntOrNull() ?: 0
      val validM = if (m > 59) "59" else digits.substring(2, 4)
      "$validH:$validM"
    }
  }
}

/**
 * Normaliza um horário incompleto ao sair do campo (ex: "7" -> "07:00", "730" -> "07:30").
 */
fun normalizeTimeOnBlur(input: String): String {
  val digits = input.filter { it.isDigit() }
  return when {
    digits.isEmpty() -> "07:00"
    digits.length == 1 -> "0${digits}:00"
    digits.length == 2 -> {
      val h = (digits.toIntOrNull() ?: 7).coerceIn(0, 23)
      String.format(Locale.US, "%02d:00", h)
    }
    digits.length == 3 -> {
      val h = (digits.substring(0, 1).toIntOrNull() ?: 7).coerceIn(0, 23)
      val m = (digits.substring(1, 3).toIntOrNull() ?: 0).coerceIn(0, 59)
      String.format(Locale.US, "%02d:%02d", h, m)
    }
    else -> {
      val h = (digits.substring(0, 2).toIntOrNull() ?: 7).coerceIn(0, 23)
      val m = (digits.substring(2, 4).toIntOrNull() ?: 0).coerceIn(0, 59)
      String.format(Locale.US, "%02d:%02d", h, m)
    }
  }
}

private val HORARIOS_RAPIDOS = listOf("06:00", "07:00", "07:30", "08:00", "13:00", "14:30")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScheduleScreen(
  viewModel: GestorUpViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val scope = rememberCoroutineScope()

  val selectedDate by viewModel.selectedDate.collectAsState()
  val escalaItens by viewModel.escalaDoDia.collectAsState()
  val anotacaoTexto by viewModel.anotacaoDoDia.collectAsState()
  val colaboradoresAtivos by viewModel.colaboradoresAtivos.collectAsState()
  val datesWithScale by viewModel.datasComEscala.collectAsState()
  val datesWithNotes by viewModel.datasComAnotacao.collectAsState()

  var showCalendarModal by remember { mutableStateOf(false) }
  var showAddColabModal by remember { mutableStateOf(false) }
  var deletingItem by remember { mutableStateOf<EscalaItem?>(null) }
  var conflitosInterjornada by remember { mutableStateOf<List<InterjornadaConflito>>(emptyList()) }

  // Estado local editável em tempo real para digitação fluida sem reconstrução lenta
  val localEntradaMap = remember { mutableStateMapOf<Long, String>() }
  val localFolgaMap = remember { mutableStateMapOf<Long, Boolean>() }
  var hasUnsavedChanges by remember { mutableStateOf(false) }
  var savedFeedback by remember { mutableStateOf(false) }

  // Sincronizar itens vindos do banco quando muda a data ou a escala
  LaunchedEffect(escalaItens, selectedDate) {
    localEntradaMap.clear()
    localFolgaMap.clear()
    escalaItens.forEach { item ->
      val isFolga = item.isFolga || item.horarioEntrada.equals("FOLGA", ignoreCase = true)
      localFolgaMap[item.id] = isFolga
      localEntradaMap[item.id] = if (isFolga) "FOLGA" else item.horarioEntrada.ifBlank { "07:00" }
    }
    hasUnsavedChanges = false
  }

  // Recalcular conflitos de interjornada
  LaunchedEffect(escalaItens, selectedDate, hasUnsavedChanges) {
    val novosConflitos = mutableListOf<InterjornadaConflito>()
    escalaItens.forEach { item ->
      val folga = localFolgaMap[item.id] ?: item.isFolga
      val entrada = localEntradaMap[item.id] ?: item.horarioEntrada
      val conflito = viewModel.verificarConflitoInterjornada(
        colaboradorId = item.colaboradorId,
        dataAtual = selectedDate,
        entrada = entrada,
        isFolga = folga,
        colaboradorNome = item.colaboradorNome
      )
      if (conflito != null) {
        novosConflitos.add(conflito)
      }
    }
    conflitosInterjornada = novosConflitos
  }

  // Salvar alterações locais em lote
  val salvarEscala = {
    val atualizados = escalaItens.map { item ->
      val folga = localFolgaMap[item.id] ?: item.isFolga
      val entrada = normalizeTimeOnBlur(localEntradaMap[item.id] ?: item.horarioEntrada)
      item.copy(
        horarioEntrada = if (folga) "FOLGA" else entrada,
        horarioSaida = "", // Não salva horário de saída na escala diária simplificada
        isFolga = folga,
        atualizadoEm = System.currentTimeMillis()
      )
    }
    viewModel.salvarEscalaLote(atualizados)
    hasUnsavedChanges = false
    savedFeedback = true
  }

  // Formatação legível da data (ex: "Quarta-feira, 28/10/2026")
  val dateFormatted = remember(selectedDate) {
    try {
      val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate) ?: Date()
      val diaSemana = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(parsed)
      val diaMes = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(parsed)
      "${diaSemana.replaceFirstChar { it.uppercase() }}, $diaMes"
    } catch (e: Exception) {
      selectedDate
    }
  }

  // Estado local para anotação
  var localAnotacao by remember(selectedDate, anotacaoTexto) { mutableStateOf(anotacaoTexto) }
  var savedAnotacaoFeedback by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .imePadding(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. BARRA DE SELEÇÃO DE DATA
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
          border = BorderStroke(1.dp, GestorUpBorder),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("date_selector_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = {
                    if (hasUnsavedChanges) salvarEscala()
                    viewModel.avancarDias(-1)
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .background(GestorUpGreenLight, CircleShape)
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Dia anterior",
                    tint = GestorUpGreenDeep,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Text(
                    text = dateFormatted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GestorUpGreenDeep
                  )
                  Text(
                    text = "Horário de Entrada do Depósito",
                    fontSize = 11.sp,
                    color = GestorUpTextSecondary
                  )
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = {
                    if (hasUnsavedChanges) salvarEscala()
                    viewModel.avancarDias(1)
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .background(GestorUpGreenLight, CircleShape)
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Próximo dia",
                    tint = GestorUpGreenDeep,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                  onClick = {
                    if (hasUnsavedChanges) salvarEscala()
                    showCalendarModal = true
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .background(GestorUpGreenSoft, CircleShape)
                ) {
                  Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Abrir calendário",
                    tint = GestorUpGreenDeep,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Status de salvamento
              if (hasUnsavedChanges) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(8.dp)
                  ) {}
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    "Alterações não salvas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                  )
                }
              } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = GestorUpGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    "Escala salva",
                    fontSize = 11.sp,
                    color = GestorUpGreen,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              OutlinedButton(
                onClick = {
                  if (hasUnsavedChanges) salvarEscala()
                  viewModel.selecionarHoje()
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                border = BorderStroke(1.dp, GestorUpGreen)
              ) {
                Text("Hoje", fontSize = 12.sp, color = GestorUpGreen, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }
      }

      // 2. CABEÇALHO DA LISTA + AÇÕES DE PREENCHIMENTO RÁPIDO
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Colaboradores (${escalaItens.size})",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              if (escalaItens.isNotEmpty()) {
                Button(
                  onClick = { salvarEscala() },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasUnsavedChanges) GestorUpGreen else GestorUpGreenLight,
                    contentColor = if (hasUnsavedChanges) Color.White else GestorUpGreenDeep
                  ),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("save_scale_button")
                ) {
                  Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (hasUnsavedChanges) "Salvar" else "Salvo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }

              Button(
                onClick = { showAddColabModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_colaborador_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          // Alertas do Detector Automático de Interjornada
          if (conflitosInterjornada.isNotEmpty()) {
            val reais = conflitosInterjornada.filter { !it.isIncompleto }
            val incompletos = conflitosInterjornada.filter { it.isIncompleto }

            Spacer(modifier = Modifier.height(8.dp))

            if (reais.isNotEmpty()) {
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      Icons.Default.WarningAmber,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onErrorContainer,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "${reais.size} alerta(s) de interjornada (< 11h de descanso):",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onErrorContainer
                    )
                  }
                  reais.forEach { c ->
                    Text(
                      text = "• ${c.nome}: Descanso de apenas ${c.descansoCalculado}h (Saída anterior: ${c.saidaAnterior} → Entrada: ${c.entradaProxima})",
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onErrorContainer,
                      modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                    )
                  }
                }
              }
            }

            if (incompletos.isNotEmpty()) {
              Spacer(modifier = Modifier.height(4.dp))
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = GestorUpGreenSoft),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = GestorUpGreenDeep,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${incompletos.size} verificação(ões) incompleta(s): horário de saída da jornada anterior não informado no sistema.",
                    fontSize = 11.sp,
                    color = GestorUpGreenDeep,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }
      }

      // BARRA DE ATALHOS RÁPIDOS DE HORÁRIOS COMUNS
      if (escalaItens.isNotEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
            border = BorderStroke(1.dp, GestorUpBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Horários frequentes do depósito:",
                fontSize = 11.sp,
                color = GestorUpTextSecondary,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(6.dp))
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(HORARIOS_RAPIDOS) { horario ->
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GestorUpGreenLight,
                    border = BorderStroke(1.dp, GestorUpGreenSoft),
                    modifier = Modifier
                      .testTag("quick_time_$horario")
                  ) {
                    Text(
                      text = horario,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = GestorUpGreenDeep,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      // ESTADO VAZIO: Botão para carregar a equipe ativa em 1 toque
      if (escalaItens.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
            border = BorderStroke(1.dp, GestorUpBorder)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(42.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Nenhum colaborador escalado para esta data",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpTextPrimary,
                textAlign = TextAlign.Center
              )
              Text(
                text = "Você pode carregar todos os colaboradores ativos de uma vez e apenas digitar os horários de entrada.",
                fontSize = 12.sp,
                color = GestorUpTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
              )

              if (colaboradoresAtivos.isNotEmpty()) {
                Button(
                  onClick = { viewModel.carregarColaboradoresAtivosParaData() },
                  colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                  modifier = Modifier.testTag("load_active_team_button")
                ) {
                  Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Carregar Equipe Ativa (${colaboradoresAtivos.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
              } else {
                OutlinedButton(
                  onClick = { showAddColabModal = true },
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text("Adicionar Colaborador", color = GestorUpGreen, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        // LISTA DE COLABORADORES COM DIGITAÇÃO DIRETA DO HORÁRIO
        itemsIndexed(escalaItens, key = { _, item -> item.id }) { index, item ->
          val isLast = index == escalaItens.size - 1
          val currentEntrada = localEntradaMap[item.id] ?: item.horarioEntrada
          val currentFolga = localFolgaMap[item.id] ?: item.isFolga
          val conflito = conflitosInterjornada.firstOrNull { it.nome == item.colaboradorNome }

          CompactColaboradorEscalaCard(
            item = item,
            entradaText = currentEntrada,
            isFolga = currentFolga,
            isLast = isLast,
            conflito = conflito,
            focusManager = focusManager,
            onEntradaChange = { novoHorario ->
              localEntradaMap[item.id] = novoHorario
              hasUnsavedChanges = true
              savedFeedback = false
            },
            onToggleFolga = { novaFolga ->
              localFolgaMap[item.id] = novaFolga
              if (novaFolga) {
                localEntradaMap[item.id] = "FOLGA"
              } else if (localEntradaMap[item.id] == "FOLGA" || localEntradaMap[item.id].isNullOrBlank()) {
                localEntradaMap[item.id] = "07:00"
              }
              hasUnsavedChanges = true
              savedFeedback = false
            },
            onDelete = { deletingItem = item }
          )
        }

        // Botão grande de Salvar na base da lista
        item {
          Button(
            onClick = { salvarEscala() },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (hasUnsavedChanges) GestorUpGreen else GestorUpGreenLight,
              contentColor = if (hasUnsavedChanges) Color.White else GestorUpGreenDeep
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("save_scale_footer_button")
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (hasUnsavedChanges) "Salvar Horários da Escala" else "✓ Todos os Horários Salvos",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // 3. BLOCO DE NOTAS / OBSERVAÇÕES DO DIA (WHATSAPP)
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
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
              Text(
                text = "Observações do dia (WhatsApp)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpGreenDeep
              )
              if (savedAnotacaoFeedback) {
                Text(
                  text = "Salvo!",
                  fontSize = 11.sp,
                  color = GestorUpGreen,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = localAnotacao,
              onValueChange = {
                localAnotacao = it
                savedAnotacaoFeedback = false
              },
              placeholder = {
                Text(
                  "Ex: Caminhão da distribuidora chega às 08:30 no portão 2. Troca de horário autorizada para Lucas.",
                  fontSize = 12.sp,
                  color = GestorUpTextSecondary
                )
              },
              minLines = 3,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("observacoes_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GestorUpGreen,
                unfocusedBorderColor = GestorUpBorder
              )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Button(
                onClick = {
                  viewModel.salvarAnotacaoDoDia(localAnotacao)
                  savedAnotacaoFeedback = true
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = GestorUpGreen,
                  contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salvar Observações", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }
            }
          }
        }
      }

      // 4. AÇÕES DE COMPARTILHAMENTO (WHATSAPP)
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = GestorUpGreenLight),
          border = BorderStroke(1.dp, GestorUpGreenSoft),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Publicação da Escala",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep
            )
            Text(
              text = "Gere a mensagem contendo apenas o horário de entrada dos colaboradores e envie no grupo do WhatsApp.",
              fontSize = 12.sp,
              color = GestorUpTextSecondary,
              modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = {
                  val msg = viewModel.gerarTextoMensagemWhatsApp(escalaItens, localAnotacao)
                  viewModel.copiarMensagem(context, msg)
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("copy_message_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GestorUpGreen)
              ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = GestorUpGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copiar Texto", color = GestorUpGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              }

              Button(
                onClick = {
                  val msg = viewModel.gerarTextoMensagemWhatsApp(escalaItens, localAnotacao)
                  viewModel.compartilharNoWhatsApp(context, msg)
                },
                modifier = Modifier
                  .weight(1.3f)
                  .testTag("share_whatsapp_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen)
              ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enviar no WhatsApp", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(20.dp)) }
    }
  }

  // --- MODAL DE CALENDÁRIO ---
  if (showCalendarModal) {
    CalendarPickerModal(
      currentSelectedDate = selectedDate,
      datesWithScale = datesWithScale,
      datesWithNotes = datesWithNotes,
      onDateSelected = { date ->
        if (hasUnsavedChanges) salvarEscala()
        viewModel.selecionarData(date)
      },
      onDismiss = { showCalendarModal = false }
    )
  }

  // --- MODAL PARA ADICIONAR COLABORADOR À ESCALA (SEM CAMPO DE SAÍDA) ---
  if (showAddColabModal) {
    AddColaboradorSimplificadoDialog(
      colaboradoresDisponiveis = colaboradoresAtivos,
      colaboradoresJaEscalados = escalaItens.map { it.colaboradorId }.toSet(),
      onConfirm = { colab, entrada, isFolga ->
        viewModel.salvarItemEscala(
          colaboradorId = colab.id,
          colaboradorNome = colab.nome,
          cargo = colab.cargo,
          horarioEntrada = entrada,
          isFolga = isFolga
        )
        showAddColabModal = false
      },
      onDismiss = { showAddColabModal = false }
    )
  }

  // --- CONFIRMAÇÃO DE EXCLUSÃO (SOMENTE DA ESCALA DO DIA) ---
  deletingItem?.let { item ->
    AlertDialog(
      onDismissRequest = { deletingItem = null },
      title = { Text("Remover da escala?", fontWeight = FontWeight.Bold) },
      text = {
        Text("Deseja remover ${item.colaboradorNome} apenas da escala de $dateFormatted? O colaborador continuará cadastrado na equipe.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.removerItemEscala(item)
            deletingItem = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Remover", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingItem = null }) {
          Text("Cancelar")
        }
      }
    )
  }
}

/**
 * Card compacto do colaborador com campo direto para digitação do horário de entrada.
 */
@Composable
private fun CompactColaboradorEscalaCard(
  item: EscalaItem,
  entradaText: String,
  isFolga: Boolean,
  isLast: Boolean,
  conflito: InterjornadaConflito?,
  focusManager: androidx.compose.ui.focus.FocusManager,
  onEntradaChange: (String) -> Unit,
  onToggleFolga: (Boolean) -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isFolga) GestorUpSurface.copy(alpha = 0.65f) else GestorUpSurface
    ),
    border = BorderStroke(
      1.dp,
      if (isFolga) GestorUpBorder.copy(alpha = 0.6f) else GestorUpBorder
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("colab_card_${item.colaboradorId}")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Esquerda: Avatar e Nome do colaborador
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(
                if (isFolga) Color.Gray.copy(alpha = 0.2f) else GestorUpGreenSoft,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = item.colaboradorNome.take(2).uppercase(),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (isFolga) Color.Gray else GestorUpGreenDeep
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = item.colaboradorNome,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isFolga) GestorUpTextSecondary else GestorUpTextPrimary
              )
              if (isFolga) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                  Text(
                    text = "FOLGA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
            if (item.cargo.isNotBlank()) {
              Text(
                text = item.cargo,
                fontSize = 11.sp,
                color = GestorUpTextSecondary
              )
            }
          }
        }

        // Direita: Botão de Folga e Botão de Excluir da escala
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedButton(
            onClick = { onToggleFolga(!isFolga) },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = if (isFolga) {
              ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            } else {
              ButtonDefaults.outlinedButtonColors()
            },
            border = BorderStroke(
              1.dp,
              if (isFolga) MaterialTheme.colorScheme.tertiary else GestorUpBorder
            ),
            modifier = Modifier.testTag("toggle_folga_${item.colaboradorId}")
          ) {
            Text(
              text = if (isFolga) "Escalar" else "Folga",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isFolga) MaterialTheme.colorScheme.onTertiaryContainer else GestorUpTextSecondary
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              Icons.Default.DeleteOutline,
              contentDescription = "Remover da escala de hoje",
              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Campo de Entrada com Digitação Direta
      if (!isFolga) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = GestorUpGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Horário de Entrada:",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = GestorUpTextPrimary
            )
          }

          // Campo numérico grande e destacado
          OutlinedTextField(
            value = entradaText,
            onValueChange = { raw ->
              val formatted = formatTimeDigits(raw)
              onEntradaChange(formatted)
            },
            modifier = Modifier
              .width(115.dp)
              .height(52.dp)
              .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                  val normalized = normalizeTimeOnBlur(entradaText)
                  if (normalized != entradaText) {
                    onEntradaChange(normalized)
                  }
                }
              }
              .testTag("time_input_${item.colaboradorId}"),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = GestorUpGreenDeep,
              textAlign = TextAlign.Center
            ),
            placeholder = {
              Text(
                "07:00",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpTextSecondary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GestorUpGreen,
              unfocusedBorderColor = GestorUpBorder,
              focusedContainerColor = GestorUpGreenSoft.copy(alpha = 0.35f),
              unfocusedContainerColor = GestorUpSurface
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = if (isLast) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
              onNext = { focusManager.moveFocus(FocusDirection.Down) },
              onDone = { focusManager.clearFocus() }
            )
          )
        }
      }

      // Alerta de interjornada para este colaborador
      if (conflito != null && !isFolga) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = if (conflito.isIncompleto) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.WarningAmber,
              contentDescription = null,
              tint = if (conflito.isIncompleto) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (conflito.isIncompleto) {
                "Verificação incompleta: horário de saída da jornada anterior não informado"
              } else {
                "Alerta: Apenas ${conflito.descansoCalculado}h de descanso desde a saída anterior (${cálculoDescanso(conflito)})"
              },
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = if (conflito.isIncompleto) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }
    }
  }
}

private fun cálculoDescanso(c: InterjornadaConflito): String {
  return "Saída ${c.dataAnterior}: ${c.saidaAnterior}"
}

/**
 * Diálogo simplificado para adicionar colaborador à escala do dia (somente nome e entrada ou folga).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddColaboradorSimplificadoDialog(
  colaboradoresDisponiveis: List<Colaborador>,
  colaboradoresJaEscalados: Set<Long>,
  onConfirm: (Colaborador, String, Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  val naoEscalados = colaboradoresDisponiveis.filter { !colaboradoresJaEscalados.contains(it.id) }

  var selectedColab by remember { mutableStateOf(naoEscalados.firstOrNull()) }
  var entrada by remember { mutableStateOf("07:00") }
  var isFolga by remember { mutableStateOf(false) }
  var dropdownExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Text(
          text = "Adicionar à Escala do Dia",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = GestorUpGreenDeep
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (naoEscalados.isEmpty()) {
          Text(
            text = "Todos os colaboradores cadastrados e ativos já foram adicionados à escala desta data.",
            fontSize = 13.sp,
            color = GestorUpTextSecondary
          )
        } else {
          Text("Selecione o colaborador:", fontSize = 12.sp, color = GestorUpTextSecondary)
          Spacer(modifier = Modifier.height(4.dp))

          ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
          ) {
            OutlinedTextField(
              value = selectedColab?.let { "${it.nome} (${it.cargo})" } ?: "Selecione...",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, enabled = true)
            )
            ExposedDropdownMenu(
              expanded = dropdownExpanded,
              onDismissRequest = { dropdownExpanded = false }
            ) {
              naoEscalados.forEach { colab ->
                DropdownMenuItem(
                  text = { Text("${colab.nome} — ${colab.cargo}") },
                  onClick = {
                    selectedColab = colab
                    dropdownExpanded = false
                  }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Opção de Folga
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Marcar como Folga", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GestorUpTextPrimary)
              Text("Não requer horário de entrada", fontSize = 11.sp, color = GestorUpTextSecondary)
            }
            Switch(
              checked = isFolga,
              onCheckedChange = { isFolga = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = GestorUpGreen,
                checkedTrackColor = GestorUpGreenSoft
              )
            )
          }

          // Horário de Entrada (se não for folga)
          if (!isFolga) {
            Spacer(modifier = Modifier.height(14.dp))
            Text("Horário de Entrada (24h)", fontSize = 12.sp, color = GestorUpTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
              value = entrada,
              onValueChange = { entrada = formatTimeDigits(it) },
              modifier = Modifier.fillMaxWidth(),
              placeholder = { Text("07:00") },
              singleLine = true,
              shape = RoundedCornerShape(8.dp),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GestorUpGreen,
                unfocusedBorderColor = GestorUpBorder
              )
            )

            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(HORARIOS_RAPIDOS) { h ->
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = GestorUpGreenLight,
                  border = BorderStroke(1.dp, GestorUpGreenSoft),
                  modifier = Modifier.clickable { entrada = h }
                ) {
                  Text(
                    text = h,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GestorUpGreenDeep,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
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
              selectedColab?.let { colab ->
                val finalEntrada = if (isFolga) "FOLGA" else normalizeTimeOnBlur(entrada)
                onConfirm(colab, finalEntrada, isFolga)
              }
            },
            enabled = selectedColab != null,
            colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Adicionar", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
