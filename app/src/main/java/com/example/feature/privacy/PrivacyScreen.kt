package com.example.feature.privacy

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.PrivacySensorState
import com.example.core.model.PrivacySensorType
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.modernCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val report = uiState.privacyReport

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Privacy & Security Shield",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGrotesk
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = PositiveMint.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "ZERO EGRESS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PositiveMint,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Microphone, Camera, USB OTG, and Sensor Guardrails",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    .testTag("privacy_scroll_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HERO SHIELD SCORE CARD
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Brush.horizontalGradient(listOf(PositiveMint.copy(alpha = 0.6f), CyberCyan.copy(alpha = 0.3f))),
                                RoundedCornerShape(26.dp)
                            )
                            .testTag("privacy_hero_card"),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = PositiveMint.copy(alpha = 0.16f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Security,
                                                contentDescription = null,
                                                tint = PositiveMint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "HARDWARE SHIELD STATUS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "All Sensors Locked & Audited",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${report.permissionAuditScore}% Score",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PositiveMint,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Active Sensors Tile
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2538BDF8)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "ACTIVE HOOKS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${report.activeSensorsCount} Sensors",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberCyan
                                        )
                                    }
                                }

                                // Blocked Trackers Tile
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2534D399)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "BLOCKED TRACKERS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "${report.blockedTrackersCount} Trackers",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PositiveMint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. KILL-SWITCHES & PERIPHERAL LOCKS
                item {
                    Text(
                        text = "HARDWARE KILL-SWITCHES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Camera Kill-Switch
                            PrivacySwitchRow(
                                title = "Camera Hardware Kill-Switch",
                                subtitle = "Instantly disables sensor power and blocks optical feeds",
                                checked = uiState.cameraKillSwitchEnabled,
                                onCheckedChange = { viewModel.toggleCameraKillSwitch(it) },
                                icon = Icons.Filled.CameraAlt,
                                testTag = "switch_camera_kill"
                            )

                            // Microphone Kill-Switch
                            PrivacySwitchRow(
                                title = "Microphone Hardware Mute",
                                subtitle = "Cuts audio recording bus across all system channels",
                                checked = uiState.micKillSwitchEnabled,
                                onCheckedChange = { viewModel.toggleMicKillSwitch(it) },
                                icon = Icons.Filled.Mic,
                                testTag = "switch_mic_kill"
                            )

                            // USB OTG Lock
                            PrivacySwitchRow(
                                title = "USB OTG Data Port Lock",
                                subtitle = "Blocks unauthenticated data transfer (Charge-Only mode)",
                                checked = uiState.usbAccessLocked,
                                onCheckedChange = { viewModel.toggleUsbLock(it) },
                                icon = Icons.Filled.Usb,
                                testTag = "switch_usb_lock"
                            )

                            // Location Fence
                            PrivacySwitchRow(
                                title = "Location Satellite Fencing",
                                subtitle = "Restricts background GPS requests to protected zone",
                                checked = uiState.locationLockEnabled,
                                onCheckedChange = { viewModel.toggleLocationLock(it) },
                                icon = Icons.Filled.LocationOn,
                                testTag = "switch_location_lock"
                            )
                        }
                    }
                }

                // 3. SENSOR AUDIT LIST
                item {
                    Text(
                        text = "HARDWARE SENSOR TELEMETRY AUDIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(report.sensors) { sensor ->
                    SensorAuditCard(sensor = sensor)
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PrivacySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String
) {
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
                color = if (checked) CriticalRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) CriticalRed else CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF070B14),
                checkedTrackColor = CriticalRed,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun SensorAuditCard(sensor: PrivacySensorState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(Color(0x3038BDF8), Color(0x101E293B))),
                RoundedCornerShape(16.dp)
            ),
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
                    text = sensor.sensorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = if (sensor.isCurrentlyInUse) CriticalRed.copy(alpha = 0.15f) else PositiveMint.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (sensor.isCurrentlyInUse) CriticalRed.copy(alpha = 0.4f) else PositiveMint.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (sensor.isCurrentlyInUse) "IN USE" else "SECURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sensor.isCurrentlyInUse) CriticalRed else PositiveMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = sensor.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Last Hook: ${sensor.lastAccessedApp}",
                style = MaterialTheme.typography.labelSmall,
                color = CyberCyan,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
