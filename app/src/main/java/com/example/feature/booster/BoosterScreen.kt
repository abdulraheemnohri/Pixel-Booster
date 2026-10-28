package com.example.feature.booster

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.booster.BoosterToolType
import com.example.core.database.OptimizationLogEntity
import com.example.feature.booster.components.BoosterGauge
import com.example.feature.booster.components.BoosterResultDialog
import com.example.feature.booster.components.BoosterScanningOverlay
import com.example.feature.booster.components.BoosterToolCard
import com.example.feature.booster.components.CacheClearResultDialog
import com.example.feature.booster.components.ClearCacheCard
import com.example.feature.booster.components.RamUsageMonitorCard
import com.example.feature.booster.components.ToolDetailSheets
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HighElevationDark
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoosterScreen(
    viewModel: BoosterViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.activity.compose.BackHandler(enabled = uiState.selectedTool != null) {
        viewModel.closeTool()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Mobile Booster",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Device Health & Safe Maintenance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshHardwareTelemetry() },
                        modifier = Modifier.testTag("action_refresh_booster")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh telemetry",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (uiState.recentLogs.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearOptimizationHistory() },
                            modifier = Modifier.testTag("action_clear_booster_logs")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear logs",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { padding ->
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
                    .testTag("booster_scroll_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Hero Gauge & 1-Tap Quick Boost
            item {
                BoosterGauge(
                    score = uiState.optimizationScore,
                    isBoosting = uiState.isBoosting,
                    onQuickBoostClick = { viewModel.runQuickBoost() }
                )
            }

            // Dedicated Real-time RAM Usage Monitor
            item {
                RamUsageMonitorCard(
                    ramMetrics = uiState.liveRamMetrics,
                    isOptimizing = uiState.isBoosting,
                    onTrimRamClick = { viewModel.runRamBoost() }
                )
            }

            // Dedicated Background Non-Essential Cache Purge
            item {
                ClearCacheCard(
                    detectedCacheFormatted = uiState.detectedCacheFormatted,
                    isClearingCache = uiState.isClearingCache,
                    progress = uiState.cacheClearProgress,
                    statusText = uiState.cacheClearStageText,
                    onClearCacheClick = { viewModel.clearNonEssentialCache() }
                )
            }

            // Dedicated System Optimizer: Clears app cache files & releases background memory
            item {
                com.example.feature.booster.components.SystemOptimizerCard(
                    isOptimizing = uiState.isSystemOptimizing,
                    detectedCacheFormatted = uiState.detectedCacheFormatted,
                    onRunOptimizerClick = { viewModel.runSystemOptimizer() }
                )
            }

            // Specialized Booster Tools Header
            item {
                Text(
                    text = "HARDWARE ACCELERATION ARSENAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // RAM Booster Card
            item {
                val ramPercentage = uiState.memoryInfo?.usedPercentage?.toInt() ?: 70
                val ramBadge = "$ramPercentage% Used"
                val ramBadgeColor = if (ramPercentage > 85) WarningAmber else CyberCyan
                BoosterToolCard(
                    tool = BoosterToolType.RAM_BOOSTER,
                    icon = Icons.Filled.Memory,
                    statusBadge = ramBadge,
                    badgeColor = ramBadgeColor,
                    onOpenTool = { viewModel.openTool(BoosterToolType.RAM_BOOSTER) },
                    onDirectBoost = { viewModel.runRamBoost() }
                )
            }

            // Junk Cleaner Card
            item {
                BoosterToolCard(
                    tool = BoosterToolType.JUNK_CLEANER,
                    icon = Icons.Filled.CleaningServices,
                    statusBadge = "685 MB Found",
                    badgeColor = PositiveMint,
                    onOpenTool = { viewModel.openTool(BoosterToolType.JUNK_CLEANER) },
                    onDirectBoost = { viewModel.runJunkClean() }
                )
            }

            // CPU Cooler Card
            item {
                val tempText = uiState.batteryInfo?.formattedTemperature ?: "31.5°C"
                BoosterToolCard(
                    tool = BoosterToolType.CPU_COOLER,
                    icon = Icons.Filled.Thermostat,
                    statusBadge = tempText,
                    badgeColor = WarningAmber,
                    onOpenTool = { viewModel.openTool(BoosterToolType.CPU_COOLER) },
                    onDirectBoost = { viewModel.runCpuCooler() }
                )
            }

            // Battery Saver Card
            item {
                BoosterToolCard(
                    tool = BoosterToolType.BATTERY_SAVER,
                    icon = Icons.Filled.PowerSettingsNew,
                    statusBadge = uiState.activePowerProfile.label,
                    badgeColor = PositiveMint,
                    onOpenTool = { viewModel.openTool(BoosterToolType.BATTERY_SAVER) },
                    onDirectBoost = { viewModel.openTool(BoosterToolType.BATTERY_SAVER) }
                )
            }

            // Network Turbo Card
            item {
                val ping = uiState.networkPingResult?.optimizedLatencyMs ?: 38L
                BoosterToolCard(
                    tool = BoosterToolType.NETWORK_TURBO,
                    icon = Icons.Filled.NetworkCheck,
                    statusBadge = "${ping}ms Ping",
                    badgeColor = CyberCyan,
                    onOpenTool = { viewModel.openTool(BoosterToolType.NETWORK_TURBO) },
                    onDirectBoost = { viewModel.runNetworkTurbo() }
                )
            }

            // Game Turbo Card
            item {
                val gameBadge = if (uiState.gameTurboActive) "ACTIVE" else "READY"
                val gameColor = if (uiState.gameTurboActive) PositiveMint else CyberCyan
                BoosterToolCard(
                    tool = BoosterToolType.GAME_TURBO,
                    icon = Icons.Filled.SportsEsports,
                    statusBadge = gameBadge,
                    badgeColor = gameColor,
                    onOpenTool = { viewModel.openTool(BoosterToolType.GAME_TURBO) },
                    onDirectBoost = { viewModel.toggleGameTurbo() }
                )
            }

            // Recent Optimization Activity Header
            if (uiState.recentLogs.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPTIMIZATION LOGS (${uiState.recentLogs.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saved in Room Database",
                            style = MaterialTheme.typography.bodySmall,
                            color = PositiveMint
                        )
                    }
                }

                items(uiState.recentLogs.take(15)) { log ->
                    OptimizationLogItemCard(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

    // High-tech Scanning HUD overlay
    BoosterScanningOverlay(
        isBoosting = uiState.isBoosting,
        stage = uiState.currentStage,
        stageText = uiState.currentStageText,
        progress = uiState.boostProgress
    )

    // Result summary dialog
    if (uiState.showResultDialog) {
        BoosterResultDialog(
            summary = uiState.lastResultSummary,
            onDismiss = { viewModel.dismissResultDialog() }
        )
    }

    // Cache clear result dialog
    if (uiState.showCacheClearDialog) {
        CacheClearResultDialog(
            report = uiState.lastCacheClearReport,
            onDismiss = { viewModel.dismissCacheClearDialog() }
        )
    }

    // System Optimizer Result dialog
    if (uiState.showSystemOptimizerDialog) {
        com.example.feature.booster.components.SystemOptimizerResultDialog(
            result = uiState.systemOptimizationResult,
            onDismiss = { viewModel.dismissSystemOptimizerDialog() }
        )
    }

    // Detail Bottom Sheets
    ToolDetailSheets(
        selectedTool = uiState.selectedTool,
        memoryInfo = uiState.memoryInfo,
        storageInfo = uiState.storageInfo,
        batteryInfo = uiState.batteryInfo,
        thermalStatus = uiState.thermalStatus,
        activePowerProfile = uiState.activePowerProfile,
        gameTurboActive = uiState.gameTurboActive,
        onDismiss = { viewModel.closeTool() },
        onRunRamBoost = { viewModel.runRamBoost() },
        onRunJunkClean = { viewModel.runJunkClean() },
        onRunCpuCooler = { viewModel.runCpuCooler() },
        onSelectPowerProfile = { profile -> viewModel.selectPowerProfile(profile) },
        onRunNetworkTurbo = { viewModel.runNetworkTurbo() },
        onToggleGameTurbo = { viewModel.toggleGameTurbo() }
    )
}

@Composable
fun OptimizationLogItemCard(log: OptimizationLogEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color(0x3038BDF8), Color(0x101E293B))
                ),
                RoundedCornerShape(16.dp)
            )
            .testTag("log_item_${log.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                    color = PositiveMint.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = log.metricFreed,
                        style = MaterialTheme.typography.labelSmall,
                        color = PositiveMint,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${log.formattedDate} at ${log.formattedTime}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                fontSize = 11.sp
            )
        }
    }
}
