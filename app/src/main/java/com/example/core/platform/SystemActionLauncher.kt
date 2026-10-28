package com.example.core.platform

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

class SystemActionLauncher(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    fun openBatterySaverSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS),
            Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openStorageSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openApplicationSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_APPLICATION_SETTINGS),
            Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openUsageAccessSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openSoundSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_SOUND_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openAppDetails(packageName: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            safeLaunch(Intent(Settings.ACTION_APPLICATION_SETTINGS))
        }
    }

    fun openBatteryOptimizationSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openWifiSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_WIFI_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openCellularNetworkSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),
            Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openPrivateDnsSettings(): Boolean {
        return safeLaunch(
            Intent("android.settings.DNS_SETTINGS"),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun openDataSaverSettings(): Boolean {
        return safeLaunch(
            Intent(Settings.ACTION_DATA_USAGE_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
    }

    fun killAppBackgroundProcess(packageName: String): Boolean {

        return try {
            activityManager?.killBackgroundProcesses(packageName)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareDiagnosticReport(reportText: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, reportText)
                putExtra(Intent.EXTRA_SUBJECT, "Pixel Booster Diagnostic Telemetry Report")
                type = "text/plain"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Export Diagnostic Telemetry").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open share sheet: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun safeLaunch(vararg intents: Intent): Boolean {
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Try fallback intent
            }
        }
        Toast.makeText(context, "Requested settings screen is unavailable on this device", Toast.LENGTH_SHORT).show()
        return false
    }
}
