package com.example

import com.example.core.health.HealthEngine
import com.example.core.model.BatteryChargingState
import com.example.core.model.BatteryInfoData
import com.example.core.model.BatteryPluggedSource
import com.example.core.model.FindingSeverity
import com.example.core.model.MemoryInfoData
import com.example.core.model.SettingsData
import com.example.core.model.StorageInfoData
import com.example.core.model.ThermalSeverityLevel
import com.example.core.model.ThermalStatusData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthEngineTest {

    private val engine = HealthEngine()
    private val defaultSettings = SettingsData()

    @Test
    fun `critical storage warning triggered below 10 percent`() {
        val storage = StorageInfoData(
            totalBytes = 100_000_000_000L,
            freeBytes = 5_000_000_000L // 5% free
        )
        val mem = MemoryInfoData(8_000_000_000L, 4_000_000_000L, 1_000_000_000L, false)
        val battery = BatteryInfoData(80, BatteryChargingState.DISCHARGING, BatteryPluggedSource.NONE, 28f, 3900, "Good", "Li-ion")
        val thermal = ThermalStatusData(0, "Nominal", ThermalSeverityLevel.NOMINAL, "Normal", "OK")

        val findings = engine.evaluate(mem, storage, battery, thermal, defaultSettings)

        val storageFinding = findings.firstOrNull { it.id == "storage_critical" }
        assertTrue("Expected critical storage finding", storageFinding != null)
        assertEquals(FindingSeverity.CRITICAL, storageFinding?.severity)
    }

    @Test
    fun `storage warning triggered between 10 and 20 percent`() {
        val storage = StorageInfoData(
            totalBytes = 100_000_000_000L,
            freeBytes = 15_000_000_000L // 15% free
        )
        val mem = MemoryInfoData(8_000_000_000L, 4_000_000_000L, 1_000_000_000L, false)
        val battery = BatteryInfoData(80, BatteryChargingState.DISCHARGING, BatteryPluggedSource.NONE, 28f, 3900, "Good", "Li-ion")
        val thermal = ThermalStatusData(0, "Nominal", ThermalSeverityLevel.NOMINAL, "Normal", "OK")

        val findings = engine.evaluate(mem, storage, battery, thermal, defaultSettings)

        val storageFinding = findings.firstOrNull { it.id == "storage_warning" }
        assertTrue("Expected storage warning", storageFinding != null)
        assertEquals(FindingSeverity.WARNING, storageFinding?.severity)
    }

    @Test
    fun `battery critical triggered below 15 percent discharging`() {
        val storage = StorageInfoData(100_000_000_000L, 50_000_000_000L)
        val mem = MemoryInfoData(8_000_000_000L, 4_000_000_000L, 1_000_000_000L, false)
        val battery = BatteryInfoData(12, BatteryChargingState.DISCHARGING, BatteryPluggedSource.NONE, 28f, 3600, "Good", "Li-ion")
        val thermal = ThermalStatusData(0, "Nominal", ThermalSeverityLevel.NOMINAL, "Normal", "OK")

        val findings = engine.evaluate(mem, storage, battery, thermal, defaultSettings)

        val batteryFinding = findings.firstOrNull { it.id == "battery_critical" }
        assertTrue("Expected critical battery finding", batteryFinding != null)
        assertEquals(FindingSeverity.CRITICAL, batteryFinding?.severity)
    }

    @Test
    fun `high battery temperature warning triggered at 42C`() {
        val storage = StorageInfoData(100_000_000_000L, 50_000_000_000L)
        val mem = MemoryInfoData(8_000_000_000L, 4_000_000_000L, 1_000_000_000L, false)
        val battery = BatteryInfoData(60, BatteryChargingState.CHARGING, BatteryPluggedSource.AC, 42.5f, 4100, "Good", "Li-ion")
        val thermal = ThermalStatusData(0, "Nominal", ThermalSeverityLevel.NOMINAL, "Normal", "OK")

        val findings = engine.evaluate(mem, storage, battery, thermal, defaultSettings)

        val tempFinding = findings.firstOrNull { it.id == "battery_temp_warning" }
        assertTrue("Expected elevated temperature warning", tempFinding != null)
        assertEquals(FindingSeverity.WARNING, tempFinding?.severity)
    }

    @Test
    fun `critical battery temperature triggered at 46C`() {
        val storage = StorageInfoData(100_000_000_000L, 50_000_000_000L)
        val mem = MemoryInfoData(8_000_000_000L, 4_000_000_000L, 1_000_000_000L, false)
        val battery = BatteryInfoData(60, BatteryChargingState.CHARGING, BatteryPluggedSource.AC, 46.5f, 4100, "Good", "Li-ion")
        val thermal = ThermalStatusData(0, "Nominal", ThermalSeverityLevel.NOMINAL, "Normal", "OK")

        val findings = engine.evaluate(mem, storage, battery, thermal, defaultSettings)

        val tempFinding = findings.firstOrNull { it.id == "battery_temp_critical" }
        assertTrue("Expected critical temperature finding", tempFinding != null)
        assertEquals(FindingSeverity.CRITICAL, tempFinding?.severity)
    }

    @Test
    fun `severe thermal status triggers critical finding`() {
        val storage = StorageInfoData(100_000_000_000L, 50_000_000_000L)
        val mem = MemoryInfoData(8_000_000_000L, 4_000_000_000L, 1_000_000_000L, false)
        val battery = BatteryInfoData(80, BatteryChargingState.DISCHARGING, BatteryPluggedSource.NONE, 30f, 3900, "Good", "Li-ion")
        val thermal = ThermalStatusData(3, "Severe", ThermalSeverityLevel.CRITICAL, "High thermal pressure", "Cooldown")

        val findings = engine.evaluate(mem, storage, battery, thermal, defaultSettings)

        val thermalFinding = findings.firstOrNull { it.id == "thermal_critical" }
        assertTrue("Expected critical thermal finding", thermalFinding != null)
        assertEquals(FindingSeverity.CRITICAL, thermalFinding?.severity)
    }
}
