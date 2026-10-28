package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.core.model.BatteryChargingState
import com.example.core.model.BatteryHealthData
import com.example.core.model.BatteryPluggedSource
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
fun BatteryHealthCard(
    battery: BatteryHealthData,
    onOpenBatterySettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusColor = when {
        battery.levelPercentage <= 15 -> CriticalRed
        battery.levelPercentage <= 30 -> WarningAmber
        else -> PositiveMint
    }

    val infiniteTransition = rememberInfiniteTransition(label = "charging_pulse")
    val boltAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bolt_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(statusColor.copy(alpha = 0.5f), Color(0x1538BDF8))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("battery_health_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Left title with weight(1f) to prevent stretching, Right status badge
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
                        color = if (battery.isCharging) PositiveMint.copy(alpha = 0.18f) else HighElevationDark,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    battery.isCharging -> Icons.Filled.BatteryChargingFull
                                    battery.levelPercentage <= 20 -> Icons.Filled.BatteryAlert
                                    else -> Icons.Filled.BatteryFull
                                },
                                contentDescription = "Battery Status",
                                tint = if (battery.isCharging) PositiveMint else statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "BATTERY HEALTH MONITOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Charge Cycles & Coulomb Capacity Tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Health Grade Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PositiveMint.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = battery.healthGrade,
                        style = MaterialTheme.typography.labelSmall,
                        color = PositiveMint,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Level & Charging Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${battery.levelPercentage}",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(bottom = 6.dp, start = 3.dp)
                        )
                    }
                    Text(
                        text = "${battery.formattedCurrentCapacity} currently stored",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Charging Status Indicator Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (battery.isCharging) PositiveMint.copy(alpha = 0.14f) else HighElevationDark,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (battery.isCharging) PositiveMint.copy(alpha = 0.5f) else OutlineDark
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (battery.isCharging) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = "Charging",
                                tint = PositiveMint.copy(alpha = boltAlpha),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "FAST CHARGING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PositiveMint
                                )
                                Text(
                                    text = "${battery.pluggedSource.name} • ${battery.formattedWattage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            Column {
                                Text(
                                    text = if (battery.isFull) "FULLY CHARGED" else "DISCHARGING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "${battery.formattedVoltage} • ${battery.technology}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capacity Progress Bar (Current mAh vs Designed mAh)
            val capacityProgress = if (battery.currentCapacityMah > 0 && battery.designedCapacityMah > 0) (battery.currentCapacityMah.toFloat() / battery.designedCapacityMah.toFloat()).coerceIn(0f, 1f) else 0f
            val animatedCapacityProgress by animateFloatAsState(
                targetValue = capacityProgress,
                animationSpec = tween(600),
                label = "capacity_anim"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current vs Design Capacity (${battery.formattedCapacityPercentage})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${battery.formattedCurrentCapacity} / ${battery.formattedDesignedCapacity}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedCapacityProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (battery.isCharging) PositiveMint else CyberCyan,
                trackColor = HighElevationDark,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DETAILED BATTERY BREAKDOWN CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = HighElevationDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2538BDF8))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HARDWARE BATTERY BREAKDOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BatteryBreakdownItem(
                            title = "CHARGE CYCLES",
                            value = if (battery.chargeCycles > 0) "${battery.chargeCycles}" else "Unavailable",
                            subtitle = battery.cycleHealthAssessment,
                            modifier = Modifier.weight(1f)
                        )
                        BatteryBreakdownItem(
                            title = "CAPACITY RATIO",
                            value = battery.formattedHealthPercentage,
                            subtitle = if (battery.designedCapacityMah > 0) "${battery.designedCapacityMah} mAh Design" else "Design capacity unavailable",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BatteryBreakdownItem(
                            title = "CELL VOLTAGE",
                            value = battery.formattedVoltage,
                            subtitle = "Nominal 3.85V-4.4V",
                            modifier = Modifier.weight(1f)
                        )
                        BatteryBreakdownItem(
                            title = "CELL TEMPERATURE",
                            value = String.format(java.util.Locale.US, "%.1f°C", battery.temperatureCelsius),
                            subtitle = if (battery.temperatureCelsius > 0f) "${(battery.temperatureCelsius * 9f / 5f + 32f).toInt()}°F Thermal" else "Sensor not exposed",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (battery.currentFlowMa != 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Coulomb Draw / Charge Rate:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                            Text(
                                text = battery.formattedCurrentFlow,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (battery.currentFlowMa > 0) PositiveMint else CyberCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sensor Source: ${battery.chargeCyclesSource}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Automatic Charging Cut-Off & Battery Safety Guardian Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = PositiveMint.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ElectricBolt,
                                contentDescription = null,
                                tint = PositiveMint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Charging Cut-Off & Safety Guardian",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PositiveMint
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = PositiveMint.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = PositiveMint,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (battery.levelPercentage >= 85 && battery.isCharging)
                            "⚠️ Charge reached 85% cutoff threshold! Unplug to preserve battery longevity."
                        else
                            "Monitoring thermal and voltage safety. Protects against overcharge and overheating.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            Button(
                onClick = onOpenBatterySettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighElevationDark,
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3038BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("btn_battery_settings")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Battery Saver & Adaptive Power",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryBreakdownItem(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
