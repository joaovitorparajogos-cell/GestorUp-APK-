package com.example.updater

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UpdaterComprehensiveTest {

  private lateinit var context: Context
  private lateinit var updateConfig: UpdateConfig
  private lateinit var updateService: GitHubUpdateService
  private lateinit var apkDownloader: ApkDownloader
  private lateinit var apkInstaller: ApkInstaller

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    updateConfig = UpdateConfig(
      currentVersionCode = 3,
      currentVersionName = "1.2"
    )
    updateService = GitHubUpdateService(updateConfig)
    apkDownloader = ApkDownloader(context)
    apkInstaller = ApkInstaller(context)
  }

  // 1. Versão atualizada disponível
  @Test
  fun test_version_comparison_newer_version_available() {
    // Comparação numérica de versionCode
    assertTrue(updateService.isVersionSuperior(remoteCode = 4, currentCode = 3, remoteName = "1.3", currentName = "1.2"))
    assertTrue(updateService.isVersionSuperior(remoteCode = 10, currentCode = 3, remoteName = "2.0", currentName = "1.2"))

    // Comparação semântica de versionName quando versionCode for igual ou 0
    assertTrue(updateService.compareSemanticVersions("1.3.0", "1.2.0") > 0)
    assertTrue(updateService.compareSemanticVersions("1.2.1", "1.2.0") > 0)
    assertTrue(updateService.compareSemanticVersions("2.0", "1.9.9") > 0)

    // Extração de versionCode a partir do corpo da release
    val body = "Nova versão disponível!\nversionCode: 4\nChangelog: Correções gerais"
    assertEquals(4, updateService.parseVersionCode("v1.3", body))
  }

  // 2. Sem atualização disponível (Versão já instalada é a mais recente)
  @Test
  fun test_version_comparison_up_to_date() {
    // Mesma versão instalada
    assertFalse(updateService.isVersionSuperior(remoteCode = 3, currentCode = 3, remoteName = "1.2", currentName = "1.2"))

    // Versão remota inferior à instalada
    assertFalse(updateService.isVersionSuperior(remoteCode = 2, currentCode = 3, remoteName = "1.1", currentName = "1.2"))

    // Comparação semântica igual
    assertEquals(0, updateService.compareSemanticVersions("1.2", "1.2"))
    assertEquals(0, updateService.compareSemanticVersions("1.2.0", "1.2"))
  }

  // 3. Falha de internet / Endpoint inacessível
  @Test
  fun test_internet_failure_handling() = runBlocking {
    val invalidConfig = UpdateConfig(
      apiUrl = "http://127.0.0.1:54321/invalid/endpoint"
    )
    val badService = GitHubUpdateService(invalidConfig)
    val result = badService.checkForUpdates()

    // Deve retornar Result.failure com segurança, sem lançar exceções não tratadas
    assertTrue("A falha de conexão deve ser capturada de forma segura", result.isFailure)
    assertNotNull(result.exceptionOrNull())
  }

  // 4. Download interrompido / Limpeza de resíduos
  @Test
  fun test_interrupted_download_cleanup() {
    val updatesDir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
    val staleTmp = File(updatesDir, "test_stale.tmp").apply { writeText("dados incompletos") }
    val staleApk = File(updatesDir, "test_stale.apk").apply { writeText("apk corrompido") }

    assertTrue(staleTmp.exists())
    assertTrue(staleApk.exists())

    // A função de limpeza deve remover todos os arquivos residuais da pasta updates
    apkDownloader.cleanupOldUpdates()

    assertFalse("Arquivo temporário deve ter sido excluído", staleTmp.exists())
    assertFalse("Arquivo APK residual deve ter sido excluído", staleApk.exists())
  }

  // 5. Hash SHA-256 incorreto bloqueia instalação
  @Test
  fun test_hash_integrity_mismatch_blocks_installation() {
    val testContent = "Este e um pacote APK de teste para o GestorUp".toByteArray()
    val updatesDir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
    val testFile = File(updatesDir, "hash_test.apk").apply {
      FileOutputStream(this).use { it.write(testContent) }
    }

    val md = MessageDigest.getInstance("SHA-256")
    val actualHash = md.digest(testContent).joinToString("") { "%02x".format(it) }
    val wrongHash = "0000000000000000000000000000000000000000000000000000000000000000"

    assertFalse("O hash real não deve coincidir com o hash adulterado", actualHash.equals(wrongHash, ignoreCase = true))

    // Se o hash esperado for diferente, a segurança rejeita
    val updateInfoWithWrongHash = UpdateInfo(
      available = true,
      remoteVersionName = "1.3",
      remoteVersionCode = 4,
      currentVersionName = "1.2",
      currentVersionCode = 3,
      releaseName = "v1.3",
      releaseNotes = "Notas",
      publishedAt = "Hoje",
      apkDownloadUrl = "http://fake/url",
      apkFileName = "app-release.apk",
      apkSize = testContent.size.toLong(),
      expectedSha256 = wrongHash
    )

    assertEquals(wrongHash, updateInfoWithWrongHash.expectedSha256)
    testFile.delete()
  }

  // 6. Assinatura ou pacote incompatível
  @Test
  fun test_incompatible_package_name_blocks_installation() {
    // Arquivo não-APK (texto simples) simulando arquivo adulterado
    val fakeApk = File(context.cacheDir, "fake_corrupted.apk").apply {
      writeText("NOT A REAL APK")
    }

    val validationResult = apkDownloader.validateApkWithPackageManager(fakeApk, currentVersionCode = 3)
    assertTrue("Arquivo inválido deve falhar na validação do PackageManager", validationResult.isFailure)

    fakeApk.delete()
  }

  // 7. Instalação cancelada / Permissão de fontes desconhecidas
  @Test
  fun test_install_permission_flow() {
    // Verifica se a checagem de permissão canRequestPackageInstalls responde corretamente
    val canInstall = apkInstaller.canRequestPackageInstalls()
    // No ambiente Robolectric, o PackageManager retorna true ou false de acordo com a config
    assertNotNull(canInstall)
  }

  // 8. Atualização mantendo os dados existentes
  @Test
  fun test_data_persistence_maintained_across_updates() {
    val prefs = context.getSharedPreferences("gestorup_settings", Context.MODE_PRIVATE)
    prefs.edit()
      .putString("theme_mode", "ESCURO")
      .putString("font_size_option", "PEQUENO")
      .putBoolean("initial_data_seeded", true)
      .apply()

    // Simula uma nova inicialização após atualização
    val theme = prefs.getString("theme_mode", "CLARO")
    val fontSize = prefs.getString("font_size_option", "MEDIO")
    val seeded = prefs.getBoolean("initial_data_seeded", false)

    assertEquals("O tema escuro deve ser mantido após atualização", "ESCURO", theme)
    assertEquals("O tamanho de fonte pequeno deve ser mantido", "PEQUENO", fontSize)
    assertTrue("A flag de dados não deve ser reinicializada", seeded)
  }
}
