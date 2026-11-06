package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.model.Funcao
import com.example.ui.theme.GestorUpBorder
import com.example.ui.theme.GestorUpGreen
import com.example.ui.theme.GestorUpGreenDeep
import com.example.ui.theme.GestorUpGreenLight
import com.example.ui.theme.GestorUpGreenSoft
import com.example.ui.theme.GestorUpSurface
import com.example.ui.theme.GestorUpTextPrimary
import com.example.ui.theme.GestorUpTextSecondary

@Composable
fun ManageFuncoesDialog(
  funcoes: List<Funcao>,
  colaboradores: List<Colaborador> = emptyList(),
  onAddFuncao: (nome: String) -> Unit,
  onEditFuncao: (funcao: Funcao, novoNome: String) -> Unit,
  onDeleteFuncao: (funcao: Funcao) -> Unit,
  onDismiss: () -> Unit,
) {
  var novoNomeFuncao by remember { mutableStateOf("") }
  var editingFuncao by remember { mutableStateOf<Funcao?>(null) }
  var deletingFuncao by remember { mutableStateOf<Funcao?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = GestorUpSurface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 16.dp)
        .testTag("manage_funcoes_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Cabeçalho
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(GestorUpGreenSoft, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.WorkOutline,
                contentDescription = null,
                tint = GestorUpGreen,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Funções e Cargos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpGreenDeep
              )
              Text(
                text = "${funcoes.size} funções cadastradas",
                fontSize = 12.sp,
                color = GestorUpTextSecondary
              )
            }
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Fechar",
              tint = GestorUpTextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Área de Cadastro / Edição
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, if (editingFuncao != null) GestorUpGreen else GestorUpBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            if (editingFuncao != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Editando Função",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = GestorUpGreen
                )
                Text(
                  text = "Cancelar",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                      editingFuncao = null
                      novoNomeFuncao = ""
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
            } else {
              Text(
                text = "Adicionar Nova Função",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GestorUpTextSecondary
              )
              Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = novoNomeFuncao,
                onValueChange = { novoNomeFuncao = it },
                placeholder = {
                  Text(
                    text = if (editingFuncao != null) "Novo nome..." else "Ex: Motorista, Auxiliar...",
                    fontSize = 13.sp
                  )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = GestorUpGreen,
                  unfocusedBorderColor = GestorUpBorder
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("input_nome_funcao")
              )

              Button(
                onClick = {
                  val clean = novoNomeFuncao.trim()
                  if (clean.isNotBlank()) {
                    val currentEdit = editingFuncao
                    if (currentEdit != null) {
                      onEditFuncao(currentEdit, clean)
                      editingFuncao = null
                    } else {
                      onAddFuncao(clean)
                    }
                    novoNomeFuncao = ""
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                enabled = novoNomeFuncao.isNotBlank(),
                modifier = Modifier.testTag("btn_salvar_funcao")
              ) {
                Icon(
                  imageVector = if (editingFuncao != null) Icons.Default.Check else Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (editingFuncao != null) "Salvar" else "Adicionar",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Funções Disponíveis",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = GestorUpTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lista de Funções Cadastradas
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 280.dp)
        ) {
          if (funcoes.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Nenhuma função cadastrada ainda.\nAdicione a primeira função acima!",
                fontSize = 13.sp,
                color = GestorUpTextSecondary,
                lineHeight = 18.sp
              )
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(funcoes, key = { it.id }) { funcao ->
                val colabCount = colaboradores.count { it.cargo.equals(funcao.nome, ignoreCase = true) }
                val isBeingEdited = editingFuncao?.id == funcao.id

                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isBeingEdited) GestorUpGreenSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                  border = BorderStroke(
                    1.dp,
                    if (isBeingEdited) GestorUpGreen else GestorUpBorder.copy(alpha = 0.5f)
                  ),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = funcao.nome,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isBeingEdited) GestorUpGreenDeep else GestorUpTextPrimary
                      )
                      Text(
                        text = if (colabCount == 0) "Nenhum colaborador vinculado" else "$colabCount colaborador(es) na equipe",
                        fontSize = 11.sp,
                        color = GestorUpTextSecondary
                      )
                    }

                    Row(
                      horizontalArrangement = Arrangement.spacedBy(4.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      // Botão Editar
                      IconButton(
                        onClick = {
                          editingFuncao = funcao
                          novoNomeFuncao = funcao.nome
                        },
                        modifier = Modifier
                          .size(32.dp)
                          .testTag("btn_edit_funcao_${funcao.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Edit,
                          contentDescription = "Editar função ${funcao.nome}",
                          tint = GestorUpGreen,
                          modifier = Modifier.size(17.dp)
                        )
                      }

                      // Botão Excluir
                      IconButton(
                        onClick = { deletingFuncao = funcao },
                        modifier = Modifier
                          .size(32.dp)
                          .testTag("btn_delete_funcao_${funcao.id}")
                      ) {
                        Icon(
                          imageVector = Icons.Default.DeleteOutline,
                          contentDescription = "Excluir função ${funcao.nome}",
                          tint = MaterialTheme.colorScheme.error,
                          modifier = Modifier.size(17.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = GestorUpGreen),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("btn_concluir_funcoes")
        ) {
          Text(
            text = "Concluir",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }

  // Diálogo de confirmação para exclusão
  deletingFuncao?.let { f ->
    val affectedCount = colaboradores.count { it.cargo.equals(f.nome, ignoreCase = true) }
    AlertDialog(
      onDismissRequest = { deletingFuncao = null },
      title = { Text("Excluir função?", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Tem certeza que deseja excluir a função \"${f.nome}\"?")
          if (affectedCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Atenção: Há $affectedCount colaborador(es) com essa função cadastrada. Eles manterão o histórico.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.error
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteFuncao(f)
            if (editingFuncao?.id == f.id) {
              editingFuncao = null
              novoNomeFuncao = ""
            }
            deletingFuncao = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Excluir", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { deletingFuncao = null }) {
          Text("Cancelar")
        }
      }
    )
  }
}
