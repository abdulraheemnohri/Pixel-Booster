package com.example.core.database

import kotlinx.coroutines.flow.Flow

class OptimizationRepository(private val dao: OptimizationLogDao) {

    val allLogs: Flow<List<OptimizationLogEntity>> = dao.getAllLogs()

    fun getRecentLogs(limit: Int = 10): Flow<List<OptimizationLogEntity>> {
        return dao.getRecentLogs(limit)
    }

    suspend fun recordLog(
        boosterType: String,
        title: String,
        details: String,
        metricFreed: String,
        success: Boolean = true
    ): Long {
        val entity = OptimizationLogEntity(
            timestamp = System.currentTimeMillis(),
            boosterType = boosterType,
            title = title,
            details = details,
            metricFreed = metricFreed,
            success = success
        )
        return dao.insertLog(entity)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
