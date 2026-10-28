package com.example.core.model

data class DeviceSpecs(
    val manufacturer: String,
    val model: String,
    val deviceCodename: String,
    val socModel: String,
    val isPixelDevice: Boolean,
    val pixelGenerationName: String,
    val androidVersion: String,
    val sdkInt: Int,
    val buildId: String,
    val supportedAbis: List<String>,
    val uptimeMillis: Long,
    val isThermalSupported: Boolean
) {
    val formattedUptime: String
        get() {
            val totalSeconds = uptimeMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return "${hours}h ${minutes}m ${seconds}s"
        }
}

data class MemoryInfoData(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val thresholdBytes: Long,
    val isLowMemory: Boolean
) {
    val usedRamBytes: Long get() = (totalRamBytes - availableRamBytes).coerceAtLeast(0L)
    val usedPercentage: Float
        get() = if (totalRamBytes > 0) ((usedRamBytes.toFloat() / totalRamBytes.toFloat()) * 100f).coerceIn(0f, 100f) else 0f
    val availablePercentage: Float
        get() = (100f - usedPercentage).coerceIn(0f, 100f)

    val formattedTotal: String get() = formatBytes(totalRamBytes)
    val formattedAvailable: String get() = formatBytes(availableRamBytes)
    val formattedUsed: String get() = formatBytes(usedRamBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val gb = bytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            return if (gb >= 1.0) {
                String.format(java.util.Locale.US, "%.1f GB", gb)
            } else {
                val mb = bytes.toDouble() / (1024.0 * 1024.0)
                String.format(java.util.Locale.US, "%.0f MB", mb)
            }
        }
    }
}

data class StorageInfoData(
    val totalBytes: Long,
    val freeBytes: Long
) {
    val usedBytes: Long get() = (totalBytes - freeBytes).coerceAtLeast(0L)
    val usedPercentage: Float
        get() = if (totalBytes > 0) ((usedBytes.toFloat() / totalBytes.toFloat()) * 100f).coerceIn(0f, 100f) else 0f
    val freePercentage: Float
        get() = (100f - usedPercentage).coerceIn(0f, 100f)

    val formattedTotal: String get() = MemoryInfoData.formatBytes(totalBytes)
    val formattedFree: String get() = MemoryInfoData.formatBytes(freeBytes)
    val formattedUsed: String get() = MemoryInfoData.formatBytes(usedBytes)
}

enum class BatteryChargingState {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

enum class BatteryPluggedSource {
    AC,
    USB,
    WIRELESS,
    NONE
}

data class BatteryInfoData(
    val levelPercentage: Int,
    val state: BatteryChargingState,
    val pluggedSource: BatteryPluggedSource,
    val temperatureCelsius: Float?,
    val voltageMilliVolts: Int?,
    val healthDescription: String,
    val technology: String?
) {
    val isCharging: Boolean get() = state == BatteryChargingState.CHARGING
    val isFull: Boolean get() = state == BatteryChargingState.FULL
    val formattedTemperature: String
        get() = temperatureCelsius?.let { String.format(java.util.Locale.US, "%.1f°C", it) } ?: "Unavailable"
}

enum class ThermalSeverityLevel {
    NOMINAL,
    WARNING,
    CRITICAL,
    UNSUPPORTED
}

data class ThermalStatusData(
    val statusCode: Int,
    val statusName: String,
    val severityLevel: ThermalSeverityLevel,
    val description: String,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AppUsageData(
    val packageName: String,
    val appLabel: String,
    val totalTimeInForegroundMillis: Long,
    val lastTimeUsed: Long,
    val percentageOfTotal: Float = 0f
) {
    val formattedForegroundTime: String
        get() {
            val totalSeconds = totalTimeInForegroundMillis / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "${totalSeconds}s"
            }
        }
}

enum class UsagePeriod {
    TODAY,
    PAST_7_DAYS,
    PAST_30_DAYS
}

enum class FindingSeverity {
    NOMINAL,
    INFO,
    WARNING,
    CRITICAL
}

enum class SuggestedActionType {
    OPEN_BATTERY_SAVER,
    OPEN_STORAGE_SETTINGS,
    OPEN_APP_SETTINGS,
    OPEN_USAGE_ACCESS,
    COOLDOWN_DEVICE,
    REFRESH,
    NONE
}

data class HealthFinding(
    val id: String,
    val severity: FindingSeverity,
    val title: String,
    val description: String,
    val evidence: String,
    val suggestedActionType: SuggestedActionType,
    val suggestedActionLabel: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DiagnosticSnapshot(
    val timestamp: Long,
    val deviceSpecs: DeviceSpecs,
    val memory: MemoryInfoData,
    val storage: StorageInfoData,
    val battery: BatteryInfoData,
    val thermal: ThermalStatusData,
    val findings: List<HealthFinding>
) {
    val hasCriticalWarnings: Boolean get() = findings.any { it.severity == FindingSeverity.CRITICAL }
    val hasWarnings: Boolean get() = findings.any { it.severity == FindingSeverity.WARNING }
    val overallStatusTitle: String
        get() = when {
            hasCriticalWarnings -> "Action Required"
            hasWarnings -> "Attention Advised"
            else -> "System Optimal"
        }
}
