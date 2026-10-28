package com.example.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.DashboardDensity
import com.example.core.model.LogEvent
import com.example.core.model.LogSeverity
import com.example.core.model.ThemeMode
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.HighElevationDark
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.modernCard
import com.example.ui.theme.pulsingGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logEvents by viewModel.logEvents.collectAsStateWithLifecycle()

    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTroubleshootingDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    val hasActiveDialog = showPrivacyPolicyDialog || showTroubleshootingDialog || showClearHistoryDialog
    androidx.activity.compose.BackHandler(enabled = hasActiveDialog) {
        showPrivacyPolicyDialog = false
        showTroubleshootingDialog = false
        showClearHistoryDialog = false
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStats()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Settings & Preferences",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGrotesk
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = CyberCyan.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "SYSTEM CONFIG",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Theme mode, hardware thresholds, and telemetry profiling",
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .align(Alignment.TopCenter)
                    .testTag("settings_scroll_column"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. DEDICATED DARK MODE & APPEARANCE CARD
                item {
                    SettingsSectionHeader("DISPLAY & THEME PREFERENCES")
                }

                item {
                    DarkModeControllerCard(
                        currentThemeMode = uiState.settings.themeMode,
                        dynamicColor = uiState.settings.dynamicColor,
                        onThemeModeSelected = { viewModel.setThemeMode(it) },
                        onDynamicColorToggled = { viewModel.setDynamicColor(it) },
                        dashboardDensity = uiState.settings.dashboardDensity,
                        onDensitySelected = { viewModel.setDashboardDensity(it) }
                    )
                }

                // 2. MONITORING & HARDWARE ALERT RULES
                item {
                    SettingsSectionHeader("MONITORING & ALERT THRESHOLDS")
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Sampling interval
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Telemetry Sampling Interval",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGrotesk,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Live sensor sampling frequency while app is active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = HighElevationDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2538BDF8))
                                ) {
                                    Text(
                                        text = "${uiState.settings.refreshIntervalSeconds}s rate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2, 5, 10, 30).forEach { interval ->
                                    val selected = uiState.settings.refreshIntervalSeconds == interval
                                    FilterChip(
                                        selected = selected,
                                        onClick = { viewModel.setRefreshInterval(interval) },
                                        label = {
                                            Text(
                                                "${interval}s",
                                                fontFamily = SpaceGrotesk,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberCyan,
                                            selectedLabelColor = Color(0xFF070B14),
                                            containerColor = HighElevationDark,
                                            labelColor = TextPrimaryDark
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = selected,
                                            borderColor = Color(0x3538BDF8),
                                            selectedBorderColor = CyberCyan
                                        ),
                                        modifier = Modifier.weight(1f).testTag("interval_${interval}s")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Low battery threshold slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Low Battery Warning Mark",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Triggers alert when battery level drops below",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = WarningAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${uiState.settings.lowBatteryWarningThreshold}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Slider(
                                value = uiState.settings.lowBatteryWarningThreshold.toFloat(),
                                onValueChange = { viewModel.setLowBatteryThreshold(it.toInt()) },
                                valueRange = 10f..40f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = HighElevationDark
                                ),
                                modifier = Modifier.testTag("slider_battery_threshold")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // RAM Warning Threshold Slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "RAM Pressure Alert Threshold",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Alerts when system memory headroom falls below limit",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = WarningAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${uiState.settings.ramWarningThreshold}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningAmber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Slider(
                                value = uiState.settings.ramWarningThreshold.toFloat(),
                                onValueChange = { viewModel.setRamWarningThreshold(it.toInt()) },
                                valueRange = 60f..95f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = HighElevationDark
                                ),
                                modifier = Modifier.testTag("slider_ram_threshold")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Thermal Warnings toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Thermal Throttling Notifications",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Alerts when thermal pressure exceeds safe limits",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.thermalWarningEnabled,
                                    onCheckedChange = { viewModel.setThermalWarningEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = CyberCyan,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_thermal_warnings")
                                )
                            }
                        }
                    }
                }


                // NETWORK & SYSTEM PREFERENCES
                item {
                    SettingsSectionHeader("NETWORK & SYSTEM PREFERENCES")
                }

                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val actionLauncher = remember { com.example.core.platform.SystemActionLauncher(context) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "System Connectivity Launchers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGrotesk,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Directly adjust network interface configurations and private DNS settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { actionLauncher.openWifiSettings() },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighElevationDark, contentColor = CyberCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3038BDF8)),
                                    modifier = Modifier.weight(1f).testTag("btn_wifi_settings")
                                ) {
                                    Text("Wi-Fi Setup", fontFamily = SpaceGrotesk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { actionLauncher.openCellularNetworkSettings() },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighElevationDark, contentColor = PositiveMint),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3034D399)),
                                    modifier = Modifier.weight(1f).testTag("btn_cellular_settings")
                                ) {
                                    Text("Mobile Data", fontFamily = SpaceGrotesk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { actionLauncher.openPrivateDnsSettings() },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighElevationDark, contentColor = TextPrimaryDark),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF)),
                                    modifier = Modifier.weight(1f).testTag("btn_private_dns")
                                ) {
                                    Text("Private DNS", fontFamily = SpaceGrotesk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { actionLauncher.openDataSaverSettings() },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighElevationDark, contentColor = TextPrimaryDark),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x20FFFFFF)),
                                    modifier = Modifier.weight(1f).testTag("btn_data_saver")
                                ) {
                                    Text("Data Saver", fontFamily = SpaceGrotesk, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. AUTOMATION & RESPONSIVENESS
                item {
                    SettingsSectionHeader("AUTOMATION & HAPTICS")
                }


                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Auto cache clean
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Smart Cache Optimization",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Periodically purges obsolete temp files during idle periods",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.autoCacheCleanEnabled,
                                    onCheckedChange = { viewModel.setAutoCacheCleanEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = PositiveMint,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_auto_cache")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Haptics feedback
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Haptic Tactile Feedback",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Provide vibration confirmation on booster button actions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.hapticFeedbackEnabled,
                                    onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = CyberCyan,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_haptics")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Auto RAM Trim
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto RAM Heap Reclaim",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Periodically trims inactive memory buffers and runs garbage collection",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.autoRamTrimEnabled,
                                    onCheckedChange = { viewModel.setAutoRamTrimEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = CyberCyan,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_auto_ram_trim")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Auto Thermal Cooling
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto Thermal Mitigation",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Automatically throttles heavy background tasks when SoC temp > 42°C",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.autoThermalCoolingEnabled,
                                    onCheckedChange = { viewModel.setAutoThermalCoolingEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = WarningAmber,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_auto_thermal")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Battery Saver Automation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Smart Battery Saver Trigger",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Engages system battery saver mode automatically at 20% charge",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.batterySaverAutomationEnabled,
                                    onCheckedChange = { viewModel.setBatterySaverAutomationEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = PositiveMint,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_battery_saver_auto")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Background Network Throttle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Background Network Limiter",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Limits background socket polling and sync during standby",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.settings.backgroundNetworkThrottleEnabled,
                                    onCheckedChange = { viewModel.setBackgroundNetworkThrottleEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF070B14),
                                        checkedTrackColor = CyberCyan,
                                        uncheckedTrackColor = HighElevationDark
                                    ),
                                    modifier = Modifier.testTag("switch_network_throttle")
                                )
                            }
                        }
                    }
                }


                // 4. PRIVACY & DATA CONTROLS
                item {
                    SettingsSectionHeader("PRIVACY & DATA CONTROLS")
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Holographic Zero Egress Banner
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = PositiveMint.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = PositiveMint.copy(alpha = 0.2f),
                                        modifier = Modifier.size(36.dp)
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
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "100% On-Device & Zero Egress",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SpaceGrotesk,
                                            color = PositiveMint
                                        )
                                        Text(
                                            text = "Telemetry is processed strictly in local RAM and stored in local Room database.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Action buttons
                            Button(
                                onClick = { viewModel.exportDiagnosticReport() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = Color(0xFF070B14)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_export_report")
                            ) {
                                Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Diagnostic Telemetry Report", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showPrivacyPolicyDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("btn_privacy_policy")
                                ) {
                                    Text("Privacy Policy", fontFamily = SpaceGrotesk)
                                }

                                OutlinedButton(
                                    onClick = { showClearHistoryDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed),
                                    modifier = Modifier.weight(1f).testTag("btn_clear_history_settings")
                                ) {
                                    Text("Clear Data", fontFamily = SpaceGrotesk)
                                }
                            }
                        }
                    }
                }

                // 5. IN-APP EVENT LOGS (Cyber Terminal Style)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsSectionHeader("IN-APP EVENT LOGS")
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = CriticalRed.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { viewModel.clearLogs() }
                        ) {
                            Text(
                                text = "Clear Logs",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = SpaceGrotesk,
                                color = CriticalRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("btn_clear_logs")
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            OutlinedTextField(
                                value = uiState.logSearchQuery,
                                onValueChange = { viewModel.setLogSearchQuery(it) },
                                placeholder = { Text("Filter logs...", style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = Color(0x3538BDF8),
                                    focusedContainerColor = HighElevationDark,
                                    unfocusedContainerColor = HighElevationDark
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("input_log_search")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (logEvents.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No telemetry events found",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = SpaceGrotesk,
                                        color = TextSecondaryDark
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    logEvents.take(15).forEach { event ->
                                        LogEventItemRow(event = event)
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. ABOUT PIXEL BOOSTER
                item {
                    SettingsSectionHeader("ABOUT PIXEL BOOSTER")
                }

                item {
                    val specs = viewModel.getDeviceSpecs()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp)),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = HighElevationDark,
                                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Speed,
                                                contentDescription = null,
                                                tint = CyberCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Pixel Booster Advanced",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGrotesk,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = HighElevationDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3538BDF8))
                                ) {
                                    Text(
                                        text = "v2.2.0",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan,
                                        fontFamily = SpaceGrotesk,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HighElevationDark.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Device: ${specs.manufacturer} ${specs.model} (${specs.pixelGenerationName})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "SoC: ${specs.socModel} • OS: Android ${specs.androidVersion} (API ${specs.sdkInt})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = { showTroubleshootingDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_troubleshooting")
                            ) {
                                Icon(imageVector = Icons.Filled.BugReport, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pixel Hardware Compatibility Guide", fontFamily = SpaceGrotesk, color = CyberCyan)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Dialogs
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text(
                    "Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk
                )
            },
            text = {
                Text(
                    "Pixel Booster operates entirely on-device without telemetry transmission, remote analytics, or external ads. All data remains stored locally in Android app storage.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF070B14))
                ) { Text("Close", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }

    if (showTroubleshootingDialog) {
        AlertDialog(
            onDismissRequest = { showTroubleshootingDialog = false },
            title = {
                Text(
                    "Hardware Compatibility Guide",
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk
                )
            },
            text = {
                Text(
                    "• Google Tensor G-Series: Full hardware thermal telemetry and PowerManager listener support.\n" +
                        "• Battery Health: Coulomb charge counter accurately reads real cell capacity.\n" +
                        "• Startup Management: Broadcast receivers responding to boot triggers can be safely restricted to accelerate boot latency.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = { showTroubleshootingDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF070B14))
                ) { Text("Got It", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    "Clear All Data?",
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk
                )
            },
            text = { Text("This will erase all diagnostic history and logs stored locally.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                ) { Text("Clear All", fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel", fontFamily = SpaceGrotesk) }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    }
}

@Composable
fun DarkModeControllerCard(
    currentThemeMode: ThemeMode,
    dynamicColor: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorToggled: (Boolean) -> Unit,
    dashboardDensity: DashboardDensity,
    onDensitySelected: (DashboardDensity) -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isEffectiveDark = when (currentThemeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .modernCard(
                shape = RoundedCornerShape(22.dp),
                borderBrush = ModernDesignTokens.AccentBorderBrush
            )
            .testTag("dark_mode_controller_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .pulsingGlow(glowColor = CyberCyan, minAlpha = 0.15f, maxAlpha = 0.40f),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HighElevationDark,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isEffectiveDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "THEME & VISUAL ARCHITECTURE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            fontFamily = SpaceGrotesk
                        )
                        Text(
                            text = when (currentThemeMode) {
                                ThemeMode.SYSTEM -> "System Auto-Sync (${if (isSystemDark) "Dark" else "Light"})"
                                ThemeMode.DARK -> "Forced Cyber Dark (OLED)"
                                ThemeMode.LIGHT -> "Forced Clean Light"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = SpaceGrotesk,
                            color = PositiveMint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = HighElevationDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3538BDF8))
                ) {
                    Text(
                        text = currentThemeMode.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SpaceGrotesk,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3-Way Segmented Control for Dark Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeModeSegmentButton(
                    title = "System Sync",
                    icon = Icons.Filled.BrightnessAuto,
                    isSelected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeSelected(ThemeMode.SYSTEM) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_system"
                )

                ThemeModeSegmentButton(
                    title = "Force Dark",
                    icon = Icons.Filled.DarkMode,
                    isSelected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onThemeModeSelected(ThemeMode.DARK) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_dark"
                )

                ThemeModeSegmentButton(
                    title = "Force Light",
                    icon = Icons.Filled.LightMode,
                    isSelected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                    modifier = Modifier.weight(1f),
                    testTag = "theme_light"
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic Color Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Material You Dynamic Accent",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Adapts color palette to Android wallpaper monet tones",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = dynamicColor,
                    onCheckedChange = { onDynamicColorToggled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF070B14),
                        checkedTrackColor = CyberCyan,
                        uncheckedTrackColor = HighElevationDark
                    ),
                    modifier = Modifier.testTag("switch_dynamic_color")
                )
            }
        }
    }
}

@Composable
fun ThemeModeSegmentButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) CyberCyan else HighElevationDark,
        animationSpec = tween(250),
        label = "segment_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF070B14) else TextPrimaryDark,
        animationSpec = tween(250),
        label = "segment_fg"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0x3538BDF8)) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = SpaceGrotesk,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        fontFamily = SpaceGrotesk,
        letterSpacing = 1.sp
    )
}

@Composable
fun LogEventItemRow(event: LogEvent) {
    val severityColor = when (event.severity) {
        LogSeverity.ERROR -> CriticalRed
        LogSeverity.WARNING -> WarningAmber
        LogSeverity.INFO -> CyberCyan
        LogSeverity.VERBOSE -> TextSecondaryDark
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = HighElevationDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(severityColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${event.category.name} • ${event.formattedTimestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = SpaceGrotesk,
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
            }
        }
    }
}
