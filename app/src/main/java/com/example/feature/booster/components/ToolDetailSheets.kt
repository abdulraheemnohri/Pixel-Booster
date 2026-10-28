package com.example.feature.booster.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.booster.BoosterToolType
import com.example.core.booster.PowerProfile
import com.example.core.model.BatteryInfoData
import com.example.core.model.MemoryInfoData
import com.example.core.model.StorageInfoData
import com.example.core.model.ThermalStatusData
import com.example.ui.theme.HighElevationDark
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailSheets(
    selectedTool: BoosterToolType?,
    memoryInfo: MemoryInfoData?,
    storageInfo: StorageInfoData?,
    batteryInfo: BatteryInfoData?,
    thermalStatus: ThermalStatusData?,
    activePowerProfile: PowerProfile,
    gameTurboActive: Boolean,
    onDismiss: () -> Unit,
    onRunRamBoost: () -> Unit,
    onRunJunkClean: () -> Unit,
    onRunCpuCooler: () -> Unit,
    onSelectPowerProfile: (PowerProfile) -> Unit,
    onRunNetworkTurbo: () -> Unit,
    onToggleGameTurbo: () -> Unit
) {
    if (selectedTool == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.testTag("tool_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            when (selectedTool) {
                BoosterToolType.RAM_BOOSTER -> RamBoosterContent(
                    memoryInfo = memoryInfo,
                    onBoost = {
                        onDismiss()
                        onRunRamBoost()
                    }
                )

                BoosterToolType.JUNK_CLEANER -> JunkCleanerContent(
                    storageInfo = storageInfo,
                    onClean = {
                        onDismiss()
                        onRunJunkClean()
                    }
                )

                BoosterToolType.CPU_COOLER -> CpuCoolerContent(
                    thermalStatus = thermalStatus,
                    batteryInfo = batteryInfo,
                    onCool = {
                        onDismiss()
                        onRunCpuCooler()
                    }
                )

                BoosterToolType.BATTERY_SAVER -> BatterySaverContent(
                    activeProfile = activePowerProfile,
                    batteryInfo = batteryInfo,
                    onSelectProfile = onSelectPowerProfile
                )

                BoosterToolType.NETWORK_TURBO -> NetworkTurboContent(
                    onBoost = {
                        onDismiss()
                        onRunNetworkTurbo()
                    }
                )

                BoosterToolType.GAME_TURBO -> GameTurboContent(
                    isActive = gameTurboActive,
                    onToggle = onToggleGameTurbo
                )
            }
        }
    }
}

@Composable
private fun RamBoosterContent(
    memoryInfo: MemoryInfoData?,
    onBoost: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Memory, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "RAM Memory Booster",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Trims background memory caches, releases JVM heap allocations, and expands active RAM headroom for smooth multitasking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (memoryInfo != null) {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, OutlineDark, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = HighElevationDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Memory Load", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                        Text(
                            text = "${memoryInfo.formattedUsed} / ${memoryInfo.formattedTotal}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { memoryInfo.usedPercentage / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (memoryInfo.usedPercentage > 85f) WarningAmber else PositiveMint,
                        trackColor = MaterialTheme.colorScheme.surface,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${memoryInfo.formattedAvailable} available headroom remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = PositiveMint
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBoost,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color(0xFF0A1020)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_boost_ram_confirm")
        ) {
            Text(text = "Reclaim RAM Headroom", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun JunkCleanerContent(
    storageInfo: StorageInfoData?,
    onClean: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = PositiveMint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Deep Junk & Cache Cleaner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Android does not let ordinary apps inspect or delete other apps’ private caches, photos, thumbnails, or system logs. This action is limited to cache data owned by Pixel Booster.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Device storage available",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = storageInfo?.formattedFree ?: "Unavailable",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "This is device storage information, not a claim about reclaimable cache.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onClean,
            colors = ButtonDefaults.buttonColors(containerColor = PositiveMint, contentColor = Color(0xFF06140D)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_clean_junk_confirm")
        ) {
            Text(text = "Clear Pixel Booster app cache", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun JunkItemTile(title: String, size: String, description: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = HighElevationDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
            }
            Text(text = size, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PositiveMint)
        }
    }
}

@Composable
private fun CpuCoolerContent(
    thermalStatus: ThermalStatusData?,
    batteryInfo: BatteryInfoData?,
    onCool: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Thermostat, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "CPU Cooler & Thermal Engine",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Shows thermal information when Android exposes it and offers supported guidance. An ordinary app cannot force-cool the processor or guarantee higher frame rates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, OutlineDark, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = HighElevationDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Sensor Telemetry", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                    Text(
                        text = "Status: ${thermalStatus?.statusName ?: "Unavailable"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Battery Temp: ${batteryInfo?.formattedTemperature ?: "Unavailable"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = thermalStatus?.recommendation ?: "Thermal status is not available from Android on this device or at this time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCool,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color(0xFF0A1020)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_cool_cpu_confirm")
        ) {
            Text(text = "Start CPU Cooldown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BatterySaverContent(
    activeProfile: PowerProfile,
    batteryInfo: BatteryInfoData?,
    onSelectProfile: (PowerProfile) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = PositiveMint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Battery Power Profiles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select an intelligent power profile to balance performance and battery endurance.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        PowerProfile.entries.forEach { profile ->
            val isSelected = activeProfile == profile
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else HighElevationDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onSelectProfile(profile) }
                    .border(
                        1.dp,
                        if (isSelected) PrimaryBlue else OutlineDark,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectProfile(profile) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = PrimaryBlue,
                            unselectedColor = TextSecondaryDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryBlue else TextPrimaryDark
                        )
                        Text(
                            text = profile.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkTurboContent(
    onBoost: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.NetworkCheck, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Network Turbo Accelerator",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pings Google DNS servers, refreshes socket buffers, flushes DNS cache buffers, and minimizes packet round-trip delay.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBoost,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue, contentColor = Color(0xFF0A1020)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_accelerate_network_confirm")
        ) {
            Text(text = "Accelerate & Flush Latency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameTurboContent(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.SportsEsports, contentDescription = null, tint = PositiveMint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Game Turbo Mode",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Locks hardware focus on the active foreground 3D game, prioritizes GPU rendering pipelines, and prevents frame drops.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(1.dp, OutlineDark, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = HighElevationDark)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isActive) "Game Turbo ACTIVE" else "Game Turbo Inactive",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) PositiveMint else TextPrimaryDark
                    )
                    Text(
                        text = if (isActive) "Memory headroom locked for active games" else "Standard Android background scheduling",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF06140D),
                        checkedTrackColor = PositiveMint,
                        uncheckedThumbColor = TextSecondaryDark,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("switch_game_turbo")
                )
            }
        }
    }
}
