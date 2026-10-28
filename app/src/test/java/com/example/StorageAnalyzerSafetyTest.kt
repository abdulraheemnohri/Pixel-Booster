package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.JunkCategoryItem
import com.example.core.model.JunkCategoryType
import com.example.core.storage.StorageAnalyzerRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StorageAnalyzerSafetyTest {
    @Test
    fun `cleaner refuses app private files and only removes app cache files`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = StorageAnalyzerRepository(context)
        val privateFile = File(context.filesDir, "must-keep-user-data.txt").apply { writeText("important") }
        val cacheFile = File(context.cacheDir, "disposable-cache.tmp").apply { writeText("temporary cache") }

        val privateItem = JunkCategoryItem(
            id = "private", category = JunkCategoryType.LARGE_FILES,
            name = privateFile.name, sizeBytes = privateFile.length(), filePath = privateFile.absolutePath
        )
        val privateCleaned = repository.cleanSelectedJunk(listOf(privateItem))
        assertEquals(0L, privateCleaned)
        assertTrue("App-private user data must remain", privateFile.exists())

        val cacheItem = JunkCategoryItem(
            id = "cache", category = JunkCategoryType.APP_CACHE,
            name = cacheFile.name, sizeBytes = cacheFile.length(), filePath = cacheFile.absolutePath
        )
        val cacheCleaned = repository.cleanSelectedJunk(listOf(cacheItem))
        assertTrue("Cache cleanup should report actual bytes removed", cacheCleaned > 0L)
        assertFalse("Selected disposable cache file should be removed", cacheFile.exists())
        assertTrue("Private file must still exist after cache cleanup", privateFile.exists())

        // A stale or manually crafted selection must not delete the cache directory itself.
        val cacheRootItem = JunkCategoryItem(
            id = "cache-root", category = JunkCategoryType.APP_CACHE,
            name = "Cache root", sizeBytes = 0L, filePath = context.cacheDir.absolutePath
        )
        repository.cleanSelectedJunk(listOf(cacheRootItem))
        assertTrue("The app cache root directory must remain available", context.cacheDir.exists())
        assertTrue("Private file must remain untouched", privateFile.exists())
    }
}
