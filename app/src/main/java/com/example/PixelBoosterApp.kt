package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsets.Companion.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.preferences.UserPreferencesRepository
import com.example.feature.booster.BoosterScreen
import com.example.feature.booster.BoosterViewModel
import com.example.feature.dashboard.DashboardScreen
import com.example.feature.dashboard.DashboardViewModel
import com.example.feature.health.HealthScreen
import com.example.feature.health.HealthViewModel
import com.example.feature.history.HistoryScreen
import com.example.feature.history.HistoryViewModel
import com.example.feature.more.MoreToolsScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.settings.SettingsViewModel
import com.example.feature.startup.StartupScreen
import com.example.feature.startup.StartupViewModel
import com.example.feature.usage.UsageScreen
import com.example.feature.usage.UsageViewModel
import com.example.ui.navigation.PixelBoosterBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.PixelBoosterTheme

@Composable
fun PixelBoosterApp(
    preferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier
) {
    val settings by preferencesRepository.settingsFlow.collectAsStateWithLifecycle(
        initialValue = com.example.core.model.SettingsData()
    )

    PixelBoosterTheme(
        themeMode = settings.themeMode,
        dynamicColor = settings.dynamicColor
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

        val configuration = LocalConfiguration.current
        val isExpandedScreen = configuration.screenWidthDp >= 600

        if (isExpandedScreen) {
            // Adaptive wide layout: NavigationRail on side
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Screen.items.forEach { screen ->
                        val isSelected = currentRoute == screen.route || (screen == Screen.More && Screen.isSecondaryRoute(currentRoute))
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.testTag(screen.testTag)
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.widthIn(max = 1100.dp).fillMaxWidth().fillMaxHeight()
                    )
                }
            }
        } else {
            // Mobile layout: Bottom navigation bar
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    PixelBoosterBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavHost(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val dashboardViewModel: DashboardViewModel = viewModel()
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToHealth = {
                    navController.navigate(Screen.Health.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToBooster = {
                    navController.navigate(Screen.Booster.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToStartup = {
                    navController.navigate(Screen.Startup.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Booster.route) {
            val boosterViewModel: BoosterViewModel = viewModel()
            BoosterScreen(viewModel = boosterViewModel)
        }

        composable(Screen.Startup.route) {
            val startupViewModel: StartupViewModel = viewModel()
            StartupScreen(viewModel = startupViewModel)
        }

        composable(Screen.Storage.route) {
            val storageViewModel: com.example.feature.storage.StorageAnalyzerViewModel = viewModel()
            com.example.feature.storage.StorageAnalyzerScreen(viewModel = storageViewModel)
        }

        composable(Screen.SpeedTest.route) {
            val speedTestViewModel: com.example.feature.speedtest.SpeedTestViewModel = viewModel()
            com.example.feature.speedtest.SpeedTestScreen(viewModel = speedTestViewModel)
        }

        composable(Screen.Health.route) {
            val healthViewModel: HealthViewModel = viewModel()
            HealthScreen(viewModel = healthViewModel)
        }

        composable(Screen.Usage.route) {
            val usageViewModel: UsageViewModel = viewModel()
            UsageScreen(viewModel = usageViewModel)
        }

        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(viewModel = historyViewModel)
        }

        composable(Screen.Privacy.route) {
            val privacyViewModel: com.example.feature.privacy.PrivacyViewModel = viewModel()
            com.example.feature.privacy.PrivacyScreen(viewModel = privacyViewModel)
        }

        composable(Screen.Diagnostics.route) {
            val diagnosticsViewModel: com.example.feature.diagnostics.DiagnosticsViewModel = viewModel()
            com.example.feature.diagnostics.DiagnosticsScreen(viewModel = diagnosticsViewModel)
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(viewModel = settingsViewModel)
        }

        composable(Screen.More.route) {
            MoreToolsScreen(
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

    }
}
