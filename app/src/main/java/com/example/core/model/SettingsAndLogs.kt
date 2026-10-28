package com.example.core.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogSeverity {
    VERBOSE,
    INFO,
    WARNING,
    ERROR
}

enum class LogCategory {
    TELEMETRY,
    THERMAL,
    BATTERY,
    STORAGE,
    HEALTH,
    SETTINGS,
    SYSTEM
}

data class LogEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val severity: LogSeverity,
    val category: LogCategory,
    val message: String,
    val errorDetails: String? = null
) {
    val formattedTimestamp: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
            return sdf.format(Date(timestamp))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return sdf.format(Date(timestamp))
        }
}

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class DashboardDensity {
    COMFORTABLE,
    COMPACT
}

data class SettingsData(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val dashboardDensity: DashboardDensity = DashboardDensity.COMFORTABLE,
    val liveMonitoringEnabled: Boolean = true,
    val refreshIntervalSeconds: Int = 10,
    val historyRetentionDays: Int = 14,
    val lowBatteryWarningThreshold: Int = 20,
    val ramWarningThreshold: Int = 85,
    val thermalWarningEnabled: Boolean = true,
    val usageInsightsEnabled: Boolean = true,
    val autoCacheCleanEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val autoRamTrimEnabled: Boolean = true,
    val autoThermalCoolingEnabled: Boolean = true,
    val batterySaverAutomationEnabled: Boolean = false,
    val backgroundNetworkThrottleEnabled: Boolean = false
)

