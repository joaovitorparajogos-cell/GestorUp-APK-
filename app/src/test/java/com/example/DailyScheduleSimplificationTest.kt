package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GestorUpDatabase
import com.example.data.dao.GestorUpDao
import com.example.data.model.EscalaItem
import com.example.data.repository.GestorUpRepository
import com.example.ui.screens.formatTimeDigits
import com.example.ui.screens.normalizeTimeOnBlur
import com.example.ui.viewmodel.GestorUpViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailyScheduleSimplificationTest {

  private lateinit var context: Context
  private lateinit var database: GestorUpDatabase
  private lateinit var dao: GestorUpDao
  private lateinit var repository: GestorUpRepository
  private lateinit var viewModel: GestorUpViewModel

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    database = GestorUpDatabase.getDatabase(context, CoroutineScope(Dispatchers.IO))
    dao = database.gestorUpDao()
    repository = GestorUpRepository(dao)
    viewModel = GestorUpViewModel(context as android.app.Application)
  }

  // 1 & 2. Teste de Digitação Direta do Horário e Máscara Automática (07:00, 07:30, 06:00, 13:00, 14:30)
  @Test
  fun test_direct_typing_and_mask_formatting() {
    // Digitação de 4 dígitos consecutivos
    assertEquals("07:00", formatTimeDigits("0700"))
    assertEquals("07:30", formatTimeDigits("0730"))
    assertEquals("06:00", formatTimeDigits("0600"))
    assertEquals("08:00", formatTimeDigits("0800"))
    assertEquals("13:00", formatTimeDigits("1300"))
    assertEquals("14:30", formatTimeDigits("1430"))

    // Digitação progressiva caractere por caractere
    assertEquals("0", formatTimeDigits("0"))
    assertEquals("07", formatTimeDigits("07"))
    assertEquals("07:3", formatTimeDigits("073"))
    assertEquals("07:30", formatTimeDigits("0730"))

    // Correção e edição com backspace
    assertEquals("07:3", formatTimeDigits("07:3"))
    assertEquals("07", formatTimeDigits("07"))
    assertEquals("0", formatTimeDigits("0"))
    assertEquals("", formatTimeDigits(""))

    // Normalização ao sair do campo (onBlur) para horários simplificados
    assertEquals("07:00", normalizeTimeOnBlur("7"))
    assertEquals("08:00", normalizeTimeOnBlur("8"))
    assertEquals("14:00", normalizeTimeOnBlur("14"))
    assertEquals("07:30", normalizeTimeOnBlur("730"))
    assertEquals("07:30", normalizeTimeOnBlur("0730"))
    assertEquals("14:30", normalizeTimeOnBlur("1430"))
  }

  // 3. Teste de Exclusão do Campo de Saída na Escala Diária e Apenas Entrada
  @Test
  fun test_daily_scale_stores_only_entrance_time() = runBlocking {
    val date = "2026-11-10"

    // Salvar item com apenas horário de entrada e saída vazia
    val itemToSave = EscalaItem(
      dataRef = date,
      colaboradorId = 101L,
      colaboradorNome = "João Silva",
      cargo = "Repositor",
      horarioEntrada = "07:30",
      horarioSaida = "",
      isFolga = false
    )
    dao.insertEscalaItem(itemToSave)

    val itens = dao.getEscalaForDateSync(date)
    val item = itens.first { it.colaboradorId == 101L }

    assertEquals("07:30", item.horarioEntrada)
    assertEquals("", item.horarioSaida) // Saída não é informada
    assertFalse(item.isFolga)
  }

  // 4. Teste de Marcação de Folga (Colaborador de Folga não precisa de horário)
  @Test
  fun test_marking_collaborator_as_folga() = runBlocking {
    val date = "2026-11-10"

    // Colaborador de Folga
    val itemFolga = EscalaItem(
      dataRef = date,
      colaboradorId = 102L,
      colaboradorNome = "Maria Santos",
      cargo = "Conferente",
      horarioEntrada = "FOLGA",
      horarioSaida = "",
      isFolga = true
    )
    dao.insertEscalaItem(itemFolga)

    val itens = dao.getEscalaForDateSync(date)
    val item = itens.first { it.colaboradorId == 102L }

    assertTrue("Deve estar marcado como folga", item.isFolga)
    assertEquals("FOLGA", item.horarioEntrada)
  }

  // 5. Teste de Formatação de Mensagem de WhatsApp (Somente Entrada e Folgas)
  @Test
  fun test_whatsapp_message_formatting_only_entry_and_folga() {
    val itens = listOf(
      EscalaItem(
        id = 1,
        dataRef = "2026-11-10",
        colaboradorId = 1,
        colaboradorNome = "Carlos Andrade",
        cargo = "Encarregado",
        horarioEntrada = "06:00",
        horarioSaida = "",
        isFolga = false
      ),
      EscalaItem(
        id = 2,
        dataRef = "2026-11-10",
        colaboradorId = 2,
        colaboradorNome = "Mariana Souza",
        cargo = "Conferente",
        horarioEntrada = "07:00",
        horarioSaida = "",
        isFolga = false
      ),
      EscalaItem(
        id = 3,
        dataRef = "2026-11-10",
        colaboradorId = 3,
        colaboradorNome = "Lucas Lima",
        cargo = "Operador de Empilhadeira",
        horarioEntrada = "FOLGA",
        horarioSaida = "",
        isFolga = true
      )
    )

    val mensagem = viewModel.gerarTextoMensagemWhatsApp(itens, "Chegada de caminhão às 08h.")

    // Confirmar que NÃO contém "Saída:" na mensagem da escala
    assertFalse("Mensagem não deve conter campo de saída", mensagem.contains("Saída:"))
    assertTrue("Mensagem deve conter a entrada de Carlos", mensagem.contains("Carlos Andrade — Entrada: 06:00"))
    assertTrue("Mensagem deve conter a entrada de Mariana", mensagem.contains("Mariana Souza — Entrada: 07:00"))
    assertTrue("Mensagem deve identificar Lucas de FOLGA", mensagem.contains("Lucas Lima — FOLGA"))
  }

  // 6. Teste do Detector de Interjornada: Incompleto quando saída anterior não foi informada
  @Test
  fun test_interjornada_incomplete_when_previous_exit_not_informed() = runBlocking {
    val dataOntem = "2026-11-09"
    val dataHoje = "2026-11-10"

    // Dia anterior registrado apenas com entrada (sem horário de saída)
    dao.insertEscalaItem(
      EscalaItem(
        dataRef = dataOntem,
        colaboradorId = 201L,
        colaboradorNome = "Rafael Teste",
        cargo = "Repositor",
        horarioEntrada = "14:00",
        horarioSaida = "",
        isFolga = false
      )
    )

    // Dia atual: entrada às 07:00
    val conflito = viewModel.verificarConflitoInterjornada(
      colaboradorId = 201L,
      dataAtual = dataHoje,
      entrada = "07:00",
      isFolga = false,
      colaboradorNome = "Rafael Teste"
    )

    assertNotNull("Deve retornar objeto de verificação", conflito)
    assertTrue("Deve sinalizar verificação incompleta", conflito!!.isIncompleto)
    assertEquals("Horário de saída da jornada anterior ($dataOntem) não informado", conflito.motivoIncompleto)
  }

  // 7. Teste do Detector de Interjornada: Conflito real quando saída anterior é conhecida (< 11h)
  @Test
  fun test_interjornada_real_conflict_when_previous_exit_is_known() = runBlocking {
    val dataOntem = "2026-11-09"
    val dataHoje = "2026-11-10"

    // Ontem: saiu às 23:00 (registro histórico com saída)
    dao.insertEscalaItem(
      EscalaItem(
        dataRef = dataOntem,
        colaboradorId = 202L,
        colaboradorNome = "Bruno Noite",
        cargo = "Repositor",
        horarioEntrada = "14:40",
        horarioSaida = "23:00",
        isFolga = false
      )
    )

    // Hoje: escalado para entrar às 07:00 (descanso de apenas 8h < 11h)
    val conflito = viewModel.verificarConflitoInterjornada(
      colaboradorId = 202L,
      dataAtual = dataHoje,
      entrada = "07:00",
      isFolga = false,
      colaboradorNome = "Bruno Noite"
    )

    assertNotNull("Deve detectar o conflito", conflito)
    assertFalse("É um conflito real, não incompleto", conflito!!.isIncompleto)
    assertEquals(8, conflito.descansoCalculado)
    assertEquals(11, conflito.referencia)
    assertEquals("23:00", conflito.saidaAnterior)
  }

  // 8. Teste do Detector de Interjornada: Sem conflito quando dia anterior foi folga
  @Test
  fun test_interjornada_no_conflict_when_previous_day_was_folga() = runBlocking {
    val dataOntem = "2026-11-09"
    val dataHoje = "2026-11-10"

    // Ontem: Folga
    dao.insertEscalaItem(
      EscalaItem(
        dataRef = dataOntem,
        colaboradorId = 203L,
        colaboradorNome = "Ana Folga",
        cargo = "Conferente",
        horarioEntrada = "FOLGA",
        horarioSaida = "",
        isFolga = true
      )
    )

    // Hoje: entrada às 07:00
    val conflito = viewModel.verificarConflitoInterjornada(
      colaboradorId = 203L,
      dataAtual = dataHoje,
      entrada = "07:00",
      isFolga = false,
      colaboradorNome = "Ana Folga"
    )

    assertNull("Não deve haver conflito quando o dia anterior foi folga", conflito)
  }

  // 9. Teste de Preservação e Salvamento ao Navegar Entre Datas
  @Test
  fun test_persistence_across_dates() = runBlocking {
    val dateA = "2026-11-15"
    val dateB = "2026-11-16"

    // Salvar na data A
    dao.insertEscalaItem(
      EscalaItem(
        dataRef = dateA,
        colaboradorId = 301L,
        colaboradorNome = "Roberto",
        cargo = "Repositor",
        horarioEntrada = "06:00",
        isFolga = false
      )
    )

    // Salvar na data B
    dao.insertEscalaItem(
      EscalaItem(
        dataRef = dateB,
        colaboradorId = 301L,
        colaboradorNome = "Roberto",
        cargo = "Repositor",
        horarioEntrada = "13:00",
        isFolga = false
      )
    )

    // Conferir data A
    val escalaA = dao.getEscalaForDateSync(dateA)
    assertEquals("06:00", escalaA.first { it.colaboradorId == 301L }.horarioEntrada)

    // Conferir data B
    val escalaB = dao.getEscalaForDateSync(dateB)
    assertEquals("13:00", escalaB.first { it.colaboradorId == 301L }.horarioEntrada)
  }
}
