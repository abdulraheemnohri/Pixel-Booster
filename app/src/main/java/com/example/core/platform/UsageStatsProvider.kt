package com.example.core.platform

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.example.core.model.AppUsageData
import com.example.core.model.UsagePeriod
import java.util.Calendar

class UsageStatsProvider(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    private val packageManager: PackageManager = context.packageManager

    fun isUsageAccessGranted(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                @Suppress("DEPRECATION")
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Throwable) {
            false
        }
    }

    fun getUsageStats(period: UsagePeriod): List<AppUsageData> {
        return try {
            if (!isUsageAccessGranted() || usageStatsManager == null) {
                return emptyList()
            }

            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis
            val startTime = when (period) {
                UsagePeriod.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }
                UsagePeriod.PAST_7_DAYS -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    calendar.timeInMillis
                }
                UsagePeriod.PAST_30_DAYS -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    calendar.timeInMillis
                }
            }

            val interval = when (period) {
                UsagePeriod.TODAY -> UsageStatsManager.INTERVAL_DAILY
                UsagePeriod.PAST_7_DAYS -> UsageStatsManager.INTERVAL_WEEKLY
                UsagePeriod.PAST_30_DAYS -> UsageStatsManager.INTERVAL_MONTHLY
            }

            val rawStats: List<UsageStats> = usageStatsManager.queryUsageStats(interval, startTime, endTime) ?: emptyList()

            // Aggregate by package name (since queryUsageStats can return multiple slices)
            val aggregatedMap = mutableMapOf<String, Pair<Long, Long>>() // pkg -> (totalTime, lastTimeUsed)

            for (stat in rawStats) {
                val foregroundTime = stat.totalTimeInForeground
                if (foregroundTime > 0) {
                    val current = aggregatedMap[stat.packageName]
                    val currentTotal = current?.first ?: 0L
                    val currentLastUsed = current?.second ?: 0L
                    val newTotal = currentTotal + foregroundTime
                    val newLastUsed = maxOf(currentLastUsed, stat.lastTimeUsed)
                    aggregatedMap[stat.packageName] = Pair(newTotal, newLastUsed)
                }
            }

            val totalDurationAllApps = aggregatedMap.values.sumOf { it.first }

            aggregatedMap.mapNotNull { (pkg, pair) ->
                val duration = pair.first
                // Filter out system noise or near-zero usage (< 2 seconds)
                if (duration < 2000L) return@mapNotNull null

                val label = getReadableAppLabel(pkg)
                val pct = if (totalDurationAllApps > 0) {
                    ((duration.toFloat() / totalDurationAllApps.toFloat()) * 100f).coerceIn(0f, 100f)
                } else 0f

                AppUsageData(
                    packageName = pkg,
                    appLabel = label,
                    totalTimeInForegroundMillis = duration,
                    lastTimeUsed = pair.second,
                    percentageOfTotal = pct
                )
            }.sortedByDescending { it.totalTimeInForegroundMillis }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun getReadableAppLabel(packageName: String): String {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            // Friendly fallback if package cannot be queried
            val parts = packageName.split(".")
            val name = parts.lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
            name
        }
    }
}
