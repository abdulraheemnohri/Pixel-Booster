package com.example.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.core.model.DashboardDensity
import com.example.core.model.SettingsData
import com.example.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pixel_booster_settings")

class UserPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val DASHBOARD_DENSITY = stringPreferencesKey("dashboard_density")
        val LIVE_MONITORING = booleanPreferencesKey("live_monitoring")
        val REFRESH_INTERVAL = intPreferencesKey("refresh_interval")
        val RETENTION_DAYS = intPreferencesKey("retention_days")
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val RAM_WARNING_THRESHOLD = intPreferencesKey("ram_warning_threshold")
        val THERMAL_WARNING_ENABLED = booleanPreferencesKey("thermal_warning_enabled")
        val USAGE_INSIGHTS_ENABLED = booleanPreferencesKey("usage_insights_enabled")
        val AUTO_CACHE_CLEAN_ENABLED = booleanPreferencesKey("auto_cache_clean_enabled")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val AUTO_RAM_TRIM_ENABLED = booleanPreferencesKey("auto_ram_trim_enabled")
        val AUTO_THERMAL_COOLING_ENABLED = booleanPreferencesKey("auto_thermal_cooling_enabled")
        val BATTERY_SAVER_AUTOMATION_ENABLED = booleanPreferencesKey("battery_saver_automation_enabled")
        val BACKGROUND_NETWORK_THROTTLE_ENABLED = booleanPreferencesKey("background_network_throttle_enabled")
        val RESTRICTED_STARTUP_PACKAGES = stringSetPreferencesKey("restricted_startup_packages")
    }

    val settingsFlow: Flow<SettingsData> = appContext.dataStore.data.map { prefs ->
        val themeModeStr = prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }

        val densityStr = prefs[PreferencesKeys.DASHBOARD_DENSITY] ?: DashboardDensity.COMFORTABLE.name
        val density = try {
            DashboardDensity.valueOf(densityStr)
        } catch (_: Exception) {
            DashboardDensity.COMFORTABLE
        }

        SettingsData(
            themeMode = themeMode,
            dynamicColor = prefs[PreferencesKeys.DYNAMIC_COLOR] ?: true,
            dashboardDensity = density,
            liveMonitoringEnabled = prefs[PreferencesKeys.LIVE_MONITORING] ?: true,
            refreshIntervalSeconds = prefs[PreferencesKeys.REFRESH_INTERVAL] ?: 10,
            historyRetentionDays = prefs[PreferencesKeys.RETENTION_DAYS] ?: 14,
            lowBatteryWarningThreshold = prefs[PreferencesKeys.LOW_BATTERY_THRESHOLD] ?: 20,
            ramWarningThreshold = prefs[PreferencesKeys.RAM_WARNING_THRESHOLD] ?: 85,
            thermalWarningEnabled = prefs[PreferencesKeys.THERMAL_WARNING_ENABLED] ?: true,
            usageInsightsEnabled = prefs[PreferencesKeys.USAGE_INSIGHTS_ENABLED] ?: true,
            autoCacheCleanEnabled = prefs[PreferencesKeys.AUTO_CACHE_CLEAN_ENABLED] ?: true,
            hapticFeedbackEnabled = prefs[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true,
            autoRamTrimEnabled = prefs[PreferencesKeys.AUTO_RAM_TRIM_ENABLED] ?: true,
            autoThermalCoolingEnabled = prefs[PreferencesKeys.AUTO_THERMAL_COOLING_ENABLED] ?: true,
            batterySaverAutomationEnabled = prefs[PreferencesKeys.BATTERY_SAVER_AUTOMATION_ENABLED] ?: false,
            backgroundNetworkThrottleEnabled = prefs[PreferencesKeys.BACKGROUND_NETWORK_THROTTLE_ENABLED] ?: false
        )
    }


    val restrictedStartupPackagesFlow: Flow<Set<String>> = appContext.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.RESTRICTED_STARTUP_PACKAGES] ?: emptySet()
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setDashboardDensity(density: DashboardDensity) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DASHBOARD_DENSITY] = density.name
        }
    }

    suspend fun setLiveMonitoring(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LIVE_MONITORING] = enabled
        }
    }

    suspend fun setRefreshInterval(seconds: Int) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.REFRESH_INTERVAL] = seconds.coerceIn(5, 60)
        }
    }

    suspend fun setHistoryRetentionDays(days: Int) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.RETENTION_DAYS] = days.coerceIn(7, 90)
        }
    }

    suspend fun setLowBatteryWarningThreshold(threshold: Int) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LOW_BATTERY_THRESHOLD] = threshold.coerceIn(10, 40)
        }
    }

    suspend fun setRamWarningThreshold(threshold: Int) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.RAM_WARNING_THRESHOLD] = threshold.coerceIn(60, 95)
        }
    }

    suspend fun setThermalWarningEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THERMAL_WARNING_ENABLED] = enabled
        }
    }

    suspend fun setUsageInsightsEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USAGE_INSIGHTS_ENABLED] = enabled
        }
    }

    suspend fun setAutoCacheCleanEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_CACHE_CLEAN_ENABLED] = enabled
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    suspend fun setAutoRamTrimEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_RAM_TRIM_ENABLED] = enabled
        }
    }

    suspend fun setAutoThermalCoolingEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_THERMAL_COOLING_ENABLED] = enabled
        }
    }

    suspend fun setBatterySaverAutomationEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BATTERY_SAVER_AUTOMATION_ENABLED] = enabled
        }
    }

    suspend fun setBackgroundNetworkThrottleEnabled(enabled: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BACKGROUND_NETWORK_THROTTLE_ENABLED] = enabled
        }
    }


    suspend fun setStartupAppRestricted(packageName: String, restricted: Boolean) {
        appContext.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.RESTRICTED_STARTUP_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (restricted) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[PreferencesKeys.RESTRICTED_STARTUP_PACKAGES] = current
        }
    }

    suspend fun clearAllRestrictedStartupApps() {
        appContext.dataStore.edit { prefs ->
            prefs[PreferencesKeys.RESTRICTED_STARTUP_PACKAGES] = emptySet()
        }
    }
}
