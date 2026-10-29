package com.example.feature.startup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppDatabase
import com.example.core.database.OptimizationRepository
import com.example.core.logging.EventLogger
import com.example.core.model.BootImpactLevel
import com.example.core.model.BootOptimizationResult
import com.example.core.model.LogCategory
import com.example.core.model.LogSeverity
import com.example.core.model.StartupAppInfo
import com.example.core.model.StartupSummary
import com.example.core.platform.StartupAppsProvider
import com.example.core.platform.SystemActionLauncher
import com.example.core.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StartupFilter {
    ALL,
    NON_ESSENTIAL,
    HIGH_IMPACT,
    SYSTEM
}

data class StartupUiState(
    val apps: List<StartupAppInfo> = emptyList(),
    val summary: StartupSummary = StartupSummary(0, 0, 0, 0, 0L),
    val isLoading: Boolean = false,
    val isOptimizing: Boolean = false,
    val searchQuery: String = "",
    val activeFilter: StartupFilter = StartupFilter.ALL,
    val lastOptimizationResult: BootOptimizationResult? = null,
    val showResultDialog: Boolean = false
)

class StartupViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository.getInstance(application)
    private val startupProvider = StartupAppsProvider(application, preferencesRepository)
    private val actionLauncher = SystemActionLauncher(application)
    private val optimizationRepository = OptimizationRepository(
        AppDatabase.getInstance(application).optimizationLogDao()
    )

    private val _uiState = MutableStateFlow(StartupUiState())
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    init {
        loadStartupApps()
    }

    fun loadStartupApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val appsList = startupProvider.getStartupApps()
            val summaryData = startupProvider.getStartupSummary()
            _uiState.update {
                it.copy(
                    apps = appsList,
                    summary = summaryData,
                    isLoading = false
                )
            }
        }
    }

    fun toggleAppStartup(packageName: String, enable: Boolean) {
        viewModelScope.launch {
            startupProvider.toggleAppStartup(packageName, enable)
            val updatedApps = _uiState.value.apps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isEnabledAtBoot = enable)
                } else app
            }
            val summaryData = startupProvider.getStartupSummary()
            _uiState.update {
                it.copy(
                    apps = updatedApps,
                    summary = summaryData
                )
            }

            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.SYSTEM,
                message = "Local startup review preference for $packageName set to: ${if (enable) "Enabled" else "Marked for review"}"
            )
        }
    }

    fun optimizeAllBootApps() {
        if (_uiState.value.isOptimizing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizing = true) }
            val result = startupProvider.optimizeAllNonEssential()

            // Record to Room optimization log
            optimizationRepository.recordLog(
                boosterType = "STARTUP_OPTIMIZE",
                title = "Boot Time Optimization",
                details = "Saved local review preferences for ${result.appsOptimizedCount} non-essential apps; boot-time and RAM changes are not measured.",
                metricFreed = "Boot-time change: not measured"
            )

            val updatedApps = startupProvider.getStartupApps()
            val summaryData = startupProvider.getStartupSummary()

            _uiState.update {
                it.copy(
                    apps = updatedApps,
                    summary = summaryData,
                    isOptimizing = false,
                    lastOptimizationResult = result,
                    showResultDialog = true
                )
            }

            EventLogger.log(
                severity = LogSeverity.INFO,
                category = LogCategory.SYSTEM,
                message = "Saved local review preferences for ${result.appsOptimizedCount} apps; system startup behavior was not changed."
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: StartupFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun dismissResultDialog() {
        _uiState.update { it.copy(showResultDialog = false) }
    }

    fun openAppDetails(packageName: String) {
        actionLauncher.openAppDetails(packageName)
    }

    fun openBatteryOptimization() {
        actionLauncher.openBatteryOptimizationSettings()
    }
}
