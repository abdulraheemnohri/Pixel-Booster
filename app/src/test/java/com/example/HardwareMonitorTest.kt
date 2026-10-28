package com.example

import android.content.Context
import android.os.PowerManager
import androidx.test.core.app.ApplicationProvider
import com.example.core.booster.BoosterService
import com.example.core.model.BatteryChargingState
import com.example.core.model.NetworkTelemetryData
import com.example.core.model.ThermalSeverityLevel
import com.example.core.platform.DeviceDiagnosticsProvider
import com.example.core.platform.HardwareMonitorService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HardwareMonitorTest {

    @Test
    fun `test thermal evaluation thresholds and overheating warning`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val boosterService = BoosterService(context)
        val diagnosticsProvider = DeviceDiagnosticsProvider(context)
        val monitorService = HardwareMonitorService(context, boosterService, diagnosticsProvider)

        // 1. Nominal temperature (< 38°C)
        val nominal = monitorService.evaluateThermals(
            currentTemp = 32.5f,
            powerManagerStatus = PowerManager.THERMAL_STATUS_NONE
        )
        assertEquals(ThermalSeverityLevel.NOMINAL, nominal.severity)
        assertFalse(nominal.isOverheating)
        assertEquals("Nominal", nominal.statusName)
        assertEquals(null, nominal.warningMessage)

        // 2. Elevated temperature (38°C - 42°C)
        val elevated = monitorService.evaluateThermals(
            currentTemp = 39.8f,
            powerManagerStatus = PowerManager.THERMAL_STATUS_LIGHT
        )
        assertEquals(ThermalSeverityLevel.WARNING, elevated.severity)
        assertFalse(elevated.isOverheating)
        assertEquals("Elevated", elevated.statusName)

        // 3. Overheating warning threshold (42°C - 47°C)
        val overheating = monitorService.evaluateThermals(
            currentTemp = 43.6f,
            powerManagerStatus = PowerManager.THERMAL_STATUS_MODERATE
        )
        assertEquals(ThermalSeverityLevel.WARNING, overheating.severity)
        assertTrue(overheating.isOverheating)
        assertEquals("Overheating Warning", overheating.statusName)
        assertNotNull(overheating.warningMessage)
        assertTrue(overheating.warningMessage!!.contains("elevated"))

        // 4. Critical Throttling threshold (>= 47°C)
        val critical = monitorService.evaluateThermals(
            currentTemp = 48.2f,
            powerManagerStatus = PowerManager.THERMAL_STATUS_SEVERE
        )
        assertEquals(ThermalSeverityLevel.CRITICAL, critical.severity)
        assertTrue(critical.isOverheating)
        assertEquals("Critical Throttle", critical.statusName)
        assertNotNull(critical.warningMessage)
        assertTrue(critical.warningMessage!!.contains("Extreme thermal rise"))
    }

    @Test
    fun `test battery health capacity and charging telemetry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val boosterService = BoosterService(context)
        val diagnosticsProvider = DeviceDiagnosticsProvider(context)
        val monitorService = HardwareMonitorService(context, boosterService, diagnosticsProvider)

        val batteryHealth = monitorService.sampleBatteryHealthTelemetry()
        assertNotNull(batteryHealth)
        assertTrue(batteryHealth.levelPercentage in 0..100)
        assertTrue(batteryHealth.designedCapacityMah >= 0)
        assertTrue(batteryHealth.currentCapacityMah >= 0)
        assertTrue(batteryHealth.healthPercentage in 0f..100f)
        if (batteryHealth.designedCapacityMah == 0) assertEquals("Unavailable", batteryHealth.formattedDesignedCapacity)
        assertTrue(batteryHealth.healthGrade.isNotBlank())
        assertNotNull(batteryHealth.chargingState)
        assertNotNull(batteryHealth.pluggedSource)
        assertTrue(batteryHealth.formattedCurrentCapacity.isNotBlank())
        assertTrue(batteryHealth.formattedDesignedCapacity.isNotBlank())
    }

    @Test
    fun `test processor telemetry with cpu gpu and tpu`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val boosterService = BoosterService(context)
        val diagnosticsProvider = DeviceDiagnosticsProvider(context)
        val monitorService = HardwareMonitorService(context, boosterService, diagnosticsProvider)

        val processor = monitorService.sampleProcessorTelemetry()
        assertNotNull(processor)
        assertTrue(processor.overallCpuUsage == -1f || processor.overallCpuUsage in 0f..100f) // unavailable is valid when /proc/stat cannot be read.
        assertTrue(processor.coreCount >= 4)
        assertEquals(processor.coreCount, processor.coreLoads.size)
        assertEquals(-1f, processor.gpuUsage, 0f) // Unavailable rather than fabricated GPU telemetry.
        assertTrue(processor.gpuEngineName.isNotBlank())
        assertEquals(-1f, processor.tpuUsage, 0f) // Unavailable rather than fabricated TPU telemetry.
        assertTrue(processor.tpuTopsRating.contains("not verified"))
        assertTrue(processor.tpuState.isNotBlank())
    }

    @Test
    fun `test network speed calculations and speed formatting`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val boosterService = BoosterService(context)
        val diagnosticsProvider = DeviceDiagnosticsProvider(context)
        val monitorService = HardwareMonitorService(context, boosterService, diagnosticsProvider)

        val network = monitorService.sampleNetworkTelemetry()
        assertNotNull(network)
        assertTrue(network.downloadSpeedBytesPerSec >= 0L)
        assertTrue(network.uploadSpeedBytesPerSec >= 0L)
        assertTrue(network.connectionType.isNotBlank())
        assertEquals(-1L, network.latencyMs) // This sampler does not run a latency probe.

        // Speed formatting helper test
        assertEquals("0 B/s", NetworkTelemetryData.formatSpeed(0L))
        assertEquals("500 B/s", NetworkTelemetryData.formatSpeed(500L))
        assertEquals("50 KB/s", NetworkTelemetryData.formatSpeed(50L * 1024L))
        assertEquals("2.50 MB/s", NetworkTelemetryData.formatSpeed((2.5 * 1024.0 * 1024.0).toLong()))
    }

    @Test
    fun `test complete hardware telemetry aggregation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val boosterService = BoosterService(context)
        val diagnosticsProvider = DeviceDiagnosticsProvider(context)
        val monitorService = HardwareMonitorService(context, boosterService, diagnosticsProvider)

        val telemetry = monitorService.sampleCompleteTelemetry()
        assertNotNull(telemetry)
        assertNotNull(telemetry.thermal)
        assertNotNull(telemetry.batteryHealth)
        assertNotNull(telemetry.processor)
        assertNotNull(telemetry.network)
        assertNotNull(telemetry.memory)
        assertNotNull(telemetry.storage)
        assertTrue(telemetry.systemVitalityScore in 0..100)
    }
}
