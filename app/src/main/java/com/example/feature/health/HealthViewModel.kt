package com.example.feature.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.health.HealthEngine
import com.example.core.logging.EventLogger
import com.example.core.model.FindingSeverity
import com.example.core.model.HealthFinding
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.model.SettingsData
import com.example.core.model.SuggestedActionType
import com.example.core.platform.DeviceDiagnosticsProvider
import com.example.core.platform.SystemActionLauncher
import com.example.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HealthFilter {
    ALL,
    CRITICAL,
    WARNINGS,
    NOMINAL
}

data class HealthUiState(
    val findings: List<HealthFinding> = emptyList(),
    val activeFilter: HealthFilter = HealthFilter.ALL,
    val isRefreshing: Boolean = false,
    val lastEvaluatedMillis: Long = System.currentTimeMillis(),
    val overallHealthScore: Int = 100,
    val hardwareTelemetry: com.example.core.model.CompleteHardwareTelemetry? = null
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val diagnosticsProvider = DeviceDiagnosticsProvider(application)
    private val actionLauncher = SystemActionLauncher(application)
    private val healthEngine = HealthEngine()
    private val preferencesRepository = UserPreferencesRepository.getInstance(application)
    private val boosterService = com.example.core.booster.BoosterService(application)
    private val hardwareMonitorService = com.example.core.platform.HardwareMonitorService(
        context = application,
        boosterService = boosterService,
        diagnosticsProvider = diagnosticsProvider
    )

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    private var currentSettings = SettingsData()

    init {
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                evaluateHealth()
            }
        }
        viewModelScope.launch {
            hardwareMonitorService.telemetryFlow.collectLatest { telemetry ->
                _uiState.update { it.copy(hardwareTelemetry = telemetry) }
            }
        }
    }

    fun triggerThermalRelief() {
        viewModelScope.launch {
            boosterService.boostRam()
            hardwareMonitorService.refreshNow()
            evaluateHealth()
        }
    }

    fun evaluateHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val mem = diagnosticsProvider.getMemoryInfo()
            val storage = diagnosticsProvider.getStorageInfo()
            val battery = diagnosticsProvider.getBatteryInfo()
            val thermal = diagnosticsProvider.getThermalStatus()

            val rawFindings = healthEngine.evaluate(
                memory = mem,
                storage = storage,
                battery = battery,
                thermal = thermal,
                settings = currentSettings
            )

            // Calculate deterministic health score out of 100
            val criticalPenalty = rawFindings.count { it.severity == FindingSeverity.CRITICAL } * 35
            val warningPenalty = rawFindings.count { it.severity == FindingSeverity.WARNING } * 15
            val score = (100 - criticalPenalty - warningPenalty).coerceIn(10, 100)

            _uiState.update {
                it.copy(
                    findings = rawFindings,
                    isRefreshing = false,
                    lastEvaluatedMillis = System.currentTimeMillis(),
                    overallHealthScore = score
                )
            }
        }
    }

    fun setFilter(filter: HealthFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun executeAction(actionType: SuggestedActionType) {
        when (actionType) {
            SuggestedActionType.OPEN_BATTERY_SAVER -> {
                actionLauncher.openBatterySaverSettings()
                EventLogger.log(LogSeverity.INFO, LogCategory.HEALTH, "Executed action: Open Battery Saver")
            }
            SuggestedActionType.OPEN_STORAGE_SETTINGS -> {
                actionLauncher.openStorageSettings()
                EventLogger.log(LogSeverity.INFO, LogCategory.HEALTH, "Executed action: Open Storage Settings")
            }
            SuggestedActionType.OPEN_APP_SETTINGS -> {
                actionLauncher.openApplicationSettings()
                EventLogger.log(LogSeverity.INFO, LogCategory.HEALTH, "Executed action: Open App Settings")
            }
            SuggestedActionType.OPEN_USAGE_ACCESS -> {
                actionLauncher.openUsageAccessSettings()
                EventLogger.log(LogSeverity.INFO, LogCategory.HEALTH, "Executed action: Open Usage Access Settings")
            }
            SuggestedActionType.REFRESH -> {
                evaluateHealth()
            }
            SuggestedActionType.COOLDOWN_DEVICE, SuggestedActionType.NONE -> {
                // Informational or handled in UI
            }
        }
    }
}
