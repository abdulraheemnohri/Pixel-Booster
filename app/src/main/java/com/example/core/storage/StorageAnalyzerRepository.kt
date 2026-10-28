package com.example.core.storage

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.core.model.JunkCategoryItem
import com.example.core.model.JunkCategoryType
import com.example.core.model.StorageAnalyzerReport
import com.example.core.model.StorageVolumeInfo
import com.example.core.model.StorageVolumeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Conservative storage view: only lists app-owned cache folders for cleanup.
 * It never scans or deletes filesDir, Downloads, photos, documents, or other apps' data.
 */
class StorageAnalyzerRepository(private val context: Context) {
    fun getStorageVolumes(): List<StorageVolumeInfo> = StorageVolumeType.values().map(::getVolumeInfo)

    fun getVolumeInfo(volumeType: StorageVolumeType): StorageVolumeInfo {
        val targetDir = when (volumeType) {
            StorageVolumeType.INTERNAL -> Environment.getDataDirectory()
            StorageVolumeType.EXTERNAL -> Environment.getExternalStorageDirectory()
            StorageVolumeType.APP_SANDBOX -> context.cacheDir
        }
        return try {
            val stat = StatFs(targetDir.path)
            val total = stat.totalBytes.coerceAtLeast(0L)
            val available = stat.availableBytes.coerceIn(0L, total)
            StorageVolumeInfo(
                volumeType = volumeType, path = targetDir.absolutePath,
                totalBytes = total, availableBytes = available,
                usedBytes = (total - available).coerceAtLeast(0L),
                isRemovable = volumeType == StorageVolumeType.EXTERNAL && Environment.isExternalStorageRemovable(),
                fileSystem = "Reported by Android"
            )
        } catch (_: Exception) {
            // Unknown means unknown; never display fabricated 64 GB / 32 GB capacities.
            StorageVolumeInfo(volumeType, targetDir.absolutePath, 0L, 0L, 0L,
                isRemovable = false, fileSystem = "Unavailable")
        }
    }

    suspend fun scanStorage(activeVolumeType: StorageVolumeType = StorageVolumeType.INTERNAL): StorageAnalyzerReport =
        withContext(Dispatchers.IO) {
            val volumes = getStorageVolumes()
            val active = volumes.find { it.volumeType == activeVolumeType } ?: volumes.first()
            val items = mutableListOf<JunkCategoryItem>()
            addCacheItem(items, context.cacheDir, "cache_internal", "This app's internal cache", JunkCategoryType.APP_CACHE)
            context.externalCacheDir?.let {
                addCacheItem(items, it, "cache_external", "This app's external cache", JunkCategoryType.APP_CACHE)
            }
            addCacheItem(items, context.codeCacheDir, "cache_code", "This app's code cache", JunkCategoryType.TEMP_FILES)

            val cacheBytes = items.filter { it.category == JunkCategoryType.APP_CACHE }.sumOf { it.sizeBytes }
            val tempBytes = items.filter { it.category == JunkCategoryType.TEMP_FILES }.sumOf { it.sizeBytes }
            StorageAnalyzerReport(
                totalJunkBytes = cacheBytes + tempBytes,
                cacheBytes = cacheBytes,
                largeFilesBytes = 0L,
                tempFilesBytes = tempBytes,
                redundantApksBytes = 0L,
                items = items,
                activeVolume = active,
                availableVolumes = volumes
            )
        }

    suspend fun cleanSelectedJunk(selectedItems: List<JunkCategoryItem>): Long = withContext(Dispatchers.IO) {
        var cleaned = 0L
        // Strict allow-list: path must resolve inside one of this app's disposable cache directories.
        val allowedRoots = listOfNotNull(context.cacheDir, context.externalCacheDir, context.codeCacheDir)
            .map { it.canonicalFile }
        selectedItems.forEach { item ->
            val target = runCatching { File(item.filePath).canonicalFile }.getOrNull() ?: return@forEach
            val isAllowed = allowedRoots.any { root ->
                target == root || target.path.startsWith(root.path + File.separator)
            }
            if (!isAllowed || target == context.filesDir.canonicalFile) return@forEach
            if (!target.exists()) return@forEach
            val before = if (target.isDirectory) calculateDirSize(target) else target.length().coerceAtLeast(0L)
            val deleted = runCatching { target.deleteRecursively() }.getOrDefault(false)
            if (deleted && !target.exists()) cleaned += before
        }
        cleaned
    }

    private fun addCacheItem(
        items: MutableList<JunkCategoryItem>,
        dir: File?,
        id: String,
        label: String,
        category: JunkCategoryType
    ) {
        if (dir == null || !dir.exists()) return
        val size = calculateDirSize(dir)
        if (size <= 0L) return
        items.add(JunkCategoryItem(id, category, label, size, dir.absolutePath))
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        return (dir.listFiles() ?: return 0L).sumOf {
            if (it.isDirectory) calculateDirSize(it) else it.length().coerceAtLeast(0L)
        }
    }
}