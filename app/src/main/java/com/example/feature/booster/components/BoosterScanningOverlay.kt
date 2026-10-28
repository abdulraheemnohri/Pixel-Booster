package com.example.feature.booster.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.booster.BoosterStage
import com.example.ui.theme.HighElevationDark
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoosterScanningOverlay(
    isBoosting: Boolean,
    stage: BoosterStage,
    stageText: String,
    progress: Float
) {
    if (!isBoosting) return

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "radarRotate"
    )

    BasicAlertDialog(
        onDismissRequest = { /* Block dismiss while active boosting */ },
        modifier = Modifier.testTag("booster_scanning_overlay")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rotating Rocket icon in glowing circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(HighElevationDark, CircleShape)
                        .border(2.dp, PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RocketLaunch,
                        contentDescription = "Boosting",
                        tint = PositiveMint,
                        modifier = Modifier
                            .size(36.dp)
                            .rotate(rotation)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "HARDWARE ACCELERATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(18.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = PositiveMint,
                    trackColor = HighElevationDark,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pipeline Stages Status List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StageItemRow(
                        title = "RAM Heap & Buffer Reclaim",
                        isDone = progress >= 0.45f,
                        isActive = stage == BoosterStage.OPTIMIZING_RAM
                    )
                    StageItemRow(
                        title = "Residual Junk & Cache Purge",
                        isDone = progress >= 0.70f,
                        isActive = stage == BoosterStage.CLEANING_CACHE
                    )
                    StageItemRow(
                        title = "SoC Thermal Cooldown & Clock Throttle",
                        isDone = progress >= 0.90f,
                        isActive = stage == BoosterStage.COOLING_HARDWARE
                    )
                    StageItemRow(
                        title = "Socket Buffer & Network Latency Tuning",
                        isDone = progress >= 1.0f,
                        isActive = stage == BoosterStage.ACCELERATING_NETWORK
                    )
                }
            }
        }
    }
}

@Composable
private fun StageItemRow(
    title: String,
    isDone: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = PositiveMint,
                modifier = Modifier.size(16.dp)
            )
        } else if (isActive) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = PrimaryBlue
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(HighElevationDark, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDone) PositiveMint else if (isActive) TextPrimaryDark else TextSecondaryDark,
            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal
        )
    }
}
