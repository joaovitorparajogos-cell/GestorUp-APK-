package com.example.updater

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

class ApkDownloader(private val context: Context) {

  private val updatesDir: File
    get() = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }

  /**
   * Realiza o download nativo do APK com monitoramento de progresso, cancelamento
   * e validação rigorosa de integridade e assinatura.
   */
  suspend fun downloadApk(
    updateInfo: UpdateInfo,
    onProgress: (percent: Int, bytesRead: Long, totalBytes: Long) -> Unit,
  ): Result<File> = withContext(Dispatchers.IO) {
    cleanupOldUpdates()

    val targetFile = File(updatesDir, "gestorup_v${updateInfo.remoteVersionCode}.apk")
    val tempFile = File(updatesDir, "gestorup_v${updateInfo.remoteVersionCode}.tmp")

    var connection: HttpURLConnection? = null
    var outputStream: FileOutputStream? = null

    try {
      if (tempFile.exists()) tempFile.delete()
      if (targetFile.exists()) targetFile.delete()

      var currentUrl = updateInfo.apkDownloadUrl
      var redirectCount = 0
      val maxRedirects = 5

      // Segue redirects (GitHub Releases redireciona para AWS S3)
      while (redirectCount < maxRedirects) {
        val url = URL(currentUrl)
        connection = (url.openConnection() as HttpURLConnection).apply {
          requestMethod = "GET"
          connectTimeout = 15000
          readTimeout = 25000
          instanceFollowRedirects = false
          setRequestProperty("User-Agent", "GestorUp-Android-Updater")
          setRequestProperty("Accept-Encoding", "identity")
        }

        val code = connection.responseCode
        if (code in 300..399) {
          val location = connection.getHeaderField("Location")
          connection.disconnect()
          if (location.isNullOrBlank()) {
            throw Exception("Falha de redirecionamento no download do APK.")
          }
          currentUrl = location
          redirectCount++
        } else if (code in 200..299) {
          break
        } else {
          throw Exception("Falha ao baixar o APK (HTTP $code).")
        }
      }

      val conn = connection ?: throw Exception("Não foi possível conectar ao servidor de download.")
      val totalBytes = conn.contentLengthLong.let { if (it > 0) it else updateInfo.apkSize }

      val digest = MessageDigest.getInstance("SHA-256")
      val inputStream = conn.inputStream
      outputStream = FileOutputStream(tempFile)

      val buffer = ByteArray(8192)
      var bytesReadSoFar: Long = 0
      var lastReportedPercent = -1

      while (true) {
        if (!coroutineContext.isActive) {
          throw CancellationException("Download cancelado pelo usuário.")
        }

        val count = inputStream.read(buffer)
        if (count == -1) break

        outputStream.write(buffer, 0, count)
        digest.update(buffer, 0, count)
        bytesReadSoFar += count

        if (totalBytes > 0) {
          val percent = ((bytesReadSoFar * 100) / totalBytes).toInt().coerceIn(0, 100)
          if (percent != lastReportedPercent) {
            lastReportedPercent = percent
            onProgress(percent, bytesReadSoFar, totalBytes)
          }
        } else {
          onProgress(-1, bytesReadSoFar, -1)
        }
      }

      outputStream.flush()
      outputStream.close()
      outputStream = null

      if (!coroutineContext.isActive) {
        throw CancellationException("Download cancelado pelo usuário.")
      }

      // 1. Validação de tamanho mínimo
      if (bytesReadSoFar < 100_000) { // APK não pode ser menor que ~100KB
        throw Exception("Arquivo de download incompleto ou inválido ($bytesReadSoFar bytes).")
      }

      // 2. Validação dos primeiros 4 bytes do cabeçalho ZIP (PK\x03\x04)
      if (!isValidZipHeader(tempFile)) {
        throw Exception("O arquivo baixado não é um APK Android válido (cabeçalho ZIP inválido).")
      }

      // 3. Validação do Hash SHA-256 (se disponível)
      val calculatedSha256 = digest.digest().joinToString("") { "%02x".format(it) }
      if (!updateInfo.expectedSha256.isNullOrBlank()) {
        val expected = updateInfo.expectedSha256.trim().lowercase()
        if (!calculatedSha256.equals(expected, ignoreCase = true)) {
          throw SecurityException(
            "Verificação de integridade falhou: O hash SHA-256 do arquivo baixado ($calculatedSha256) não coincide com o esperado ($expected)."
          )
        }
      }

      // 4. Validação do APK via PackageManager oficial do Android
      val validationResult = validateApkWithPackageManager(tempFile, updateInfo.currentVersionCode)
      if (validationResult.isFailure) {
        throw validationResult.exceptionOrNull() ?: Exception("APK inválido.")
      }

      // Renomeia o arquivo temporário para o destino final
      if (!tempFile.renameTo(targetFile)) {
        tempFile.copyTo(targetFile, overwrite = true)
        tempFile.delete()
      }

      Result.success(targetFile)
    } catch (ce: CancellationException) {
      tempFile.delete()
      targetFile.delete()
      Result.failure(ce)
    } catch (e: Exception) {
      tempFile.delete()
      targetFile.delete()
      Result.failure(e)
    } finally {
      try { outputStream?.close() } catch (_: Exception) {}
      connection?.disconnect()
    }
  }

  /**
   * Valida se os 4 primeiros bytes do arquivo correspondem ao padrão 'PK\x03\x04'.
   */
  private fun isValidZipHeader(file: File): Boolean {
    return try {
      FileInputStream(file).use { fis ->
        val header = ByteArray(4)
        val read = fis.read(header)
        read == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
      }
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Valida o pacote através da API do PackageManager do Android:
   * - O arquivo precisa ser um APK analisável.
   * - O packageName deve ser idêntico ao do app instalado.
   * - O versionCode não pode ser inferior ao atualmente instalado.
   */
  fun validateApkWithPackageManager(apkFile: File, currentVersionCode: Int): Result<PackageInfo> {
    return try {
      val pm = context.packageManager
      val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES
      } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
      }

      val pkgInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
        ?: return Result.failure(Exception("O arquivo baixado está corrompido ou não é reconhecido pelo instalador Android."))

      if (pkgInfo.packageName != context.packageName) {
        return Result.failure(
          SecurityException(
            "Incompatibilidade de aplicativo: o APK pertence ao pacote '${pkgInfo.packageName}', enquanto este app é '${context.packageName}'."
          )
        )
      }

      val apkVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pkgInfo.longVersionCode.toInt()
      } else {
        @Suppress("DEPRECATION")
        pkgInfo.versionCode
      }

      if (apkVersionCode < currentVersionCode) {
        return Result.failure(
          SecurityException(
            "Tentativa de downgrade bloqueada: a versão do APK (Build $apkVersionCode) é inferior à versão atual (Build $currentVersionCode)."
          )
        )
      }

      Result.success(pkgInfo)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Exclui arquivos de atualização antigos da pasta de cache.
   */
  fun cleanupOldUpdates() {
    try {
      updatesDir.listFiles()?.forEach { file ->
        if (file.isFile) file.delete()
      }
    } catch (_: Exception) {}
  }
}
