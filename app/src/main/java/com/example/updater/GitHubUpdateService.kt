package com.example.updater

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

class GitHubUpdateService(private val config: UpdateConfig = UpdateConfig()) {

  suspend fun checkForUpdates(): Result<UpdateInfo?> = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
      val url = URL(config.apiUrl)
      connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10000
        readTimeout = 15000
        setRequestProperty("Accept", "application/vnd.github.v3+json")
        setRequestProperty("User-Agent", "GestorUp-Android/${config.currentVersionName}")
      }

      val responseCode = connection.responseCode
      if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
        return@withContext Result.failure(Exception("Nenhuma release encontrada no repositório GitHub."))
      }
      if (responseCode == 403 || responseCode == 429) {
        return@withContext Result.failure(Exception("Limite de requisições do GitHub excedido temporariamente. Tente novamente em alguns minutos."))
      }
      if (responseCode !in 200..299) {
        return@withContext Result.failure(Exception("Falha na consulta ao GitHub (HTTP $responseCode)."))
      }

      val responseText = connection.inputStream.bufferedReader().use(BufferedReader::readText)
      val json = JSONObject(responseText)

      val tagName = json.optString("tag_name", "").trim()
      val releaseName = json.optString("name", tagName).ifBlank { tagName }
      val releaseBody = json.optString("body", "").trim()
      val publishedAt = formatIsoDate(json.optString("published_at", ""))

      val assetsArray = json.optJSONArray("assets") ?: JSONArray()
      var targetApkAsset: JSONObject? = null
      var sha256AssetUrl: String? = null

      for (i in 0 until assetsArray.length()) {
        val asset = assetsArray.getJSONObject(i)
        val assetName = asset.optString("name", "")
        if (assetName.equals(config.expectedApkName, ignoreCase = true)) {
          targetApkAsset = asset
        } else if (targetApkAsset == null && assetName.equals(config.fallbackApkName, ignoreCase = true)) {
          targetApkAsset = asset
        } else if (targetApkAsset == null && assetName.endsWith(".apk", ignoreCase = true)) {
          targetApkAsset = asset
        }

        if (assetName.endsWith(".sha256", ignoreCase = true) || assetName.contains("sha256", ignoreCase = true)) {
          sha256AssetUrl = asset.optString("browser_download_url").ifBlank { null }
        }
      }

      if (targetApkAsset == null) {
        return@withContext Result.failure(Exception("Nenhum arquivo APK encontrado na release '$tagName'."))
      }

      val apkDownloadUrl = targetApkAsset.optString("browser_download_url", "")
      val apkFileName = targetApkAsset.optString("name", config.expectedApkName)
      val apkSize = targetApkAsset.optLong("size", 0L)

      // Extração de Version Code e Version Name
      val remoteVersionCode = parseVersionCode(tagName, releaseBody)
      val remoteVersionName = parseVersionName(tagName)

      // Verificação se a versão remota é superior à instalada
      val isNewer = isVersionSuperior(
        remoteCode = remoteVersionCode,
        currentCode = config.currentVersionCode,
        remoteName = remoteVersionName,
        currentName = config.currentVersionName
      )

      // Extração do hash SHA-256 esperado (do corpo da release ou download do asset)
      var expectedSha256 = extractSha256FromBody(releaseBody, apkFileName)
      if (expectedSha256 == null && !sha256AssetUrl.isNullOrBlank()) {
        expectedSha256 = fetchSha256FromUrl(sha256AssetUrl)
      }

      val updateInfo = UpdateInfo(
        available = isNewer,
        remoteVersionName = remoteVersionName,
        remoteVersionCode = remoteVersionCode,
        currentVersionName = config.currentVersionName,
        currentVersionCode = config.currentVersionCode,
        releaseName = releaseName,
        releaseNotes = if (releaseBody.isBlank()) "Correções de desempenho, estabilidade e melhorias gerais." else releaseBody,
        publishedAt = publishedAt,
        apkDownloadUrl = apkDownloadUrl,
        apkFileName = apkFileName,
        apkSize = apkSize,
        sha256DownloadUrl = sha256AssetUrl,
        expectedSha256 = expectedSha256
      )

      Result.success(updateInfo)
    } catch (e: Exception) {
      Result.failure(e)
    } finally {
      connection?.disconnect()
    }
  }

  /**
   * Identifica o versionCode da release a partir do corpo ou tag_name.
   */
  fun parseVersionCode(tagName: String, body: String): Int {
    // 1. Procura no corpo da release por: versionCode: 4 ou Build 4
    val bodyMatcher = Pattern.compile("(?i)(?:versioncode|build)[\\s:=]+(\\d+)").matcher(body)
    if (bodyMatcher.find()) {
      bodyMatcher.group(1)?.toIntOrNull()?.let { if (it > 0) return it }
    }

    // 2. Se a tag for algo como "v4" ou "4"
    val tagNumericMatcher = Pattern.compile("^v?(\\d+)$").matcher(tagName.trim())
    if (tagNumericMatcher.find()) {
      tagNumericMatcher.group(1)?.toIntOrNull()?.let { if (it > 0) return it }
    }

    // 3. Se a tag for "v1.2-b4" ou "1.2-build4"
    val tagBuildMatcher = Pattern.compile("(?i)(?:-b|-build)(\\d+)").matcher(tagName)
    if (tagBuildMatcher.find()) {
      tagBuildMatcher.group(1)?.toIntOrNull()?.let { if (it > 0) return it }
    }

    // 4. Fallback semântico: v1.3 -> 1 * 100 + 3 = 103
    val semverMatcher = Pattern.compile("^v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?").matcher(tagName.trim())
    if (semverMatcher.find()) {
      val major = semverMatcher.group(1)?.toIntOrNull() ?: 0
      val minor = semverMatcher.group(2)?.toIntOrNull() ?: 0
      val patch = semverMatcher.group(3)?.toIntOrNull() ?: 0
      return (major * 10000) + (minor * 100) + patch
    }

    return 0
  }

  /**
   * Extrai o nome de versão amigável (ex: "1.3" de "v1.3").
   */
  fun parseVersionName(tagName: String): String {
    return tagName.removePrefix("v").removePrefix("V").trim()
  }

  /**
   * Compara se a versão remota é superior à instalada sem comparação textual ingênua.
   */
  fun isVersionSuperior(
    remoteCode: Int,
    currentCode: Int,
    remoteName: String,
    currentName: String
  ): Boolean {
    // Prioridade 1: Comparação numérica de versionCode
    if (remoteCode > 0 && currentCode > 0) {
      return remoteCode > currentCode
    }

    // Prioridade 2: Comparação semântica de versionName (ex: 1.2.1 > 1.2)
    return compareSemanticVersions(remoteName, currentName) > 0
  }

  /**
   * Compara duas versões semânticas (ex: "1.2.1" vs "1.2").
   * Retorna > 0 se v1 > v2, < 0 se v1 < v2, 0 se iguais.
   */
  fun compareSemanticVersions(v1: String, v2: String): Int {
    val clean1 = v1.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }
    val clean2 = v2.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }

    val maxLen = maxOf(clean1.size, clean2.size)
    for (i in 0 until maxLen) {
      val p1 = clean1.getOrElse(i) { 0 }
      val p2 = clean2.getOrElse(i) { 0 }
      if (p1 != p2) {
        return p1.compareTo(p2)
      }
    }
    return 0
  }

  private fun extractSha256FromBody(body: String, apkFileName: String): String? {
    if (body.isBlank()) return null

    // Procura por linhas contendo o nome do APK e um hash SHA-256 (64 hexadecimais)
    val lines = body.lines()
    for (line in lines) {
      val shaMatcher = Pattern.compile("(?i)\\b([a-f0-9]{64})\\b").matcher(line)
      if (shaMatcher.find()) {
        val hash = shaMatcher.group(1)
        if (line.contains(apkFileName, ignoreCase = true) || !hash.isNullOrBlank()) {
          return hash?.lowercase()
        }
      }
    }
    return null
  }

  private fun fetchSha256FromUrl(urlStr: String): String? {
    return try {
      val url = URL(urlStr)
      val conn = url.openConnection() as HttpURLConnection
      conn.connectTimeout = 8000
      conn.readTimeout = 8000
      conn.inputStream.bufferedReader().use { reader ->
        val text = reader.readText().trim()
        val matcher = Pattern.compile("(?i)\\b([a-f0-9]{64})\\b").matcher(text)
        if (matcher.find()) {
          matcher.group(1)?.lowercase()
        } else {
          null
        }
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun formatIsoDate(isoDate: String): String {
    if (isoDate.isBlank()) return "Recente"
    return try {
      val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
      val date = parser.parse(isoDate) ?: return isoDate
      val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
      formatter.format(date)
    } catch (_: Exception) {
      isoDate.substringBefore("T")
    }
  }
}
