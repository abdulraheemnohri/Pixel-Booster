package com.example.feature.health

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.core.model.HealthFinding
import com.example.core.model.SuggestedActionType
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filteredFindings = when (uiState.activeFilter) {
        HealthFilter.ALL -> uiState.findings
        HealthFilter.CRITICAL -> uiState.findings.filter { it.severity == FindingSeverity.CRITICAL }
        HealthFilter.WARNINGS -> uiState.findings.filter { it.severity == FindingSeverity.WARNING }
        HealthFilter.NOMINAL -> uiState.findings.filter { it.severity == FindingSeverity.NOMINAL }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hardware Health Check",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Deterministic diagnostic heuristics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.evaluateHealth() },
                        enabled = !uiState.isRefreshing,
                        modifier = Modifier.testTag("action_health_refresh")
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CyberCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh health evaluation",
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
                    .testTag("health_scroll_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Overall Score Card
            item {
                HealthScoreCard(
                    score = uiState.overallHealthScore,
                    totalFindings = uiState.findings.size,
                    criticalCount = uiState.findings.count { it.severity == FindingSeverity.CRITICAL },
                    warningCount = uiState.findings.count { it.severity == FindingSeverity.WARNING }
                )
            }

            // Real-time Battery Health & Capacity Breakdown Card
            if (uiState.hardwareTelemetry != null) {
                item {
                    com.example.ui.components.BatteryHealthCard(
                        battery = uiState.hardwareTelemetry!!.batteryHealth,
                        onOpenBatterySettings = { viewModel.executeAction(SuggestedActionType.OPEN_BATTERY_SAVER) }
                    )
                }

                // Real-time Hardware Temperature Status Card
                item {
                    com.example.ui.components.ThermalMonitorCard(
                        thermal = uiState.hardwareTelemetry!!.thermal,
                        onCoolDownClick = { viewModel.triggerThermalRelief() }
                    )
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        HealthFilterChip(
                            label = "All (${uiState.findings.size})",
                            selected = uiState.activeFilter == HealthFilter.ALL,
                            onClick = { viewModel.setFilter(HealthFilter.ALL) },
                            testTag = "filter_all"
                        )
                    }
                    item {
                        val count = uiState.findings.count { it.severity == FindingSeverity.CRITICAL }
                        HealthFilterChip(
                            label = "Critical ($count)",
                            selected = uiState.activeFilter == HealthFilter.CRITICAL,
                            onClick = { viewModel.setFilter(HealthFilter.CRITICAL) },
                            testTag = "filter_critical"
                        )
                    }
                    item {
                        val count = uiState.findings.count { it.severity == FindingSeverity.WARNING }
                        HealthFilterChip(
                            label = "Warnings ($count)",
                            selected = uiState.activeFilter == HealthFilter.WARNINGS,
                            onClick = { viewModel.setFilter(HealthFilter.WARNINGS) },
                            testTag = "filter_warnings"
                        )
                    }
                    item {
                        val count = uiState.findings.count { it.severity == FindingSeverity.NOMINAL }
                        HealthFilterChip(
                            label = "Nominal ($count)",
                            selected = uiState.activeFilter == HealthFilter.NOMINAL,
                            onClick = { viewModel.setFilter(HealthFilter.NOMINAL) },
                            testTag = "filter_nominal"
                        )
                    }
                }
            }

            // Findings List
            if (filteredFindings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineDark, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "No findings",
                                tint = PositiveMint,
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No findings in this category",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "All evaluated subsystem metrics match operational targets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredFindings) { finding ->
                    FindingDetailCard(
                        finding = finding,
                        onAction = { viewModel.executeAction(finding.suggestedActionType) }
                    )
                }
            }

            // Pixel Specific Advice & Cooldown Rules
            item {
                PixelThermalGuidanceCard()
            }

            // Diagnostic Scope Disclaimer
            item {
                DiagnosticScopeCard()
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
}

@Composable
fun HealthScoreCard(
    score: Int,
    totalFindings: Int,
    criticalCount: Int,
    warningCount: Int
) {
    val scoreColor = when {
        score >= 85 -> PositiveMint
        score >= 60 -> WarningAmber
        else -> CriticalRed
    }

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
            .testTag("health_score_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HARDWARE HEALTH INDEX",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        score >= 85 -> "Nominal Condition"
                        score >= 60 -> "Advisory Actions"
                        else -> "Mitigation Urged"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$criticalCount critical alerts • $warningCount warnings across $totalFindings checked rules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier.size(76.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(76.dp)) {
                    val stroke = 6.dp.toPx()
                    drawArc(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                    val sweep = 270f * (score / 100f).coerceIn(0f, 1f)
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
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = "/100",
                        style = MaterialTheme.typography.labelSmall,
                        color = scoreColor.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HealthFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = CyberCyan,
            selectedLabelColor = Color(0xFF031628),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = OutlineDark,
            selectedBorderColor = CyberCyan
        ),
        modifier = Modifier.testTag(testTag)
    )
}

@Composable
fun FindingDetailCard(
    finding: HealthFinding,
    onAction: () -> Unit
) {
    val severityColor = when (finding.severity) {
        FindingSeverity.CRITICAL -> CriticalRed
        FindingSeverity.WARNING -> WarningAmber
        FindingSeverity.NOMINAL -> PositiveMint
        FindingSeverity.INFO -> CyberCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(severityColor.copy(alpha = 0.5f), Color(0x101E293B))
                ),
                RoundedCornerShape(18.dp)
            )
            .testTag("finding_card_${finding.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (finding.severity) {
                            FindingSeverity.CRITICAL, FindingSeverity.WARNING -> Icons.Filled.Warning
                            FindingSeverity.NOMINAL -> Icons.Filled.CheckCircle
                            FindingSeverity.INFO -> Icons.Filled.Info
                        },
                        contentDescription = finding.severity.name,
                        tint = severityColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = finding.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                    color = severityColor.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = finding.severity.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = severityColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Evidence Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Measured Evidence: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = finding.evidence,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Suggested Action button if applicable
            if (finding.suggestedActionType != SuggestedActionType.NONE && finding.suggestedActionLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (finding.severity == FindingSeverity.CRITICAL) CriticalRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = if (finding.severity == FindingSeverity.CRITICAL) CriticalRed else CyberCyan
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (finding.severity == FindingSeverity.CRITICAL) CriticalRed.copy(alpha = 0.4f) else Color(0x3538BDF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_action_${finding.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = finding.suggestedActionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PixelThermalGuidanceCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "PIXEL THERMAL & BATTERY LONGEVITY",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Natural Cooldown: Let the phone cool in room-temperature air. Never place in a refrigerator or ice pack; rapid temperature deltas cause internal moisture condensation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Adaptive Charging: On Pixel devices, Adaptive Charging pauses at 80% overnight and finishes before your morning alarm, extending battery chemistry lifespan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Fast Charging Thermals: Remove heavy silicone or leather cases during 20W–30W fast charging sessions to facilitate passive chassis heat dissipation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DiagnosticScopeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OutlineDark, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "TRANSPARENT DIAGNOSTIC SCOPE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Heuristic evaluations monitor genuine battery controller telemetry, filesystem StatFs allocations, and Android PowerManager thermal triggers. They do not simulate repairs or replace professional hardware triage.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
