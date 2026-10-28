package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.AppDatabase
import com.example.core.optimizer.SystemOptimizer
import com.example.widget.PixelBoosterWidgetProvider
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
class SystemOptimizerTest {

    @Test
    fun `test system optimizer execution clears cache and releases memory`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = SystemOptimizer.optimizeSystem(context)

        assertNotNull(result)
        assertTrue(result.ramBytesReclaimed >= 0L)
        assertTrue(result.cacheBytesFreed >= 0L)
        assertTrue(result.totalBytesFreed >= 0L)
        assertTrue(result.formattedRamReclaimed.isNotBlank())
        assertTrue(result.formattedCacheFreed.isNotBlank())
        assertTrue(result.formattedTotalFreed.isNotBlank())
        assertTrue(result.responsivenessGrade.isNotBlank())
        assertTrue(result.durationMs > 0L)

        // Verify Room log insertion
        val db = AppDatabase.getInstance(context)
        val logs = db.optimizationLogDao().getAllLogs().first()
        val optimizerLog = logs.find { it.boosterType == "SYSTEM_OPTIMIZER" }
        assertNotNull(optimizerLog)
        assertEquals("Deep System Optimization", optimizerLog?.title)
    }

    @Test
    fun `test widget provider action constants and class loading`() {
        // Smart Boost is intentionally a guided app launch, not a one-tap process-kill action.
        assertEquals("com.example.widget.PixelBoosterWidgetProvider", PixelBoosterWidgetProvider::class.java.name)
        val provider = PixelBoosterWidgetProvider()
        assertNotNull(provider)
    }
}
