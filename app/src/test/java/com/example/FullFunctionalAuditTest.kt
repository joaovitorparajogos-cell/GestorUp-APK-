package com.example

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import com.example.brand.BrandAssets
import com.example.data.GestorUpDatabase
import com.example.data.dao.GestorUpDao
import com.example.data.model.AnotacaoDiaria
import com.example.data.model.Colaborador
import com.example.data.model.EscalaItem
import com.example.data.model.EscalaMensal
import com.example.data.model.Funcao
import com.example.data.model.Pendencia
import com.example.data.repository.GestorUpRepository
import com.example.ui.viewmodel.GestorUpViewModel
import com.example.updater.ApkInstaller
import com.example.updater.GitHubUpdateService
import com.example.updater.UpdateConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FullFunctionalAuditTest {

  private lateinit var context: Application
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
    viewModel = GestorUpViewModel(context)
  }

  // =========================================================================
  // 1. TELA DE ABERTURA & RECURSOS DA MARCA
  // =========================================================================
  @Test
  fun test_01_splash_and_brand_assets_integrity() {
    // 1. Nome e slogan oficiais
    assertEquals("GestorUp", BrandAssets.APP_NAME)
    assertEquals("Sua equipe. Sua gestão. Tudo em ordem.", BrandAssets.SLOGAN)

    // 2. Carregamento e decodificação dos drawables oficiais
    val logoRes = context.resources.openRawResource(BrandAssets.LOGO_FULL_RES)
    assertNotNull("Logo completa deve ser acessível como recurso", logoRes)
    val logoBitmap = BitmapFactory.decodeStream(logoRes)
    assertNotNull("Logo completa deve decodificar como imagem válida", logoBitmap)
    assertTrue("Largura da logo deve ser maior que zero", logoBitmap.width > 0)
    logoRes.close()

    val symbolRes = context.resources.openRawResource(BrandAssets.SYMBOL_RES)
    assertNotNull("Símbolo isolado deve ser acessível como recurso", symbolRes)
    val symbolBitmap = BitmapFactory.decodeStream(symbolRes)
    assertNotNull("Símbolo isolado deve decodificar como imagem válida", symbolBitmap)
    assertTrue("Largura do símbolo deve ser maior que zero", symbolBitmap.width > 0)
    symbolRes.close()

    // 3. Geometria da seta no 'U'
    assertNotNull(BrandAssets.UArrowGeometry.StartLeftStem)
    assertNotNull(BrandAssets.UArrowGeometry.ArrowPeak)
    assertEquals(1250L, BrandAssets.UArrowGeometry.DEFAULT_ANIMATION_DURATION_MS)
  }

  // =========================================================================
  // 2. INÍCIO (DASHBOARD) - INDICADORES E AGREGAÇÃO
  // =========================================================================
  @Test
  fun test_02_dashboard_indicators_and_aggregation() = runBlocking {
    val date = "2026-11-20"
    
    // Cadastrar colaboradores
    val c1Id = dao.insertColaborador(Colaborador(nome = "Colab Ativo 1", cargo = "Repositor", ativo = true))
    val c2Id = dao.insertColaborador(Colaborador(nome = "Colab Ativo 2", cargo = "Conferente", ativo = true))
    val c3Id = dao.insertColaborador(Colaborador(nome = "Colab Inativo", cargo = "Auxiliar", ativo = false))

    // Escalar no dia: 1 trabalhando e 1 folga
    dao.insertEscalaItems(
      listOf(
        EscalaItem(dataRef = date, colaboradorId = c1Id, colaboradorNome = "Colab Ativo 1", cargo = "Repositor", horarioEntrada = "08:00", isFolga = false),
        EscalaItem(dataRef = date, colaboradorId = c2Id, colaboradorNome = "Colab Ativo 2", cargo = "Conferente", horarioEntrada = "FOLGA", isFolga = true)
      )
    )

    // Inserir pendências: 1 urgente, 1 baixa
    dao.insertPendencias(
      listOf(
        Pendencia(titulo = "Urgente", prioridade = "Urgente", resolvido = false),
        Pendencia(titulo = "Baixa", prioridade = "Baixa", resolvido = false),
        Pendencia(titulo = "Resolvida", prioridade = "Alta", resolvido = true)
      )
    )

    val escalaDoDia = dao.getEscalaForDateSync(date)
    val trabalhando = escalaDoDia.filter { !it.isFolga }
    val folgas = escalaDoDia.filter { it.isFolga }
    val pendenciasAbertas = dao.getPendenciasAbertas().first()

    assertEquals(1, trabalhando.size)
    assertEquals("08:00", trabalhando.first().horarioEntrada)
    assertEquals(1, folgas.size)
    assertTrue(folgas.first().isFolga)
    assertTrue("Pendências ativas devem incluir a urgente", pendenciasAbertas.any { it.prioridade == "Urgente" })
  }

  // =========================================================================
  // 3. ESCALA DIÁRIA - CRUD, MÁSCARAS, ANOTAÇÕES E WHATSAPP
  // =========================================================================
  @Test
  fun test_03_daily_schedule_full_cycle_and_persistence() = runBlocking {
    val date = "2026-11-21"

    // 1. Cadastrar colaboradores e adicionar na escala
    val colabId = dao.insertColaborador(Colaborador(nome = "Fernando Santos", cargo = "Encarregado", ativo = true))
    val item1 = EscalaItem(
      dataRef = date,
      colaboradorId = colabId,
      colaboradorNome = "Fernando Santos",
      cargo = "Encarregado",
      horarioEntrada = "07:00",
      isFolga = false
    )
    val itemId = dao.insertEscalaItem(item1)
    assertTrue(itemId > 0)

    // 2. Recuperar e verificar
    var itens = dao.getEscalaForDateSync(date)
    assertEquals(1, itens.size)
    assertEquals("07:00", itens.first().horarioEntrada)
    assertFalse(itens.first().isFolga)

    // 3. Editar registro: mudar para 08:30
    dao.updateEscalaItem(itens.first().copy(horarioEntrada = "08:30"))
    itens = dao.getEscalaForDateSync(date)
    assertEquals("08:30", itens.first().horarioEntrada)

    // 4. Marcar folga
    dao.updateEscalaItem(itens.first().copy(horarioEntrada = "FOLGA", isFolga = true))
    itens = dao.getEscalaForDateSync(date)
    assertTrue(itens.first().isFolga)
    assertEquals("FOLGA", itens.first().horarioEntrada)

    // 5. Anotação diária do gestor
    dao.upsertAnotacao(AnotacaoDiaria(dataRef = date, texto = "Reunião de alinhamento com a diretoria às 15h."))
    val anotacao = dao.getAnotacaoForDateSync(date)
    assertNotNull(anotacao)
    assertEquals("Reunião de alinhamento com a diretoria às 15h.", anotacao?.texto)

    // 6. Mensagem formatada WhatsApp
    val msg = viewModel.gerarTextoMensagemWhatsApp(itens, anotacao?.texto ?: "")
    assertTrue(msg.contains("Fernando Santos — FOLGA"))
    assertTrue(msg.contains("Reunião de alinhamento"))

    // 7. Remover item da escala
    dao.deleteEscalaItem(itens.first())
    val itensAposExclusao = dao.getEscalaForDateSync(date)
    assertTrue(itensAposExclusao.isEmpty())
  }

  // =========================================================================
  // 4. INTERJORNADA - AUDITORIA DE TODAS AS CONDIÇÕES CLT (11H)
  // =========================================================================
  @Test
  fun test_04_interjornada_all_conditions() = runBlocking {
    val colabId = 555L
    val dataOntem = "2026-11-22"
    val dataHoje = "2026-11-23"

    // CASO A: Descanso INFERIOR a 11 horas (Saída ontem 23:00, Entrada hoje 08:00 = 9h descanso)
    dao.insertEscalaItem(
      EscalaItem(dataRef = dataOntem, colaboradorId = colabId, colaboradorNome = "Colab CLT", cargo = "Repositor", horarioEntrada = "14:40", horarioSaida = "23:00", isFolga = false)
    )
    val conflitoMenor11 = viewModel.verificarConflitoInterjornada(colabId, dataHoje, "08:00", colaboradorNome = "Colab CLT")
    assertNotNull("Deve acusar conflito para 9h de descanso", conflitoMenor11)
    assertFalse("Não é incompleto", conflitoMenor11!!.isIncompleto)
    assertEquals(9, conflitoMenor11.descansoCalculado)
    assertEquals(11, conflitoMenor11.referencia)

    // CASO B: Descanso EXATAMENTE IGUAL a 11 horas (Saída ontem 22:00, Entrada hoje 09:00 = 11h descanso)
    dao.insertEscalaItem(
      EscalaItem(dataRef = dataOntem, colaboradorId = 556L, colaboradorNome = "Colab Exato 11", cargo = "Repositor", horarioEntrada = "13:40", horarioSaida = "22:00", isFolga = false)
    )
    val conflitoExato11 = viewModel.verificarConflitoInterjornada(556L, dataHoje, "09:00", colaboradorNome = "Colab Exato 11")
    assertNull("Descanso de exatamente 11h NÃO deve gerar conflito (CLT art. 66 cumprido)", conflitoExato11)

    // CASO C: Descanso SUPERIOR a 11 horas (Saída ontem 20:00, Entrada hoje 08:00 = 12h descanso)
    dao.insertEscalaItem(
      EscalaItem(dataRef = dataOntem, colaboradorId = 557L, colaboradorNome = "Colab Folgado", cargo = "Repositor", horarioEntrada = "11:40", horarioSaida = "20:00", isFolga = false)
    )
    val conflitoMaior11 = viewModel.verificarConflitoInterjornada(557L, dataHoje, "08:00", colaboradorNome = "Colab Folgado")
    assertNull("Descanso de 12h (> 11h) NÃO deve gerar conflito", conflitoMaior11)

    // CASO D: Virada de Madrugada (Ontem entrada 22:00, saída 06:00 da manhã do dia atual)
    // Se hoje a entrada for 14:00 (descanso das 06:00 às 14:00 = 8h < 11h)
    dao.insertEscalaItem(
      EscalaItem(dataRef = dataOntem, colaboradorId = 558L, colaboradorNome = "Turno Noite", cargo = "Operador", horarioEntrada = "22:00", horarioSaida = "06:00", isFolga = false)
    )
    val conflitoMadrugada = viewModel.verificarConflitoInterjornada(558L, dataHoje, "14:00", colaboradorNome = "Turno Noite")
    assertNotNull("Virada de madrugada com 8h de descanso deve acusar conflito", conflitoMadrugada)
    assertEquals(8, conflitoMadrugada!!.descansoCalculado)

    // CASO E: Ausência de Horário de Saída Anterior
    dao.insertEscalaItem(
      EscalaItem(dataRef = dataOntem, colaboradorId = 559L, colaboradorNome = "Sem Saida", cargo = "Operador", horarioEntrada = "14:00", horarioSaida = "", isFolga = false)
    )
    val conflitoIncompleto = viewModel.verificarConflitoInterjornada(559L, dataHoje, "07:00", colaboradorNome = "Sem Saida")
    assertNotNull("Ausência de saída deve retornar alerta de checagem", conflitoIncompleto)
    assertTrue("Deve estar marcado como incompleto", conflitoIncompleto!!.isIncompleto)
    assertEquals(-1, conflitoIncompleto.descansoCalculado)

    // CASO F: Colaborador está de FOLGA no dia atual -> Nunca gera conflito
    val conflitoHojeFolga = viewModel.verificarConflitoInterjornada(colabId, dataHoje, "FOLGA", isFolga = true, colaboradorNome = "Colab CLT")
    assertNull("Entrada como FOLGA não deve gerar conflito", conflitoHojeFolga)
  }

  // =========================================================================
  // 5. ESCALA MENSAL - NAVEGAÇÃO, IMAGEM E PERSISTÊNCIA
  // =========================================================================
  @Test
  fun test_05_monthly_schedule_persistence() = runBlocking {
    val mesAno = "2026-11"
    val fakeUri = "content://media/external/images/media/98765"

    // Salvar escala mensal
    dao.upsertEscalaMensal(
      EscalaMensal(mesAno = mesAno, imageUri = fakeUri, notas = "Escala oficial aprovada pelo RH")
    )

    // Recuperar
    val mensal = dao.getEscalaMensal(mesAno).first()
    assertNotNull(mensal)
    assertEquals(fakeUri, mensal?.imageUri)
    assertEquals("Escala oficial aprovada pelo RH", mensal?.notas)

    // Excluir
    dao.deleteEscalaMensal(mesAno)
    val mensalExcluido = dao.getEscalaMensal(mesAno).first()
    assertNull(mensalExcluido)
  }

  // =========================================================================
  // 6. EQUIPE & FUNÇÕES - CADASTRO, EDIÇÃO, DESATIVAÇÃO E FILTROS
  // =========================================================================
  @Test
  fun test_06_team_and_roles_management() = runBlocking {
    // 1. Cadastrar colaboradores
    val cId = dao.insertColaborador(
      Colaborador(nome = "Lucas Teste", cargo = "Repositor", vinculo = "CLT", matricula = "MAT-123", ativo = true, observacoes = "Experiente")
    )
    assertTrue(cId > 0)

    // 2. Editar
    var colab = dao.getAllColaboradores().first().first { it.id == cId }
    dao.updateColaborador(colab.copy(cargo = "Conferente", telefone = "(11) 99999-8888"))
    colab = dao.getAllColaboradores().first().first { it.id == cId }
    assertEquals("Conferente", colab.cargo)
    assertEquals("(11) 99999-8888", colab.telefone)

    // 3. Desativar colaborador (soft delete/inativo)
    dao.updateColaborador(colab.copy(ativo = false))
    val ativos = dao.getColaboradoresAtivos().first()
    assertFalse("Colaborador desativado não deve estar na lista de ativos", ativos.any { it.id == cId })

    val todos = dao.getAllColaboradores().first()
    assertTrue("Colaborador desativado DEVE continuar no histórico", todos.any { it.id == cId })

    // 4. Funções / Cargos personalizados
    val funcaoId = dao.insertFuncao(Funcao(nome = "Auditor de Estoque", ordem = 10))
    val funcoes = dao.getAllFuncoes().first()
    assertTrue("Nova função cadastrada deve existir", funcoes.any { it.id == funcaoId && it.nome == "Auditor de Estoque" })

    // Editar função
    val f = funcoes.first { it.id == funcaoId }
    dao.updateFuncao(f.copy(nome = "Auditor Chefe"))
    val fAtualizada = dao.getAllFuncoes().first().first { it.id == funcaoId }
    assertEquals("Auditor Chefe", fAtualizada.nome)

    // Excluir função
    dao.deleteFuncao(fAtualizada)
    val fExcluida = dao.getAllFuncoes().first()
    assertFalse("Função excluída não deve constar", fExcluida.any { it.id == funcaoId })
  }

  // =========================================================================
  // 7. PENDÊNCIAS - CICLO DE VIDA, PRIORIDADES E REABERTURA
  // =========================================================================
  @Test
  fun test_07_pendencias_lifecycle() = runBlocking {
    // 1. Criar
    val pId = dao.insertPendencia(
      Pendencia(titulo = "Solicitar uniformes", descricao = "Tamanhos M e G para repositores", categoria = "Colaboradores", prioridade = "Média", resolvido = false)
    )
    assertTrue(pId > 0)

    // 2. Editar prioridade
    var p = dao.getAllPendencias().first().first { it.id == pId }
    dao.updatePendencia(p.copy(prioridade = "Alta", prazo = "2026-11-25"))
    p = dao.getAllPendencias().first().first { it.id == pId }
    assertEquals("Alta", p.prioridade)
    assertEquals("2026-11-25", p.prazo)

    // 3. Marcar como resolvida
    dao.setPendenciaResolvida(pId, true, System.currentTimeMillis())
    val abertasAposResolver = dao.getPendenciasAbertas().first()
    assertFalse("Não deve estar nas pendências ativas", abertasAposResolver.any { it.id == pId })

    // 4. Reabrir pendência
    dao.setPendenciaResolvida(pId, false, null)
    val reabertas = dao.getPendenciasAbertas().first()
    assertTrue("Deve voltar para a lista de ativas após reabertura", reabertas.any { it.id == pId })
  }

  // =========================================================================
  // 8. CONFIGURAÇÕES - TEMA, FONTE E PERSISTÊNCIA DE PREFERÊNCIAS
  // =========================================================================
  @Test
  fun test_08_settings_preferences_persistence() = runBlocking {
    val prefs: SharedPreferences = context.getSharedPreferences("gestorup_settings", Context.MODE_PRIVATE)

    // Testar alternância de temas ("ESCURO", "CLARO", "SISTEMA")
    viewModel.setThemeMode("ESCURO")
    assertEquals("ESCURO", prefs.getString("theme_mode", "ESCURO"))
    assertEquals("ESCURO", viewModel.themeMode.value)

    viewModel.setThemeMode("CLARO")
    assertEquals("CLARO", prefs.getString("theme_mode", "ESCURO"))
    assertEquals("CLARO", viewModel.themeMode.value)

    viewModel.setThemeMode("SISTEMA")
    assertEquals("SISTEMA", prefs.getString("theme_mode", "ESCURO"))
    assertEquals("SISTEMA", viewModel.themeMode.value)

    // Testar tamanhos de fonte ("PEQUENO", "MEDIO", "GRANDE")
    viewModel.setFontSizeOption("GRANDE")
    assertEquals("GRANDE", prefs.getString("font_size_option", "PEQUENO"))
    assertEquals("GRANDE", viewModel.fontSizeOption.value)

    viewModel.setFontSizeOption("PEQUENO")
    assertEquals("PEQUENO", prefs.getString("font_size_option", "PEQUENO"))
    assertEquals("PEQUENO", viewModel.fontSizeOption.value)

    viewModel.setFontSizeOption("MEDIO")
    assertEquals("MEDIO", prefs.getString("font_size_option", "PEQUENO"))
    assertEquals("MEDIO", viewModel.fontSizeOption.value)
  }

  // =========================================================================
  // 9. ATUALIZAÇÕES OTA - COMPARAÇÃO DE VERSÕES, LOGS E SEGURANÇA
  // =========================================================================
  @Test
  fun test_09_ota_updates_logic() {
    val config = UpdateConfig(
      owner = "joaovitormf2001-cmd",
      repo = "GestorUp-APK",
      currentVersionCode = 4,
      currentVersionName = "1.3"
    )
    val service = GitHubUpdateService(config)

    // Comparação de versão superior
    assertTrue("Versão remota v5 > atual v4", service.isVersionSuperior(remoteCode = 5, currentCode = 4, remoteName = "1.4", currentName = "1.3"))
    assertFalse("Versão remota v4 == atual v4", service.isVersionSuperior(remoteCode = 4, currentCode = 4, remoteName = "1.3", currentName = "1.3"))
    assertFalse("Versão remota v3 < atual v4", service.isVersionSuperior(remoteCode = 3, currentCode = 4, remoteName = "1.2", currentName = "1.3"))

    // Comparação semântica pura
    assertTrue(service.compareSemanticVersions("1.4.0", "1.3.0") > 0)
    assertTrue(service.compareSemanticVersions("1.3.1", "1.3.0") > 0)
    assertEquals(0, service.compareSemanticVersions("1.3.0", "1.3.0"))
    assertTrue(service.compareSemanticVersions("1.2.9", "1.3.0") < 0)

    // Teste do ApkInstaller
    val installer = ApkInstaller(context)
    val nonExistentFile = File(context.cacheDir, "non_existent.apk")
    val installResult = installer.installApk(nonExistentFile)
    assertTrue("Deve falhar com segurança se o arquivo APK não existir", installResult.isFailure)
  }
}
