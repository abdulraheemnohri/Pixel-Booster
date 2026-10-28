package com.example.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.FindingSeverity
import com.example.ui.components.BatteryHealthCard
import com.example.ui.components.HealthSummaryCard
import com.example.ui.components.NetworkUsageCard
import com.example.ui.components.ProcessorTelemetryCard
import com.example.ui.components.QuickActionButton
import com.example.ui.components.TelemetryMetricCard
import com.example.ui.components.ThermalMonitorCard
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToHealth: () -> Unit,
    onNavigateToBooster: () -> Unit = {},
    onNavigateToStartup: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snapshot = uiState.snapshot
    val hardwareTelemetry = uiState.hardwareTelemetry

    ScaffoldWithTopBar(
        title = "Pixel Booster",
        subtitle = snapshot?.deviceSpecs?.pixelGenerationName ?: "Google Pixel Hardware Engine",
        isLive = uiState.isLiveMonitoring,
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshDiagnostics() },
        onShare = { viewModel.shareDiagnosticReport() },
        onSettings = onNavigateToSettings,
        modifier = modifier
    ) { padding ->
        if (snapshot == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberCyan)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                        .testTag("dashboard_scroll_column"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Device Hardware Info Banner
                item {
                    DeviceBannerCard(
                        specs = snapshot.deviceSpecs,
                        lastUpdatedMillis = uiState.lastRefreshedTime
                    )
                }

                // System Vitality HUD Card
                if (hardwareTelemetry != null) {
                    item {
                        SystemVitalityHudCard(
                            vitalityScore = hardwareTelemetry.systemVitalityScore,
                            tempCelsius = hardwareTelemetry.thermal.currentTempCelsius,
                            isOverheating = hardwareTelemetry.thermal.isOverheating,
                            batteryHealthGrade = hardwareTelemetry.batteryHealth.healthGrade,
                            cpuUsage = hardwareTelemetry.processor.formattedCpuUsage,
                            networkSpeed = hardwareTelemetry.network.formattedDownloadSpeed,
                            onOpenHealth = onNavigateToHealth
                        )
                    }
                }

                // Mobile Booster Quick Access Hero Card
                item {
                    DashboardBoosterHeroCard(
                        availableRam = snapshot.memory.formattedAvailable,
                        batteryTemp = hardwareTelemetry?.thermal?.formattedCurrentTemp ?: snapshot.battery.formattedTemperature,
                        freeStorage = snapshot.storage.formattedFree,
                        onOpenBooster = onNavigateToBooster
                    )
                }

                // 1. REAL-TIME THERMAL MONITORING CARD
                if (hardwareTelemetry != null) {
                    item {
                        ThermalMonitorCard(
                            thermal = hardwareTelemetry.thermal,
                            onCoolDownClick = { viewModel.triggerThermalCoolDown() }
                        )
                    }
                }

                // 2. BATTERY HEALTH & CAPACITY MONITORING CARD
                if (hardwareTelemetry != null) {
                    item {
                        BatteryHealthCard(
                            battery = hardwareTelemetry.batteryHealth,
                            onOpenBatterySettings = { viewModel.openBatterySaver() }
                        )
                    }
                }

                // 3. CPU, GPU, TPU TELEMETRY CARD
                if (hardwareTelemetry != null) {
                    item {
                        ProcessorTelemetryCard(
                            processor = hardwareTelemetry.processor
                        )
                    }
                }

                // 4. INTERNET & DATA TELEMETRY CARD
                if (hardwareTelemetry != null) {
                    item {
                        NetworkUsageCard(
                            network = hardwareTelemetry.network
                        )
                    }
                }

                // Health Diagnostic Summary Card
                item {
                    val criticalCount = snapshot.findings.count { it.severity == FindingSeverity.CRITICAL }
                    val warningCount = snapshot.findings.count { it.severity == FindingSeverity.WARNING }
                    val nominalCount = snapshot.findings.count { it.severity == FindingSeverity.NOMINAL }

                    HealthSummaryCard(
                        statusTitle = snapshot.overallStatusTitle,
                        criticalCount = criticalCount,
                        warningCount = warningCount,
                        nominalCount = nominalCount,
                        onViewHealthClick = onNavigateToHealth
                    )
                }

                // System Memory Headroom Card
                item {
                    val ramStatusColor = if (snapshot.memory.isLowMemory || snapshot.memory.usedPercentage > 85f) WarningAmber else PositiveMint
                    TelemetryMetricCard(
                        title = "System RAM Headroom",
                        primaryValue = snapshot.memory.formattedUsed,
                        unit = "of ${snapshot.memory.formattedTotal}",
                        percentage = snapshot.memory.usedPercentage,
                        breakdownText = "${snapshot.memory.formattedAvailable} available memory headroom",
                        statusColor = ramStatusColor,
                        icon = Icons.Filled.Memory,
                        testTag = "ram_metric_card"
                    )
                }

                // Internal Storage Card
                item {
                    val storageStatusColor = when {
                        snapshot.storage.freePercentage < 10f -> CriticalRed
                        snapshot.storage.freePercentage < 20f -> WarningAmber
                        else -> PositiveMint
                    }
                    TelemetryMetricCard(
                        title = "Internal Storage",
                        primaryValue = snapshot.storage.formattedUsed,
                        unit = "of ${snapshot.storage.formattedTotal}",
                        percentage = snapshot.storage.usedPercentage,
                        breakdownText = "${snapshot.storage.formattedFree} free capacity remaining",
                        statusColor = storageStatusColor,
                        icon = Icons.Filled.SdStorage,
                        testTag = "storage_metric_card"
                    )
                }

                // Quick Safe Optimization Tools
                item {
                    Text(
                        text = "SYSTEM CONTROLS & SHORTCUTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            title = "Startup Apps",
                            subtitle = "Boot Manager",
                            icon = Icons.Filled.Bolt,
                            onClick = onNavigateToStartup,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_startup"
                        )
                        QuickActionButton(
                            title = "Battery Saver",
                            subtitle = "Power Profiles",
                            icon = Icons.Filled.PowerSettingsNew,
                            onClick = { viewModel.openBatterySaver() },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_battery"
                        )
                        QuickActionButton(
                            title = "Storage",
                            subtitle = "Files & Cache",
                            icon = Icons.Filled.SdStorage,
                            onClick = { viewModel.openStorageSettings() },
                            modifier = Modifier.weight(1f),
                            testTag = "quick_action_storage"
                        )
                    }
                }

                // Live Monitoring Control Tile
                item {
                    MonitoringControlCard(
                        isLive = uiState.isLiveMonitoring,
                        intervalSeconds = uiState.refreshIntervalSeconds,
                        onToggle = { viewModel.toggleLiveMonitoring() }
                    )
                }

                // Active Warnings Section
                val warningFindings = snapshot.findings.filter {
                    it.severity == FindingSeverity.CRITICAL || it.severity == FindingSeverity.WARNING
                }
                if (warningFindings.isNotEmpty()) {
                    item {
                        Text(
                            text = "ACTIVE HARDWARE WARNINGS (${warningFindings.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningAmber,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(warningFindings) { finding ->
                        WarningItemCard(
                            finding = finding,
                            onClick = onNavigateToHealth
                        )
                    }
                }

                // Philosophy note
                item {
                    PhilosophyCard()
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldWithTopBar(
    title: String,
    subtitle: String,
    isLive: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    onSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    androidx.compose.material3.Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Live monitoring indicator dot
                            Surface(
                                shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                                color = if (isLive) PositiveMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isLive) PositiveMint.copy(alpha = 0.4f) else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isLive) PositiveMint else MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isLive) "LIVE 1.0s" else "PAUSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isLive) PositiveMint else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                        modifier = Modifier.testTag("action_refresh")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CyberCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh telemetry",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("action_share")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Export diagnostic report",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onSettings,
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        content = content
    )
}

@Composable
fun SystemVitalityHudCard(
    vitalityScore: Int,
    tempCelsius: Float,
    isOverheating: Boolean,
    batteryHealthGrade: String,
    cpuUsage: String,
    networkSpeed: String,
    onOpenHealth: () -> Unit
) {
    val scoreColor = when {
        vitalityScore < 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        vitalityScore >= 85 -> PositiveMint
        vitalityScore >= 60 -> WarningAmber
        else -> CriticalRed
    }

    val animatedScore by animateFloatAsState(
        targetValue = vitalityScore.toFloat(),
        animationSpec = tween(600),
        label = "vitality_anim"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(scoreColor.copy(alpha = 0.6f), Color(0x2038BDF8))
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable { onOpenHealth() }
            .testTag("system_vitality_hud_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SYSTEM VITALITY INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = when {
                            vitalityScore < 0 -> "Status unavailable"
                            vitalityScore >= 85 -> "No major issues detected"
                            vitalityScore >= 60 -> "Review device conditions"
                            else -> "Device needs attention"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            isOverheating -> "Elevated thermal readings reported by Android"
                            vitalityScore < 0 -> "Not enough exposed telemetry to calculate a score"
                            else -> "Heuristic status from available readings; not a diagnostic certification"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverheating) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Modern Circular Dial with score
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val stroke = 6.dp.toPx()
                        drawArc(
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        val sweep = 270f * (animatedScore / 100f).coerceIn(0f, 1f)
                        drawArc(
                            color = scoreColor,
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (vitalityScore >= 0) "${animatedScore.toInt()}" else "—",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = if (vitalityScore >= 0) "PTS" else "N/A",
                            style = MaterialTheme.typography.labelSmall,
                            color = scoreColor.copy(alpha = 0.8f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subsystem Telemetry Quick Readouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Cell Temp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(
                            String.format(Locale.US, "%.1f°C", tempCelsius),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverheating) WarningAmber else PositiveMint
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Battery Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(batteryHealthGrade, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PositiveMint)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("CPU Usage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(cpuUsage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Data Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(networkSpeed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PositiveMint)
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceBannerCard(
    specs: com.example.core.model.DeviceSpecs,
    lastUpdatedMillis: Long
) {
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)
    val timeStr = timeFormatter.format(Date(lastUpdatedMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0x4538BDF8), Color(0x101E293B))
                ),
                RoundedCornerShape(24.dp)
            )
            .testTag("device_banner_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${specs.manufacturer} ${specs.model}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = specs.socModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3038BDF8))
                ) {
                    Text(
                        text = "Android ${specs.androidVersion} (API ${specs.sdkInt})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Uptime: ${specs.formattedUptime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sampled: $timeStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MonitoringControlCard(
    isLive: Boolean,
    intervalSeconds: Int,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
            .testTag("monitoring_control_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live Active Sampling",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isLive) "Sampling hardware sensors every ${intervalSeconds}s while screen active" else "Monitoring paused to conserve battery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = isLive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF031628),
                    checkedTrackColor = CyberCyan,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.testTag("switch_live_monitoring")
            )
        }
    }
}

@Composable
fun WarningItemCard(
    finding: com.example.core.model.HealthFinding,
    onClick: () -> Unit
) {
    val borderColor = if (finding.severity == FindingSeverity.CRITICAL) CriticalRed else WarningAmber

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(borderColor.copy(alpha = 0.6f), borderColor.copy(alpha = 0.2f))
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .testTag("warning_item_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Alert",
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = finding.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = finding.evidence,
                    style = MaterialTheme.typography.bodySmall,
                    color = borderColor
                )
            }
        }
    }
}

@Composable
fun PhilosophyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "NO-ROOT PIXEL OPTIMIZATION PHILOSOPHY",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pixel Booster reports real, measured Android hardware states without deceptive RAM cleaners or simulated benchmarks. Battery Saver and Storage controls delegate directly to Android's native subsystem.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DashboardBoosterHeroCard(
    availableRam: String,
    batteryTemp: String,
    freeStorage: String,
    onOpenBooster: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(PositiveMint.copy(alpha = 0.6f), Color(0x3038BDF8))
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable { onOpenBooster() }
            .testTag("dashboard_booster_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PositiveMint.copy(alpha = 0.16f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.RocketLaunch,
                                contentDescription = "Booster",
                                tint = PositiveMint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mobile Booster Suite",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hardware Acceleration & Cache Purge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                    color = PositiveMint.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = PositiveMint,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("RAM Headroom", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(availableRam, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Cell Temp", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(batteryTemp, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Free Storage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Text(freeStorage, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenBooster,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PositiveMint,
                    contentColor = Color(0xFF022C1C)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("btn_open_booster_hero")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Mobile Booster",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
