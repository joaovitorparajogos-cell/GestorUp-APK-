package com.example.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkOutline
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import com.example.data.model.Funcao
import com.example.ui.components.ManageFuncoesDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Colaborador
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen(
  viewModel: GestorUpViewModel,
  modifier: Modifier = Modifier,
) {
  val todosColaboradores by viewModel.todosColaboradores.collectAsState()
  val todasFuncoes by viewModel.todasFuncoes.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var filterStatus by remember { mutableStateOf("Todos") } // "Todos", "Ativos", "Inativos"

  var showAddEditModal by remember { mutableStateOf(false) }
  var showManageFuncoesModal by remember { mutableStateOf(false) }
  var editingColaborador by remember { mutableStateOf<Colaborador?>(null) }
  var deletingColaborador by remember { mutableStateOf<Colaborador?>(null) }

  val filteredColaboradores = remember(todosColaboradores, searchQuery, filterStatus) {
    todosColaboradores.filter { colab ->
      val matchesSearch = colab.nome.contains(searchQuery, ignoreCase = true) ||
        colab.cargo.contains(searchQuery, ignoreCase = true)
      val matchesFilter = when (filterStatus) {
        "Ativos" -> colab.ativo
        "Inativos" -> !colab.ativo
        else -> true
      }
      matchesSearch && matchesFilter
    }.sortedBy { it.nome }
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
      // 1. CABEÇALHO COM TÍTULO E BOTÃO DE CADASTRO
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
                  text = "Equipe do Depósito",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep
                )
                Text(
                  text = "${todosColaboradores.count { it.ativo }} colaboradores ativos cadastrados",
                  fontSize = 12.sp,
                  color = GestorUpTextSecondary
                )
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = {
                    editingColaborador = null
                    showAddEditModal = true
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                  modifier = Modifier
                    .weight(1.3f)
                    .testTag("novo_colaborador_button")
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Novo Colaborador",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                  )
                }
                OutlinedButton(
                  onClick = { showManageFuncoesModal = true },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, GestorUpGreen),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                  modifier = Modifier
                    .weight(0.9f)
                    .testTag("btn_gerenciar_funcoes_header")
                ) {
                  Icon(
                    imageVector = Icons.Default.WorkOutline,
                    contentDescription = null,
                    tint = GestorUpGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Funções",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GestorUpGreen,
                    maxLines = 1,
                    softWrap = false
                  )
                }
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
                  text = "Equipe do Depósito",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreenDeep
                )
                Text(
                  text = "${todosColaboradores.count { it.ativo }} colaboradores ativos",
                  fontSize = 12.sp,
                  color = GestorUpTextSecondary
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { showManageFuncoesModal = true },
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, GestorUpGreen),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                  modifier = Modifier.testTag("btn_gerenciar_funcoes_header")
                ) {
                  Icon(
                    imageVector = Icons.Default.WorkOutline,
                    contentDescription = null,
                    tint = GestorUpGreen,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Funções",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GestorUpGreen,
                    maxLines = 1,
                    softWrap = false
                  )
                }
                Button(
                  onClick = {
                    editingColaborador = null
                    showAddEditModal = true
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                  modifier = Modifier.testTag("novo_colaborador_button")
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Novo Colaborador",
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
      }

      // 2. CAMPO DE BUSCA
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Buscar por nome ou função...", fontSize = 13.sp) },
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

      // 3. CHIPS DE FILTRO (TODOS / ATIVOS / INATIVOS)
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          listOf("Todos", "Ativos", "Inativos").forEach { status ->
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

      // 4. LISTA DE COLABORADORES
      if (filteredColaboradores.isEmpty()) {
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
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = GestorUpTextSecondary,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Nenhum colaborador encontrado",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GestorUpTextPrimary
              )
              Text(
                text = "Tente alterar os termos de busca ou filtros aplicados.",
                fontSize = 12.sp,
                color = GestorUpTextSecondary
              )
            }
          }
        }
      } else {
        items(filteredColaboradores, key = { it.id }) { colab ->
          ColaboradorCard(
            colaborador = colab,
            onToggleAtivo = { viewModel.alternarStatusColaborador(colab) },
            onEdit = {
              editingColaborador = colab
              showAddEditModal = true
            },
            onDelete = { deletingColaborador = colab }
          )
        }
      }
    }
  }

  // MODAL CADASTRO / EDIÇÃO
  if (showAddEditModal) {
    AddEditColaboradorDialog(
      initial = editingColaborador,
      funcoesDisponiveis = todasFuncoes,
      onOpenManageFuncoes = { showManageFuncoesModal = true },
      onConfirm = { nome, cargo, vinculo, telefone, ativo, matricula, dataCadastro, observacoes ->
        viewModel.salvarColaborador(
          id = editingColaborador?.id ?: 0L,
          nome = nome,
          cargo = cargo,
          vinculo = vinculo,
          telefone = telefone,
          ativo = ativo,
          matricula = matricula,
          dataCadastro = dataCadastro,
          observacoes = observacoes
        )
        showAddEditModal = false
        editingColaborador = null
      },
      onDismiss = {
        showAddEditModal = false
        editingColaborador = null
      }
    )
  }

  // MODAL GERENCIAR FUNÇÕES / CARGOS
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

  // CONFIRMAÇÃO DE EXCLUSÃO
  deletingColaborador?.let { colab ->
    AlertDialog(
      onDismissRequest = { deletingColaborador = null },
      title = { Text("Excluir colaborador?", fontWeight = FontWeight.Bold) },
      text = {
        Text("Deseja realmente remover ${colab.nome}? Recomendamos desativar o colaborador para preservar o histórico das escalas passadas.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deletarColaborador(colab)
            deletingColaborador = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Excluir de verdade", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingColaborador = null }) {
          Text("Cancelar")
        }
      }
    )
  }
}

@Composable
private fun ColaboradorCard(
  colaborador: Colaborador,
  onToggleAtivo: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
    border = BorderStroke(1.dp, GestorUpBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .background(
            if (colaborador.ativo) GestorUpGreenSoft else GestorUpBorder,
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = colaborador.nome.take(2).uppercase(),
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = if (colaborador.ativo) GestorUpGreenDeep else GestorUpTextSecondary
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = colaborador.nome,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = GestorUpTextPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .background(
                if (colaborador.ativo) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                RoundedCornerShape(4.dp)
              )
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (colaborador.ativo) "Ativo" else "Inativo",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (colaborador.ativo) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
          }
        }

        Text(
          text = "${colaborador.cargo} • ${colaborador.vinculo}",
          fontSize = 12.sp,
          color = GestorUpTextSecondary
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onEdit) {
          Icon(Icons.Default.Edit, contentDescription = "Editar", tint = GestorUpGreen)
        }
        IconButton(onClick = onDelete) {
          Icon(Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = GestorUpTextSecondary)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditColaboradorDialog(
  initial: Colaborador?,
  funcoesDisponiveis: List<Funcao> = emptyList(),
  onOpenManageFuncoes: () -> Unit = {},
  onConfirm: (nome: String, cargo: String, vinculo: String, telefone: String, ativo: Boolean, matricula: String, dataCadastro: Long, observacoes: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var nome by remember { mutableStateOf(initial?.nome ?: "") }
  var cargo by remember { mutableStateOf(initial?.cargo ?: "Repositor de Mercadorias") }
  var vinculo by remember { mutableStateOf(initial?.vinculo ?: "CLT") }
  var telefone by remember { mutableStateOf(initial?.telefone ?: "") }
  var ativo by remember { mutableStateOf(initial?.ativo ?: true) }
  var matricula by remember { mutableStateOf(initial?.matricula ?: "") }
  var observacoes by remember { mutableStateOf(initial?.observacoes ?: "") }

  val listaCargos = remember(funcoesDisponiveis) {
    if (funcoesDisponiveis.isNotEmpty()) {
      funcoesDisponiveis.map { it.nome }
    } else {
      listOf(
        "Encarregado de Depósito",
        "Conferente de Entrada",
        "Conferente de Saída",
        "Operador de Empilhadeira",
        "Repositor de Mercadorias",
        "Separador de Carga",
        "Auxiliar de Depósito"
      )
    }
  }

  var cargoDropdownExpanded by remember { mutableStateOf(false) }

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
          text = if (initial == null) "Novo Colaborador" else "Editar Colaborador",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = GestorUpGreenDeep
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = nome,
          onValueChange = { nome = it },
          label = { Text("Nome completo") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
           OutlinedTextField(
              value = matricula,
              onValueChange = { matricula = it },
              label = { Text("Matrícula") },
              modifier = Modifier.weight(1f)
           )
           OutlinedTextField(
              value = telefone,
              onValueChange = { telefone = it },
              label = { Text("Telefone") },
              modifier = Modifier.weight(1f)
           )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Função / Cargo",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GestorUpTextSecondary
          )
          Text(
            text = "+ Gerenciar Funções",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GestorUpGreen,
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .clickable { onOpenManageFuncoes() }
              .padding(horizontal = 4.dp, vertical = 2.dp)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))

        ExposedDropdownMenuBox(
          expanded = cargoDropdownExpanded,
          onExpandedChange = { cargoDropdownExpanded = it }
        ) {
          OutlinedTextField(
            value = cargo,
            onValueChange = { cargo = it },
            label = { Text("Selecione a função") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cargoDropdownExpanded) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor()
          )
          ExposedDropdownMenu(
            expanded = cargoDropdownExpanded,
            onDismissRequest = { cargoDropdownExpanded = false }
          ) {
            listaCargos.forEach { c ->
              DropdownMenuItem(
                text = { Text(c) },
                onClick = {
                  cargo = c
                  cargoDropdownExpanded = false
                }
              )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
              text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GestorUpGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Gerenciar / Adicionar Função...",
                    color = GestorUpGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                }
              },
              onClick = {
                cargoDropdownExpanded = false
                onOpenManageFuncoes()
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("CLT", "Freelancer", "Temporário").forEach { v ->
            val isSelected = vinculo == v
            androidx.compose.material3.Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) GestorUpGreenSoft else Color.Transparent,
              border = BorderStroke(
                1.dp,
                if (isSelected) GestorUpGreen else MaterialTheme.colorScheme.outlineVariant
              ),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clickable { vinculo = v }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp)
              ) {
                Text(
                  text = v,
                  fontSize = 11.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) GestorUpGreenDeep else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  softWrap = false,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
           value = observacoes,
           onValueChange = { observacoes = it },
           label = { Text("Observações internas") },
           modifier = Modifier.fillMaxWidth(),
           minLines = 2
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (ativo) "Colaborador Ativo" else "Colaborador Inativo",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
          Switch(
            checked = ativo,
            onCheckedChange = { ativo = it },
            colors = SwitchDefaults.colors(checkedThumbColor = GestorUpGreen)
          )
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
              if (nome.isNotBlank()) {
                onConfirm(nome, cargo, vinculo, telefone, ativo, matricula, initial?.dataCadastro ?: System.currentTimeMillis(), observacoes)
              }
            },
            enabled = nome.isNotBlank(),
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
