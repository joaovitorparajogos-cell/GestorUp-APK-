package com.example.updater

import com.example.BuildConfig
import java.io.File

/**
 * Configuração do repositório GitHub para verificação e download de atualizações.
 */
data class UpdateConfig(
  val owner: String = "joaovitormf2001-cmd",
  val repo: String = "GestorUp-APK",
  val expectedApkName: String = "app-release.apk",
  val fallbackApkName: String = "app-debug.apk",
  val currentVersionCode: Int = BuildConfig.VERSION_CODE,
  val currentVersionName: String = BuildConfig.VERSION_NAME,
  val apiUrl: String = "https://api.github.com/repos/$owner/$repo/releases/latest",
)

/**
 * Detalhes da nova versão disponível no GitHub Releases.
 */
data class UpdateInfo(
  val available: Boolean,
  val remoteVersionName: String,
  val remoteVersionCode: Int,
  val currentVersionName: String,
  val currentVersionCode: Int,
  val releaseName: String,
  val releaseNotes: String,
  val publishedAt: String,
  val apkDownloadUrl: String,
  val apkFileName: String,
  val apkSize: Long,
  val sha256DownloadUrl: String? = null,
  val expectedSha256: String? = null,
)

/**
 * Estados do ciclo de vida da atualização do aplicativo.
 */
sealed class UpdateState {
  object Idle : UpdateState()
  object Checking : UpdateState()
  data class UpToDate(val versionName: String, val checkedAt: Long) : UpdateState()
  data class UpdateAvailable(val info: UpdateInfo) : UpdateState()
  data class Downloading(
    val progressPercent: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
  ) : UpdateState()
  data class Downloaded(val apkFile: File, val info: UpdateInfo) : UpdateState()
  data class Installing(val apkFile: File) : UpdateState()
  data class Error(val message: String, val canRetry: Boolean = true) : UpdateState()
}
