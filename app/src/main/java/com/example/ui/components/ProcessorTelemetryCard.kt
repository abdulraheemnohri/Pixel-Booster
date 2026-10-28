package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ProcessorTelemetryData
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HighElevationDark
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

@Composable
fun ProcessorTelemetryCard(
    processor: ProcessorTelemetryData,
    modifier: Modifier = Modifier
) {
    val cpuColor = when {
        processor.overallCpuUsage > 85f -> CriticalRed
        processor.overallCpuUsage > 65f -> WarningAmber
        else -> CyberCyan
    }

    val gpuColor = when {
        processor.gpuUsage > 85f -> CriticalRed
        processor.gpuUsage > 65f -> WarningAmber
        else -> PositiveMint
    }

    val tpuColor = Color(0xFFC084FC) // Futuristic violet for Neural/TPU

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0x5038BDF8), Color(0x15A855F7))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("processor_telemetry_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CyberCyan.copy(alpha = 0.16f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.DeveloperBoard,
                                contentDescription = "Processor Array",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CPU • GPU • TPU TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Heterogeneous Compute Matrix",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                    color = HighElevationDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3038BDF8))
                ) {
                    Text(
                        text = "${processor.coreCount} Cores",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimaryDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. CPU LOAD SECTION
            ProcessorMetricRow(
                label = "CPU Load",
                value = processor.formattedCpuUsage,
                percentage = processor.overallCpuUsage,
                subtext = "${processor.coreCount}-Core Dynamic Cluster",
                accentColor = cpuColor,
                icon = Icons.Filled.Memory
            )
            
            // Real-time CPU Trend Graph
            if (processor.overallCpuUsage >= 0f) {
                LiveTrendGraph(
                    currentValue = processor.overallCpuUsage,
                    lineColor = cpuColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "CPU utilization unavailable from this device sampler.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Per-core values are unavailable unless individually measured.
            if (processor.coreLoads.isNotEmpty() && processor.coreLoads.all { it >= 0f }) {
    
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    processor.coreLoads.take(8).forEachIndexed { index, load ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val animatedLoad by animateFloatAsState(
                                targetValue = (load / 100f).coerceIn(0f, 1f),
                                animationSpec = tween(400),
                                label = "core_anim_$index"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(HighElevationDark),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp * animatedLoad)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(cpuColor, cpuColor.copy(alpha = 0.7f))
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "C$index",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Per-core utilization unavailable; only aggregate CPU sampling is attempted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. GPU LOAD SECTION
            ProcessorMetricRow(
                label = "GPU Graphics Engine",
                value = processor.formattedGpuUsage,
                percentage = processor.gpuUsage,
                subtext = "${processor.gpuEngineName} • FPS telemetry unavailable",
                accentColor = gpuColor,
                icon = Icons.Filled.Games
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. TPU (TENSOR PROCESSING UNIT) SECTION
            ProcessorMetricRow(
                label = "TPU / Neural Engine",
                value = processor.formattedTpuUsage,
                percentage = processor.tpuUsage,
                subtext = "${processor.tpuTopsRating} • ${processor.tpuState}",
                accentColor = tpuColor,
                icon = Icons.Filled.AutoAwesome
            )
        }
    }
}

@Composable
fun ProcessorMetricRow(
    label: String,
    value: String,
    percentage: Float,
    subtext: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "metric_progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (percentage >= 0f) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = HighElevationDark,
                strokeCap = StrokeCap.Round
            )
        } else {
            Text(
                text = "Measurement unavailable",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondaryDark,
            fontSize = 10.sp
        )
    }
}
