package com.example.data.repository

import com.example.data.dao.GestorUpDao
import com.example.data.model.AnotacaoDiaria
import com.example.data.model.Colaborador
import com.example.data.model.EscalaItem
import com.example.data.model.EscalaMensal
import com.example.data.model.Funcao
import com.example.data.model.Pendencia
import kotlinx.coroutines.flow.Flow

class GestorUpRepository(private val dao: GestorUpDao) {

  // Colaboradores
  val todosColaboradores: Flow<List<Colaborador>> = dao.getAllColaboradores()
  val colaboradoresAtivos: Flow<List<Colaborador>> = dao.getColaboradoresAtivos()

  suspend fun salvarColaborador(colaborador: Colaborador): Long {
    return if (colaborador.id == 0L) {
      dao.insertColaborador(colaborador)
    } else {
      dao.updateColaborador(colaborador)
      colaborador.id
    }
  }

  suspend fun deletarColaborador(colaborador: Colaborador) = dao.deleteColaborador(colaborador)

  // Escala Diária
  fun getEscalaDoDia(dataRef: String): Flow<List<EscalaItem>> = dao.getEscalaForDate(dataRef)
  suspend fun getEscalaDoDiaSync(dataRef: String): List<EscalaItem> = dao.getEscalaForDateSync(dataRef)
  val datasComEscala: Flow<List<String>> = dao.getDatasComEscala()

  suspend fun salvarItemEscala(item: EscalaItem): Long {
    return if (item.id == 0L) {
      dao.insertEscalaItem(item)
    } else {
      dao.updateEscalaItem(item.copy(atualizadoEm = System.currentTimeMillis()))
      item.id
    }
  }

  suspend fun salvarItensEscala(itens: List<EscalaItem>) {
    itens.forEach { salvarItemEscala(it) }
  }

  suspend fun removerItemEscala(item: EscalaItem) = dao.deleteEscalaItem(item)
  suspend fun removerItemEscalaPorId(id: Long) = dao.deleteEscalaItemById(id)
  suspend fun getEscalaForCollaboratorRange(colaboradorId: Long, startDate: String, endDate: String) = 
      dao.getEscalaForCollaboratorRange(colaboradorId, startDate, endDate)

  // Anotações Diárias
  fun getAnotacaoDoDia(dataRef: String): Flow<AnotacaoDiaria?> = dao.getAnotacaoForDate(dataRef)
  suspend fun getAnotacaoDoDiaSync(dataRef: String): AnotacaoDiaria? = dao.getAnotacaoForDateSync(dataRef)
  val datasComAnotacao: Flow<List<String>> = dao.getDatasComAnotacao()

  suspend fun salvarAnotacao(dataRef: String, texto: String, autor: String = "Gestor") {
    dao.upsertAnotacao(
      AnotacaoDiaria(
        dataRef = dataRef,
        texto = texto,
        atualizadoEm = System.currentTimeMillis(),
        autor = autor
      )
    )
  }

  // Escala Mensal (Anexo de imagem)
  fun getEscalaMensal(mesAno: String): Flow<EscalaMensal?> = dao.getEscalaMensal(mesAno)

  suspend fun salvarEscalaMensal(mesAno: String, imageUri: String, notas: String = "") {
    dao.upsertEscalaMensal(
      EscalaMensal(
        mesAno = mesAno,
        imageUri = imageUri,
        enviadoEm = System.currentTimeMillis(),
        notas = notas
      )
    )
  }

  suspend fun removerEscalaMensal(mesAno: String) = dao.deleteEscalaMensal(mesAno)

  // Pendências
  val todasPendencias: Flow<List<Pendencia>> = dao.getAllPendencias()
  val pendenciasAbertas: Flow<List<Pendencia>> = dao.getPendenciasAbertas()
  val contagemPendenciasAbertas: Flow<Int> = dao.getContagemPendenciasAbertas()

  suspend fun salvarPendencia(pendencia: Pendencia): Long {
    return if (pendencia.id == 0L) {
      dao.insertPendencia(pendencia)
    } else {
      dao.updatePendencia(pendencia)
      pendencia.id
    }
  }

  suspend fun alternarResolvido(pendencia: Pendencia) {
    val novoStatus = !pendencia.resolvido
    val resolvidoEm = if (novoStatus) System.currentTimeMillis() else null
    dao.setPendenciaResolvida(pendencia.id, novoStatus, resolvidoEm)
  }

  suspend fun deletarPendencia(pendencia: Pendencia) = dao.deletePendencia(pendencia)

  // Funções / Cargos
  val todasFuncoes: Flow<List<Funcao>> = dao.getAllFuncoes()

  suspend fun salvarFuncao(funcao: Funcao): Long {
    return if (funcao.id == 0L) {
      dao.insertFuncao(funcao)
    } else {
      dao.updateFuncao(funcao)
      funcao.id
    }
  }

  suspend fun renomearFuncao(funcao: Funcao, novoNome: String) {
    val nomeAntigo = funcao.nome
    dao.updateFuncao(funcao.copy(nome = novoNome))
    if (nomeAntigo != novoNome) {
      dao.updateCargoInColaboradores(nomeAntigo, novoNome)
      dao.updateCargoInEscalas(nomeAntigo, novoNome)
    }
  }

  suspend fun deletarFuncao(funcao: Funcao) {
    dao.deleteFuncao(funcao)
  }
}

