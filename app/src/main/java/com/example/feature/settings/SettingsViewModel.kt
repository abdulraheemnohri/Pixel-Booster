package com.example.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppDatabase
import com.example.core.database.DiagnosticHistoryRepository
import com.example.core.logging.EventLogger
import com.example.core.model.DashboardDensity
import com.example.core.model.LogCategory
import com.example.core.model.LogEvent
import com.example.core.model.LogSeverity
import com.example.core.model.SettingsData
import com.example.core.model.ThemeMode
import com.example.core.platform.DeviceDiagnosticsProvider
import com.example.core.platform.SystemActionLauncher
import com.example.core.platform.UsageStatsProvider
import com.example.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: SettingsData = SettingsData(),
    val isUsagePermissionGranted: Boolean = false,
    val selectedLogSeverity: LogSeverity? = null,
    val logSearchQuery: String = "",
    val sampleCount: Int = 0
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository.getInstance(application)
    private val diagnosticsProvider = DeviceDiagnosticsProvider(application)
    private val usageStatsProvider = UsageStatsProvider(application)
    private val actionLauncher = SystemActionLauncher(application)
    private val historyRepository = DiagnosticHistoryRepository(
        AppDatabase.getInstance(application).diagnosticSampleDao()
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val logEvents: StateFlow<List<LogEvent>> = combine(
        EventLogger.eventsFlow,
        _uiState
    ) { events, state ->
        events.filter { event ->
            val matchesSeverity = state.selectedLogSeverity == null || event.severity == state.selectedLogSeverity
            val matchesSearch = state.logSearchQuery.isBlank() ||
                    event.message.contains(state.logSearchQuery, ignoreCase = true) ||
                    event.category.name.contains(state.logSearchQuery, ignoreCase = true)
            matchesSeverity && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collect { settings ->
                _uiState.update { it.copy(settings = settings) }
            }
        }
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            val usageGranted = usageStatsProvider.isUsageAccessGranted()
            val count = historyRepository.getSampleCount()
            _uiState.update {
                it.copy(
                    isUsagePermissionGranted = usageGranted,
                    sampleCount = count
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Theme changed to: ${mode.name}")
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Dynamic color toggled: $enabled")
        }
    }

    fun setDashboardDensity(density: DashboardDensity) {
        viewModelScope.launch {
            preferencesRepository.setDashboardDensity(density)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Dashboard density set to: ${density.name}")
        }
    }

    fun setRefreshInterval(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setRefreshInterval(seconds)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Telemetry refresh interval set to ${seconds}s")
        }
    }

    fun setHistoryRetentionDays(days: Int) {
        viewModelScope.launch {
            preferencesRepository.setHistoryRetentionDays(days)
            historyRepository.pruneOlderThanDays(days)
            refreshStats()
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "History retention set to $days days")
        }
    }

    fun setLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesRepository.setLowBatteryWarningThreshold(threshold)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Low battery warning threshold set to $threshold%")
        }
    }

    fun setThermalWarningEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setThermalWarningEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Thermal warnings toggled: $enabled")
        }
    }

    fun setRamWarningThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesRepository.setRamWarningThreshold(threshold)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "RAM warning threshold set to $threshold%")
        }
    }

    fun setAutoCacheCleanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoCacheCleanEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Auto cache clean toggled: $enabled")
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticFeedbackEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Haptic feedback toggled: $enabled")
        }
    }

    fun setAutoRamTrimEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoRamTrimEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Auto RAM trim automation toggled: $enabled")
        }
    }

    fun setAutoThermalCoolingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoThermalCoolingEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Auto thermal cooling automation toggled: $enabled")
        }
    }

    fun setBatterySaverAutomationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBatterySaverAutomationEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Battery saver automation toggled: $enabled")
        }
    }

    fun setBackgroundNetworkThrottleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBackgroundNetworkThrottleEnabled(enabled)
            EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Background network throttle automation toggled: $enabled")
        }
    }


    fun setLogSeverityFilter(severity: LogSeverity?) {
        _uiState.update { it.copy(selectedLogSeverity = severity) }
    }

    fun setLogSearchQuery(query: String) {
        _uiState.update { it.copy(logSearchQuery = query) }
    }

    fun clearLogs() {
        EventLogger.clear()
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            refreshStats()
            EventLogger.log(LogSeverity.INFO, LogCategory.STORAGE, "Cleared diagnostic sample history")
        }
    }

    fun exportDiagnosticReport() {
        val specs = diagnosticsProvider.getDeviceSpecs()
        val mem = diagnosticsProvider.getMemoryInfo()
        val storage = diagnosticsProvider.getStorageInfo()
        val battery = diagnosticsProvider.getBatteryInfo()
        val thermal = diagnosticsProvider.getThermalStatus()

        val summary = """
            Device: ${specs.manufacturer} ${specs.model} (${specs.pixelGenerationName})
            Android: ${specs.androidVersion} (API ${specs.sdkInt})
            Processor / SoC: ${specs.socModel}
            RAM: ${mem.formattedUsed} used / ${mem.formattedTotal} total (${String.format(java.util.Locale.US, "%.1f", mem.usedPercentage)}%)
            Storage: ${storage.formattedUsed} used / ${storage.formattedTotal} total (${String.format(java.util.Locale.US, "%.1f", storage.usedPercentage)}%)
            Battery: ${battery.levelPercentage}% (${battery.state.name}, ${battery.formattedTemperature})
            Thermal Status: ${thermal.statusName} (Code: ${thermal.statusCode})
        """.trimIndent()

        val report = EventLogger.generateFormattedExport(summary)
        actionLauncher.shareDiagnosticReport(report)
    }

    fun openUsageAccessSettings() {
        actionLauncher.openUsageAccessSettings()
    }

    fun getDeviceSpecs() = diagnosticsProvider.getDeviceSpecs()
}
