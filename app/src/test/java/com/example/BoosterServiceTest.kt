package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.booster.BoosterService
import com.example.core.booster.BoosterToolType
import com.example.core.booster.PowerProfile
import com.example.core.database.AppDatabase
import com.example.core.database.OptimizationRepository
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BoosterServiceTest {

    @Test
    fun `test booster service ram reclaim execution`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BoosterService(context)
        val (before, freed) = service.boostRam()
        assertTrue(before >= 0L)
        assertTrue(freed >= 0L) // Zero is a valid measured result; no synthetic minimum is added.
    }

    @Test
    fun `test booster service junk scan and clean`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BoosterService(context)
        val scan = service.scanJunk()
        assertNotNull(scan)
        assertTrue(scan.totalBytes >= 0L) // Empty app cache is valid.
        assertTrue(scan.formattedTotal.isNotBlank())

        val cleaned = service.cleanJunk(scan)
        assertTrue(cleaned >= 0L) // Report bytes actually removed, not the pre-clean scan estimate.
    }

    @Test
    fun `test cpu cooler thermal relief calculation`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BoosterService(context)
        val drop = service.coolCpu()
        if (drop != null) {
            assertTrue(drop.isFinite()) // Sensor change can be positive, zero, or negative.
            assertTrue(drop < 10.0f)
        } // Missing sensor data is a valid unavailable result.
    }

    @Test
    fun `test optimization repository room logging`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val repo = OptimizationRepository(db.optimizationLogDao())

        repo.clearHistory()
        val initialLogs = repo.allLogs.first()
        assertEquals(0, initialLogs.size)

        val id = repo.recordLog(
            boosterType = BoosterToolType.RAM_BOOSTER.name,
            title = "RAM Memory Booster",
            details = "Reclaimed 380 MB RAM",
            metricFreed = "+380 MB RAM"
        )
        assertTrue(id > 0)

        val updatedLogs = repo.allLogs.first()
        assertEquals(1, updatedLogs.size)
        assertEquals("RAM Memory Booster", updatedLogs.first().title)
        assertEquals("+380 MB RAM", updatedLogs.first().metricFreed)
    }

    @Test
    fun `test booster service live ram metrics telemetry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BoosterService(context)
        val ram = service.getLiveRamMetrics()
        if (ram == null) {
            // Android/Robolectric may not expose valid system RAM telemetry in every runner.
            // Unavailable is an allowed result; never require synthetic fallback values.
            return
        }
        assertTrue(ram.totalBytes > 0L)
        assertTrue(ram.availableBytes >= 0L)
        assertTrue(ram.usedPercentage in 0f..100f)
        assertTrue(ram.formattedTotal.isNotBlank())
        assertTrue(ram.formattedUsed.isNotBlank())
        assertNotNull(ram.memoryPressure)
    }

    @Test
    fun `test booster service non-essential cache clear background process`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val service = BoosterService(context)

        val estimatedCache = service.estimateNonEssentialCacheBytes()
        assertTrue(estimatedCache >= 0L) // Empty cache is valid.

        val report = service.clearNonEssentialCache()
        assertNotNull(report)
        assertTrue(report.totalFreedBytes >= 0L)
        assertTrue(report.formattedTotal.isNotBlank())
        assertTrue(report.internalCacheBytes >= 0L)
        assertTrue(report.durationMs >= 0L)
    }

    @Test
    fun `network latency marks partial failures unavailable`() {
        val partial = com.example.core.booster.NetworkPingResult(
            host = "test host",
            initialLatencyMs = -1L,
            optimizedLatencyMs = 42L,
            isOnline = true
        )
        assertEquals(-1L, partial.initialLatencyMs)
        assertEquals(42L, partial.optimizedLatencyMs)
        assertEquals(Long.MIN_VALUE, partial.latencyImprovementMs)

        val bothSuccessful = com.example.core.booster.NetworkPingResult(
            host = "test host",
            initialLatencyMs = 55L,
            optimizedLatencyMs = 42L,
            isOnline = true
        )
        assertEquals(13L, bothSuccessful.latencyImprovementMs)

        val slowerSecondAttempt = com.example.core.booster.NetworkPingResult(
            host = "test host", initialLatencyMs = 55L, optimizedLatencyMs = 65L, isOnline = true
        )
        assertEquals(-10L, slowerSecondAttempt.latencyImprovementMs)
    }

    @Test
    fun `test Screen Booster route exists and items contain it`() {
        assertNotNull(Screen.Booster)
        assertEquals("booster", Screen.Booster.route)
        assertEquals("Booster", Screen.Booster.title)
        assertTrue(Screen.items.contains(Screen.Booster))
    }
}
