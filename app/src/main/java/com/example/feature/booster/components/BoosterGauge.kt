package com.example.feature.booster.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.HighElevationDark
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BoosterGauge(
    score: Int,
    isBoosting: Boolean,
    onQuickBoostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isBoosting) 1.08f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoosting) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isBoosting) 3000 else 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    val scoreColor = when {
        score >= 85 -> PositiveMint
        score >= 65 -> CyberCyan
        else -> WarningAmber
    }

    val statusLabel = when {
        score >= 90 -> "PEAK OPTIMAL"
        score >= 75 -> "GOOD HEADROOM"
        score >= 60 -> "OPTIMIZATION READY"
        else -> "TURBO BOOST RECOMMENDED"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(scoreColor.copy(alpha = 0.6f), Color(0x2538BDF8))
                ),
                RoundedCornerShape(26.dp)
            )
            .testTag("booster_hero_gauge_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(ModernDesignTokens.RadiusFull),
                color = scoreColor.copy(alpha = 0.16f),
                border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = null,
                        tint = scoreColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Multi-ring Futuristic Circular Reactor Dial with Score
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(210.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth - 10.dp.toPx()
                    val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Outer subtle tick ring
                    val tickRadius = (diameter / 2f) + 12.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    for (i in 0 until 36) {
                        val angleDeg = (i * 10f) + rotationAngle * 0.15f
                        val rad = Math.toRadians(angleDeg.toDouble())
                        val tickLen = if (i % 3 == 0) 6.dp.toPx() else 3.dp.toPx()
                        val p1 = Offset(
                            (center.x + (tickRadius - tickLen) * cos(rad)).toFloat(),
                            (center.y + (tickRadius - tickLen) * sin(rad)).toFloat()
                        )
                        val p2 = Offset(
                            (center.x + tickRadius * cos(rad)).toFloat(),
                            (center.y + tickRadius * sin(rad)).toFloat()
                        )
                        drawLine(
                            color = if (i % 3 == 0) CyberCyan.copy(alpha = 0.4f) else TextSecondaryDark.copy(alpha = 0.15f),
                            start = p1,
                            end = p2,
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }

                    // Background track arc (240 degrees)
                    drawArc(
                        color = HighElevationDark,
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active sweep arc based on score
                    val sweep = (score / 100f).coerceIn(0f, 1f) * 240f
                    val gradient = Brush.sweepGradient(
                        0.0f to CyberCyan,
                        0.4f to PositiveMint,
                        0.7f to ElectricIndigo,
                        1.0f to CyberCyan
                    )

                    drawArc(
                        brush = gradient,
                        startAngle = 150f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content inside dial
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "PERFORMANCE INDEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Big 1-Tap Quick Boost Button
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onQuickBoostClick()
                },
                enabled = !isBoosting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PositiveMint,
                    contentColor = Color(0xFF022C1C)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_quick_boost_now")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RocketLaunch,
                        contentDescription = "Boost",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBoosting) "TURBO BOOSTING HARDWARE..." else "1-TAP QUICK BOOST ALL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
