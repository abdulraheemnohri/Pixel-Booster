package com.example.feature.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppDatabase
import com.example.core.database.DiagnosticHistoryRepository
import com.example.core.database.DiagnosticSampleEntity
import com.example.core.logging.EventLogger
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HistoryMetricType(val label: String, val unit: String) {
    RAM_PERCENT("RAM Used", "%"),
    STORAGE_GB("Storage Used", "GB"),
    BATTERY_PERCENT("Battery Level", "%"),
    BATTERY_TEMP("Battery Temp", "°C")
}

enum class HistoryTimeWindow(val label: String, val durationMillis: Long) {
    ONE_HOUR("1 Hour", 3_600_000L),
    TWENTY_FOUR_HOURS("24 Hours", 86_400_000L),
    SEVEN_DAYS("7 Days", 604_800_000L),
    ALL_TIME("All Time", Long.MAX_VALUE)
}

data class HistoryUiState(
    val selectedMetric: HistoryMetricType = HistoryMetricType.RAM_PERCENT,
    val selectedWindow: HistoryTimeWindow = HistoryTimeWindow.TWENTY_FOUR_HOURS,
    val samples: List<DiagnosticSampleEntity> = emptyList(),
    val filteredSamples: List<DiagnosticSampleEntity> = emptyList(),
    val minVal: Float = 0f,
    val maxVal: Float = 0f,
    val avgVal: Float = 0f,
    val retentionDays: Int = 14,
    val isClearing: Boolean = false
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyRepository = DiagnosticHistoryRepository(
        AppDatabase.getInstance(application).diagnosticSampleDao()
    )
    private val preferencesRepository = UserPreferencesRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        // Observe all samples
        viewModelScope.launch {
            historyRepository.allSamples.collectLatest { all ->
                _uiState.update { current ->
                    val filtered = applyFilter(all, current.selectedWindow)
                    val stats = calculateStats(filtered, current.selectedMetric)
                    current.copy(
                        samples = all,
                        filteredSamples = filtered,
                        minVal = stats.first,
                        maxVal = stats.second,
                        avgVal = stats.third
                    )
                }
            }
        }

        // Observe retention
        viewModelScope.launch {
            preferencesRepository.settingsFlow.collectLatest { settings ->
                _uiState.update { it.copy(retentionDays = settings.historyRetentionDays) }
            }
        }
    }

    fun selectMetric(metric: HistoryMetricType) {
        _uiState.update { current ->
            val stats = calculateStats(current.filteredSamples, metric)
            current.copy(
                selectedMetric = metric,
                minVal = stats.first,
                maxVal = stats.second,
                avgVal = stats.third
            )
        }
    }

    fun selectWindow(window: HistoryTimeWindow) {
        _uiState.update { current ->
            val filtered = applyFilter(current.samples, window)
            val stats = calculateStats(filtered, current.selectedMetric)
            current.copy(
                selectedWindow = window,
                filteredSamples = filtered,
                minVal = stats.first,
                maxVal = stats.second,
                avgVal = stats.third
            )
        }
    }

    private fun applyFilter(samples: List<DiagnosticSampleEntity>, window: HistoryTimeWindow): List<DiagnosticSampleEntity> {
        if (window == HistoryTimeWindow.ALL_TIME) {
            return samples.sortedBy { it.timestamp }
        }
        val cutoff = System.currentTimeMillis() - window.durationMillis
        return samples.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    }

    private fun calculateStats(samples: List<DiagnosticSampleEntity>, metric: HistoryMetricType): Triple<Float, Float, Float> {
        if (samples.isEmpty()) return Triple(0f, 0f, 0f)

        val values = samples.mapNotNull { sample ->
            when (metric) {
                HistoryMetricType.RAM_PERCENT -> sample.usedRamPercentage
                HistoryMetricType.STORAGE_GB -> sample.usedStorageGb
                HistoryMetricType.BATTERY_PERCENT -> sample.batteryPercentage.toFloat()
                HistoryMetricType.BATTERY_TEMP -> sample.batteryTemperatureCelsius
            }
        }

        if (values.isEmpty()) return Triple(0f, 0f, 0f)

        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val avg = values.average().toFloat()
        return Triple(min, max, avg)
    }

    fun clearHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isClearing = true) }
            val deleted = historyRepository.clearHistory()
            _uiState.update { it.copy(isClearing = false) }
            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.STORAGE,
                message = "Cleared $deleted diagnostic historical records from Room database."
            )
        }
    }
}
