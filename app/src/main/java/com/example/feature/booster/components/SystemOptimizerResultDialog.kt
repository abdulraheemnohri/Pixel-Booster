package com.example.feature.booster.components

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.optimizer.SystemOptimizationResult
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.modernCard
import com.example.ui.theme.pulsingGlow

@Composable
fun SystemOptimizerResultDialog(
    result: SystemOptimizationResult?,
    onDismiss: () -> Unit
) {
    if (result == null) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .modernCard(
                    shape = RoundedCornerShape(26.dp),
                    borderBrush = ModernDesignTokens.AccentBorderBrush
                )
                .testTag("system_optimizer_result_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1224))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Glowing Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .pulsingGlow(glowColor = CyberCyan, minAlpha = 0.2f, maxAlpha = 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = androidx.compose.foundation.BorderStroke(2.dp, ModernDesignTokens.PrimaryGlowBrush),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "System Optimized",
                                tint = CyberCyan,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "System Optimization Complete",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "App cache purged & background memory released",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Big Headline Reclaimed Metric
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL SYSTEM RESOURCE RECLAIMED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            fontFamily = SpaceGrotesk
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "+${result.formattedTotalFreed}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGrotesk,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = PositiveMint.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Responsiveness: ${result.responsivenessGrade} (99%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = SpaceGrotesk,
                                color = PositiveMint,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown Items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OptimizerMetricRow(
                        label = "Cache Files Purged",
                        value = result.formattedCacheFreed,
                        highlight = true
                    )
                    OptimizerMetricRow(
                        label = "RAM Headroom Released",
                        value = result.formattedRamReclaimed,
                        highlight = true
                    )
                    OptimizerMetricRow(
                        label = "Background Processes Trimmed",
                        value = "${result.backgroundProcessesTrimmed} processes",
                        highlight = false
                    )
                    OptimizerMetricRow(
                        label = "Optimizer Duration",
                        value = "${result.durationMs}ms",
                        highlight = false
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF070B14)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_dismiss_system_optimizer")
                ) {
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimizerMetricRow(
    label: String,
    value: String,
    highlight: Boolean
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = SpaceGrotesk,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                color = if (highlight) CyberCyan else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
