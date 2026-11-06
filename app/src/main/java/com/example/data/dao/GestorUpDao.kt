package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.data.model.AnotacaoDiaria
import com.example.data.model.Colaborador
import com.example.data.model.EscalaItem
import com.example.data.model.EscalaMensal
import com.example.data.model.Funcao
import com.example.data.model.Pendencia
import kotlinx.coroutines.flow.Flow

@Dao
interface GestorUpDao {

  // --- COLABORADORES ---
  @Query("SELECT * FROM colaboradores ORDER BY ativo DESC, ordem ASC, nome ASC")
  fun getAllColaboradores(): Flow<List<Colaborador>>

  @Query("SELECT * FROM colaboradores WHERE ativo = 1 ORDER BY ordem ASC, nome ASC")
  fun getColaboradoresAtivos(): Flow<List<Colaborador>>

  @Query("SELECT * FROM colaboradores WHERE id = :id LIMIT 1")
  suspend fun getColaboradorById(id: Long): Colaborador?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertColaborador(colaborador: Colaborador): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertColaboradores(colaboradores: List<Colaborador>)

  @Update
  suspend fun updateColaborador(colaborador: Colaborador)

  @Delete
  suspend fun deleteColaborador(colaborador: Colaborador)

  @Query("SELECT COUNT(*) FROM colaboradores")
  suspend fun getColaboradoresCount(): Int


  // --- ESCALAS DIÁRIAS ---
  @Query("SELECT * FROM escalas_diarias WHERE dataRef = :dataRef ORDER BY ordem ASC, horarioEntrada ASC")
  fun getEscalaForDate(dataRef: String): Flow<List<EscalaItem>>

  @Query("SELECT * FROM escalas_diarias WHERE colaboradorId = :colaboradorId AND dataRef BETWEEN :startDate AND :endDate ORDER BY dataRef ASC, horarioEntrada ASC")
  suspend fun getEscalaForCollaboratorRange(colaboradorId: Long, startDate: String, endDate: String): List<EscalaItem>


  @Query("SELECT * FROM escalas_diarias WHERE dataRef = :dataRef ORDER BY ordem ASC, horarioEntrada ASC")
  suspend fun getEscalaForDateSync(dataRef: String): List<EscalaItem>

  @Query("SELECT DISTINCT dataRef FROM escalas_diarias")
  fun getDatasComEscala(): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEscalaItem(item: EscalaItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEscalaItems(items: List<EscalaItem>)

  @Update
  suspend fun updateEscalaItem(item: EscalaItem)

  @Delete
  suspend fun deleteEscalaItem(item: EscalaItem)

  @Query("DELETE FROM escalas_diarias WHERE id = :id")
  suspend fun deleteEscalaItemById(id: Long)

  @Query("DELETE FROM escalas_diarias WHERE dataRef = :dataRef")
  suspend fun deleteEscalaForDate(dataRef: String)


  // --- ANOTAÇÕES DIÁRIAS ---
  @Query("SELECT * FROM anotacoes_diarias WHERE dataRef = :dataRef LIMIT 1")
  fun getAnotacaoForDate(dataRef: String): Flow<AnotacaoDiaria?>

  @Query("SELECT * FROM anotacoes_diarias WHERE dataRef = :dataRef LIMIT 1")
  suspend fun getAnotacaoForDateSync(dataRef: String): AnotacaoDiaria?

  @Query("SELECT DISTINCT dataRef FROM anotacoes_diarias WHERE LENGTH(texto) > 0")
  fun getDatasComAnotacao(): Flow<List<String>>

  @Upsert
  suspend fun upsertAnotacao(anotacao: AnotacaoDiaria)


  // --- ESCALA MENSAL (ANEXO DE IMAGEM) ---
  @Query("SELECT * FROM escalas_mensais WHERE mesAno = :mesAno LIMIT 1")
  fun getEscalaMensal(mesAno: String): Flow<EscalaMensal?>

  @Upsert
  suspend fun upsertEscalaMensal(escala: EscalaMensal)

  @Query("DELETE FROM escalas_mensais WHERE mesAno = :mesAno")
  suspend fun deleteEscalaMensal(mesAno: String)


  // --- PENDÊNCIAS OPERACIONAIS ---
  @Query("SELECT * FROM pendencias ORDER BY resolvido ASC, criadoEm DESC")
  fun getAllPendencias(): Flow<List<Pendencia>>

  @Query("SELECT * FROM pendencias WHERE resolvido = 0 ORDER BY criadoEm DESC")
  fun getPendenciasAbertas(): Flow<List<Pendencia>>

  @Query("SELECT COUNT(*) FROM pendencias WHERE resolvido = 0")
  fun getContagemPendenciasAbertas(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPendencia(pendencia: Pendencia): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPendencias(pendencias: List<Pendencia>)

  @Update
  suspend fun updatePendencia(pendencia: Pendencia)

  @Delete
  suspend fun deletePendencia(pendencia: Pendencia)

  @Query("UPDATE pendencias SET resolvido = :resolvido, resolvidoEm = :resolvidoEm WHERE id = :id")
  suspend fun setPendenciaResolvida(id: Long, resolvido: Boolean, resolvidoEm: Long?)


  // --- FUNÇÕES / CARGOS ---
  @Query("SELECT * FROM funcoes ORDER BY ordem ASC, nome ASC")
  fun getAllFuncoes(): Flow<List<Funcao>>

  @Query("SELECT * FROM funcoes ORDER BY ordem ASC, nome ASC")
  suspend fun getAllFuncoesSync(): List<Funcao>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFuncao(funcao: Funcao): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFuncoes(funcoes: List<Funcao>)

  @Update
  suspend fun updateFuncao(funcao: Funcao)

  @Delete
  suspend fun deleteFuncao(funcao: Funcao)

  @Query("DELETE FROM funcoes WHERE id = :id")
  suspend fun deleteFuncaoById(id: Long)

  @Query("SELECT COUNT(*) FROM funcoes")
  suspend fun getFuncoesCount(): Int

  @Query("UPDATE colaboradores SET cargo = :novoNome WHERE cargo = :nomeAntigo")
  suspend fun updateCargoInColaboradores(nomeAntigo: String, novoNome: String)

  @Query("UPDATE escalas_diarias SET cargo = :novoNome WHERE cargo = :nomeAntigo")
  suspend fun updateCargoInEscalas(nomeAntigo: String, novoNome: String)
}

