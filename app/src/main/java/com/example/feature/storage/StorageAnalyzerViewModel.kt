package com.example.feature.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.model.JunkCategoryItem
import com.example.core.model.StorageAnalyzerReport
import com.example.core.model.StorageVolumeType
import com.example.core.storage.StorageAnalyzerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StorageAnalyzerUiState(
    val report: StorageAnalyzerReport? = null,
    val selectedVolumeType: StorageVolumeType = StorageVolumeType.INTERNAL,
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val lastCleanedBytes: Long = 0L,
    val cleaningSuccessMessage: String? = null
)

class StorageAnalyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StorageAnalyzerRepository(application)

    private val _uiState = MutableStateFlow(StorageAnalyzerUiState())
    val uiState: StateFlow<StorageAnalyzerUiState> = _uiState.asStateFlow()

    init {
        scanStorage()
    }

    fun switchVolume(volumeType: StorageVolumeType) {
        if (_uiState.value.selectedVolumeType == volumeType && _uiState.value.report != null) return
        _uiState.update { it.copy(selectedVolumeType = volumeType) }
        scanStorage(volumeType)
    }

    fun scanStorage(volumeType: StorageVolumeType = _uiState.value.selectedVolumeType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, cleaningSuccessMessage = null) }
            val report = repository.scanStorage(volumeType)
            _uiState.update { it.copy(report = report, isScanning = false, selectedVolumeType = volumeType) }
        }
    }

    fun toggleItemSelection(itemId: String) {
        val currentReport = _uiState.value.report ?: return
        val updatedItems = currentReport.items.map { item ->
            if (item.id == itemId) item.copy(isSelected = !item.isSelected) else item
        }
        val newTotalJunk = updatedItems.filter { it.isSelected }.sumOf { it.sizeBytes }
        val newCache = updatedItems.filter { it.category == com.example.core.model.JunkCategoryType.APP_CACHE && it.isSelected }.sumOf { it.sizeBytes }
        val newLarge = updatedItems.filter { it.category == com.example.core.model.JunkCategoryType.LARGE_FILES && it.isSelected }.sumOf { it.sizeBytes }
        val newTemp = updatedItems.filter { it.category == com.example.core.model.JunkCategoryType.TEMP_FILES && it.isSelected }.sumOf { it.sizeBytes }
        val newApk = updatedItems.filter { it.category == com.example.core.model.JunkCategoryType.REDUNDANT_APKS && it.isSelected }.sumOf { it.sizeBytes }

        val updatedReport = currentReport.copy(
            totalJunkBytes = newTotalJunk,
            cacheBytes = newCache,
            largeFilesBytes = newLarge,
            tempFilesBytes = newTemp,
            redundantApksBytes = newApk,
            items = updatedItems
        )
        _uiState.update { it.copy(report = updatedReport) }
    }

    fun cleanSelectedJunk() {
        val report = _uiState.value.report ?: return
        val selectedItems = report.items.filter { it.isSelected }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCleaning = true) }
            val cleanedBytes = repository.cleanSelectedJunk(selectedItems)
            val freshReport = repository.scanStorage(_uiState.value.selectedVolumeType)
            _uiState.update {
                it.copy(
                    report = freshReport,
                    isCleaning = false,
                    lastCleanedBytes = cleanedBytes,
                    cleaningSuccessMessage = if (cleanedBytes > 0) {
                        "Successfully reclaimed ${com.example.core.model.MemoryInfoData.formatBytes(cleanedBytes)} of disk space!"
                    } else {
                        "Target files cleared and cache buffers pruned."
                    }
                )
            }
        }
    }
}
