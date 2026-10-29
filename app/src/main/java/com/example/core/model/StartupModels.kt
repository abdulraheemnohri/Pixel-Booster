package com.example.core.model

import java.util.Locale

enum class BootImpactLevel(val label: String, val delayDesc: String) {
    HIGH("High review priority", "Heuristic only; boot impact not measured"),
    MEDIUM("Moderate review priority", "Heuristic only; boot impact not measured"),
    LOW("Low review priority", "Heuristic only; boot impact not measured")
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
        get() = if (estimatedBootDelayMs > 0) String.format(Locale.US, "%d ms (estimate)", estimatedBootDelayMs) else "Not measured"
}

data class BootOptimizationResult(
    val appsOptimizedCount: Int,
    val totalDelayReducedMs: Int,
    val ramHeadroomReclaimedBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTimeSaved: String
        get() = if (totalDelayReducedMs > 0) String.format(Locale.US, "Estimated %.1f seconds", totalDelayReducedMs / 1000f) else "Not measured"

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
        get() = if (totalBootDelayMs > 0) String.format(Locale.US, "%.1f sec (estimate)", totalBootDelayMs / 1000f) else "Not measured"

    val formattedTotalRam: String
        get() = if (totalRamFootprintBytes > 0L) MemoryInfoData.formatBytes(totalRamFootprintBytes) else "Not exposed by Android"
}
