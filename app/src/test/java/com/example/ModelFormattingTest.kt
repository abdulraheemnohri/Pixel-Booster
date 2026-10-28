package com.example

import com.example.core.model.AppUsageData
import com.example.core.model.DeviceSpecs
import com.example.core.model.MemoryInfoData
import com.example.core.model.StorageInfoData
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelFormattingTest {

    @Test
    fun `formatBytes converts gigabytes correctly`() {
        val bytes = 8_589_934_592L // 8.0 GB
        assertEquals("8.0 GB", MemoryInfoData.formatBytes(bytes))
    }

    @Test
    fun `formatBytes converts megabytes correctly`() {
        val bytes = 524_288_000L // ~500 MB
        assertEquals("500 MB", MemoryInfoData.formatBytes(bytes))
    }

    @Test
    fun `memory percentages calculate within bounds`() {
        val mem = MemoryInfoData(
            totalRamBytes = 8_000_000_000L,
            availableRamBytes = 2_000_000_000L,
            thresholdBytes = 1_000_000_000L,
            isLowMemory = false
        )
        assertEquals(75.0f, mem.usedPercentage, 0.01f)
        assertEquals(25.0f, mem.availablePercentage, 0.01f)
    }

    @Test
    fun `storage percentages calculate correctly`() {
        val storage = StorageInfoData(
            totalBytes = 128_000_000_000L,
            freeBytes = 32_000_000_000L
        )
        assertEquals(75.0f, storage.usedPercentage, 0.01f)
        assertEquals(25.0f, storage.freePercentage, 0.01f)
    }

    @Test
    fun `app usage duration formatting handles hours and minutes`() {
        val usage = AppUsageData(
            packageName = "com.google.android.youtube",
            appLabel = "YouTube",
            totalTimeInForegroundMillis = 3_720_000L, // 1 hour 2 minutes
            lastTimeUsed = System.currentTimeMillis()
        )
        assertEquals("1h 2m", usage.formattedForegroundTime)
    }

    @Test
    fun `device specs formatted uptime calculates hours and minutes`() {
        val specs = DeviceSpecs(
            manufacturer = "Google",
            model = "Pixel 6",
            deviceCodename = "oriole",
            socModel = "Google Tensor",
            isPixelDevice = true,
            pixelGenerationName = "Pixel 6 Series (Tensor G1)",
            androidVersion = "14",
            sdkInt = 34,
            buildId = "UP1A.231005.007",
            supportedAbis = listOf("arm64-v8a"),
            uptimeMillis = 3661000L, // 1h 1m 1s
            isThermalSupported = true
        )
        assertEquals("1h 1m 1s", specs.formattedUptime)
    }
}
