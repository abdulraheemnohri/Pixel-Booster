package com.example.core.health

import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter

data class BatterySaverStatus(
    val batteryLevelPercent: Int,
    val isCharging: Boolean,
    val isSmartSaverActive: Boolean,
    val reducedBackgroundSync: Boolean,
    val limitedHighPerformanceCores: Boolean,
    val adaptiveBrightnessRecommended: Boolean
)

class SmartBatterySaverManager(private val context: Context) {

    fun evaluateStatus(userEnabledForce: Boolean = false): BatterySaverStatus {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }

        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            65 // default fallback
        }

        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Smart Battery Saver activates automatically if battery < 20% and not charging, or if user explicitly enabled
        val isLowBattery = batteryPct <= 20 && !isCharging
        val isActive = userEnabledForce || isLowBattery

        return BatterySaverStatus(
            batteryLevelPercent = batteryPct,
            isCharging = isCharging,
            isSmartSaverActive = isActive,
            reducedBackgroundSync = isActive,
            limitedHighPerformanceCores = isActive,
            adaptiveBrightnessRecommended = isActive
        )
    }
}
