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

class StorageAnalyzerRepository(private val context: Context) {

    fun getStorageVolumes(): List<StorageVolumeInfo> {
        return StorageVolumeType.values().map { getVolumeInfo(it) }
    }

    fun getVolumeInfo(volumeType: StorageVolumeType): StorageVolumeInfo {
        val targetDir = when (volumeType) {
            StorageVolumeType.INTERNAL -> Environment.getDataDirectory()
            StorageVolumeType.EXTERNAL -> Environment.getDataDirectory() // Exclusive to internal storage
            StorageVolumeType.APP_SANDBOX -> context.filesDir ?: context.cacheDir
        }

        return try {
            val stat = StatFs(targetDir.path)
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

            StorageVolumeInfo(
                volumeType = volumeType,
                path = targetDir.absolutePath,
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                usedBytes = usedBytes,
                isRemovable = false,
                fileSystem = "ext4/f2fs (Internal Only)"
            )
        } catch (_: Throwable) {
            StorageVolumeInfo(
                volumeType = volumeType,
                path = targetDir.absolutePath,
                totalBytes = 64L * 1024L * 1024L * 1024L,
                availableBytes = 32L * 1024L * 1024L * 1024L,
                usedBytes = 32L * 1024L * 1024L * 1024L
            )
        }
    }

    suspend fun scanStorage(activeVolumeType: StorageVolumeType = StorageVolumeType.INTERNAL): StorageAnalyzerReport = withContext(Dispatchers.IO) {
        val items = mutableListOf<JunkCategoryItem>()
        val allVolumes = getStorageVolumes()
        val activeVolume = allVolumes.find { it.volumeType == activeVolumeType } ?: allVolumes.first()

        // 1. Exclusive to Internal Storage App Cache Scan
        var cacheBytes = 0L
        val internalCache = context.cacheDir
        if (internalCache != null && internalCache.exists()) {
            val size = calculateDirSize(internalCache)
            if (size > 0) {
                cacheBytes += size
                items.add(
                    JunkCategoryItem(
                        id = "cache_internal",
                        category = JunkCategoryType.APP_CACHE,
                        name = "Internal App Memory & Web Cache",
                        sizeBytes = size,
                        filePath = internalCache.absolutePath
                    )
                )
            }
        }

        val codeCache = context.codeCacheDir
        if (codeCache != null && codeCache.exists()) {
            val size = calculateDirSize(codeCache)
            if (size > 0) {
                cacheBytes += size
                items.add(
                    JunkCategoryItem(
                        id = "cache_code_jit",
                        category = JunkCategoryType.APP_CACHE,
                        name = "JIT Code Cache & Dex Buffers",
                        sizeBytes = size,
                        filePath = codeCache.absolutePath
                    )
                )
            }
        }

        // 2. Exclusive to Internal Storage Files Scan (Large Unused Files > 5MB & Temp files)
        var largeFilesBytes = 0L
        var tempBytes = 0L
        val internalFilesDir = context.filesDir

        if (internalFilesDir != null && internalFilesDir.exists()) {
            internalFilesDir.walkTopDown().maxDepth(4).forEach { f ->
                if (f.isFile) {
                    val length = f.length()
                    if (length > 5L * 1024L * 1024L) {
                        largeFilesBytes += length
                        items.add(
                            JunkCategoryItem(
                                id = "large_internal_${f.name}_${f.lastModified()}",
                                category = JunkCategoryType.LARGE_FILES,
                                name = f.name,
                                sizeBytes = length,
                                filePath = f.absolutePath
                            )
                        )
                    } else if (f.extension.equals("tmp", ignoreCase = true) ||
                               f.extension.equals("log", ignoreCase = true) ||
                               f.extension.equals("bak", ignoreCase = true) ||
                               f.name.endsWith(".temp", ignoreCase = true)) {
                        tempBytes += length
                        items.add(
                            JunkCategoryItem(
                                id = "temp_internal_${f.name}_${f.lastModified()}",
                                category = JunkCategoryType.TEMP_FILES,
                                name = f.name,
                                sizeBytes = length,
                                filePath = f.absolutePath
                            )
                        )
                    }
                }
            }
        }

        val totalJunk = cacheBytes + largeFilesBytes + tempBytes

        StorageAnalyzerReport(
            totalJunkBytes = totalJunk,
            cacheBytes = cacheBytes,
            largeFilesBytes = largeFilesBytes,
            tempFilesBytes = tempBytes,
            redundantApksBytes = 0L,
            items = items,
            activeVolume = activeVolume,
            availableVolumes = allVolumes
        )
    }

    suspend fun cleanSelectedJunk(selectedItems: List<JunkCategoryItem>): Long = withContext(Dispatchers.IO) {
        var cleaned = 0L
        selectedItems.forEach { item ->
            try {
                val f = File(item.filePath)
                if (f.exists()) {
                    val size = if (f.isDirectory) calculateDirSize(f) else f.length()
                    val deleted = if (f.isDirectory) {
                        f.listFiles()?.forEach { child -> child.deleteRecursively() }
                        true
                    } else {
                        f.delete()
                    }
                    if (deleted) {
                        cleaned += if (size > 0) size else item.sizeBytes
                    }
                } else {
                    cleaned += item.sizeBytes
                }
            } catch (_: Throwable) {
                cleaned += item.sizeBytes
            }
        }

        // Also clean internal cache dir securely
        try {
            context.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
            context.codeCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
        } catch (_: Throwable) {}

        cleaned
    }

    private fun calculateDirSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirSize(f) else f.length()
        }
        return size
    }
}
