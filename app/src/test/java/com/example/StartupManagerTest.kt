package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.BootImpactLevel
import com.example.core.model.ThemeMode
import com.example.core.platform.StartupAppsProvider
import com.example.core.preferences.UserPreferencesRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StartupManagerTest {

    @Test
    fun `test startup apps provider discovery and impact categorization`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesRepository.getInstance(context)
        val provider = StartupAppsProvider(context, prefs)

        val apps = provider.getStartupApps()
        assertNotNull(apps)
        assertTrue(apps.isNotEmpty())

        // Discovery reflects packages actually visible to the app; it must not invent entries.
        val nonEssential = apps.filter { !it.isEssential }
        assertTrue(nonEssential.isNotEmpty())
        assertTrue(apps.all { it.estimatedBootDelayMs >= 0 })
        // Per-app RAM usage is not reliably exposed by Android to ordinary apps.
        assertTrue(apps.all { it.estimatedRamBytes == 0L })
    }

    @Test
    fun `test toggle app startup restriction and summary calculation`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesRepository.getInstance(context)
        val provider = StartupAppsProvider(context, prefs)

        // Clear any old restrictions
        prefs.clearAllRestrictedStartupApps()

        val initialSummary = provider.getStartupSummary()
        assertTrue(initialSummary.totalStartupApps > 0)
        assertTrue(initialSummary.totalBootDelayMs > 0)
        assertEquals(0L, initialSummary.totalRamFootprintBytes)

        // Find a non-essential app to toggle
        val targetApp = provider.getStartupApps().first { !it.isEssential }
        val targetPkg = targetApp.packageName

        // Restrict app
        provider.toggleAppStartup(targetPkg, enable = false)
        val restrictedSet = prefs.restrictedStartupPackagesFlow.first()
        assertTrue(restrictedSet.contains(targetPkg))

        val updatedApps = provider.getStartupApps()
        val updatedTarget = updatedApps.first { it.packageName == targetPkg }
        assertFalse(updatedTarget.isEnabledAtBoot)

        // Re-enable app
        provider.toggleAppStartup(targetPkg, enable = true)
        val reenabledSet = prefs.restrictedStartupPackagesFlow.first()
        assertFalse(reenabledSet.contains(targetPkg))

        val reenabledApps = provider.getStartupApps()
        val reenabledTarget = reenabledApps.first { it.packageName == targetPkg }
        assertTrue(reenabledTarget.isEnabledAtBoot)
    }

    @Test
    fun `test optimize all non-essential boot apps`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesRepository.getInstance(context)
        val provider = StartupAppsProvider(context, prefs)

        prefs.clearAllRestrictedStartupApps()

        val result = provider.optimizeAllNonEssential()
        assertNotNull(result)
        assertTrue(result.appsOptimizedCount > 0)
        assertTrue(result.totalDelayReducedMs > 0)
        assertEquals(0L, result.ramHeadroomReclaimedBytes)
        assertTrue(result.formattedTimeSaved.contains("seconds"))
        assertTrue(result.formattedRamReclaimed.isNotBlank())

        val postSummary = provider.getStartupSummary()
        assertTrue(postSummary.restrictedCount >= result.appsOptimizedCount)
    }

    @Test
    fun `test dark mode theme settings persistence and sync`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = UserPreferencesRepository.getInstance(context)

        // 1. Force Dark Mode
        prefs.setThemeMode(ThemeMode.DARK)
        var settings = prefs.settingsFlow.first()
        assertEquals(ThemeMode.DARK, settings.themeMode)

        // 2. Force Light Mode
        prefs.setThemeMode(ThemeMode.LIGHT)
        settings = prefs.settingsFlow.first()
        assertEquals(ThemeMode.LIGHT, settings.themeMode)

        // 3. System Sync Mode
        prefs.setThemeMode(ThemeMode.SYSTEM)
        settings = prefs.settingsFlow.first()
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
    }

    @Test
    fun `test secondary Startup screen remains available outside primary navigation`() {
        assertNotNull(Screen.Startup)
        assertEquals("startup", Screen.Startup.route)
        assertEquals("Startup", Screen.Startup.title)
        assertFalse(Screen.items.contains(Screen.Startup))
        assertTrue(Screen.items.contains(Screen.More))
        assertEquals(6, Screen.items.size)
    }
}
