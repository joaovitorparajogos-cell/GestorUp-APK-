package com.example.updater

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit

class UpdateManager(
  private val context: Context,
  val config: UpdateConfig = UpdateConfig(),
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {

  private val prefs = context.getSharedPreferences("gestorup_updater", Context.MODE_PRIVATE)

  val service = GitHubUpdateService(config)
  val downloader = ApkDownloader(context)
  val installer = ApkInstaller(context)

  private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
  val state: StateFlow<UpdateState> = _state.asStateFlow()

  private var downloadJob: Job? = null
  private var lastCheckedUpdateInfo: UpdateInfo? = null

  val lastCheckedTime: Long
    get() = prefs.getLong("last_check_timestamp", 0L)

  /**
   * Consulta a API do GitHub por novas versões.
   * Se [isManual] for false, respeita o intervalo mínimo de verificação discreta (24h).
   */
  fun checkForUpdates(isManual: Boolean = true) {
    if (!isManual) {
      val lastCheck = lastCheckedTime
      val elapsed = System.currentTimeMillis() - lastCheck
      if (lastCheck > 0 && elapsed < TimeUnit.HOURS.toMillis(24)) {
        return // Ainda dentro do intervalo configurável de 24h
      }
    }

    _state.value = UpdateState.Checking

    scope.launch {
      val result = service.checkForUpdates()
      result.fold(
        onSuccess = { info ->
          prefs.edit().putLong("last_check_timestamp", System.currentTimeMillis()).apply()
          if (info != null && info.available) {
            lastCheckedUpdateInfo = info
            _state.value = UpdateState.UpdateAvailable(info)
          } else {
            _state.value = UpdateState.UpToDate(
              versionName = config.currentVersionName,
              checkedAt = System.currentTimeMillis()
            )
          }
        },
        onFailure = { error ->
          _state.value = UpdateState.Error(
            message = error.localizedMessage ?: "Não foi possível verificar atualizações no momento."
          )
        }
      )
    }
  }

  /**
   * Inicia o download nativo do APK com monitoramento contínuo.
   */
  fun startDownload(info: UpdateInfo) {
    downloadJob?.cancel()
    _state.value = UpdateState.Downloading(
      progressPercent = 0,
      downloadedBytes = 0,
      totalBytes = info.apkSize
    )

    downloadJob = scope.launch {
      val result = downloader.downloadApk(info) { percent, bytesRead, totalBytes ->
        _state.value = UpdateState.Downloading(
          progressPercent = percent,
          downloadedBytes = bytesRead,
          totalBytes = totalBytes
        )
      }

      result.fold(
        onSuccess = { apkFile ->
          _state.value = UpdateState.Downloaded(apkFile = apkFile, info = info)
        },
        onFailure = { err ->
          if (err !is kotlinx.coroutines.CancellationException) {
            _state.value = UpdateState.Error(
              message = err.localizedMessage ?: "Falha durante o download da atualização."
            )
          } else {
            lastCheckedUpdateInfo?.let {
              _state.value = UpdateState.UpdateAvailable(it)
            } ?: run {
              _state.value = UpdateState.Idle
            }
          }
        }
      )
    }
  }

  /**
   * Cancela o download em andamento e remove arquivos residuais.
   */
  fun cancelDownload() {
    downloadJob?.cancel()
    downloader.cleanupOldUpdates()
    lastCheckedUpdateInfo?.let {
      _state.value = UpdateState.UpdateAvailable(it)
    } ?: run {
      _state.value = UpdateState.Idle
    }
  }

  /**
   * Executa a instalação oficial chamando o instalador do sistema operacional.
   */
  fun triggerInstall(apkFile: File): Result<Unit> {
    if (!installer.canRequestPackageInstalls()) {
      installer.openInstallPermissionSettings()
      return Result.failure(
        SecurityException("Permissão para instalar aplicativos desconhecidos é necessária. Ative-a nas configurações.")
      )
    }
    _state.value = UpdateState.Installing(apkFile)
    return installer.installApk(apkFile)
  }

  /**
   * Reseta o estado para ocioso ou última versão verificada.
   */
  fun resetState() {
    _state.value = UpdateState.Idle
  }

  /**
   * Limpa arquivos temporários ao encerrar.
   */
  fun cleanup() {
    downloader.cleanupOldUpdates()
  }
}
