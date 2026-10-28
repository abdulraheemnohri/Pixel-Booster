package com.example.core.platform

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import com.example.core.model.BatteryChargingState
import com.example.core.model.BatteryInfoData
import com.example.core.model.BatteryPluggedSource
import com.example.core.model.DeviceSpecs
import com.example.core.model.MemoryInfoData
import com.example.core.model.StorageInfoData
import com.example.core.model.ThermalSeverityLevel
import com.example.core.model.ThermalStatusData
import java.io.File

class DeviceDiagnosticsProvider(private val context: Context) {

    private val powerManager: PowerManager? =
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val activityManager: ActivityManager? =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    fun getDeviceSpecs(): DeviceSpecs {
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val model = Build.MODEL ?: "Unknown"
        val deviceCodename = Build.DEVICE ?: "Unknown"
        val isPixel = manufacturer.equals("Google", ignoreCase = true) || model.contains("Pixel", ignoreCase = true)

        val pixelGen = determinePixelGeneration(model, deviceCodename, isPixel)
        val soc = determineSocModel(model, isPixel)

        val uptime = SystemClock.elapsedRealtime()
        val supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList()
        val isThermalSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null

        return DeviceSpecs(
            manufacturer = manufacturer,
            model = model,
            deviceCodename = deviceCodename,
            socModel = soc,
            isPixelDevice = isPixel,
            pixelGenerationName = pixelGen,
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            sdkInt = Build.VERSION.SDK_INT,
            buildId = Build.ID ?: "Unknown",
            supportedAbis = supportedAbis,
            uptimeMillis = uptime,
            isThermalSupported = isThermalSupported
        )
    }

    private fun determinePixelGeneration(model: String, deviceCodename: String, isPixel: Boolean): String {
        if (!isPixel) return "Compatible Android Device"
        val lowerModel = model.lowercase()
        return when {
            lowerModel.contains("pixel 10") -> "Pixel 10 Series (Tensor G5)"
            lowerModel.contains("pixel 9 pro fold") -> "Pixel 9 Pro Fold (Tensor G4)"
            lowerModel.contains("pixel 9 pro xl") -> "Pixel 9 Pro XL (Tensor G4)"
            lowerModel.contains("pixel 9 pro") -> "Pixel 9 Pro (Tensor G4)"
            lowerModel.contains("pixel 9") -> "Pixel 9 Series (Tensor G4)"
            lowerModel.contains("pixel 8a") -> "Pixel 8a (Tensor G3)"
            lowerModel.contains("pixel 8 pro") -> "Pixel 8 Pro (Tensor G3)"
            lowerModel.contains("pixel 8") -> "Pixel 8 Series (Tensor G3)"
            lowerModel.contains("pixel 7a") -> "Pixel 7a (Tensor G2)"
            lowerModel.contains("pixel 7 pro") -> "Pixel 7 Pro (Tensor G2)"
            lowerModel.contains("pixel 7") -> "Pixel 7 Series (Tensor G2)"
            lowerModel.contains("pixel 6a") -> "Pixel 6a (Tensor G1)"
            lowerModel.contains("pixel 6 pro") -> "Pixel 6 Pro (Tensor G1)"
            lowerModel.contains("pixel 6") -> "Pixel 6 Series (Tensor G1)"
            lowerModel.contains("pixel fold") -> "Pixel Fold (Tensor G2)"
            lowerModel.contains("pixel tablet") -> "Pixel Tablet (Tensor G2)"
            lowerModel.contains("pixel 5") -> "Pixel 5 Series"
            lowerModel.contains("pixel 4") -> "Pixel 4 Series"
            else -> "Google Pixel ($model)"
        }
    }

    private fun determineSocModel(model: String, isPixel: Boolean): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val soc = Build.SOC_MODEL
            if (!soc.isNullOrBlank() && soc != "unknown") {
                return soc
            }
        }
        val lowerModel = model.lowercase()
        return if (isPixel) {
            when {
                lowerModel.contains("pixel 10") -> "Google Tensor G5"
                lowerModel.contains("pixel 9") -> "Google Tensor G4"
                lowerModel.contains("pixel 8") -> "Google Tensor G3"
                lowerModel.contains("pixel 7") -> "Google Tensor G2"
                lowerModel.contains("pixel 6") -> "Google Tensor (G1)"
                lowerModel.contains("pixel fold") -> "Google Tensor G2"
                lowerModel.contains("pixel tablet") -> "Google Tensor G2"
                else -> Build.HARDWARE ?: "Google Tensor / Platform"
            }
        } else {
            Build.HARDWARE ?: "Standard ARM SoC"
        }
    }

    fun getMemoryInfo(): MemoryInfoData {
        val memInfo = ActivityManager.MemoryInfo()
        runCatching { activityManager?.getMemoryInfo(memInfo) }
        // Use Android's API first, then read the kernel-provided procfs values if needed.
        // Never substitute a guessed device RAM capacity or guessed free-memory percentage.
        val total = memInfo.totalMem.takeIf { it > 0L } ?: readProcMemInfoBytes("MemTotal") ?: 0L
        val avail = memInfo.availMem.takeIf { it > 0L }
            ?: readProcMemInfoBytes("MemAvailable")
            ?: readProcMemInfoBytes("MemFree")
            ?: 0L
        return MemoryInfoData(
            totalRamBytes = total,
            availableRamBytes = avail,
            thresholdBytes = memInfo.threshold,
            isLowMemory = memInfo.lowMemory
        )
    }

    private fun readProcMemInfoBytes(key: String): Long? = runCatching {
        File("/proc/meminfo").useLines { lines ->
            lines.firstOrNull { it.startsWith("$key:") }
                ?.substringAfter(":")
                ?.trim()
                ?.substringBefore(" ")
                ?.toLongOrNull()
                ?.times(1024L)
        }
    }.getOrNull()

    fun getStorageInfo(): StorageInfoData {
        return try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val total = stat.totalBytes
            val free = stat.availableBytes
            StorageInfoData(
                totalBytes = total,
                freeBytes = free
            )
        } catch (e: Exception) {
            StorageInfoData(
                totalBytes = 0L,
                freeBytes = 0L
            )
        }
    }

    fun getBatteryInfo(): BatteryInfoData {
        val batteryIntent = try {
            context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        } catch (_: Throwable) {
            null
        } ?: return BatteryInfoData(
            levelPercentage = 0,
            state = BatteryChargingState.UNKNOWN,
            pluggedSource = BatteryPluggedSource.NONE,
            temperatureCelsius = null,
            voltageMilliVolts = null,
            healthDescription = "Unknown",
            technology = null
        )

        val rawLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (rawLevel >= 0 && scale > 0) {
            ((rawLevel.toFloat() / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val state = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryChargingState.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryChargingState.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryChargingState.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryChargingState.NOT_CHARGING
            else -> BatteryChargingState.UNKNOWN
        }

        val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> BatteryPluggedSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> BatteryPluggedSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryPluggedSource.WIRELESS
            else -> BatteryPluggedSource.NONE
        }

        val rawTemp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val temperature = if (rawTemp > 0) rawTemp / 10.0f else null

        val voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1).takeIf { it > 0 }

        val healthCode = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthDesc = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Normal"
        }

        val tech = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        return BatteryInfoData(
            levelPercentage = percentage,
            state = state,
            pluggedSource = pluggedSource,
            temperatureCelsius = temperature,
            voltageMilliVolts = voltage,
            healthDescription = healthDesc,
            technology = tech
        )
    }

    fun getThermalStatus(): ThermalStatusData {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val status = powerManager.currentThermalStatus
            return mapThermalStatus(status)
        }
        return ThermalStatusData(
            statusCode = -1,
            statusName = "Unsupported",
            severityLevel = ThermalSeverityLevel.UNSUPPORTED,
            description = "Thermal telemetry requires Android 10+ (API 29).",
            recommendation = "Keep device in a cool environment during intensive tasks."
        )
    }

    fun mapThermalStatus(status: Int): ThermalStatusData {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (status) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Nominal",
                    severityLevel = ThermalSeverityLevel.NOMINAL,
                    description = "Operating temperature is normal. No hardware throttling active.",
                    recommendation = "Optimal performance headroom available."
                )
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Light",
                    severityLevel = ThermalSeverityLevel.WARNING,
                    description = "Device is warming up. Minor background throttling may occur.",
                    recommendation = "Pause intensive downloads or heavy multitasking."
                )
                PowerManager.THERMAL_STATUS_MODERATE -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Moderate",
                    severityLevel = ThermalSeverityLevel.WARNING,
                    description = "Moderate thermal rise. Display brightness or frame rate may adapt.",
                    recommendation = "Remove thick cases and avoid direct sunlight."
                )
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Severe",
                    severityLevel = ThermalSeverityLevel.CRITICAL,
                    description = "High thermal pressure. CPU/GPU clock rates are actively reduced.",
                    recommendation = "Pause 3D gaming or video recording. Disconnect fast charging."
                )
                PowerManager.THERMAL_STATUS_CRITICAL -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Critical",
                    severityLevel = ThermalSeverityLevel.CRITICAL,
                    description = "Critical temperature reached. Core performance is strongly throttled.",
                    recommendation = "Let the device cool down naturally. Do not place in ice or refrigerator."
                )
                PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Emergency",
                    severityLevel = ThermalSeverityLevel.CRITICAL,
                    description = "Emergency thermal threshold reached. Essential systems only.",
                    recommendation = "Immediate cooldown needed. Turn off screen and stop charging."
                )
                PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Shutdown",
                    severityLevel = ThermalSeverityLevel.CRITICAL,
                    description = "Thermal shutdown imminent to prevent hardware damage.",
                    recommendation = "Power off device immediately."
                )
                else -> ThermalStatusData(
                    statusCode = status,
                    statusName = "Normal",
                    severityLevel = ThermalSeverityLevel.NOMINAL,
                    description = "Device thermals within acceptable operational parameters.",
                    recommendation = "System running stably."
                )
            }
        } else {
            ThermalStatusData(
                statusCode = status,
                statusName = "Unsupported",
                severityLevel = ThermalSeverityLevel.UNSUPPORTED,
                description = "Thermal API requires Android 10+.",
                recommendation = "Monitor battery temperature for thermal indications."
            )
        }
    }

    fun registerThermalListener(onStatusChanged: (ThermalStatusData) -> Unit): () -> Unit {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val listener = PowerManager.OnThermalStatusChangedListener { status ->
                try {
                    onStatusChanged(mapThermalStatus(status))
                } catch (_: Throwable) {}
            }
            try {
                val executor = androidx.core.content.ContextCompat.getMainExecutor(context)
                powerManager.addThermalStatusListener(executor, listener)
                return {
                    try {
                        powerManager.removeThermalStatusListener(listener)
                    } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {
                return {}
            }
        }
        return {}
    }
}
