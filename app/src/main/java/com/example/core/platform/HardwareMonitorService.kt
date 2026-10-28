package com.example.core.platform

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.example.core.booster.BoosterService
import com.example.core.model.BatteryChargingState
import com.example.core.model.BatteryHealthData
import com.example.core.model.BatteryPluggedSource
import com.example.core.model.CompleteHardwareTelemetry
import com.example.core.model.NetworkTelemetryData
import com.example.core.model.ProcessorTelemetryData
import com.example.core.model.ThermalSeverityLevel
import com.example.core.model.ThermalTelemetryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.roundToInt

import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import java.net.Inet4Address
import java.net.NetworkInterface

class HardwareMonitorService(
    private val context: Context,
    private val boosterService: BoosterService,
    private val diagnosticsProvider: DeviceDiagnosticsProvider
) {
    private val batteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager


    private val scope = CoroutineScope(Dispatchers.Default)
    private var samplingJob: Job? = null

    // Network tracking state
    private var lastRxBytes: Long = TrafficStats.getTotalRxBytes()
    private var lastTxBytes: Long = TrafficStats.getTotalTxBytes()
    private var lastNetworkSampleTime: Long = SystemClock.elapsedRealtime()
    private var sessionTotalRx: Long = 0L
    private var sessionTotalTx: Long = 0L

    // CPU tracking state (/proc/stat jiffies)
    private var lastCpuTotal: Long = 0L
    private var lastCpuIdle: Long = 0L

    // Thermals tracking state
    private val temperatureHistory = mutableListOf<Float>()
    private var peakRecordedTemp: Float = 0f

    private val _telemetryFlow = MutableStateFlow<CompleteHardwareTelemetry>(sampleInitialTelemetry())
    val telemetryFlow: StateFlow<CompleteHardwareTelemetry> = _telemetryFlow.asStateFlow()

    init {
        startLiveSampling()
    }

    fun startLiveSampling(intervalMillis: Long = 1500L) {
        samplingJob?.cancel()
        samplingJob = scope.launch {
            while (isActive) {
                try {
                    val sample = sampleCompleteTelemetry()
                    _telemetryFlow.value = sample
                } catch (_: Throwable) {}
                delay(intervalMillis)
            }
        }
    }

    fun stopSampling() {
        samplingJob?.cancel()
        samplingJob = null
    }

    fun refreshNow() {
        scope.launch {
            try {
                _telemetryFlow.value = sampleCompleteTelemetry()
            } catch (_: Throwable) {}
        }
    }

    private fun sampleInitialTelemetry(): CompleteHardwareTelemetry {
        val thermal = sampleThermalTelemetry()
        val battery = sampleBatteryHealthTelemetry()
        val processor = sampleProcessorTelemetry()
        val network = NetworkTelemetryData(
            downloadSpeedBytesPerSec = 0L,
            uploadSpeedBytesPerSec = 0L,
            sessionTotalRxBytes = 0L,
            sessionTotalTxBytes = 0L,
            connectionType = determineConnectionType(),
            latencyMs = -1L, // Initial state: no latency probe has run.
            isConnected = connectivityManager?.activeNetwork != null
        )
        val memory = boosterService.getLiveRamMetrics()
        val storage = diagnosticsProvider.getStorageInfo()
        val vitality = calculateVitalityScore(thermal, battery, memory.usedPercentage)

        return CompleteHardwareTelemetry(
            thermal = thermal,
            batteryHealth = battery,
            processor = processor,
            network = network,
            memory = memory,
            storage = storage,
            systemVitalityScore = vitality
        )
    }

    fun sampleCompleteTelemetry(): CompleteHardwareTelemetry {
        val thermal = sampleThermalTelemetry()
        val battery = sampleBatteryHealthTelemetry()
        val processor = sampleProcessorTelemetry()
        val network = sampleNetworkTelemetry()
        val memory = boosterService.getLiveRamMetrics()
        val storage = diagnosticsProvider.getStorageInfo()
        val vitality = calculateVitalityScore(thermal, battery, memory.usedPercentage)

        return CompleteHardwareTelemetry(
            thermal = thermal,
            batteryHealth = battery,
            processor = processor,
            network = network,
            memory = memory,
            storage = storage,
            systemVitalityScore = vitality
        )
    }

    // 1. THERMAL TELEMETRY
    fun sampleThermalTelemetry(): ThermalTelemetryData {
        val batteryIntent = try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Throwable) {
            null
        }

        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        // Zero means unavailable; never substitute a made-up temperature.
        val batteryTemp = if (rawTemp > 0) rawTemp / 10.0f else 0f

        val (socTemp, activeZones) = readSocThermalZones(batteryTemp)
        val currentTemp = maxOf(batteryTemp, socTemp)

        if (currentTemp > peakRecordedTemp) {
            peakRecordedTemp = currentTemp
        }

        synchronized(temperatureHistory) {
            temperatureHistory.add(currentTemp)
            if (temperatureHistory.size > 20) {
                temperatureHistory.removeAt(0)
            }
        }

        // Hardware thermal status via PowerManager
        val powerManagerStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            powerManager.currentThermalStatus
        } else {
            PowerManager.THERMAL_STATUS_NONE
        }

        val pmStatusName = when (powerManagerStatus) {
            PowerManager.THERMAL_STATUS_NONE -> "Nominal"
            PowerManager.THERMAL_STATUS_LIGHT -> "Light Throttle"
            PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Throttle"
            PowerManager.THERMAL_STATUS_SEVERE -> "Severe Mitigation"
            PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Throttle"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Cool-Down"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "Thermal Shutdown"
            else -> "Nominal"
        }

        val evaluation = if (currentTemp <= 0f && powerManagerStatus == PowerManager.THERMAL_STATUS_NONE) {
            ThermalEvaluation(
                severity = ThermalSeverityLevel.NOMINAL,
                isOverheating = false,
                statusName = "Sensor unavailable",
                warningMessage = null,
                coolingAdvice = "Temperature sensor readings are not exposed to this app on this device."
            )
        } else {
            evaluateThermals(currentTemp = currentTemp, powerManagerStatus = powerManagerStatus)
        }
        val severity = evaluation.severity
        val isOverheating = evaluation.isOverheating
        val statusName = evaluation.statusName
        val warningMsg = evaluation.warningMessage
        val advice = evaluation.coolingAdvice

        return ThermalTelemetryData(
            currentTempCelsius = currentTemp,
            peakTempCelsius = peakRecordedTemp,
            statusName = statusName,
            severityLevel = severity,
            isOverheating = isOverheating,
            warningMessage = warningMsg,
            coolingAdvice = advice,
            temperatureHistory = temperatureHistory.toList(),
            socTempCelsius = socTemp,
            batteryTempCelsius = batteryTemp,
            activeThermalZones = activeZones,
            powerManagerStatusName = pmStatusName
        )
    }

    private fun readSocThermalZones(defaultTemp: Float): Pair<Float, Int> {
        var maxZoneTemp = defaultTemp
        var zoneCount = 0

        for (i in 0..15) {
            val file = java.io.File("/sys/class/thermal/thermal_zone$i/temp")
            if (file.exists() && file.canRead()) {
                zoneCount++
                try {
                    val raw = file.readText().trim().toFloatOrNull() ?: continue
                    val deg = if (raw > 1000f) raw / 1000f else raw
                    if (deg in 15f..105f && deg > maxZoneTemp) {
                        maxZoneTemp = deg
                    }
                } catch (_: Throwable) {}
            }
        }

        return Pair(maxZoneTemp, zoneCount)
    }

    fun evaluateThermals(
        currentTemp: Float,
        powerManagerStatus: Int
    ): ThermalEvaluation {
        val isHardwareThrottling = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            powerManagerStatus >= PowerManager.THERMAL_STATUS_SEVERE

        return when {
            currentTemp >= 47.0f || powerManagerStatus >= PowerManager.THERMAL_STATUS_CRITICAL -> {
                ThermalEvaluation(
                    severity = ThermalSeverityLevel.CRITICAL,
                    isOverheating = true,
                    statusName = "Critical Throttle",
                    warningMessage = "Extreme thermal rise detected (>47°C). Processor clock rates are throttled to safeguard battery chemistry.",
                    coolingAdvice = "Cease 3D rendering or high-framerate camera recording. Disconnect fast chargers and allow natural convection cooling."
                )
            }
            currentTemp >= 42.0f || isHardwareThrottling -> {
                ThermalEvaluation(
                    severity = ThermalSeverityLevel.WARNING,
                    isOverheating = true,
                    statusName = "Overheating Warning",
                    warningMessage = "Device temperature is elevated (>42°C). Thermal mitigation active.",
                    coolingAdvice = "Remove heavy cases, avoid direct sunlight, pause demanding tasks, and unplug charging if the device is unusually hot."
                )
            }
            currentTemp >= 38.0f || powerManagerStatus >= PowerManager.THERMAL_STATUS_LIGHT -> {
                ThermalEvaluation(
                    severity = ThermalSeverityLevel.WARNING,
                    isOverheating = false,
                    statusName = "Elevated",
                    warningMessage = "Temperature is mildly elevated (38°C–42°C).",
                    coolingAdvice = "Normal under prolonged gaming or turbo charging. Keep vents and chassis unobstructed."
                )
            }
            else -> {
                ThermalEvaluation(
                    severity = ThermalSeverityLevel.NOMINAL,
                    isOverheating = false,
                    statusName = "Nominal",
                    warningMessage = null,
                    coolingAdvice = "No elevated thermal status reported by Android; temperature sensor availability may vary."
                )
            }
        }
    }

    data class ThermalEvaluation(
        val severity: ThermalSeverityLevel,
        val isOverheating: Boolean,
        val statusName: String,
        val warningMessage: String?,
        val coolingAdvice: String
    )

    // 2. BATTERY HEALTH & CAPACITY TELEMETRY
    fun sampleBatteryHealthTelemetry(): BatteryHealthData {
        val batteryIntent = try {
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Throwable) {
            null
        }

        val rawLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (rawLevel >= 0 && scale > 0) {
            ((rawLevel.toFloat() / scale.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
        } else 0

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val chargingState = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> BatteryChargingState.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> BatteryChargingState.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> BatteryChargingState.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryChargingState.NOT_CHARGING
            else -> BatteryChargingState.UNKNOWN
        }

        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val pluggedSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> BatteryPluggedSource.AC
            BatteryManager.BATTERY_PLUGGED_USB -> BatteryPluggedSource.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryPluggedSource.WIRELESS
            else -> BatteryPluggedSource.NONE
        }

        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 } ?: 0
        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.takeIf { it > 0 } ?: 0
        val tempCelsius = rawTemp / 10.0f
        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        // Designed capacity calculation based on device profile
        val model = Build.MODEL ?: ""
        val designedCapacity = resolveDesignedCapacityMah()

        // Read real hardware charge counter if accessible
        var currentMah = -1
        var currentFlowMa = 0
        if (batteryManager != null) {
            val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            if (chargeCounterUah > 0) {
                currentMah = chargeCounterUah / 1000
            }
            val currentNowUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            if (currentNowUa != Int.MIN_VALUE && currentNowUa != 0) {
                currentFlowMa = currentNowUa / 1000
            }
        }

        // If hardware charge counter unavailable, derive from level percentage and design
        if (currentMah <= 0 || currentMah > designedCapacity * 1.2) {
            currentMah = 0
        }

        // Read sysfs full charge vs design for accurate battery health wear calculation
        val sysfsHealthRatio = readSysfsBatteryHealthRatio()
        val healthRatio = sysfsHealthRatio
        val healthPercentage = healthRatio?.let { (it * 100f).coerceIn(0f, 100f) } ?: 0f
        val capacityPercentage = if (currentMah > 0 && designedCapacity > 0) ((currentMah.toFloat() / designedCapacity.toFloat()) * 100f).coerceIn(0f, 100f) else 0f

        val healthGrade = if (healthRatio == null) "Unavailable (sensor not exposed)" else when {
            healthPercentage >= 95f -> "Optimal (A+)"
            healthPercentage >= 90f -> "Good (A)"
            healthPercentage >= 82f -> "Fair (B)"
            else -> "Degraded"
        }

        // Real Charge Cycles tracking
        val (chargeCycles, cyclesSource) = resolveRealChargeCycles(batteryIntent, percentage, chargingState)

        // Estimated charging wattage
        val wattage = if (voltageMv > 0 && currentFlowMa != 0) {
            voltageMv.toFloat() * kotlin.math.abs(currentFlowMa).toFloat() / 1_000_000f
        } else null

        return BatteryHealthData(
            levelPercentage = percentage,
            currentCapacityMah = currentMah,
            designedCapacityMah = designedCapacity,
            healthPercentage = healthPercentage,
            healthGrade = healthGrade,
            chargingState = chargingState,
            pluggedSource = pluggedSource,
            voltageMilliVolts = voltageMv,
            estimatedWattage = wattage,
            technology = technology,
            temperatureCelsius = tempCelsius,
            chargeCyclesEstimated = chargeCycles,
            chargeCycles = chargeCycles,
            chargeCyclesSource = cyclesSource,
            currentFlowMa = currentFlowMa,
            capacityPercentage = capacityPercentage
        )
    }

    private fun readSysfsBatteryHealthRatio(): Float? {
        val chargeFullFiles = listOf(
            "/sys/class/power_supply/battery/charge_full",
            "/sys/class/power_supply/bms/charge_full"
        )
        val chargeDesignFiles = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/bms/charge_full_design"
        )

        for (i in chargeFullFiles.indices) {
            try {
                val fullF = java.io.File(chargeFullFiles[i])
                val designF = java.io.File(chargeDesignFiles[i])
                if (fullF.exists() && designF.exists()) {
                    val full = fullF.readText().trim().toFloatOrNull() ?: continue
                    val design = designF.readText().trim().toFloatOrNull() ?: continue
                    if (full > 0f && design > 0f) {
                        return (full / design).coerceIn(0f, 1.0f)
                    }
                }
            } catch (_: Throwable) {}
        }
        return null
    }

    private fun resolveRealChargeCycles(
        batteryIntent: Intent?,
        currentLevel: Int,
        chargingState: BatteryChargingState
    ): Pair<Int, String> {
        // 1. Android 14+ Power HAL BatteryManager.EXTRA_CYCLE_COUNT
        if (batteryIntent != null) {
            val extraCycles = if (Build.VERSION.SDK_INT >= 34) {
                batteryIntent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
            } else {
                batteryIntent.getIntExtra("android.os.extra.CYCLE_COUNT", -1)
            }
            if (extraCycles > 0) {
                return Pair(extraCycles, "Android 14+ Power HAL")
            }
        }

        // 2. Sysfs power supply hardware cycle registers
        val sysfsPaths = listOf(
            "/sys/class/power_supply/battery/cycle_count",
            "/sys/class/power_supply/bms/cycle_count",
            "/sys/class/power_supply/battery/device/cycle_count"
        )
        for (path in sysfsPaths) {
            try {
                val file = java.io.File(path)
                if (file.exists() && file.canRead()) {
                    val count = file.readText().trim().toIntOrNull()
                    if (count != null && count >= 0) {
                        return Pair(count, "Hardware BMS Register")
                    }
                }
            } catch (_: Throwable) {}
        }

        // 3. Persistent battery wear cycle tracker
        return Pair(0, "Unavailable (hardware counter not exposed)")
    }

    private fun resolveDesignedCapacityMah(): Int {
        val paths = listOf("/sys/class/power_supply/battery/charge_full_design", "/sys/class/power_supply/bms/charge_full_design")
        for (path in paths) {
            val value = runCatching { java.io.File(path).takeIf { it.canRead() }?.readText()?.trim()?.toLongOrNull() }.getOrNull()
            if (value != null && value > 0L) {
                val mah = if (value > 100_000L) value / 1_000L else value
                if (mah in 1L..20_000L) return mah.toInt()
            }
        }
        return 0
    }

    // 3. CPU, GPU, TPU TELEMETRY
    fun sampleProcessorTelemetry(): ProcessorTelemetryData {
        val cpuUsage = readCpuUsagePercent()
        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)

        // Core load distribution
        // Per-core utilization is not measured by this aggregate /proc/stat sampler.
        val coreLoads = List(coreCount) { -1f }

        // Estimated GPU usage (frame composition & graphics memory bandwidth)
        val gpuUsage = -1f // GPU utilization is not exposed by a supported public API.
        val gpuFps = 0 // FPS telemetry is unavailable; do not infer it from CPU load.

        // Google Tensor TPU / Neural Processing Engine Telemetry
        val tpuUsage = -1f // TPU/NPU utilization is not exposed by a supported public API.
        val tpuState = "Telemetry unavailable"

        return ProcessorTelemetryData(
            overallCpuUsage = cpuUsage,
            coreCount = coreCount,
            coreLoads = coreLoads,
            gpuUsage = gpuUsage,
            gpuEngineName = "GPU utilization unavailable",
            gpuFpsEstimate = gpuFps,
            tpuUsage = tpuUsage,
            tpuEngineName = "TPU/NPU utilization unavailable",
            tpuTopsRating = "TOPS rating not verified",
            tpuState = tpuState
        )
    }

    private fun readCpuUsagePercent(): Float {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()

            val tokens = load.split("\\s+".toRegex())
            if (tokens.size >= 8) {
                val user = tokens[1].toLong()
                val nice = tokens[2].toLong()
                val system = tokens[3].toLong()
                val idle = tokens[4].toLong()
                val iowait = tokens[5].toLong()
                val irq = tokens[6].toLong()
                val softirq = tokens[7].toLong()

                val currentTotal = user + nice + system + idle + iowait + irq + softirq
                val currentIdle = idle + iowait

                val deltaTotal = currentTotal - lastCpuTotal
                val deltaIdle = currentIdle - lastCpuIdle

                lastCpuTotal = currentTotal
                lastCpuIdle = currentIdle

                if (deltaTotal > 0) {
                    val usage = ((deltaTotal - deltaIdle).toFloat() / deltaTotal.toFloat()) * 100f
                    usage.coerceIn(0f, 100f)
                } else {
                    sampleFallbackCpuUsage()
                }
            } else {
                sampleFallbackCpuUsage()
            }
        } catch (_: Throwable) {
            sampleFallbackCpuUsage()
        }
    }

    private fun sampleFallbackCpuUsage(): Float = -1f // Unavailable sentinel; no synthetic estimate.

    // 4. INTERNET / NETWORK TELEMETRY
    fun sampleNetworkTelemetry(): NetworkTelemetryData {
        val currentRx = TrafficStats.getTotalRxBytes()
        val currentTx = TrafficStats.getTotalTxBytes()
        val currentTime = SystemClock.elapsedRealtime()

        val timeDeltaSeconds = ((currentTime - lastNetworkSampleTime) / 1000.0).coerceAtLeast(0.5)

        val rxDelta = if (lastRxBytes > 0 && currentRx >= lastRxBytes) currentRx - lastRxBytes else 0L
        val txDelta = if (lastTxBytes > 0 && currentTx >= lastTxBytes) currentTx - lastTxBytes else 0L

        val rxSpeed = (rxDelta / timeDeltaSeconds).toLong()
        val txSpeed = (txDelta / timeDeltaSeconds).toLong()

        sessionTotalRx += rxDelta
        sessionTotalTx += txDelta

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastNetworkSampleTime = currentTime

        val connectionType = determineConnectionType()
        val isConnected = connectionType != "Disconnected"

        val (localIp, ipv6Addr, activeInterface) = resolveLocalNetworkAddresses()
        val (wifiSsid, rssiDbm, signalPercent, freqMhz, linkSpeed) = sampleWifiInfo()
        val (carrier, netType) = sampleCellularInfo()

        return NetworkTelemetryData(
            downloadSpeedBytesPerSec = rxSpeed,
            uploadSpeedBytesPerSec = txSpeed,
            sessionTotalRxBytes = sessionTotalRx.coerceAtLeast(0L),
            sessionTotalTxBytes = sessionTotalTx.coerceAtLeast(0L),
            connectionType = connectionType,
            latencyMs = -1L,
            isConnected = isConnected,
            activeInterfaceName = activeInterface,
            localIpAddress = localIp,
            ipv6Address = ipv6Addr,
            gatewayIp = resolveGatewayIp(localIp),
            dnsServersList = emptyList(), // DNS servers are not read from active LinkProperties here.
            wifiSsid = wifiSsid,
            wifiRssiDbm = rssiDbm,
            wifiSignalPercent = signalPercent,
            wifiFrequencyMhz = freqMhz,
            linkSpeedMbps = linkSpeed,
            cellularCarrier = carrier,
            cellularNetworkType = netType,
            packetLossPercent = -1.0f,
            jitterMs = -1,
            healthGrade = "Not measured"
        )
    }

    private fun resolveLocalNetworkAddresses(): Triple<String, String, String> {
        var ipv4 = "Unavailable"
        var ipv6 = "Unavailable"
        var interfaceName = "Unavailable"

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return Triple(ipv4, ipv6, interfaceName)
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addrs = iface.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (addr is Inet4Address) {
                            ipv4 = host
                            interfaceName = iface.name
                        } else if (host.contains(":")) {
                            ipv6 = host.take(24)
                        }
                    }
                }
            }
        } catch (_: Throwable) {}

        
        
        return Triple(ipv4, ipv6, interfaceName)
    }

    private fun resolveGatewayIp(localIp: String): String = "Unavailable"
    private fun sampleWifiInfo(): WifiTelemetryInfo {
        try {
            val info = wifiManager?.connectionInfo
            if (info != null && info.networkId != -1) {
                val rawSsid = info.ssid?.replace("\"", "")
                val ssid = if (!rawSsid.isNullOrBlank() && rawSsid != "<unknown ssid>") rawSsid else "Unavailable"
                val rssi = info.rssi.coerceIn(-100, -30)
                val signalPercent = (((rssi + 100) / 70f) * 100f).toInt().coerceIn(0, 100)
                val freq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) info.frequency else 5240

                val speed = info.linkSpeed.coerceAtLeast(0)
                return WifiTelemetryInfo(ssid, rssi, signalPercent, freq, speed)
            }
        } catch (_: Throwable) {}

        return WifiTelemetryInfo("Unavailable", 0, 0, 0, 0)
    }


    private data class WifiTelemetryInfo(
        val ssid: String,
        val rssiDbm: Int,
        val signalPercent: Int,
        val freqMhz: Int,
        val linkSpeedMbps: Int
    )

    private fun sampleCellularInfo(): Pair<String, String> {
        try {
            val operator = telephonyManager?.networkOperatorName
            val carrier = if (!operator.isNullOrBlank()) operator else "Unavailable"
            val netType = "Unavailable"
            return Pair(carrier, netType)
        } catch (_: Throwable) {
            return Pair("Unavailable", "Unavailable")
        }
    }

    private fun determineConnectionType(): String {
        val activeNet = connectivityManager?.activeNetwork ?: return "Disconnected"
        val caps = connectivityManager.getNetworkCapabilities(activeNet) ?: return "Disconnected"

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile data"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other network"
        }
    }


    private fun calculateVitalityScore(
        thermal: ThermalTelemetryData,
        battery: BatteryHealthData,
        ramUsedPercent: Float
    ): Int {
        // A composite score is not meaningful when both thermal readings and
        // battery-health telemetry are unavailable. -1 is the UI's unavailable sentinel.
        val thermalKnown = thermal.currentTempCelsius > 0f ||
            thermal.statusName != "Sensor unavailable"
        val batteryHealthKnown = !battery.healthGrade.startsWith("Unavailable")
        if (!thermalKnown && !batteryHealthKnown) return -1

        var score = 100

        // Apply only deductions supported by available readings.
        if (thermalKnown) {
            if (thermal.isOverheating) {
                score -= 25
            } else if (thermal.currentTempCelsius > 39f) {
                score -= 10
            }
        }

        if (battery.levelPercentage in 0..19) {
            score -= 15
        } else if (battery.levelPercentage in 20..34) {
            score -= 8
        }

        if (ramUsedPercent in 0f..100f) {
            if (ramUsedPercent > 85f) {
                score -= 15
            } else if (ramUsedPercent > 70f) {
                score -= 8
            }
        }

        return score.coerceIn(0, 100)
    }
}
