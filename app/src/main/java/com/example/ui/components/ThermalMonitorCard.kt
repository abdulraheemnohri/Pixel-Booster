package com.example.ui.components

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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ThermalSeverityLevel
import com.example.core.model.ThermalTelemetryData
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
fun ThermalMonitorCard(
    thermal: ThermalTelemetryData,
    onCoolDownClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusColor = when (thermal.severityLevel) {
        ThermalSeverityLevel.CRITICAL -> CriticalRed
        ThermalSeverityLevel.WARNING -> if (thermal.isOverheating) WarningAmber else WarningAmber.copy(alpha = 0.9f)
        ThermalSeverityLevel.NOMINAL -> PositiveMint
        ThermalSeverityLevel.UNSUPPORTED -> TextSecondaryDark
    }

    val infiniteTransition = rememberInfiniteTransition(label = "thermal_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (thermal.isOverheating) 0.9f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (thermal.isOverheating) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val animatedColor by animateColorAsState(
        targetValue = statusColor,
        animationSpec = tween(400),
        label = "thermal_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (thermal.isOverheating) 2.dp else 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        animatedColor.copy(alpha = if (thermal.isOverheating) pulseAlpha else 0.6f),
                        animatedColor.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("thermal_monitor_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Left title with weight(1f), Right status badge
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
                        color = animatedColor.copy(alpha = 0.16f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Thermostat,
                                contentDescription = "Thermal Sensor",
                                tint = animatedColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HARDWARE TEMPERATURE STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-time SoC & Thermal Zone Telemetry",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = animatedColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(animatedColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = thermal.statusName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = animatedColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Temperature Display & Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(java.util.Locale.US, "%.1f", thermal.currentTempCelsius),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "°C",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor,
                            modifier = Modifier.padding(bottom = 6.dp, start = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "(${thermal.formattedTempFahrenheit})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryDark,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        text = "Peak: ${thermal.formattedPeakTemp} • Target: <38°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Mini Heatwave / Sparkline Visualizer
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(HighElevationDark)
                        .border(1.dp, animatedColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(6.dp)
                ) {
                    ThermalSparkline(
                        history = thermal.temperatureHistory,
                        color = animatedColor,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thermal Bar Scale (20°C to 55°C)
            val normalizedTemp = ((thermal.currentTempCelsius - 20f) / 35f).coerceIn(0f, 1f)
            val animatedBar by animateFloatAsState(
                targetValue = normalizedTemp,
                animationSpec = tween(600),
                label = "temp_bar"
            )

            LinearProgressIndicator(
                progress = { animatedBar },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = animatedColor,
                trackColor = HighElevationDark,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("20°C Cool", style = MaterialTheme.typography.labelSmall, color = PositiveMint, fontSize = 10.sp)
                Text("38°C Normal", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark, fontSize = 10.sp)
                Text("42°C Warning", style = MaterialTheme.typography.labelSmall, color = WarningAmber, fontSize = 10.sp)
                Text("55°C Critical", style = MaterialTheme.typography.labelSmall, color = CriticalRed, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DETAILED HARDWARE TEMPERATURE STATUS BREAKDOWN
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = HighElevationDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2038BDF8))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "THERMAL SENSOR ARRAY STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThermalStatItem(
                            title = "SoC PROCESSOR",
                            value = thermal.formattedSocTemp,
                            subtitle = "CPU/GPU Core Die",
                            modifier = Modifier.weight(1f)
                        )
                        ThermalStatItem(
                            title = "BATTERY CELL",
                            value = thermal.formattedBatteryTemp,
                            subtitle = "Chemistry Sensor",
                            modifier = Modifier.weight(1f)
                        )
                        ThermalStatItem(
                            title = "HAL MITIGATION",
                            value = thermal.powerManagerStatusName,
                            subtitle = "${thermal.activeThermalZones} Zones Polled",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Overheating Warning Banner or Cooling Advice
            if (thermal.isOverheating && thermal.warningMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = animatedColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, animatedColor.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("thermal_overheat_warning_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Thermal Warning",
                                tint = animatedColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OVERHEATING WARNING",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = animatedColor
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = thermal.warningMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onCoolDownClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = animatedColor,
                                contentColor = Color(0xFF031628)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_trigger_cooldown")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AcUnit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Trigger 1-Tap Thermal Relief",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Guidance: ${thermal.coolingAdvice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThermalStatItem(
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
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
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

@Composable
fun ThermalSparkline(
    history: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (history.size < 2) {
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val min = (history.minOrNull() ?: 25f).coerceAtLeast(20f)
        val max = (history.maxOrNull() ?: 45f).coerceAtLeast(min + 2f)
        val range = max - min

        val stepX = size.width / (history.size - 1)
        val points = history.mapIndexed { index, temp ->
            val x = index * stepX
            val normalizedY = (temp - min) / range
            val y = size.height - (normalizedY * size.height)
            Offset(x, y.coerceIn(2f, size.height - 2f))
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
