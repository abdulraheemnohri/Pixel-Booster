package com.example.core.booster

import android.content.Context
import android.os.SystemClock
import com.example.core.platform.DeviceDiagnosticsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

/** Reports observations only; Android does not let ordinary apps control system-wide RAM, CPU cooling or other apps' sockets. */
class BoosterService(private val context: Context) {
    private val diagnosticsProvider = DeviceDiagnosticsProvider(context)

    suspend fun boostRam(): Pair<Long, Long> = withContext(Dispatchers.Default) {
        val before = diagnosticsProvider.getMemoryInfo().usedRamBytes
        runCatching { Runtime.getRuntime().gc() } // current app runtime only
        delay(250)
        val after = diagnosticsProvider.getMemoryInfo().usedRamBytes
        Pair(before, (before - after).coerceAtLeast(0L))
    }

    suspend fun scanJunk(): JunkScanResult = withContext(Dispatchers.IO) {
        JunkScanResult(
            systemCacheBytes = calculateDirSize(context.cacheDir),
            appThumbnailsBytes = context.externalCacheDir?.let(::calculateDirSize) ?: 0L,
            temporaryFilesBytes = calculateDirSize(context.codeCacheDir),
            residualLogsBytes = 0L
        )
    }

    fun getLiveRamMetrics(): LiveRamMetrics {
        val mem = diagnosticsProvider.getMemoryInfo()
        val runtime = Runtime.getRuntime()
        val allocated = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L)
        val pressure = when {
            mem.isLowMemory || mem.usedPercentage > 85f -> MemoryPressureLevel.CRITICAL
            mem.usedPercentage > 68f -> MemoryPressureLevel.MODERATE
            else -> MemoryPressureLevel.NOMINAL
        }
        return LiveRamMetrics(mem.totalRamBytes, mem.availableRamBytes, mem.usedRamBytes,
            mem.usedPercentage, mem.thresholdBytes, mem.isLowMemory, allocated,
            runtime.maxMemory(), runtime.freeMemory(), pressure)
    }

    suspend fun estimateNonEssentialCacheBytes(): Long = withContext(Dispatchers.IO) {
        calculateDirSize(context.cacheDir) +
            (context.externalCacheDir?.let(::calculateDirSize) ?: 0L) +
            calculateDirSize(context.codeCacheDir)
    }

    fun markCacheCleared() = Unit

    suspend fun clearNonEssentialCache(): CacheClearReport = withContext(Dispatchers.IO) {
        val start = SystemClock.elapsedRealtime()
        val iBefore = calculateDirSize(context.cacheDir)
        val eBefore = context.externalCacheDir?.let(::calculateDirSize) ?: 0L
        val cBefore = calculateDirSize(context.codeCacheDir)
        // Only this app's cache folders; never filesDir, shared media, downloads, or other apps.
        deleteDirContents(context.cacheDir)
        context.externalCacheDir?.let(::deleteDirContents)
        deleteDirContents(context.codeCacheDir)
        val iFreed = (iBefore - calculateDirSize(context.cacheDir)).coerceAtLeast(0L)
        val eFreed = (eBefore - (context.externalCacheDir?.let(::calculateDirSize) ?: 0L)).coerceAtLeast(0L)
        val cFreed = (cBefore - calculateDirSize(context.codeCacheDir)).coerceAtLeast(0L)
        CacheClearReport(iFreed + eFreed + cFreed, iFreed, eFreed, cFreed, 0L,
            responsivenessScoreBoost = 0,
            durationMs = (SystemClock.elapsedRealtime() - start).coerceAtLeast(0L))
    }

    suspend fun cleanJunk(junkScanResult: JunkScanResult): Long =
        clearNonEssentialCache().totalFreedBytes

    /** Measured battery-temperature change in °C: positive means the sensor reading fell. */
    suspend fun coolCpu(): Float = withContext(Dispatchers.Default) {
        val before = diagnosticsProvider.getBatteryInfo().temperatureCelsius ?: return@withContext 0f
        delay(3000)
        val after = diagnosticsProvider.getBatteryInfo().temperatureCelsius ?: return@withContext 0f
        before - after
    }

    suspend fun testAndOptimizeNetwork(): NetworkPingResult = withContext(Dispatchers.IO) {
        val host = "8.8.8.8"
        val first = measureLatency(host, 53)
        delay(200)
        val second = measureLatency(host, 53)
        NetworkPingResult("Google DNS ($host)", first.coerceAtLeast(0L),
            second.coerceAtLeast(0L), first > 0L || second > 0L)
    }

    suspend fun optimizeNetworkSocketsAndDns(): NetworkOptimizationReport = withContext(Dispatchers.IO) {
        val start = SystemClock.elapsedRealtime()
        val ping = testAndOptimizeNetwork()
        NetworkOptimizationReport(0, ping.latencyImprovementMs, false,
            (SystemClock.elapsedRealtime() - start).coerceAtLeast(0L),
            if (ping.isOnline) "Measured two connection latencies; system sockets and DNS cache were not changed."
            else "Connection test failed; no latency result is available. No network settings were changed.")
    }

    private fun measureLatency(host: String, port: Int): Long {
        val start = SystemClock.elapsedRealtime()
        return try {
            Socket().use { it.connect(InetSocketAddress(host, port), 1500) }
            (SystemClock.elapsedRealtime() - start).coerceAtLeast(1L)
        } catch (_: Exception) { -1L }
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return (dir.listFiles() ?: return 0L).sumOf {
            if (it.isDirectory) calculateDirSize(it) else it.length().coerceAtLeast(0L)
        }
    }

    private fun deleteDirContents(dir: File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { runCatching { it.deleteRecursively() } }
    }
}