package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.widthIn
import com.example.data.model.Pendencia
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
fun PendingScreen(
  viewModel: GestorUpViewModel,
  modifier: Modifier = Modifier,
) {
  val todasPendencias by viewModel.todasPendencias.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var filterStatus by remember { mutableStateOf("Abertas") } // "Abertas", "Resolvidas", "Todas"
  var selectedCategory by remember { mutableStateOf("Todas") }

  var showAddEditModal by remember { mutableStateOf(false) }
  var editingPendencia by remember { mutableStateOf<Pendencia?>(null) }
  var deletingPendencia by remember { mutableStateOf<Pendencia?>(null) }

  val categorias = listOf("Todas", "Tesouraria", "Colaboradores", "Documentos", "Freelancers", "Escalas", "Outros")

  val filteredPendencias = remember(todasPendencias, searchQuery, filterStatus, selectedCategory) {
    todasPendencias.filter { p ->
      val matchesSearch = p.titulo.contains(searchQuery, ignoreCase = true) ||
        p.descricao.contains(searchQuery, ignoreCase = true)

      val matchesStatus = when (filterStatus) {
        "Abertas" -> !p.resolvido
        "Resolvidas" -> p.resolvido
        else -> true
      }

      val matchesCategory = if (selectedCategory == "Todas") true else p.categoria.equals(selectedCategory, ignoreCase = true)

      matchesSearch && matchesStatus && matchesCategory
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(GestorUpBackground)
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. CABEÇALHO
      item {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
          val isNarrow = maxWidth < 380.dp
          if (isNarrow) {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Column {
                Text(
                  text = "Lembretes & Pendências",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep
                )
                val abertasCount = todasPendencias.count { !it.resolvido }
                Text(
                  text = "$abertasCount pendências abertas no depósito",
                  fontSize = 12.sp,
                  color = GestorUpTextSecondary
                )
              }
              Button(
                onClick = {
                  editingPendencia = null
                  showAddEditModal = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("nova_pendencia_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Nova Pendência",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Lembretes & Pendências",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep
                )
                val abertasCount = todasPendencias.count { !it.resolvido }
                Text(
                  text = "$abertasCount pendências abertas",
                  fontSize = 12.sp,
                  color = GestorUpTextSecondary
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Button(
                onClick = {
                  editingPendencia = null
                  showAddEditModal = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("nova_pendencia_button")
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Nova Pendência",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }
        }
      }

      // 2. CAMPO DE BUSCA
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Buscar lembretes e tarefas...", fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GestorUpGreen) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = GestorUpSurface,
            unfocusedContainerColor = GestorUpSurface,
            focusedBorderColor = GestorUpGreen,
            unfocusedBorderColor = GestorUpBorder
          )
        )
      }

      // 3. FILTROS DE SITUAÇÃO (ABERTAS / RESOLVIDAS / TODAS)
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Abertas", "Resolvidas", "Todas").forEach { status ->
            val isSelected = filterStatus == status
            FilterChip(
              selected = isSelected,
              onClick = { filterStatus = status },
              label = { Text(status, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GestorUpGreenSoft,
                selectedLabelColor = GestorUpGreenDeep
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = if (isSelected) GestorUpGreen else GestorUpBorder,
                enabled = true,
                selected = isSelected
              )
            )
          }
        }
      }

      // 4. CHIPS DE CATEGORIAS
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(categorias) { cat ->
            val isSelected = selectedCategory == cat
            FilterChip(
              selected = isSelected,
              onClick = { selectedCategory = cat },
              label = { Text(cat, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GestorUpGreenLight,
                selectedLabelColor = GestorUpGreenDeep
              )
            )
          }
        }
      }

      // 5. LISTA DE PENDÊNCIAS
      if (filteredPendencias.isEmpty()) {
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
                .padding(horizontal = 20.dp, vertical = 20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = if (filterStatus == "Abertas") "Nenhuma pendência aberta!" else "Nenhum registro encontrado",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpGreenDeep,
                textAlign = TextAlign.Center
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (filterStatus == "Abertas")
                  "Tudo em ordem. Use o botão 'Nova Pendência' para criar lembretes para a equipe ou tesouraria."
                else "Tente ajustar os filtros ou a busca.",
                fontSize = 12.sp,
                color = GestorUpTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
              )
            }
          }
        }
      } else {
        items(filteredPendencias, key = { it.id }) { pendencia ->
          PendenciaCard(
            pendencia = pendencia,
            onToggleResolvido = { viewModel.alternarResolvidoPendencia(pendencia) },
            onEdit = {
              editingPendencia = pendencia
              showAddEditModal = true
            },
            onDelete = { deletingPendencia = pendencia }
          )
        }
      }
    }
  }

  // MODAL CADASTRO / EDIÇÃO
  if (showAddEditModal) {
    AddEditPendenciaDialog(
      initial = editingPendencia,
      onConfirm = { titulo, desc, cat, prioridade, prazo ->
        viewModel.salvarPendencia(
          id = editingPendencia?.id ?: 0L,
          titulo = titulo,
          descricao = desc,
          categoria = cat,
          prioridade = prioridade,
          prazo = prazo
        )
        showAddEditModal = false
        editingPendencia = null
      },
      onDismiss = {
        showAddEditModal = false
        editingPendencia = null
      }
    )
  }

  // CONFIRMAÇÃO DE EXCLUSÃO
  deletingPendencia?.let { p ->
    AlertDialog(
      onDismissRequest = { deletingPendencia = null },
      title = { Text("Excluir pendência?", fontWeight = FontWeight.Bold) },
      text = { Text("Deseja remover o lembrete \"${p.titulo}\"?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deletarPendencia(p)
            deletingPendencia = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Excluir de verdade", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingPendencia = null }) {
          Text("Cancelar")
        }
      }
    )
  }
}

@Composable
private fun PendenciaCard(
  pendencia: Pendencia,
  onToggleResolvido: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val cardBg by animateColorAsState(
    if (pendencia.resolvido) GestorUpSurface.copy(alpha = 0.65f) else GestorUpSurface,
    label = "cardBg"
  )

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = cardBg),
    border = BorderStroke(1.dp, if (pendencia.resolvido) GestorUpBorder.copy(alpha = 0.5f) else GestorUpBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = if (pendencia.resolvido) 0.dp else 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.Top
    ) {
      // Botão de conclusão com feedback
      IconButton(
        onClick = onToggleResolvido,
        modifier = Modifier.size(36.dp)
      ) {
        if (pendencia.resolvido) {
          Icon(Icons.Default.CheckCircle, contentDescription = "Resolvida", tint = GestorUpGreen, modifier = Modifier.size(24.dp))
        } else {
          Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Marcar como resolvida", tint = GestorUpTextSecondary, modifier = Modifier.size(24.dp))
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = pendencia.titulo,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = if (pendencia.resolvido) GestorUpTextSecondary else GestorUpTextPrimary,
          textDecoration = if (pendencia.resolvido) TextDecoration.LineThrough else TextDecoration.None
        )

        if (pendencia.descricao.isNotBlank()) {
          Text(
            text = pendencia.descricao,
            fontSize = 12.sp,
            color = GestorUpTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Badges: Categoria e Prioridade
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Badge Categoria (ex: Tesouraria, Documentos, etc.)
          Box(
            modifier = Modifier
              .background(
                if (pendencia.categoria == "Tesouraria") Color(0xFFE65100).copy(alpha = 0.15f) else GestorUpGreenLight,
                RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = pendencia.categoria,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (pendencia.categoria == "Tesouraria") Color(0xFFFFA726) else GestorUpGreenDeep
            )
          }

          // Badge Prioridade
          val prioridadeColor = when (pendencia.prioridade) {
            "Urgente" -> Color(0xFFC03939)
            "Alta" -> Color(0xFFA96508)
            "Média" -> GestorUpGreen
            else -> GestorUpTextSecondary
          }
          Box(
            modifier = Modifier
              .background(prioridadeColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = pendencia.prioridade,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = prioridadeColor
            )
          }

          // Data de conclusão, se resolvida
          if (pendencia.resolvido && pendencia.resolvidoEm != null) {
            val resolvidoData = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(pendencia.resolvidoEm))
            Text(text = "Resolvido em $resolvidoData", fontSize = 10.sp, color = GestorUpGreen)
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onEdit) {
          Icon(Icons.Default.Edit, contentDescription = "Editar", tint = GestorUpGreen, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = GestorUpTextSecondary, modifier = Modifier.size(20.dp))
        }
      }
    }
  }
}

@Composable
private fun AddEditPendenciaDialog(
  initial: Pendencia?,
  onConfirm: (titulo: String, descricao: String, categoria: String, prioridade: String, prazo: String?) -> Unit,
  onDismiss: () -> Unit,
) {
  var titulo by remember { mutableStateOf(initial?.titulo ?: "") }
  var descricao by remember { mutableStateOf(initial?.descricao ?: "") }
  var categoria by remember { mutableStateOf(initial?.categoria ?: "Tesouraria") }
  var prioridade by remember { mutableStateOf(initial?.prioridade ?: "Alta") }

  val categorias = listOf("Tesouraria", "Colaboradores", "Documentos", "Freelancers", "Escalas", "Outros")
  val prioridades = listOf("Baixa", "Média", "Alta", "Urgente")

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .widthIn(max = 440.dp)
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        Text(
          text = if (initial == null) "Nova Pendência / Lembrete" else "Editar Pendência",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = GestorUpGreenDeep
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = titulo,
          onValueChange = { titulo = it },
          label = { Text("O que precisa ser feito ou lembrado?") },
          placeholder = { Text("Ex: Encaminhar horas do freelancer para tesouraria") },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = descricao,
          onValueChange = { descricao = it },
          label = { Text("Detalhes / Anotações (opcional)") },
          placeholder = { Text("Informações de contato, valores ou orientações") },
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Categoria:", fontSize = 12.sp, color = GestorUpTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(categorias) { cat ->
            FilterChip(
              selected = categoria == cat,
              onClick = { categoria = cat },
              label = { Text(cat, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GestorUpGreenSoft,
                selectedLabelColor = GestorUpGreenDeep
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Prioridade:", fontSize = 12.sp, color = GestorUpTextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          prioridades.forEach { p ->
            FilterChip(
              selected = prioridade == p,
              onClick = { prioridade = p },
              modifier = Modifier.weight(1f),
              label = {
                Text(
                  text = p,
                  fontSize = 10.5.sp,
                  fontWeight = if (prioridade == p) FontWeight.Bold else FontWeight.Normal,
                  textAlign = TextAlign.Center,
                  maxLines = 1,
                  softWrap = false,
                  modifier = Modifier.fillMaxWidth()
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = GestorUpGreenSoft,
                selectedLabelColor = GestorUpGreenDeep
              )
            )
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
              if (titulo.isNotBlank()) {
                onConfirm(titulo, descricao, categoria, prioridade, null)
              }
            },
            enabled = titulo.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
