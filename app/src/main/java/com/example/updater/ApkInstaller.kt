package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

class ApkInstaller(private val context: Context) {

  /**
   * Verifica se o aplicativo possui permissão do usuário para instalar pacotes dessa origem.
   */
  fun canRequestPackageInstalls(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.packageManager.canRequestPackageInstalls()
    } else {
      true
    }
  }

  /**
   * Abre a tela oficial de configurações do sistema para o usuário conceder a permissão.
   */
  fun openInstallPermissionSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }
  }

  /**
   * Dispara o instalador de pacotes oficial do Android usando FileProvider e Intent.ACTION_VIEW.
   * Não instala silenciosamente; solicita confirmação explícita do usuário.
   */
  fun installApk(apkFile: File): Result<Unit> {
    return try {
      if (!apkFile.exists()) {
        return Result.failure(Exception("Arquivo APK não encontrado em: ${apkFile.absolutePath}"))
      }

      val apkUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
      )

      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      context.startActivity(intent)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
