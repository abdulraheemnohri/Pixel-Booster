package com.example.feature.booster

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.booster.BoostResultSummary
import com.example.core.booster.BoosterService
import com.example.core.booster.BoosterStage
import com.example.core.booster.BoosterToolType
import com.example.core.booster.CacheClearReport
import com.example.core.booster.JunkScanResult
import com.example.core.booster.LiveRamMetrics
import com.example.core.booster.NetworkPingResult
import com.example.core.booster.PowerProfile
import com.example.core.database.AppDatabase
import com.example.core.database.OptimizationLogEntity
import com.example.core.database.OptimizationRepository
import com.example.core.logging.EventLogger
import com.example.core.model.BatteryInfoData
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.model.MemoryInfoData
import com.example.core.model.StorageInfoData
import com.example.core.model.ThermalStatusData
import com.example.core.platform.DeviceDiagnosticsProvider
import com.example.core.platform.SystemActionLauncher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class BoosterUiState(
    val isBoosting: Boolean = false,
    val currentStage: BoosterStage = BoosterStage.IDLE,
    val currentStageText: String = "",
    val boostProgress: Float = 0f,
    val selectedTool: BoosterToolType? = null,
    val activePowerProfile: PowerProfile = PowerProfile.BALANCED,
    val lastResultSummary: BoostResultSummary? = null,
    val showResultDialog: Boolean = false,
    val memoryInfo: MemoryInfoData? = null,
    val liveRamMetrics: LiveRamMetrics? = null,
    val storageInfo: StorageInfoData? = null,
    val batteryInfo: BatteryInfoData? = null,
    val thermalStatus: ThermalStatusData? = null,
    val junkScanResult: JunkScanResult? = null,
    val networkPingResult: NetworkPingResult? = null,
    val gameTurboActive: Boolean = false,
    val recentLogs: List<OptimizationLogEntity> = emptyList(),
    val optimizationScore: Int = 0,
    val isClearingCache: Boolean = false,
    val cacheClearProgress: Float = 0f,
    val cacheClearStageText: String = "",
    val lastCacheClearReport: CacheClearReport? = null,
    val showCacheClearDialog: Boolean = false,
    val detectedCacheBytes: Long = 0L,
    val detectedCacheFormatted: String = "Scanning app cache…",
    val isSystemOptimizing: Boolean = false,
    val systemOptimizationResult: com.example.core.optimizer.SystemOptimizationResult? = null,
    val showSystemOptimizerDialog: Boolean = false
)

class BoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val boosterService = BoosterService(application)
    private val diagnosticsProvider = DeviceDiagnosticsProvider(application)
    private val actionLauncher = SystemActionLauncher(application)
    private val repository = OptimizationRepository(
        AppDatabase.getInstance(application).optimizationLogDao()
    )

    private val _uiState = MutableStateFlow(BoosterUiState())
    val uiState: StateFlow<BoosterUiState> = _uiState.asStateFlow()

    private var boostJob: Job? = null

    init {
        refreshHardwareTelemetry()

        // Observe recent optimization history
        viewModelScope.launch {
            repository.allLogs.collectLatest { logs ->
                _uiState.update { it.copy(recentLogs = logs) }
            }
        }
    }

    fun refreshHardwareTelemetry() {
        val mem = diagnosticsProvider.getMemoryInfo()
        val ramMetrics = boosterService.getLiveRamMetrics()
        val storage = diagnosticsProvider.getStorageInfo()
        val battery = diagnosticsProvider.getBatteryInfo()
        val thermal = diagnosticsProvider.getThermalStatus()

        // Compute dynamic optimization health score (0 to 100)
        var score = 100
        if (mem.isAvailable) {
            if (mem.usedPercentage > 80f) score -= 15
            else if (mem.usedPercentage > 65f) score -= 8
        }

        if (storage.isAvailable) {
            if (storage.freePercentage < 15f) score -= 15
            else if (storage.freePercentage < 25f) score -= 8
        }

        if (thermal.statusCode >= 2) score -= 15
        if (battery.levelPercentage <= 20) score -= 10

        val finalScore = score.coerceIn(0, 100)

        _uiState.update {
            it.copy(
                memoryInfo = mem,
                liveRamMetrics = ramMetrics,
                storageInfo = storage,
                batteryInfo = battery,
                thermalStatus = thermal,
                optimizationScore = finalScore
            )
        }

        viewModelScope.launch {
            val cacheBytes = boosterService.estimateNonEssentialCacheBytes()
            _uiState.update {
                it.copy(
                    detectedCacheBytes = cacheBytes,
                    detectedCacheFormatted = MemoryInfoData.formatBytes(cacheBytes)
                )
            }
        }
    }

    fun runQuickBoost() {
        if (_uiState.value.isBoosting) return

        boostJob?.cancel()
        boostJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBoosting = true,
                    currentStage = BoosterStage.SCANNING,
                    currentStageText = "Measuring device memory and this app’s disposable cache...",
                    boostProgress = 0.1f
                )
            }
            delay(400)

            // Stage 1: RAM Reclaim
            _uiState.update {
                it.copy(
                    currentStage = BoosterStage.OPTIMIZING_RAM,
                    currentStageText = "Measuring memory before and after this app’s garbage collection...",
                    boostProgress = 0.35f
                )
            }
            val (ramBeforeBytes, freedRamBytes) = boosterService.boostRam()
            val freedRamMb = if (ramBeforeBytes > 0L) freedRamBytes / (1024L * 1024L) else null
            val ramSummary = freedRamMb?.let { "$it MB observed RAM-used decrease" } ?: "RAM measurement unavailable"
            delay(450)

            // Stage 2: Junk Purge
            _uiState.update {
                it.copy(
                    currentStage = BoosterStage.CLEANING_CACHE,
                    currentStageText = "Clearing only this app’s cache folders...",
                    boostProgress = 0.60f
                )
            }
            val junkScan = boosterService.scanJunk()
            val freedJunkBytes = boosterService.cleanJunk(junkScan)
            delay(450)

            // Stage 3: CPU Cooling
            _uiState.update {
                it.copy(
                    currentStage = BoosterStage.COOLING_HARDWARE,
                    currentStageText = "Observing battery temperature; no forced CPU cooling...",
                    boostProgress = 0.80f
                )
            }
            val tempDrop = boosterService.coolCpu()
            delay(400)

            // Stage 4: Network Latency
            _uiState.update {
                it.copy(
                    currentStage = BoosterStage.ACCELERATING_NETWORK,
                    currentStageText = "Measuring two network connection attempts; no network settings changed...",
                    boostProgress = 0.95f
                )
            }
            val networkResult = boosterService.testAndOptimizeNetwork()
            delay(350)

            // Stage 5: Completed
            _uiState.update {
                it.copy(
                    currentStage = BoosterStage.COMPLETED,
                    currentStageText = "Device checks complete",
                    boostProgress = 1.0f
                )
            }
            delay(300)

            val freedJunkMb = freedJunkBytes / (1024L * 1024L)

            val summary = BoostResultSummary(
                toolType = null,
                title = "Device Check Complete",
                headlineMetric = ramSummary,
                details = listOf(
                    "Observed RAM-used change" to (freedRamMb?.let { "$it MB decrease (system-wide memory may fluctuate; not reclaimed RAM)" } ?: "Unavailable; no valid memory measurement"),
                    "This app’s cache cleared" to "$freedJunkMb MB measured",
                    "Battery sensor" to (tempDrop?.let { String.format(Locale.US, "%.1f°C observed change (positive means cooler)", it) } ?: "Unavailable; no valid sensor reading"),
                    "Connection test" to if (networkResult.isOnline) "${networkResult.initialLatencyMs.takeIf { it > 0L }?.let { "$it ms" } ?: "First attempt unavailable"}; ${networkResult.optimizedLatencyMs.takeIf { it > 0L }?.let { "$it ms" } ?: "Second attempt unavailable"} (measured; not optimized)" else "Unavailable; connection test failed"
                )
            )

            // Persist to Room
            repository.recordLog(
                boosterType = "QUICK_BOOST",
                title = "Quick Device Check",
                details = "$ramSummary (not guaranteed reclaimed). Removed $freedJunkMb MB from this app cache. Battery sensor: ${tempDrop?.let { String.format(Locale.US, "%.1f°C observed change", it) } ?: "unavailable"}; no active CPU cooling was performed.",
                metricFreed = ramSummary
            )

            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.SYSTEM,
                message = "Quick maintenance completed: $ramSummary; removed $freedJunkMb MB from app-owned cache. No system-wide RAM/CPU optimization is claimed."
            )

            refreshHardwareTelemetry()

            _uiState.update {
                it.copy(
                    isBoosting = false,
                    lastResultSummary = summary,
                    showResultDialog = true,
                    optimizationScore = it.optimizationScore
                )
            }
        }
    }

    fun runRamBoost() {
        if (_uiState.value.isBoosting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBoosting = true,
                    currentStage = BoosterStage.OPTIMIZING_RAM,
                    currentStageText = "Requesting garbage collection in this app and measuring system memory...",
                    boostProgress = 0.5f
                )
            }
            val (ramBeforeBytes, freedBytes) = boosterService.boostRam()
            delay(600)

            val freedMb = if (ramBeforeBytes > 0L) freedBytes / (1024L * 1024L) else null
            val ramSummary = freedMb?.let { "$it MB observed RAM-used decrease" } ?: "RAM measurement unavailable"
            val summary = BoostResultSummary(
                toolType = BoosterToolType.RAM_BOOSTER,
                title = "Memory Measurement Complete",
                headlineMetric = ramSummary,
                details = listOf(
                    "Observed RAM-used change" to (freedMb?.let { "$it MB decrease; system-wide reclaim is not guaranteed" } ?: "Unavailable; no valid memory measurement"),
                    "Background apps" to "Not terminated (Android controls other apps)",
                    "Garbage collector" to "Requested GC for this app only"
                )
            )

            repository.recordLog(
                boosterType = BoosterToolType.RAM_BOOSTER.name,
                title = "Memory Measurement",
                details = "$ramSummary after requesting garbage collection in this app. This is not a guaranteed system-wide reclaim.",
                metricFreed = ramSummary
            )

            refreshHardwareTelemetry()

            _uiState.update {
                it.copy(
                    isBoosting = false,
                    selectedTool = null,
                    lastResultSummary = summary,
                    showResultDialog = true
                )
            }
        }
    }

    fun runJunkClean() {
        if (_uiState.value.isBoosting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBoosting = true,
                    currentStage = BoosterStage.CLEANING_CACHE,
                    currentStageText = "Scanning & sweeping temporary cache files...",
                    boostProgress = 0.5f
                )
            }
            val scan = boosterService.scanJunk()
            val freedBytes = boosterService.cleanJunk(scan)
            delay(600)

            val freedMb = freedBytes / (1024L * 1024L)
            val summary = BoostResultSummary(
                toolType = BoosterToolType.JUNK_CLEANER,
                title = "App Cache Cleanup Complete",
                headlineMetric = "$freedMb MB app cache removed",
                details = listOf(
                    "App cache directory" to scan.formattedSystemCache,
                    "External app cache" to scan.formattedThumbnails,
                    "Code cache" to scan.formattedTempFiles,
                    "Residual logs" to "Not scanned or removed"
                )
            )

            repository.recordLog(
                boosterType = BoosterToolType.JUNK_CLEANER.name,
                title = "App-Owned Cache Cleanup",
                details = "Removed $freedMb MB from this app’s cache directories only; personal files, shared media, and other apps were not touched.",
                metricFreed = "$freedMb MB"
            )

            refreshHardwareTelemetry()

            _uiState.update {
                it.copy(
                    isBoosting = false,
                    selectedTool = null,
                    lastResultSummary = summary,
                    showResultDialog = true
                )
            }
        }
    }

    fun runCpuCooler() {
        if (_uiState.value.isBoosting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBoosting = true,
                    currentStage = BoosterStage.COOLING_HARDWARE,
                    currentStageText = "Observing battery temperature; this app cannot control processor cooling...",
                    boostProgress = 0.5f
                )
            }
            val drop = boosterService.coolCpu()
            delay(700)

            val summary = BoostResultSummary(
                toolType = BoosterToolType.CPU_COOLER,
                title = "Temperature Observation Complete",
                headlineMetric = drop?.let { String.format(Locale.US, "%.1f°C observed battery-temperature decrease", it) } ?: "Battery temperature unavailable",
                details = listOf(
                    "Thermal State" to (_uiState.value.thermalStatus?.statusName ?: "Unavailable"),
                    "Processor throttling" to "Not changed by this app",
                    "Safe guidance" to "Pause demanding tasks, remove direct sunlight, and unplug charging if hot"
                )
            )

            repository.recordLog(
                boosterType = BoosterToolType.CPU_COOLER.name,
                title = "Thermal Observation",
                details = "Observed battery-sensor temperature: ${drop?.let { String.format(Locale.US, "%.1f°C change", it) } ?: "unavailable"}. No CPU frequency or thermal controls were changed.",
                metricFreed = drop?.let { String.format(Locale.US, "%.1f°C observed sensor change", it) } ?: "Unavailable"
            )

            refreshHardwareTelemetry()

            _uiState.update {
                it.copy(
                    isBoosting = false,
                    selectedTool = null,
                    lastResultSummary = summary,
                    showResultDialog = true
                )
            }
        }
    }

    fun selectPowerProfile(profile: PowerProfile) {
        _uiState.update { it.copy(activePowerProfile = profile) }
        viewModelScope.launch {
            repository.recordLog(
                boosterType = BoosterToolType.BATTERY_SAVER.name,
                title = "Power Profile: ${profile.label}",
                details = profile.description,
                metricFreed = profile.name
            )
            if (profile == PowerProfile.EXTREME_SAVER) {
                actionLauncher.openBatterySaverSettings()
            }
        }
    }

    fun runNetworkTurbo() {
        if (_uiState.value.isBoosting) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBoosting = true,
                    currentStage = BoosterStage.ACCELERATING_NETWORK,
                    currentStageText = "Measuring two connection latencies; network settings will not be changed...",
                    boostProgress = 0.5f
                )
            }
            val res = boosterService.testAndOptimizeNetwork()
            delay(600)

            val summary = BoostResultSummary(
                toolType = BoosterToolType.NETWORK_TURBO,
                title = if (res.isOnline) "Network Test Complete" else "Network Test Unavailable",
                headlineMetric = res.optimizedLatencyMs.takeIf { it > 0L }?.let { "$it ms measured latency" } ?: "No latency result available",
                details = listOf(
                    "Target" to res.host,
                    "First measurement" to (res.initialLatencyMs.takeIf { it > 0L }?.let { "$it ms" } ?: "Unavailable"),
                    "Second measurement" to (res.optimizedLatencyMs.takeIf { it > 0L }?.let { "$it ms" } ?: "Unavailable"),
                    "Latency difference" to (res.latencyImprovementMs.takeIf { it != Long.MIN_VALUE }?.let { "$it ms (positive = second attempt faster; negative = slower)" } ?: "Unavailable unless both attempts succeed")
                )
            )

            repository.recordLog(
                boosterType = BoosterToolType.NETWORK_TURBO.name,
                title = "Network Diagnostics",
                details = "Connection test to ${res.host}: first ${res.initialLatencyMs.takeIf { it > 0L }?.let { "$it ms" } ?: "unavailable"}, second ${res.optimizedLatencyMs.takeIf { it > 0L }?.let { "$it ms" } ?: "unavailable"}. No network settings were changed.",
                metricFreed = res.latencyImprovementMs.takeIf { it != Long.MIN_VALUE }?.let { "$it ms observed latency difference" } ?: "Unavailable"
            )

            _uiState.update {
                it.copy(
                    isBoosting = false,
                    networkPingResult = res,
                    selectedTool = null,
                    lastResultSummary = summary,
                    showResultDialog = true
                )
            }
        }
    }

    fun toggleGameTurbo() {
        val next = !_uiState.value.gameTurboActive
        viewModelScope.launch {
            if (next) {
                boosterService.boostRam()
            }
            _uiState.update { it.copy(gameTurboActive = next) }
            repository.recordLog(
                boosterType = BoosterToolType.GAME_TURBO.name,
                title = if (next) "Gaming preference enabled" else "Gaming preference disabled",
                details = if (next) "Game guidance mode enabled in the app; Android does not grant extra RAM or GPU priority." else "Game guidance mode disabled in the app.",
                metricFreed = if (next) "GUIDANCE ON" else "OFF"
            )
        }
    }

    fun openTool(tool: BoosterToolType) {
        _uiState.update { it.copy(selectedTool = tool) }
    }

    fun closeTool() {
        _uiState.update { it.copy(selectedTool = null) }
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false, lastResultSummary = null) }
    }

    fun clearNonEssentialCache() {
        if (_uiState.value.isClearingCache || _uiState.value.isBoosting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isClearingCache = true,
                    cacheClearProgress = 0.20f,
                    cacheClearStageText = "Scanning non-essential cache directories..."
                )
            }
            delay(300)

            _uiState.update {
                it.copy(
                    cacheClearProgress = 0.55f,
                    cacheClearStageText = "Clearing this app’s cache folders only..."
                )
            }
            delay(350)

            _uiState.update {
                it.copy(
                    cacheClearProgress = 0.82f,
                    cacheClearStageText = "Measuring bytes actually removed..."
                )
            }

            val report = boosterService.clearNonEssentialCache()
            delay(250)

            _uiState.update {
                it.copy(
                    cacheClearProgress = 1.0f,
                    cacheClearStageText = "App cache cleanup finished; performance improvement is not guaranteed."
                )
            }
            delay(250)

            val freedMb = report.totalFreedBytes / (1024L * 1024L)

            // Persist to Room
            repository.recordLog(
                boosterType = "CLEAR_CACHE",
                title = "App-Owned Cache Cleanup",
                details = "Removed $freedMb MB from this app’s own cache folders (Internal: ${report.formattedInternal}, Code cache: ${report.formattedCodeCache}) in ${report.durationMs}ms. System-wide performance improvement is not guaranteed.",
                metricFreed = "$freedMb MB app cache removed"
            )

            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.STORAGE,
                message = "Cleared non-essential cache: $freedMb MB freed in ${report.durationMs}ms"
            )

            refreshHardwareTelemetry()

            _uiState.update {
                it.copy(
                    isClearingCache = false,
                    lastCacheClearReport = report,
                    showCacheClearDialog = true,
                    detectedCacheBytes = 0L,
                    detectedCacheFormatted = "0 B (Clean)",
                    optimizationScore = it.optimizationScore
                )
            }
        }
    }

    fun dismissCacheClearDialog() {
        _uiState.update { it.copy(showCacheClearDialog = false) }
    }

    fun runSystemOptimizer() {
        if (_uiState.value.isSystemOptimizing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSystemOptimizing = true) }
            val result = com.example.core.optimizer.SystemOptimizer.optimizeSystem(getApplication())
            boosterService.markCacheCleared()
            refreshHardwareTelemetry()
            _uiState.update {
                it.copy(
                    isSystemOptimizing = false,
                    systemOptimizationResult = result,
                    showSystemOptimizerDialog = true,
                    detectedCacheBytes = 0L,
                    detectedCacheFormatted = "0 B (Clean)",
                    optimizationScore = it.optimizationScore
                )
            }
        }
    }

    fun dismissSystemOptimizerDialog() {
        _uiState.update { it.copy(showSystemOptimizerDialog = false) }
    }

    fun clearOptimizationHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
