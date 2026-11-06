package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GestorUpDatabase
import com.example.data.model.Colaborador
import com.example.data.model.EscalaItem
import com.example.data.model.EscalaMensal
import com.example.data.model.Funcao
import com.example.data.model.Pendencia
import com.example.data.repository.GestorUpRepository
import com.example.updater.UpdateInfo
import com.example.updater.UpdateManager
import com.example.updater.UpdateState
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GestorUpViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: GestorUpRepository
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

  // Data selecionada na Escala Diária ("yyyy-MM-dd")
  private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
  val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

  // Mês selecionado na Escala Mensal ("yyyy-MM")
  private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
  private val _selectedMonth = MutableStateFlow(monthFormat.format(Date()))
  val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

  // Mensagens de feedback para a UI (Toast/Snackbar)
  private val _uiEvents = MutableSharedFlow<String>()
  val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

  // Configurações do Aplicativo (Tema e Tamanho de Fonte) com persistência
  private val prefs = application.getSharedPreferences("gestorup_settings", Context.MODE_PRIVATE)

  private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "ESCURO") ?: "ESCURO")
  val themeMode: StateFlow<String> = _themeMode.asStateFlow()

  private val _fontSizeOption = MutableStateFlow(prefs.getString("font_size_option", "PEQUENO") ?: "PEQUENO")
  val fontSizeOption: StateFlow<String> = _fontSizeOption.asStateFlow()

  fun setThemeMode(mode: String) {
    _themeMode.value = mode
    prefs.edit().putString("theme_mode", mode).apply()
    viewModelScope.launch {
      _uiEvents.emit(
        when (mode) {
          "ESCURO" -> "Tema escuro ativado"
          "CLARO" -> "Tema claro ativado"
          else -> "Tema do sistema ativado"
        }
      )
    }
  }

  fun setFontSizeOption(option: String) {
    _fontSizeOption.value = option
    prefs.edit().putString("font_size_option", option).apply()
    viewModelScope.launch {
      _uiEvents.emit(
        when (option) {
          "PEQUENO" -> "Tamanho de fonte: Pequeno"
          "GRANDE" -> "Tamanho de fonte: Grande"
          else -> "Tamanho de fonte: Médio (Padrão)"
        }
      )
    }
  }

  // --- ATUALIZADOR NATIVO GITHUB RELEASES ---
  val updateManager = UpdateManager(application, scope = viewModelScope)
  val updateState: StateFlow<UpdateState> = updateManager.state

  fun verificarAtualizacoes(isManual: Boolean = true) {
    updateManager.checkForUpdates(isManual = isManual)
  }

  fun baixarAtualizacao(info: UpdateInfo) {
    updateManager.startDownload(info)
  }

  fun cancelarDownloadAtualizacao() {
    updateManager.cancelDownload()
  }

  fun instalarAtualizacao(file: File) {
    val result = updateManager.triggerInstall(file)
    if (result.isFailure) {
      viewModelScope.launch {
        _uiEvents.emit(result.exceptionOrNull()?.localizedMessage ?: "Permissão necessária para instalar o APK.")
      }
    }
  }

  init {
    val db = GestorUpDatabase.getDatabase(application, viewModelScope)
    repository = GestorUpRepository(db.gestorUpDao())
    // Verificação discreta ao abrir o aplicativo (intervalo 24h)
    updateManager.checkForUpdates(isManual = false)
  }

  override fun onCleared() {
    super.onCleared()
    updateManager.cleanup()
  }

  // --- ESCALA DIÁRIA & ANOTAÇÕES ---

  @OptIn(ExperimentalCoroutinesApi::class)
  val escalaDoDia: StateFlow<List<EscalaItem>> = _selectedDate
    .flatMapLatest { date -> repository.getEscalaDoDia(date) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val anotacaoDoDia: StateFlow<String> = _selectedDate
    .flatMapLatest { date -> repository.getAnotacaoDoDia(date).map { it?.texto ?: "" } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

  val datasComEscala: StateFlow<Set<String>> = repository.datasComEscala
    .map { it.toSet() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  val datasComAnotacao: StateFlow<Set<String>> = repository.datasComAnotacao
    .map { it.toSet() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  fun selecionarData(dateStr: String) {
    _selectedDate.value = dateStr
  }

  fun selecionarHoje() {
    _selectedDate.value = dateFormat.format(Date())
  }

  fun avancarDias(quantidade: Int) {
    try {
      val cal = Calendar.getInstance()
      val date = dateFormat.parse(_selectedDate.value) ?: Date()
      cal.time = date
      cal.add(Calendar.DAY_OF_MONTH, quantidade)
      _selectedDate.value = dateFormat.format(cal.time)
    } catch (e: Exception) {
      _selectedDate.value = dateFormat.format(Date())
    }
  }

  fun salvarItemEscala(
    id: Long = 0,
    colaboradorId: Long,
    colaboradorNome: String,
    cargo: String,
    horarioEntrada: String,
    horarioSaida: String = "",
    isFolga: Boolean = false
  ) {
    viewModelScope.launch {
      val item = EscalaItem(
        id = id,
        dataRef = _selectedDate.value,
        colaboradorId = colaboradorId,
        colaboradorNome = colaboradorNome,
        cargo = cargo,
        horarioEntrada = if (isFolga) "FOLGA" else horarioEntrada.trim(),
        horarioSaida = horarioSaida.trim(),
        isFolga = isFolga,
        ordem = 0
      )
      repository.salvarItemEscala(item)
      _uiEvents.emit("Escala atualizada com sucesso.")
    }
  }

  fun salvarEscalaLote(itens: List<EscalaItem>) {
    viewModelScope.launch {
      repository.salvarItensEscala(itens)
      _uiEvents.emit("Escala salva com sucesso!")
    }
  }

  fun carregarColaboradoresAtivosParaData() {
    viewModelScope.launch {
      val ativos = colaboradoresAtivos.value
      val jaEscalados = repository.getEscalaDoDiaSync(_selectedDate.value).map { it.colaboradorId }.toSet()
      val novos = ativos.filter { it.id !in jaEscalados }.map { colab ->
        EscalaItem(
          dataRef = _selectedDate.value,
          colaboradorId = colab.id,
          colaboradorNome = colab.nome,
          cargo = colab.cargo,
          horarioEntrada = "07:00",
          horarioSaida = "",
          isFolga = false,
          ordem = colab.ordem
        )
      }
      if (novos.isNotEmpty()) {
        repository.salvarItensEscala(novos)
        _uiEvents.emit("${novos.size} colaborador(es) adicionado(s) à escala.")
      } else {
        _uiEvents.emit("Todos os colaboradores ativos já estão na escala deste dia.")
      }
    }
  }

  fun removerItemEscala(item: EscalaItem) {
    viewModelScope.launch {
      repository.removerItemEscala(item)
      _uiEvents.emit("Colaborador removido da escala do dia.")
    }
  }

  fun salvarAnotacaoDoDia(texto: String) {
    viewModelScope.launch {
      repository.salvarAnotacao(_selectedDate.value, texto.trim())
      _uiEvents.emit("Observações salvas com sucesso.")
    }
  }

  // --- FORMATAÇÃO E COMPARTILHAMENTO DE MENSAGEM (WHATSAPP) ---

  fun gerarTextoMensagemWhatsApp(itens: List<EscalaItem>, observacao: String): String {
    val date = try { dateFormat.parse(_selectedDate.value) ?: Date() } catch (e: Exception) { Date() }

    val diaSemana = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(date).uppercase(Locale("pt", "BR"))
    val diaMes = SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)

    val builder = StringBuilder()
    builder.append("ESCALA — $diaSemana, $diaMes\n\n")

    if (itens.isEmpty()) {
      builder.append("(Nenhum colaborador escalado para esta data)\n")
    } else {
      itens.forEach { item ->
        if (item.isFolga || item.horarioEntrada.equals("FOLGA", ignoreCase = true)) {
          builder.append("• ${item.colaboradorNome} — FOLGA\n")
        } else {
          builder.append("• ${item.colaboradorNome} — Entrada: ${item.horarioEntrada}\n")
        }
      }
    }

    if (observacao.isNotBlank()) {
      builder.append("\nOBSERVAÇÕES:\n")
      builder.append(observacao.trim())
      builder.append("\n")
    }

    return builder.toString()
  }

  fun compartilharNoWhatsApp(context: Context, texto: String) {
    try {
      val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
        setPackage("com.whatsapp")
      }
      context.startActivity(sendIntent)
    } catch (e: Exception) {
      val chooserIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
      }
      context.startActivity(Intent.createChooser(chooserIntent, "Compartilhar Escala do Depósito"))
    }
  }

  fun copiarMensagem(context: Context, texto: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Escala GestorUp", texto)
    clipboard?.setPrimaryClip(clip)
    viewModelScope.launch {
      _uiEvents.emit("Mensagem copiada para a área de transferência!")
    }
  }

  // --- DETECTOR AUTOMÁTICO DE INTERJORNADA ---
  suspend fun verificarConflitoInterjornada(
    colaboradorId: Long,
    dataAtual: String,
    entrada: String,
    saida: String = "",
    isFolga: Boolean = false,
    colaboradorNome: String = ""
  ): InterjornadaConflito? {
    if (isFolga || entrada.isBlank() || entrada.equals("FOLGA", ignoreCase = true)) {
      return null
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val currentDay = try { sdfDate.parse(dataAtual) } catch (e: Exception) { null } ?: return null

    val calPrev = Calendar.getInstance().apply { time = currentDay; add(Calendar.DAY_OF_MONTH, -1) }
    val prevDate = sdfDate.format(calPrev.time)

    val currentEntrada = try { sdf.parse("$dataAtual $entrada") } catch (e: Exception) { null } ?: return null

    val escalonamento = repository.getEscalaForCollaboratorRange(colaboradorId, prevDate, dataAtual)
    val nomeColab = colaboradorNome.ifBlank {
      escalonamento.firstOrNull { it.colaboradorId == colaboradorId }?.colaboradorNome ?: "Colaborador"
    }

    val prevShift = escalonamento.filter { it.dataRef == prevDate }.maxByOrNull { it.horarioEntrada }

    if (prevShift == null) {
      // Não há registro da escala no dia anterior
      return InterjornadaConflito(
        nome = nomeColab,
        dataAnterior = prevDate,
        saidaAnterior = "Não informada",
        dataProxima = dataAtual,
        entradaProxima = entrada,
        descansoCalculado = -1,
        referencia = 11,
        isIncompleto = true,
        motivoIncompleto = "Horário de saída da jornada anterior ($prevDate) não informado"
      )
    }

    if (prevShift.isFolga || prevShift.horarioEntrada.equals("FOLGA", ignoreCase = true)) {
      // Dia anterior foi folga confirmada (> 24h descanso)
      return null
    }

    val prevSaidaStr = prevShift.horarioSaida.trim()
    if (prevSaidaStr.isBlank() || prevSaidaStr.equals("FOLGA", ignoreCase = true)) {
      // Dia anterior trabalhado, porém saída não informada
      return InterjornadaConflito(
        nome = prevShift.colaboradorNome,
        dataAnterior = prevDate,
        saidaAnterior = "Não informada",
        dataProxima = dataAtual,
        entradaProxima = entrada,
        descansoCalculado = -1,
        referencia = 11,
        isIncompleto = true,
        motivoIncompleto = "Horário de saída da jornada anterior ($prevDate) não informado"
      )
    }

    // Fonte confiável disponível (prevShift.horarioSaida)
    val prevSaida = try {
      if (prevSaidaStr < prevShift.horarioEntrada) {
        sdf.parse("$dataAtual $prevSaidaStr")
      } else {
        sdf.parse("$prevDate $prevSaidaStr")
      }
    } catch (e: Exception) {
      null
    }

    if (prevSaida != null) {
      val diffMs = currentEntrada.time - prevSaida.time
      val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
      if (diffHours < 11) {
        return InterjornadaConflito(
          nome = prevShift.colaboradorNome,
          dataAnterior = prevDate,
          saidaAnterior = prevSaidaStr,
          dataProxima = dataAtual,
          entradaProxima = entrada,
          descansoCalculado = diffHours.toInt(),
          referencia = 11,
          isIncompleto = false
        )
      }
    }

    return null
  }

  // --- ESCALA MENSAL (ANEXO DE IMAGEM) ---

  fun selecionarMes(mesAno: String) {
    _selectedMonth.value = mesAno
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val escalaMensalAtual: StateFlow<EscalaMensal?> = _selectedMonth
    .flatMapLatest { mesAno -> repository.getEscalaMensal(mesAno) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  fun salvarEscalaMensal(imageUri: String, notas: String = "") {
    viewModelScope.launch {
      repository.salvarEscalaMensal(_selectedMonth.value, imageUri, notas)
      _uiEvents.emit("Imagem da escala mensal anexada com sucesso!")
    }
  }

  fun removerEscalaMensal() {
    viewModelScope.launch {
      repository.removerEscalaMensal(_selectedMonth.value)
      _uiEvents.emit("Imagem da escala removida.")
    }
  }

  // --- EQUIPE DE COLABORADORES ---

  val todosColaboradores: StateFlow<List<Colaborador>> = repository.todosColaboradores
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val colaboradoresAtivos: StateFlow<List<Colaborador>> = repository.colaboradoresAtivos
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun salvarColaborador(
    id: Long = 0,
    nome: String,
    cargo: String,
    vinculo: String = "CLT",
    telefone: String = "",
    ativo: Boolean = true,
    matricula: String = "",
    dataCadastro: Long = System.currentTimeMillis(),
    observacoes: String = ""
  ) {
    viewModelScope.launch {
      val colab = Colaborador(
        id = id,
        nome = nome.trim(),
        cargo = cargo.trim(),
        vinculo = vinculo.trim(),
        telefone = telefone.trim(),
        ativo = ativo,
        matricula = matricula.trim(),
        dataCadastro = dataCadastro,
        observacoes = observacoes.trim()
      )
      repository.salvarColaborador(colab)
      _uiEvents.emit(if (id == 0L) "Colaborador cadastrado!" else "Dados do colaborador atualizados!")
    }
  }

  fun alternarStatusColaborador(colaborador: Colaborador) {
    viewModelScope.launch {
      val updated = colaborador.copy(ativo = !colaborador.ativo)
      repository.salvarColaborador(updated)
      _uiEvents.emit(if (updated.ativo) "Colaborador ativado." else "Colaborador desativado (histórico preservado).")
    }
  }

  fun deletarColaborador(colaborador: Colaborador) {
    viewModelScope.launch {
      repository.deletarColaborador(colaborador)
      _uiEvents.emit("Colaborador removido da equipe.")
    }
  }

  // --- FUNÇÕES / CARGOS DA EQUIPE ---

  val todasFuncoes: StateFlow<List<Funcao>> = repository.todasFuncoes
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun salvarFuncao(id: Long = 0, nome: String) {
    val cleanName = nome.trim()
    if (cleanName.isBlank()) return
    viewModelScope.launch {
      if (id == 0L) {
        val existe = todasFuncoes.value.any { it.nome.equals(cleanName, ignoreCase = true) }
        if (existe) {
          _uiEvents.emit("A função '$cleanName' já existe no cadastro.")
          return@launch
        }
        val novaOrdem = (todasFuncoes.value.maxOfOrNull { it.ordem } ?: 0) + 1
        repository.salvarFuncao(Funcao(nome = cleanName, ordem = novaOrdem))
        _uiEvents.emit("Função '$cleanName' adicionada com sucesso!")
      } else {
        val funcaoAtual = todasFuncoes.value.find { it.id == id }
        if (funcaoAtual != null) {
          repository.renomearFuncao(funcaoAtual, cleanName)
          _uiEvents.emit("Função atualizada para '$cleanName'!")
        }
      }
    }
  }

  fun deletarFuncao(funcao: Funcao) {
    viewModelScope.launch {
      repository.deletarFuncao(funcao)
      _uiEvents.emit("Função '${funcao.nome}' removida!")
    }
  }

  // --- PENDÊNCIAS OPERACIONAIS ---

  val todasPendencias: StateFlow<List<Pendencia>> = repository.todasPendencias
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendenciasAbertas: StateFlow<List<Pendencia>> = repository.pendenciasAbertas
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val contagemPendenciasAbertas: StateFlow<Int> = repository.contagemPendenciasAbertas
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  fun salvarPendencia(
    id: Long = 0,
    titulo: String,
    descricao: String = "",
    categoria: String = "Outros",
    prioridade: String = "Média",
    prazo: String? = null
  ) {
    viewModelScope.launch {
      val p = Pendencia(
        id = id,
        titulo = titulo.trim(),
        descricao = descricao.trim(),
        categoria = categoria,
        prioridade = prioridade,
        prazo = prazo
      )
      repository.salvarPendencia(p)
      _uiEvents.emit("Pendência salva com sucesso.")
    }
  }

  fun alternarResolvidoPendencia(pendencia: Pendencia) {
    viewModelScope.launch {
      repository.alternarResolvido(pendencia)
      _uiEvents.emit(if (!pendencia.resolvido) "Pendência marcada como resolvida!" else "Pendência reaberta.")
    }
  }

  fun deletarPendencia(pendencia: Pendencia) {
    viewModelScope.launch {
      repository.deletarPendencia(pendencia)
      _uiEvents.emit("Pendência excluída.")
    }
  }
}
