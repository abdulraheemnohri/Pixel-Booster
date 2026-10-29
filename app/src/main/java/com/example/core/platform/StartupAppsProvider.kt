package com.example.core.platform

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.core.model.BootImpactLevel
import com.example.core.model.BootOptimizationResult
import com.example.core.model.StartupAppInfo
import com.example.core.model.StartupSummary
import com.example.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StartupAppsProvider(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun getStartupApps(): List<StartupAppInfo> = withContext(Dispatchers.IO) {
        val restrictedSet = preferencesRepository.restrictedStartupPackagesFlow.first()

        // 1. Query broadcast receivers responding to BOOT_COMPLETED
        val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED)
        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryBroadcastReceivers(
                    bootIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.GET_RESOLVED_FILTER.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryBroadcastReceivers(bootIntent, 0)
            }
        } catch (_: Throwable) {
            emptyList()
        }

        val discoveredPackages = mutableMapOf<String, StartupAppInfo>()

        for (resolveInfo in resolveInfos) {
            val appInfo = resolveInfo.activityInfo?.applicationInfo ?: continue
            val pkg = appInfo.packageName
            if (discoveredPackages.containsKey(pkg)) continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isEssential = isCorePixelPackage(pkg)
            val label = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Throwable) {
                pkg
            }

            val impact = evaluateBootImpact(pkg, isSystem)
            val isEnabled = !restrictedSet.contains(pkg)

            discoveredPackages[pkg] = StartupAppInfo(
                packageName = pkg,
                appLabel = label,
                isSystemApp = isSystem,
                isEssential = isEssential,
                isEnabledAtBoot = isEnabled,
                bootImpact = impact,
                estimatedBootDelayMs = delayMs,
                estimatedRamBytes = ramFootprint,
                bootTriggerReason = "BOOT_COMPLETED Receiver"
            )
        }

        // 2. Query all launchable apps installed on the user's mobile device
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launcherResolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    launcherIntent,
                    PackageManager.ResolveInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(launcherIntent, 0)
            }
        } catch (_: Throwable) {
            emptyList()
        }

        for (resolveInfo in launcherResolveInfos) {
            val appInfo = resolveInfo.activityInfo?.applicationInfo ?: continue
            val pkg = appInfo.packageName
            if (discoveredPackages.containsKey(pkg)) continue

            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isEssential = isCorePixelPackage(pkg)
            val label = try {
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Throwable) {
                pkg
            }

            // APK size is not a measurement of startup impact or runtime RAM.
            val impact = evaluateBootImpact(pkg, isSystem)
            val isEnabled = !restrictedSet.contains(pkg)

            discoveredPackages[pkg] = StartupAppInfo(
                packageName = pkg,
                appLabel = label,
                isSystemApp = isSystem,
                isEssential = isEssential,
                isEnabledAtBoot = isEnabled,
                bootImpact = impact,
                estimatedBootDelayMs = 0,
                estimatedRamBytes = 0L,
                bootTriggerReason = "Launchable app; auto-start behavior not verified"
            )
        }

        // 3. Include any other installed non-system user applications
        try {
            val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledApplications(0)
            }

            for (appInfo in installedApps) {
                val pkg = appInfo.packageName
                if (discoveredPackages.containsKey(pkg)) continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (isSystem) continue // Only add third-party/user apps if not already in list

                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Throwable) {
                    pkg
                }
                val isEnabled = !restrictedSet.contains(pkg)
                val impact = evaluateBootImpact(pkg, false)

                discoveredPackages[pkg] = StartupAppInfo(
                    packageName = pkg,
                    appLabel = label,
                    isSystemApp = false,
                    isEssential = false,
                    isEnabledAtBoot = isEnabled,
                    bootImpact = impact,
                    estimatedBootDelayMs = 0,
                    estimatedRamBytes = 0L,
                    bootTriggerReason = "Installed app; auto-start behavior not verified"
                )
            }
        } catch (_: Throwable) {}

        discoveredPackages.values.sortedWith(
            compareBy<StartupAppInfo> { it.isEssential }
                .thenByDescending { it.bootImpact }
                .thenBy { it.appLabel }
        )
    }

    suspend fun toggleAppStartup(packageName: String, enable: Boolean): Unit = withContext(Dispatchers.IO) {
        val restrict = !enable
        preferencesRepository.setStartupAppRestricted(packageName, restrict)
    }

    suspend fun optimizeAllNonEssential(): BootOptimizationResult = withContext(Dispatchers.IO) {
        val apps = getStartupApps()
        val nonEssentialActive = apps.filter { !it.isEssential && it.isEnabledAtBoot }

        for (app in nonEssentialActive) {
            // Local review preference only; public APIs cannot disable another app's boot receivers.
            preferencesRepository.setStartupAppRestricted(app.packageName, true)
        }

        BootOptimizationResult(
            appsOptimizedCount = nonEssentialActive.size,
            totalDelayReducedMs = 0, // No measured boot-time change is available.
            ramHeadroomReclaimedBytes = 0L // No verified RAM reclaim is available.
        )
    }

    suspend fun getStartupSummary(): StartupSummary = withContext(Dispatchers.IO) {
        val apps = getStartupApps()
        val nonEssential = apps.filter { !it.isEssential }
        val restricted = nonEssential.filter { !it.isEnabledAtBoot }
        val active = apps.filter { it.isEnabledAtBoot }

        val totalDelay = active.sumOf { it.estimatedBootDelayMs }
        val totalRam = active.sumOf { it.estimatedRamBytes }

        StartupSummary(
            totalStartupApps = apps.size,
            nonEssentialCount = nonEssential.size,
            restrictedCount = restricted.size,
            totalBootDelayMs = totalDelay,
            totalRamFootprintBytes = totalRam
        )
    }

    private fun isCorePixelPackage(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower.contains("com.android.systemui") ||
            lower.contains("com.google.android.dialer") ||
            lower.contains("com.android.phone") ||
            lower.contains("com.android.server.telecom") ||
            lower.contains("com.google.android.gms")
    }

    private fun evaluateBootImpact(packageName: String, isSystem: Boolean): BootImpactLevel {
        val lower = packageName.lowercase()
        return when {
            lower.contains("photos") || lower.contains("instagram") || lower.contains("facebook") || lower.contains("tiktok") -> BootImpactLevel.HIGH
            lower.contains("music") || lower.contains("spotify") || lower.contains("video") || lower.contains("game") -> BootImpactLevel.MEDIUM
            isSystem -> BootImpactLevel.MEDIUM
            else -> BootImpactLevel.LOW
        }
    }

    // Per-app boot delay and RAM usage are not reliably measurable through public APIs here.
}
