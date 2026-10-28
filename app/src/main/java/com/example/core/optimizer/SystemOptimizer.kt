package com.example.core.optimizer

import android.content.Context
import android.os.SystemClock
import com.example.core.database.AppDatabase
import com.example.core.database.OptimizationLogEntity
import com.example.core.logging.EventLogger
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.model.MemoryInfoData
import com.example.core.platform.DeviceDiagnosticsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

data class SystemOptimizationResult(
    val initialUsedRamBytes: Long,
    val postUsedRamBytes: Long,
    val ramBytesReclaimed: Long,
    val cacheBytesFreed: Long,
    val internalCacheFreed: Long,
    val externalCacheFreed: Long,
    val codeCacheFreed: Long,
    val backgroundProcessesTrimmed: Int,
    val durationMs: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalBytesFreed: Long
        get() = cacheBytesFreed

    val formattedRamReclaimed: String
        get() = MemoryInfoData.formatBytes(ramBytesReclaimed)

    val formattedCacheFreed: String
        get() = MemoryInfoData.formatBytes(cacheBytesFreed)

    val formattedTotalFreed: String
        get() = MemoryInfoData.formatBytes(totalBytesFreed)

    val responsivenessGrade: String
        get() = if (cacheBytesFreed > 0L) "CACHE_CLEARED" else "NO_CACHE_TO_CLEAR"
}

object SystemOptimizer {

    /** Clears this app's cache and records observed metrics without claiming system-wide RAM gains. */
    suspend fun optimizeSystem(context: Context): SystemOptimizationResult = withContext(Dispatchers.IO) {
        val startTime = SystemClock.elapsedRealtime()
        val diagnosticsProvider = DeviceDiagnosticsProvider(context)

        // 1. Measure Initial State
        val initialMem = diagnosticsProvider.getMemoryInfo()
        val beforeUsedRam = initialMem.usedRamBytes

        val internalBefore = calculateDirSize(context.cacheDir)
        val externalBefore = context.externalCacheDir?.let { calculateDirSize(it) } ?: 0L
        val codeBefore = calculateDirSize(context.codeCacheDir)

        // 2. Clear App Cache Files on Disk
        deleteDirContents(context.cacheDir)
        context.externalCacheDir?.let { deleteDirContents(it) }

        // Keep Smart Boost scoped to this app. Android does not allow ordinary apps to reliably manage other apps' processes.

        // 4. Force Garbage Collection and Buffer Trim
        try {
            System.runFinalization()
            Runtime.getRuntime().gc()
            System.gc()
        } catch (_: Throwable) {}

        // Settle window
        delay(350)

        // 5. Measure Post State
        val postMem = diagnosticsProvider.getMemoryInfo()
        val afterUsedRam = postMem.usedRamBytes

        // Report only measurable values; ordinary apps cannot guarantee global RAM reclamation.
        val measuredRamDelta = (beforeUsedRam - afterUsedRam).coerceAtLeast(0L)
        val internalAfter = calculateDirSize(context.cacheDir)
        val externalAfter = context.externalCacheDir?.let { calculateDirSize(it) } ?: 0L
        val codeAfter = calculateDirSize(context.codeCacheDir)
        val internalFreed = (internalBefore - internalAfter).coerceAtLeast(0L)
        val externalFreed = (externalBefore - externalAfter).coerceAtLeast(0L)
        val codeFreed = (codeBefore - codeAfter).coerceAtLeast(0L)
        val finalCacheFreed = internalFreed + externalFreed + codeFreed
        val duration = SystemClock.elapsedRealtime() - startTime

        val result = SystemOptimizationResult(
            initialUsedRamBytes = beforeUsedRam,
            postUsedRamBytes = afterUsedRam,
            ramBytesReclaimed = measuredRamDelta,
            cacheBytesFreed = finalCacheFreed,
            internalCacheFreed = internalFreed,
            externalCacheFreed = externalFreed,
            codeCacheFreed = codeFreed,
            backgroundProcessesTrimmed = 0,
            durationMs = duration
        )

        // 6. Record to EventLogger
        EventLogger.log(
            severity = LogSeverity.INFO,
            category = LogCategory.SYSTEM,
            message = "Smart Boost completed: removed ${result.formattedCacheFreed} from this app cache; observed system used-RAM decrease ${result.formattedRamReclaimed} (not guaranteed reclaimed memory) in ${result.durationMs}ms."
        )

        // 7. Persist to Room Database
        try {
            val dao = AppDatabase.getInstance(context).optimizationLogDao()
            dao.insertLog(
                OptimizationLogEntity(
                    timestamp = result.timestamp,
                    boosterType = "SYSTEM_OPTIMIZER",
                    title = "Smart Boost",
                    details = "Removed ${result.formattedCacheFreed} from this app cache. Observed system used-RAM decrease: ${result.formattedRamReclaimed}; No other-app processes were terminated. These are measurements, not guaranteed system-wide gains.",
                    metricFreed = "${result.formattedCacheFreed} app cache removed",
                    success = true
                )
            )
        } catch (_: Throwable) {
            // Fallback gracefully if database busy
        }

        result
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirSize(f) else f.length()
        }
        return size
    }

    private fun deleteDirContents(dir: File?) {
        if (dir == null || !dir.exists()) return
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                deleteDirContents(f)
            }
            try {
                f.delete()
            } catch (_: Throwable) {}
        }
    }
}
