package com.example.feature.usage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.AppUsageData
import com.example.core.model.UsagePeriod
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ModernDesignTokens
import com.example.ui.theme.PositiveMint
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.modernCard
import com.example.ui.theme.pulsingGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreen(
    viewModel: UsageViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Recheck permission when returning to screen
    LaunchedEffect(Unit) {
        viewModel.checkPermissionAndLoad()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "App Usage Insights",
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
                                    text = "PROFILER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "On-device foreground time & system resource breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.checkPermissionAndLoad() },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.testTag("action_usage_refresh")
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CyberCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh usage statistics",
                                tint = CyberCyan
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
        if (!uiState.isPermissionGranted) {
            UsagePermissionDeniedContent(
                onEnableClick = { viewModel.openUsageAccessSettings() },
                onRefreshClick = { viewModel.checkPermissionAndLoad() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
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
                        .testTag("usage_scroll_column"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                // Period Tabs (Today / 7 Days / 30 Days) with Futuristic Dock Style
                item {
                    val periods = listOf(
                        UsagePeriod.TODAY to "Today",
                        UsagePeriod.PAST_7_DAYS to "Past 7 Days",
                        UsagePeriod.PAST_30_DAYS to "Past 30 Days"
                    )
                    val selectedIndex = periods.indexOfFirst { it.first == uiState.selectedPeriod }.coerceAtLeast(0)

                    TabRow(
                        selectedTabIndex = selectedIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        indicator = { tabPositions ->
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                color = CyberCyan,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x3538BDF8), RoundedCornerShape(14.dp))
                    ) {
                        periods.forEachIndexed { index, (period, title) ->
                            Tab(
                                selected = selectedIndex == index,
                                onClick = { viewModel.setPeriod(period) },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedIndex == index) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.testTag("tab_usage_${period.name.lowercase()}")
                            )
                        }
                    }
                }

                // Aggregate Summary Hero Card with Cyber Glass & Digital Readout
                item {
                    val totalSec = uiState.totalForegroundMillis / 1000
                    val totalHours = totalSec / 3600
                    val totalMins = (totalSec % 3600) / 60
                    val totalFormatted = if (totalHours > 0) "${totalHours}h ${totalMins}m" else "${totalMins}m"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .modernCard(shape = RoundedCornerShape(22.dp))
                            .testTag("usage_summary_card"),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PositiveMint)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AGGREGATE FOREGROUND PROFILE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp,
                                        fontFamily = SpaceGrotesk
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(9999.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2538BDF8))
                                ) {
                                    Text(
                                        text = "${uiState.usageList.size} Active Apps",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = totalFormatted,
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGrotesk,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Total active on-screen session time across all apps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .pulsingGlow(glowColor = CyberCyan, minAlpha = 0.15f, maxAlpha = 0.40f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        border = androidx.compose.foundation.BorderStroke(2.dp, ModernDesignTokens.PrimaryGlowBrush),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.HourglassTop,
                                                contentDescription = null,
                                                tint = CyberCyan,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Top app highlight preview if available
                            if (uiState.usageList.isNotEmpty()) {
                                val topApp = uiState.usageList.first()
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1538BDF8)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Peak: ${topApp.appLabel}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${topApp.formattedForegroundTime} (${String.format(java.util.Locale.US, "%.1f", topApp.percentageOfTotal)}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = SpaceGrotesk,
                                            color = PositiveMint,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sort selector Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Sort,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "APP TELEMETRY BREAKDOWN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = uiState.sortOrder == UsageSortOrder.DURATION,
                                onClick = { viewModel.setSortOrder(UsageSortOrder.DURATION) },
                                label = {
                                    Text(
                                        "Duration",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SpaceGrotesk
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan,
                                    selectedLabelColor = Color(0xFF070B14),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = uiState.sortOrder == UsageSortOrder.DURATION,
                                    borderColor = Color(0x3538BDF8),
                                    selectedBorderColor = CyberCyan
                                ),
                                modifier = Modifier.testTag("sort_duration")
                            )

                            FilterChip(
                                selected = uiState.sortOrder == UsageSortOrder.NAME,
                                onClick = { viewModel.setSortOrder(UsageSortOrder.NAME) },
                                label = {
                                    Text(
                                        "Name",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SpaceGrotesk
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyan,
                                    selectedLabelColor = Color(0xFF070B14),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = uiState.sortOrder == UsageSortOrder.NAME,
                                    borderColor = Color(0x3538BDF8),
                                    selectedBorderColor = CyberCyan
                                ),
                                modifier = Modifier.testTag("sort_name")
                            )
                        }
                    }
                }

                // App Usage Items
                if (uiState.usageList.isEmpty() && !uiState.isLoading) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .modernCard(shape = RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x2538BDF8)),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.HourglassTop,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Usage Telemetry Recorded",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SpaceGrotesk,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Foreground statistics will populate automatically as applications are actively opened on your device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.usageList) { appUsage ->
                        AppUsageItemCard(appUsage = appUsage)
                    }
                }

                // Usage Disclaimer
                item {
                    UsageDisclaimerCard()
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
}

@Composable
fun AppUsageItemCard(appUsage: AppUsageData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .modernCard(shape = RoundedCornerShape(16.dp))
            .testTag("usage_item_${appUsage.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // App initial avatar with cyber glowing ring
                    val initial = appUsage.appLabel.firstOrNull()?.uppercase() ?: "A"
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGrotesk,
                                color = CyberCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = appUsage.appLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = appUsage.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = appUsage.formattedForegroundTime,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = PositiveMint.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PositiveMint.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", appUsage.percentageOfTotal)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold,
                            color = PositiveMint,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Dual Gradient Progress Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                val fraction = (appUsage.percentageOfTotal / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(9999.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(CyberCyan, MaterialTheme.colorScheme.primary, ElectricIndigo)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun UsagePermissionDeniedContent(
    onEnableClick: () -> Unit,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .pulsingGlow(glowColor = CyberCyan, minAlpha = 0.2f, maxAlpha = 0.5f),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = androidx.compose.foundation.BorderStroke(2.dp, ModernDesignTokens.PrimaryGlowBrush),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Usage Telemetry Access Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = SpaceGrotesk,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To inspect on-device application foreground duration and resource profiling, Android requires granting Usage Access permission in system settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .modernCard(shape = RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = PositiveMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LOCAL PRIVACY SAFEGUARDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = PositiveMint,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 100% On-Device: App usage durations are processed strictly in local device memory.\n• Zero Network Access: Pixel Booster has no internet access capability and cannot transmit data off your device.\n• No In-App Accounts: Zero trackers, telemetry beacons, or advertising SDKs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onEnableClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color(0xFF070B14)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_enable_usage_access")
        ) {
            Text(
                text = "Enable Usage Access in Settings",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGrotesk
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRefreshClick,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_check_permission_again")
        ) {
            Text(
                text = "I've Enabled It — Check Again",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = SpaceGrotesk,
                color = CyberCyan
            )
        }
    }
}

@Composable
fun UsageDisclaimerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .modernCard(shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "USAGE DURATION VS. BATTERY CONSUMPTION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGrotesk
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Foreground duration represents active screen-on time. It is not proportional to battery drain; a lightweight text reader used for an hour consumes vastly less energy than 10 minutes of HDR video recording or GPU gaming.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
