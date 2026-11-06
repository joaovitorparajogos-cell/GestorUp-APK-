package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.brand.BrandAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GestorUp", appName)
  }

  @Test
  fun `verify brand assets are properly configured`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val logoDrawable = context.getDrawable(BrandAssets.LOGO_FULL_RES)
    val symbolDrawable = context.getDrawable(BrandAssets.SYMBOL_RES)

    assertNotNull("Full logo resource must exist", logoDrawable)
    assertNotNull("Symbol resource must exist", symbolDrawable)
    assertEquals("GestorUp", BrandAssets.APP_NAME)
    assertEquals("Sua equipe. Sua gestão. Tudo em ordem.", BrandAssets.SLOGAN)
  }
}
