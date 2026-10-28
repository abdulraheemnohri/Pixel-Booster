package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticSampleDao {

    @Query("SELECT * FROM diagnostic_samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<DiagnosticSampleEntity>>

    @Query("SELECT * FROM diagnostic_samples WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    fun getSamplesSince(startTime: Long): Flow<List<DiagnosticSampleEntity>>

    @Query("SELECT * FROM diagnostic_samples WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    fun getSamplesBetween(startTime: Long, endTime: Long): Flow<List<DiagnosticSampleEntity>>

    @Query("SELECT * FROM diagnostic_samples ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSamples(limit: Int): Flow<List<DiagnosticSampleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: DiagnosticSampleEntity): Long

    @Query("DELETE FROM diagnostic_samples WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM diagnostic_samples")
    suspend fun clearAll(): Int

    @Query("SELECT COUNT(*) FROM diagnostic_samples")
    suspend fun getCount(): Int
}
