package com.example.core.model

import com.example.core.booster.LiveRamMetrics
import java.util.Locale

data class ThermalTelemetryData(
    val currentTempCelsius: Float,
    val peakTempCelsius: Float,
    val statusName: String,
    val severityLevel: ThermalSeverityLevel,
    val isOverheating: Boolean,
    val warningMessage: String?,
    val coolingAdvice: String,
    val temperatureHistory: List<Float> = emptyList(),
    val socTempCelsius: Float = currentTempCelsius,
    val batteryTempCelsius: Float = currentTempCelsius,
    val activeThermalZones: Int = 1,
    val powerManagerStatusName: String = "Nominal"
) {
    val formattedCurrentTemp: String
        get() = String.format(Locale.US, "%.1f°C", currentTempCelsius)

    val formattedPeakTemp: String
        get() = String.format(Locale.US, "%.1f°C", peakTempCelsius)

    val tempInFahrenheit: Float
        get() = (currentTempCelsius * 9f / 5f) + 32f

    val formattedTempFahrenheit: String
        get() = String.format(Locale.US, "%.1f°F", tempInFahrenheit)

    val formattedSocTemp: String
        get() = String.format(Locale.US, "%.1f°C", socTempCelsius)

    val formattedBatteryTemp: String
        get() = String.format(Locale.US, "%.1f°C", batteryTempCelsius)
}

data class BatteryHealthData(
    val levelPercentage: Int,
    val currentCapacityMah: Int,
    val designedCapacityMah: Int,
    val healthPercentage: Float,
    val healthGrade: String,
    val chargingState: BatteryChargingState,
    val pluggedSource: BatteryPluggedSource,
    val voltageMilliVolts: Int?,
    val estimatedWattage: Float?,
    val technology: String,
    val temperatureCelsius: Float,
    val chargeCyclesEstimated: Int,
    val chargeCycles: Int = chargeCyclesEstimated,
    val chargeCyclesSource: String = "Hardware BMS Counter",
    val currentFlowMa: Int = 0,
    val capacityPercentage: Float = healthPercentage
) {
    val isCharging: Boolean get() = chargingState == BatteryChargingState.CHARGING
    val isFull: Boolean get() = chargingState == BatteryChargingState.FULL

    val formattedCurrentCapacity: String
        get() = if (currentCapacityMah > 0) "$currentCapacityMah mAh" else "Unavailable"

    val formattedDesignedCapacity: String
        get() = if (designedCapacityMah > 0) "$designedCapacityMah mAh" else "Unavailable"

    val formattedHealthPercentage: String
        get() = if (healthGrade.startsWith("Unavailable")) "Unavailable" else String.format(Locale.US, "%.1f%%", healthPercentage)

    val formattedCapacityPercentage: String
        get() = if (currentCapacityMah <= 0 || designedCapacityMah <= 0) "Unavailable" else String.format(Locale.US, "%.1f%%", capacityPercentage)

    val formattedVoltage: String
        get() = voltageMilliVolts?.takeIf { it > 0 }?.let { String.format(Locale.US, "%.2f V", it / 1000f) } ?: "Unavailable"

    val formattedWattage: String
        get() = estimatedWattage?.let { String.format(Locale.US, "%.1f W", it) } ?: "Unavailable"

    val formattedCurrentFlow: String
        get() = if (currentFlowMa > 0) "+$currentFlowMa mA" else "$currentFlowMa mA"

    val cycleHealthAssessment: String
        get() = when {
            chargeCycles <= 0 -> "Unavailable (cycle count not exposed)"
            chargeCycles < 300 -> "Peak Endurance (Cycle < 300)"
            chargeCycles < 600 -> "Optimal Chemistry (Cycle < 600)"
            chargeCycles < 900 -> "Normal Aging (Cycle < 900)"
            else -> "High Wear (800+ Cycles)"
        }
}

data class ProcessorTelemetryData(
    val overallCpuUsage: Float,
    val coreCount: Int,
    val coreLoads: List<Float>,
    val gpuUsage: Float,
    val gpuEngineName: String,
    val gpuFpsEstimate: Int,
    val tpuUsage: Float,
    val tpuEngineName: String,
    val tpuTopsRating: String,
    val tpuState: String
) {
    val formattedCpuUsage: String
        get() = String.format(Locale.US, "%.0f%%", overallCpuUsage)

    val formattedGpuUsage: String
        get() = if (gpuUsage < 0f) "Unavailable" else String.format(Locale.US, "%.0f%%", gpuUsage)

    val formattedTpuUsage: String
        get() = if (tpuUsage < 0f) "Unavailable" else String.format(Locale.US, "%.0f%%", tpuUsage)
}

data class NetworkInterfaceInfo(
    val name: String,
    val displayName: String,
    val ipAddress: String,
    val isUp: Boolean,
    val mtu: Int,
    val rxBytes: Long,
    val txBytes: Long
)

data class PingTargetDiagnostic(
    val label: String,
    val host: String,
    val pingMs: Long,
    val isReachable: Boolean,
    val statusColorHex: String = "#34D399"
)

data class DnsPreset(
    val id: String,
    val name: String,
    val primaryIp: String,
    val secondaryIp: String,
    val description: String
)

data class NetworkTelemetryData(
    val downloadSpeedBytesPerSec: Long,
    val uploadSpeedBytesPerSec: Long,
    val sessionTotalRxBytes: Long,
    val sessionTotalTxBytes: Long,
    val connectionType: String,
    val latencyMs: Long,
    val isConnected: Boolean,
    val activeInterfaceName: String = "wlan0",
    val localIpAddress: String = "192.168.1.100",
    val ipv6Address: String = "fe80::1",
    val gatewayIp: String = "192.168.1.1",
    val dnsServersList: List<String> = listOf("8.8.8.8", "1.1.1.1"),
    val wifiSsid: String = "Connected Network",
    val wifiRssiDbm: Int = -58,
    val wifiSignalPercent: Int = 84,
    val wifiFrequencyMhz: Int = 5240,
    val linkSpeedMbps: Int = 866,
    val cellularCarrier: String = "Mobile Carrier",
    val cellularNetworkType: String = "5G NR SA",
    val packetLossPercent: Float = 0.0f,
    val jitterMs: Int = 2,
    val healthGrade: String = "A+ (Optimal)"
) {
    val formattedDownloadSpeed: String
        get() = formatSpeed(downloadSpeedBytesPerSec)

    val formattedUploadSpeed: String
        get() = formatSpeed(uploadSpeedBytesPerSec)

    val formattedSessionTotalRx: String
        get() = MemoryInfoData.formatBytes(sessionTotalRxBytes.coerceAtLeast(0L))

    val formattedSessionTotalTx: String
        get() = MemoryInfoData.formatBytes(sessionTotalTxBytes.coerceAtLeast(0L))

    val formattedRssiDbm: String
        get() = if (wifiRssiDbm == 0) "Unavailable" else "$wifiRssiDbm dBm"

    val formattedFrequencyBand: String
        get() = when {
            wifiFrequencyMhz >= 5900 -> "Wi-Fi 6E/7 (6 GHz)"
            wifiFrequencyMhz >= 4900 -> "Wi-Fi 5/6 (5 GHz)"
            wifiFrequencyMhz > 0 -> "Wi-Fi (2.4 GHz)"
            else -> cellularNetworkType
        }

    companion object {
        fun formatSpeed(bytesPerSec: Long): String {
            return when {
                bytesPerSec == 0L -> "0 B/s"
                bytesPerSec < 1024L -> "$bytesPerSec B/s"
                bytesPerSec < 1024L * 1024L -> String.format(Locale.US, "%d KB/s", bytesPerSec / 1024L)
                else -> String.format(Locale.US, "%.2f MB/s", bytesPerSec.toDouble() / (1024.0 * 1024.0))
            }
        }
    }
}

data class CompleteHardwareTelemetry(
    val thermal: ThermalTelemetryData,
    val batteryHealth: BatteryHealthData,
    val processor: ProcessorTelemetryData,
    val network: NetworkTelemetryData,
    val memory: LiveRamMetrics,
    val storage: StorageInfoData,
    val systemVitalityScore: Int
)

