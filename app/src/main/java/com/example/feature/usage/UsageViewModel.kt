package com.example.feature.usage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.logging.EventLogger
import com.example.core.model.AppUsageData
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.model.UsagePeriod
import com.example.core.platform.SystemActionLauncher
import com.example.core.platform.UsageStatsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UsageSortOrder {
    DURATION,
    NAME
}

data class UsageUiState(
    val isPermissionGranted: Boolean = false,
    val selectedPeriod: UsagePeriod = UsagePeriod.TODAY,
    val sortOrder: UsageSortOrder = UsageSortOrder.DURATION,
    val usageList: List<AppUsageData> = emptyList(),
    val isLoading: Boolean = false,
    val totalForegroundMillis: Long = 0L
)

class UsageViewModel(application: Application) : AndroidViewModel(application) {

    private val usageStatsProvider = UsageStatsProvider(application)
    private val actionLauncher = SystemActionLauncher(application)

    private val _uiState = MutableStateFlow(UsageUiState())
    val uiState: StateFlow<UsageUiState> = _uiState.asStateFlow()

    init {
        checkPermissionAndLoad()
    }

    fun checkPermissionAndLoad() {
        val granted = usageStatsProvider.isUsageAccessGranted()
        _uiState.update { it.copy(isPermissionGranted = granted) }
        if (granted) {
            loadUsageData(_uiState.value.selectedPeriod, _uiState.value.sortOrder)
        }
    }

    fun setPeriod(period: UsagePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadUsageData(period, _uiState.value.sortOrder)
    }

    fun setSortOrder(sortOrder: UsageSortOrder) {
        _uiState.update { it.copy(sortOrder = sortOrder) }
        loadUsageData(_uiState.value.selectedPeriod, sortOrder)
    }

    fun loadUsageData(period: UsagePeriod = _uiState.value.selectedPeriod, sortOrder: UsageSortOrder = _uiState.value.sortOrder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val raw = usageStatsProvider.getUsageStats(period)
            val sorted = when (sortOrder) {
                UsageSortOrder.DURATION -> raw.sortedByDescending { it.totalTimeInForegroundMillis }
                UsageSortOrder.NAME -> raw.sortedBy { it.appLabel.lowercase() }
            }
            val totalTime = raw.sumOf { it.totalTimeInForegroundMillis }
            _uiState.update {
                it.copy(
                    usageList = sorted,
                    totalForegroundMillis = totalTime,
                    isLoading = false
                )
            }
            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.TELEMETRY,
                message = "Loaded app usage insights: ${sorted.size} active packages for ${period.name}."
            )
        }
    }

    fun openUsageAccessSettings() {
        actionLauncher.openUsageAccessSettings()
        EventLogger.log(LogSeverity.INFO, LogCategory.SETTINGS, "Dispatched user to Usage Access Settings.")
    }
}
