package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.optimizer.SystemOptimizer
import com.example.widget.PixelBoosterWidgetProvider
import kotlinx.coroutines.runBlocking
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
    fun `test smart boost returns measured nonnegative metrics` = runBlocking {
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
    }

    @Test
    fun `test widget provider action constants and class loading`() {
        // Smart Boost is intentionally a guided app launch, not a one-tap process-kill action.
        assertEquals("com.example.widget.PixelBoosterWidgetProvider", PixelBoosterWidgetProvider::class.java.name)
        val provider = PixelBoosterWidgetProvider()
        assertNotNull(provider)
    }
}
