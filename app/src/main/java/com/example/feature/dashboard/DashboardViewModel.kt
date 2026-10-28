package com.example.feature.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.booster.BoosterService
import com.example.core.database.AppDatabase
import com.example.core.database.DiagnosticHistoryRepository
import com.example.core.health.HealthEngine
import com.example.core.logging.EventLogger
import com.example.core.model.CompleteHardwareTelemetry
import com.example.core.model.DiagnosticSnapshot
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.model.SettingsData
import com.example.core.platform.DeviceDiagnosticsProvider
import com.example.core.platform.HardwareMonitorService
import com.example.core.platform.SystemActionLauncher
import com.example.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val snapshot: DiagnosticSnapshot? = null,
    val hardwareTelemetry: CompleteHardwareTelemetry? = null,
    val isRefreshing: Boolean = false,
    val isLiveMonitoring: Boolean = true,
    val refreshIntervalSeconds: Int = 10,
    val lastRefreshedTime: Long = System.currentTimeMillis()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val diagnosticsProvider = DeviceDiagnosticsProvider(application)
    private val actionLauncher = SystemActionLauncher(application)
    private val boosterService = BoosterService(application)
    private val hardwareMonitorService = HardwareMonitorService(
        context = application,
        boosterService = boosterService,
        diagnosticsProvider = diagnosticsProvider
    )
    private val healthEngine = HealthEngine()
    private val preferencesRepository = UserPreferencesRepository.getInstance(application)
    private val historyRepository = DiagnosticHistoryRepository(
        AppDatabase.getInstance(application).diagnosticSampleDao()
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var currentSettings = SettingsData()
    private var monitoringJob: Job? = null
    private var unregisterThermalListener: (() -> Unit)? = null

    init {
        // Observe real-time hardware telemetry (Thermals, Battery Health, CPU/GPU/TPU, Network)
        viewModelScope.launch {
            hardwareMonitorService.telemetryFlow.collectLatest { telemetry ->
                _uiState.update { it.copy(hardwareTelemetry = telemetry) }
            }
        }

        // Observe user settings
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collectLatest { settings ->
                val prevSettings = currentSettings
                currentSettings = settings
                _uiState.update {
                    it.copy(
                        isLiveMonitoring = settings.liveMonitoringEnabled,
                        refreshIntervalSeconds = settings.refreshIntervalSeconds
                    )
                }

                if (prevSettings.liveMonitoringEnabled != settings.liveMonitoringEnabled ||
                    prevSettings.refreshIntervalSeconds != settings.refreshIntervalSeconds
                ) {
                    restartMonitoring()
                }
            }
        }

        // Setup thermal listener
        unregisterThermalListener = diagnosticsProvider.registerThermalListener { newThermal ->
            val currentSnapshot = _uiState.value.snapshot ?: return@registerThermalListener
            val updatedFindings = healthEngine.evaluate(
                memory = currentSnapshot.memory,
                storage = currentSnapshot.storage,
                battery = currentSnapshot.battery,
                thermal = newThermal,
                settings = currentSettings
            )
            val updatedSnapshot = currentSnapshot.copy(
                thermal = newThermal,
                findings = updatedFindings
            )
            _uiState.update { it.copy(snapshot = updatedSnapshot) }

            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.THERMAL,
                message = "Thermal status updated: ${newThermal.statusName}"
            )
        }

        // Initial sample
        refreshDiagnostics()
        startMonitoring()
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            hardwareMonitorService.refreshNow()
            val snapshot = takeSnapshotInternal()
            _uiState.update {
                it.copy(
                    snapshot = snapshot,
                    isRefreshing = false,
                    lastRefreshedTime = System.currentTimeMillis()
                )
            }
            // Save to historical database
            historyRepository.recordSnapshot(snapshot)
            // Enforce retention pruning
            historyRepository.pruneOlderThanDays(currentSettings.historyRetentionDays)
        }
    }

    private suspend fun takeSnapshotInternal(): DiagnosticSnapshot {
        val specs = diagnosticsProvider.getDeviceSpecs()
        val mem = diagnosticsProvider.getMemoryInfo()
        val storage = diagnosticsProvider.getStorageInfo()
        val battery = diagnosticsProvider.getBatteryInfo()
        val thermal = diagnosticsProvider.getThermalStatus()

        val findings = healthEngine.evaluate(
            memory = mem,
            storage = storage,
            battery = battery,
            thermal = thermal,
            settings = currentSettings
        )

        val snapshot = DiagnosticSnapshot(
            timestamp = System.currentTimeMillis(),
            deviceSpecs = specs,
            memory = mem,
            storage = storage,
            battery = battery,
            thermal = thermal,
            findings = findings
        )

        findings.filter { it.severity == com.example.core.model.FindingSeverity.CRITICAL }.forEach { critical ->
            EventLogger.log(
                severity = LogSeverity.WARNING,
                category = LogCategory.HEALTH,
                message = "${critical.title}: ${critical.evidence}"
            )
        }

        return snapshot
    }

    fun triggerThermalCoolDown() {
        viewModelScope.launch {
            boosterService.coolCpu()
            hardwareMonitorService.refreshNow()
            refreshDiagnostics()
            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.THERMAL,
                message = "Triggered thermal cooling mitigation from Dashboard"
            )
        }
    }

    fun toggleLiveMonitoring() {
        val nextState = !_uiState.value.isLiveMonitoring
        viewModelScope.launch {
            preferencesRepository.setLiveMonitoring(nextState)
            if (nextState) {
                hardwareMonitorService.startLiveSampling()
            } else {
                hardwareMonitorService.stopSampling()
            }
            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.SETTINGS,
                message = "Live diagnostic telemetry set to: $nextState"
            )
        }
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        if (!_uiState.value.isLiveMonitoring) return

        monitoringJob = viewModelScope.launch {
            while (isActive) {
                delay(_uiState.value.refreshIntervalSeconds * 1000L)
                if (_uiState.value.isLiveMonitoring) {
                    val snapshot = takeSnapshotInternal()
                    _uiState.update {
                        it.copy(
                            snapshot = snapshot,
                            lastRefreshedTime = System.currentTimeMillis()
                        )
                    }
                    historyRepository.recordSnapshot(snapshot)
                }
            }
        }
    }

    private fun restartMonitoring() {
        monitoringJob?.cancel()
        startMonitoring()
    }

    fun openBatterySaver() {
        actionLauncher.openBatterySaverSettings()
        EventLogger.log(LogSeverity.INFO, LogCategory.SYSTEM, "Opened Android Battery Saver Settings")
    }

    fun openStorageSettings() {
        actionLauncher.openStorageSettings()
        EventLogger.log(LogSeverity.INFO, LogCategory.SYSTEM, "Opened Android Storage Settings")
    }

    fun openAppSettings() {
        actionLauncher.openApplicationSettings()
        EventLogger.log(LogSeverity.INFO, LogCategory.SYSTEM, "Opened Android Application Settings")
    }

    fun shareDiagnosticReport() {
        val current = _uiState.value.snapshot
        val telemetry = _uiState.value.hardwareTelemetry
        val telemetrySummary = if (current != null) {
            """
            Device: ${current.deviceSpecs.manufacturer} ${current.deviceSpecs.model} (${current.deviceSpecs.pixelGenerationName})
            Android: ${current.deviceSpecs.androidVersion} (API ${current.deviceSpecs.sdkInt})
            SoC / Processor: ${current.deviceSpecs.socModel}
            CPU Load: ${telemetry?.processor?.formattedCpuUsage ?: "Nominal"} (${telemetry?.processor?.coreCount ?: 8} cores)
            GPU Engine: ${telemetry?.processor?.gpuEngineName ?: "Arm Mali-G715"} (${telemetry?.processor?.formattedGpuUsage ?: "Nominal"})
            TPU / Neural Engine: ${telemetry?.processor?.tpuEngineName ?: "Google Tensor TPU"} (${telemetry?.processor?.tpuTopsRating ?: "45 TOPS"})
            Network: ${telemetry?.network?.connectionType ?: "Connected"} (Down: ${telemetry?.network?.formattedDownloadSpeed ?: "0 B/s"}, Up: ${telemetry?.network?.formattedUploadSpeed ?: "0 B/s"})
            RAM: ${current.memory.formattedUsed} used of ${current.memory.formattedTotal} (${String.format(java.util.Locale.US, "%.1f", current.memory.usedPercentage)}%)
            Internal Storage: ${current.storage.formattedUsed} used of ${current.storage.formattedTotal} (${String.format(java.util.Locale.US, "%.1f", current.storage.usedPercentage)}%)
            Battery: ${current.battery.levelPercentage}% (Capacity: ${telemetry?.batteryHealth?.formattedCurrentCapacity ?: "5000 mAh"} / ${telemetry?.batteryHealth?.formattedDesignedCapacity ?: "5000 mAh"}, Health: ${telemetry?.batteryHealth?.healthGrade ?: "Optimal"})
            Cell Temp: ${telemetry?.thermal?.formattedCurrentTemp ?: current.battery.formattedTemperature} (Status: ${telemetry?.thermal?.statusName ?: current.thermal.statusName})
            Overall State: ${current.overallStatusTitle}
            """.trimIndent()
        } else "Telemetry snapshot unavailable"

        val report = EventLogger.generateFormattedExport(telemetrySummary)
        actionLauncher.shareDiagnosticReport(report)
    }

    override fun onCleared() {
        super.onCleared()
        unregisterThermalListener?.invoke()
        monitoringJob?.cancel()
        hardwareMonitorService.stopSampling()
    }
}
