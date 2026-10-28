package com.example.core.database

import com.example.core.model.DiagnosticSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DiagnosticHistoryRepository(
    private val sampleDao: DiagnosticSampleDao
) {
    val allSamples: Flow<List<DiagnosticSampleEntity>> = sampleDao.getAllSamples()

    fun getSamplesSince(startTime: Long): Flow<List<DiagnosticSampleEntity>> {
        return sampleDao.getSamplesSince(startTime)
    }

    suspend fun recordSnapshot(snapshot: DiagnosticSnapshot): Long = withContext(Dispatchers.IO) {
        val entity = DiagnosticSampleEntity.fromSnapshot(snapshot)
        sampleDao.insertSample(entity)
    }

    suspend fun pruneOlderThanDays(days: Int): Int = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - (days.toLong() * 24L * 60L * 60L * 1000L)
        sampleDao.deleteOlderThan(cutoff)
    }

    suspend fun clearHistory(): Int = withContext(Dispatchers.IO) {
        sampleDao.clearAll()
    }

    suspend fun getSampleCount(): Int = withContext(Dispatchers.IO) {
        sampleDao.getCount()
    }
}
