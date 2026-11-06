package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Colaborador do depósito (ex: Encarregado, Conferente, Repositor, Operador de Empilhadeira, etc.).
 */
@Entity(tableName = "colaboradores")
data class Colaborador(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val nome: String,
  val cargo: String,
  val vinculo: String = "CLT", // CLT, Freelancer, Temporário
  val telefone: String = "",
  val ativo: Boolean = true,
  val ordem: Int = 0,
  val matricula: String = "",
  val dataCadastro: Long = System.currentTimeMillis(),
  val observacoes: String = "",
)

/**
 * Item individual de escala diária do depósito.
 * Cada colaborador possui horário de entrada e saída próprio definido pelo gestor.
 */
@Entity(
  tableName = "escalas_diarias",
  indices = [Index(value = ["dataRef"])]
)
data class EscalaItem(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val dataRef: String, // Formato "yyyy-MM-dd"
  val colaboradorId: Long,
  val colaboradorNome: String,
  val cargo: String = "",
  val horarioEntrada: String, // Formato 24h ex: "07:00" ou "FOLGA"
  val horarioSaida: String = "",   // Mantido no banco por compatibilidade
  val isFolga: Boolean = false,
  val ordem: Int = 0,
  val criadoEm: Long = System.currentTimeMillis(),
  val atualizadoEm: Long = System.currentTimeMillis(),
)

/**
 * Anotações e observações diárias vinculadas a uma data específica.
 * Funciona como o bloco de notas da mensagem do WhatsApp.
 */
@Entity(tableName = "anotacoes_diarias")
data class AnotacaoDiaria(
  @PrimaryKey
  val dataRef: String, // Formato "yyyy-MM-dd"
  val texto: String = "",
  val atualizadoEm: Long = System.currentTimeMillis(),
  val autor: String = "Gestor",
)

/**
 * Registro da imagem anexada para a escala mensal oficial.
 */
@Entity(tableName = "escalas_mensais")
data class EscalaMensal(
  @PrimaryKey
  val mesAno: String, // Formato "yyyy-MM" (ex: "2026-10")
  val imageUri: String, // Caminho local persistido ou URI do arquivo
  val enviadoEm: Long = System.currentTimeMillis(),
  val autor: String = "Gestor",
  val notas: String = "",
)

/**
 * Pendências operacionais e lembretes para o gestor lembrar de encaminhar ou resolver.
 */
@Entity(tableName = "pendencias")
data class Pendencia(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val titulo: String,
  val descricao: String = "",
  val categoria: String = "Outros", // "Colaboradores", "Documentos", "Freelancers", "Tesouraria", "Escalas", "Outros"
  val prioridade: String = "Média", // "Baixa", "Média", "Alta", "Urgente"
  val prazo: String? = null,
  val resolvido: Boolean = false,
  val criadoEm: Long = System.currentTimeMillis(),
  val resolvidoEm: Long? = null,
  val autor: String = "Gestor",
)

/**
 * Função / Cargo da equipe do depósito (ex: Repositor, Conferente, Operador de Empilhadeira, etc.).
 * O gestor pode adicionar, editar/renomear e excluir funções livremente.
 */
@Entity(tableName = "funcoes")
data class Funcao(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val nome: String,
  val ordem: Int = 0,
  val criadaEm: Long = System.currentTimeMillis(),
)

