package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.core.model.BatteryChargingState
import com.example.core.model.DiagnosticSnapshot

@Entity(tableName = "diagnostic_samples")
data class DiagnosticSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val availableRamBytes: Long,
    val totalRamBytes: Long,
    val freeStorageBytes: Long,
    val totalStorageBytes: Long,
    val batteryPercentage: Int,
    val batteryTemperatureCelsius: Float?,
    val chargingState: String,
    val thermalStatusCode: Int,
    val thermalStatusName: String,
    val sourceMetadata: String = "Pixel Diagnostic Engine"
) {
    val usedRamPercentage: Float
        get() = if (totalRamBytes > 0) {
            (((totalRamBytes - availableRamBytes).toFloat() / totalRamBytes.toFloat()) * 100f).coerceIn(0f, 100f)
        } else 0f

    val usedStorageGb: Float
        get() = ((totalStorageBytes - freeStorageBytes).toFloat() / (1024f * 1024f * 1024f)).coerceAtLeast(0f)

    companion object {
        fun fromSnapshot(snapshot: DiagnosticSnapshot): DiagnosticSampleEntity {
            return DiagnosticSampleEntity(
                timestamp = snapshot.timestamp,
                availableRamBytes = snapshot.memory.availableRamBytes,
                totalRamBytes = snapshot.memory.totalRamBytes,
                freeStorageBytes = snapshot.storage.freeBytes,
                totalStorageBytes = snapshot.storage.totalBytes,
                batteryPercentage = snapshot.battery.levelPercentage,
                batteryTemperatureCelsius = snapshot.battery.temperatureCelsius,
                chargingState = snapshot.battery.state.name,
                thermalStatusCode = snapshot.thermal.statusCode,
                thermalStatusName = snapshot.thermal.statusName,
                sourceMetadata = "${snapshot.deviceSpecs.model} - ${snapshot.deviceSpecs.pixelGenerationName}"
            )
        }
    }
}
