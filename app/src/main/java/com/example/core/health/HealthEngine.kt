package com.example.core.health

import com.example.core.model.BatteryChargingState
import com.example.core.model.BatteryInfoData
import com.example.core.model.FindingSeverity
import com.example.core.model.HealthFinding
import com.example.core.model.MemoryInfoData
import com.example.core.model.SettingsData
import com.example.core.model.StorageInfoData
import com.example.core.model.SuggestedActionType
import com.example.core.model.ThermalSeverityLevel
import com.example.core.model.ThermalStatusData
import java.util.Locale

class HealthEngine {

    fun evaluate(
        memory: MemoryInfoData,
        storage: StorageInfoData,
        battery: BatteryInfoData,
        thermal: ThermalStatusData,
        settings: SettingsData
    ): List<HealthFinding> {
        val findings = mutableListOf<HealthFinding>()

        // 1. Storage Evaluation
        evaluateStorage(storage, findings)

        // 2. Battery Level & State Evaluation
        evaluateBatteryLevel(battery, settings.lowBatteryWarningThreshold, findings)

        // 3. Battery Temperature Evaluation
        evaluateBatteryTemperature(battery, findings)

        // 4. Android Thermal Status Evaluation
        if (settings.thermalWarningEnabled) {
            evaluateThermalStatus(thermal, findings)
        }

        // 5. Memory Pressure Evaluation
        evaluateMemory(memory, findings)

        return findings
    }

    private fun evaluateStorage(storage: StorageInfoData, out: MutableList<HealthFinding>) {
        if (storage.totalBytes <= 0) {
            out.add(
                HealthFinding(
                    id = "storage_unavailable",
                    severity = FindingSeverity.INFO,
                    title = "Storage Telemetry Unavailable",
                    description = "System storage metrics could not be read directly from the filesystem.",
                    evidence = "Total capacity unreadable",
                    suggestedActionType = SuggestedActionType.REFRESH,
                    suggestedActionLabel = "Refresh Status"
                )
            )
            return
        }

        val freePct = storage.freePercentage
        when {
            freePct < 10f -> {
                out.add(
                    HealthFinding(
                        id = "storage_critical",
                        severity = FindingSeverity.CRITICAL,
                        title = "Storage Critically Low",
                        description = "Less than 10% storage remaining. Device performance, app updates, and system caches may be impacted.",
                        evidence = "${String.format(Locale.US, "%.1f", freePct)}% free (${storage.formattedFree} available of ${storage.formattedTotal})",
                        suggestedActionType = SuggestedActionType.OPEN_STORAGE_SETTINGS,
                        suggestedActionLabel = "Manage Storage"
                    )
                )
            }
            freePct < 20f -> {
                out.add(
                    HealthFinding(
                        id = "storage_warning",
                        severity = FindingSeverity.WARNING,
                        title = "Storage Capacity Reduced",
                        description = "Storage is over 80% full. Consider removing large unused downloads or offline media.",
                        evidence = "${String.format(Locale.US, "%.1f", freePct)}% free (${storage.formattedFree} available)",
                        suggestedActionType = SuggestedActionType.OPEN_STORAGE_SETTINGS,
                        suggestedActionLabel = "Review Storage"
                    )
                )
            }
            else -> {
                out.add(
                    HealthFinding(
                        id = "storage_optimal",
                        severity = FindingSeverity.NOMINAL,
                        title = "Storage Headroom Healthy",
                        description = "Ample internal storage is available for system operations, caching, and media.",
                        evidence = "${String.format(Locale.US, "%.1f", freePct)}% free (${storage.formattedFree} available)",
                        suggestedActionType = SuggestedActionType.NONE,
                        suggestedActionLabel = ""
                    )
                )
            }
        }
    }

    private fun evaluateBatteryLevel(
        battery: BatteryInfoData,
        threshold: Int,
        out: MutableList<HealthFinding>
    ) {
        val level = battery.levelPercentage
        val isCharging = battery.isCharging

        when {
            level <= 15 && !isCharging -> {
                out.add(
                    HealthFinding(
                        id = "battery_critical",
                        severity = FindingSeverity.CRITICAL,
                        title = "Battery Level Critical",
                        description = "Battery is at $level%. Connect to a charger or activate Extreme Battery Saver to avoid abrupt shutdown.",
                        evidence = "$level% remaining (${battery.state.name})",
                        suggestedActionType = SuggestedActionType.OPEN_BATTERY_SAVER,
                        suggestedActionLabel = "Enable Battery Saver"
                    )
                )
            }
            level <= threshold && !isCharging -> {
                out.add(
                    HealthFinding(
                        id = "battery_warning",
                        severity = FindingSeverity.WARNING,
                        title = "Battery Below Configured Threshold",
                        description = "Battery has dropped below your configured alert threshold of $threshold%.",
                        evidence = "$level% remaining (discharging)",
                        suggestedActionType = SuggestedActionType.OPEN_BATTERY_SAVER,
                        suggestedActionLabel = "Open Battery Saver"
                    )
                )
            }
            isCharging -> {
                out.add(
                    HealthFinding(
                        id = "battery_charging",
                        severity = FindingSeverity.NOMINAL,
                        title = "Charging in Progress",
                        description = "Device is actively receiving power via ${battery.pluggedSource.name}. Adaptive charging manages battery longevity.",
                        evidence = "$level% (${battery.pluggedSource.name} connected)",
                        suggestedActionType = SuggestedActionType.NONE,
                        suggestedActionLabel = ""
                    )
                )
            }
            else -> {
                out.add(
                    HealthFinding(
                        id = "battery_nominal",
                        severity = FindingSeverity.NOMINAL,
                        title = "Battery State Balanced",
                        description = "Battery capacity is sufficient for normal workloads.",
                        evidence = "$level% remaining (${battery.state.name})",
                        suggestedActionType = SuggestedActionType.NONE,
                        suggestedActionLabel = ""
                    )
                )
            }
        }
    }

    private fun evaluateBatteryTemperature(battery: BatteryInfoData, out: MutableList<HealthFinding>) {
        val temp = battery.temperatureCelsius ?: return

        when {
            temp >= 45.0f -> {
                out.add(
                    HealthFinding(
                        id = "battery_temp_critical",
                        severity = FindingSeverity.CRITICAL,
                        title = "High Battery Temperature",
                        description = "Battery sensor reports ${String.format(Locale.US, "%.1f°C", temp)}. High temperatures accelerate battery chemical wear.",
                        evidence = "${String.format(Locale.US, "%.1f°C", temp)} reported by battery controller",
                        suggestedActionType = SuggestedActionType.COOLDOWN_DEVICE,
                        suggestedActionLabel = "Cooldown Advice"
                    )
                )
            }
            temp >= 40.0f -> {
                out.add(
                    HealthFinding(
                        id = "battery_temp_warning",
                        severity = FindingSeverity.WARNING,
                        title = "Elevated Battery Temperature",
                        description = "Device is running warm. Heavy sustained loads or direct sunlight can increase thermal wear.",
                        evidence = "${String.format(Locale.US, "%.1f°C", temp)} battery temperature",
                        suggestedActionType = SuggestedActionType.COOLDOWN_DEVICE,
                        suggestedActionLabel = "View Cooldown Tips"
                    )
                )
            }
            else -> {
                out.add(
                    HealthFinding(
                        id = "battery_temp_nominal",
                        severity = FindingSeverity.NOMINAL,
                        title = "Battery Temperature Normal",
                        description = "Battery operating temperature is within healthy parameters.",
                        evidence = "${String.format(Locale.US, "%.1f°C", temp)} nominal",
                        suggestedActionType = SuggestedActionType.NONE,
                        suggestedActionLabel = ""
                    )
                )
            }
        }
    }

    private fun evaluateThermalStatus(thermal: ThermalStatusData, out: MutableList<HealthFinding>) {
        when (thermal.severityLevel) {
            ThermalSeverityLevel.CRITICAL -> {
                out.add(
                    HealthFinding(
                        id = "thermal_critical",
                        severity = FindingSeverity.CRITICAL,
                        title = "Hardware Thermal Throttle: ${thermal.statusName}",
                        description = thermal.description,
                        evidence = "Platform status code: ${thermal.statusCode} (${thermal.statusName})",
                        suggestedActionType = SuggestedActionType.COOLDOWN_DEVICE,
                        suggestedActionLabel = "Safe Cooldown Steps"
                    )
                )
            }
            ThermalSeverityLevel.WARNING -> {
                out.add(
                    HealthFinding(
                        id = "thermal_warning",
                        severity = FindingSeverity.WARNING,
                        title = "Thermal Pressure: ${thermal.statusName}",
                        description = thermal.description,
                        evidence = "Platform status code: ${thermal.statusCode} (${thermal.statusName})",
                        suggestedActionType = SuggestedActionType.COOLDOWN_DEVICE,
                        suggestedActionLabel = "Thermal Guidance"
                    )
                )
            }
            ThermalSeverityLevel.NOMINAL -> {
                out.add(
                    HealthFinding(
                        id = "thermal_nominal",
                        severity = FindingSeverity.NOMINAL,
                        title = "Thermal Status Stable",
                        description = thermal.description,
                        evidence = "Status: ${thermal.statusName} (No throttling)",
                        suggestedActionType = SuggestedActionType.NONE,
                        suggestedActionLabel = ""
                    )
                )
            }
            ThermalSeverityLevel.UNSUPPORTED -> {
                // Informational only
            }
        }
    }

    private fun evaluateMemory(memory: MemoryInfoData, out: MutableList<HealthFinding>) {
        if (!memory.isAvailable) return

        if (memory.isLowMemory || memory.availablePercentage < 10f) {
            out.add(
                HealthFinding(
                    id = "memory_pressure",
                    severity = FindingSeverity.WARNING,
                    title = "System Memory Pressure",
                    description = "Android OS reports low available memory. Background apps may be reclaimed by the low memory killer.",
                    evidence = "${String.format(Locale.US, "%.1f", memory.availablePercentage)}% available (${memory.formattedAvailable} free)",
                    suggestedActionType = SuggestedActionType.OPEN_APP_SETTINGS,
                    suggestedActionLabel = "Review Running Apps"
                )
            )
        } else {
            out.add(
                HealthFinding(
                    id = "memory_nominal",
                    severity = FindingSeverity.NOMINAL,
                    title = "RAM Headroom Sufficient",
                    description = "System has adequate memory headroom for multitasking and smooth navigation.",
                    evidence = "${memory.formattedAvailable} available of ${memory.formattedTotal}",
                    suggestedActionType = SuggestedActionType.NONE,
                    suggestedActionLabel = ""
                )
            )
        }
    }
}
