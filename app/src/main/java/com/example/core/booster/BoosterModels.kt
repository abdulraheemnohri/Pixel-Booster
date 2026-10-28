package com.example.core.booster

import com.example.core.model.MemoryInfoData

enum class BoosterToolType(
    val title: String,
    val subtitle: String,
    val description: String
) {
    RAM_BOOSTER(
        title = "RAM Booster",
        subtitle = "Memory Reclaim",
        description = "Shows measured system memory and requests garbage collection for this app only; Android manages system-wide RAM."
    ),
    JUNK_CLEANER(
        title = "Junk Cleaner",
        subtitle = "Cache & Temp Purge",
        description = "Scans and clears only this app’s disposable cache folders; other apps and personal files are not touched."
    ),
    CPU_COOLER(
        title = "CPU Cooler",
        subtitle = "Thermal Relief",
        description = "Reads available thermal and battery sensors and offers safe cooling advice; it cannot force-cool the CPU."
    ),
    BATTERY_SAVER(
        title = "Battery Saver",
        subtitle = "Power Profiles",
        description = "Provides supported power-setting guidance; it cannot unlock CPU frequencies or guarantee battery gains."
    ),
    NETWORK_TURBO(
        title = "Network Turbo",
        subtitle = "Ping & Latency",
        description = "Measures connection latency only; it does not flush system sockets or guarantee lower ping."
    ),
    GAME_TURBO(
        title = "Game Turbo",
        subtitle = "Maximum FPS Priority",
        description = "Provides game-use guidance; Android does not expose a general no-root FPS boost switch."
    )
}

enum class BoosterStage {
    IDLE,
    SCANNING,
    OPTIMIZING_RAM,
    CLEANING_CACHE,
    COOLING_HARDWARE,
    ACCELERATING_NETWORK,
    COMPLETED
}

enum class PowerProfile(val label: String, val description: String) {
    BALANCED(
        label = "Balanced Smart",
        description = "Default adaptive tuning for all-day reliability and smooth responsiveness."
    ),
    PERFORMANCE(
        label = "Performance Turbo",
        description = "Unlocks maximum CPU frequency and zero-throttle display refresh rate."
    ),
    EXTREME_SAVER(
        label = "Ultra Power Saver",
        description = "Limits background sync and launches system Battery Saver for emergency endurance."
    )
}

data class BoostResultSummary(
    val toolType: BoosterToolType?,
    val title: String,
    val headlineMetric: String,
    val details: List<Pair<String, String>>,
    val timestamp: Long = System.currentTimeMillis()
)

data class JunkScanResult(
    val systemCacheBytes: Long,
    val appThumbnailsBytes: Long,
    val temporaryFilesBytes: Long,
    val residualLogsBytes: Long
) {
    val totalBytes: Long
        get() = systemCacheBytes + appThumbnailsBytes + temporaryFilesBytes + residualLogsBytes

    val formattedTotal: String
        get() = MemoryInfoData.formatBytes(totalBytes)

    val formattedSystemCache: String
        get() = MemoryInfoData.formatBytes(systemCacheBytes)

    val formattedThumbnails: String
        get() = MemoryInfoData.formatBytes(appThumbnailsBytes)

    val formattedTempFiles: String
        get() = MemoryInfoData.formatBytes(temporaryFilesBytes)

    val formattedLogs: String
        get() = MemoryInfoData.formatBytes(residualLogsBytes)
}

data class NetworkPingResult(
    val host: String,
    val initialLatencyMs: Long,
    val optimizedLatencyMs: Long,
    val isOnline: Boolean
) {
    val latencyImprovementMs: Long
        get() = (initialLatencyMs - optimizedLatencyMs).coerceAtLeast(0L)
}

enum class MemoryPressureLevel(val label: String, val description: String) {
    NOMINAL("Optimal Headroom", "Substantial RAM headroom available for foreground apps"),
    MODERATE("Moderate Buffer Load", "Active background buffers and cached processes in memory"),
    CRITICAL("High Pressure", "System memory paging under load; cache purge and RAM trim recommended")
}

data class LiveRamMetrics(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Float,
    val thresholdBytes: Long,
    val isLowMemory: Boolean,
    val jvmAllocatedBytes: Long,
    val jvmMaxBytes: Long,
    val jvmFreeBytes: Long,
    val memoryPressure: MemoryPressureLevel
) {
    val formattedTotal: String get() = MemoryInfoData.formatBytes(totalBytes)
    val formattedAvailable: String get() = MemoryInfoData.formatBytes(availableBytes)
    val formattedUsed: String get() = MemoryInfoData.formatBytes(usedBytes)
    val formattedJvmAllocated: String get() = MemoryInfoData.formatBytes(jvmAllocatedBytes)
    val formattedJvmMax: String get() = MemoryInfoData.formatBytes(jvmMaxBytes)
    val formattedJvmFree: String get() = MemoryInfoData.formatBytes(jvmFreeBytes)
}

data class CacheClearReport(
    val totalFreedBytes: Long,
    val internalCacheBytes: Long,
    val externalCacheBytes: Long,
    val codeCacheBytes: Long,
    val temporaryBuffersBytes: Long,
    val responsivenessScoreBoost: Int = 12,
    val durationMs: Long = 420L,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTotal: String get() = MemoryInfoData.formatBytes(totalFreedBytes)
    val formattedInternal: String get() = MemoryInfoData.formatBytes(internalCacheBytes)
    val formattedExternal: String get() = MemoryInfoData.formatBytes(externalCacheBytes)
    val formattedCodeCache: String get() = MemoryInfoData.formatBytes(codeCacheBytes)
    val formattedTempBuffers: String get() = MemoryInfoData.formatBytes(temporaryBuffersBytes)
}

data class NetworkOptimizationReport(
    val socketsFlushed: Int,
    val tcpLatencyTrimMs: Long,
    val dnsCacheFlushed: Boolean,
    val durationMs: Long,
    val statusSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

