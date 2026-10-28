package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OptimizationLogDao {

    @Query("SELECT * FROM optimization_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<OptimizationLogEntity>>

    @Query("SELECT * FROM optimization_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<OptimizationLogEntity>>

    @Query("SELECT * FROM optimization_logs WHERE boosterType = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<OptimizationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: OptimizationLogEntity): Long

    @Query("DELETE FROM optimization_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM optimization_logs")
    suspend fun clearAll(): Int

    @Query("SELECT COUNT(*) FROM optimization_logs")
    suspend fun getCount(): Int
}
