package com.example.core.model

import java.util.Locale

enum class BootImpactLevel(val label: String, val delayDesc: String) {
    HIGH("High Impact", "+450ms to 850ms boot delay"),
    MEDIUM("Moderate Impact", "+180ms to 350ms boot delay"),
    LOW("Minimal Impact", "<100ms boot delay")
}

data class StartupAppInfo(
    val packageName: String,
    val appLabel: String,
    val isSystemApp: Boolean,
    val isEssential: Boolean,
    val isEnabledAtBoot: Boolean,
    val bootImpact: BootImpactLevel,
    val estimatedBootDelayMs: Int,
    val estimatedRamBytes: Long,
    val bootTriggerReason: String
) {
    val formattedRamFootprint: String
        get() = if (estimatedRamBytes > 0L) MemoryInfoData.formatBytes(estimatedRamBytes) else "Not exposed by Android"

    val formattedBootDelay: String
        get() = String.format(Locale.US, "+%d ms", estimatedBootDelayMs)
}

data class BootOptimizationResult(
    val appsOptimizedCount: Int,
    val totalDelayReducedMs: Int,
    val ramHeadroomReclaimedBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTimeSaved: String
        get() = String.format(Locale.US, "Estimated %.1f seconds", totalDelayReducedMs / 1000f)

    val formattedRamReclaimed: String
        get() = if (ramHeadroomReclaimedBytes > 0L) MemoryInfoData.formatBytes(ramHeadroomReclaimedBytes) else "Not measured"
}

data class StartupSummary(
    val totalStartupApps: Int,
    val nonEssentialCount: Int,
    val restrictedCount: Int,
    val totalBootDelayMs: Int,
    val totalRamFootprintBytes: Long
) {
    val formattedTotalBootDelay: String
        get() = String.format(Locale.US, "%.1f sec", totalBootDelayMs / 1000f)

    val formattedTotalRam: String
        get() = if (totalRamFootprintBytes > 0L) MemoryInfoData.formatBytes(totalRamFootprintBytes) else "Not exposed by Android"
}
