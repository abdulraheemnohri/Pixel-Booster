package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Pixel Booster", appName)
  }

  @Test
  fun `create MainActivity without crashing`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).create()
    org.junit.Assert.assertNotNull(controller.get())
  }

  @Test
  fun `test database and provider initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val provider = com.example.core.platform.DeviceDiagnosticsProvider(context)
    val specs = provider.getDeviceSpecs()
    org.junit.Assert.assertNotNull(specs)
    val mem = provider.getMemoryInfo()
    org.junit.Assert.assertNotNull(mem)
    val storage = provider.getStorageInfo()
    org.junit.Assert.assertNotNull(storage)
    val battery = provider.getBatteryInfo()
    org.junit.Assert.assertNotNull(battery)
  }

  @Test
  fun `verify Screen items are all non-null and have valid routes`() {
    val items = com.example.ui.navigation.Screen.items
    org.junit.Assert.assertTrue("At least one navigation destination must exist", items.isNotEmpty())
    org.junit.Assert.assertEquals("Navigation routes must be unique", items.size, items.map { it.route }.toSet().size)
    items.forEach { screen ->
      org.junit.Assert.assertNotNull(screen)
      org.junit.Assert.assertTrue(screen.route.isNotBlank())
      org.junit.Assert.assertTrue(screen.title.isNotBlank())
    }
  }


}
