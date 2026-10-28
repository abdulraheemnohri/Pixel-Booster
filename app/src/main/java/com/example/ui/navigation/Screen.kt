package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.outlined.Build
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Home : Screen(
        route = "dashboard",
        title = "Home",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_item_home"
    )

    data object Booster : Screen(
        route = "booster",
        title = "Booster",
        selectedIcon = Icons.Filled.RocketLaunch,
        unselectedIcon = Icons.Outlined.RocketLaunch,
        testTag = "nav_item_booster"
    )

    data object Startup : Screen(
        route = "startup",
        title = "Startup",
        selectedIcon = Icons.Filled.Bolt,
        unselectedIcon = Icons.Outlined.Bolt,
        testTag = "nav_item_startup"
    )

    data object Health : Screen(
        route = "health",
        title = "Health",
        selectedIcon = Icons.Filled.Security,
        unselectedIcon = Icons.Outlined.Security,
        testTag = "nav_item_health"
    )

    data object Storage : Screen(
        route = "storage",
        title = "Storage",
        selectedIcon = Icons.Filled.SdStorage,
        unselectedIcon = Icons.Outlined.SdStorage,
        testTag = "nav_item_storage"
    )

    data object SpeedTest : Screen(
        route = "speedtest",
        title = "Speed",
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed,
        testTag = "nav_item_speedtest"
    )

    data object Usage : Screen(
        route = "usage",
        title = "Usage",
        selectedIcon = Icons.Filled.HourglassTop,
        unselectedIcon = Icons.Outlined.HourglassTop,
        testTag = "nav_item_usage"
    )

    data object History : Screen(
        route = "history",
        title = "History",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "nav_item_history"
    )

    data object Privacy : Screen(
        route = "privacy",
        title = "Privacy",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock,
        testTag = "nav_item_privacy"
    )

    data object Diagnostics : Screen(
        route = "diagnostics",
        title = "Tests",
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build,
        testTag = "nav_item_diagnostics"
    )

    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_item_settings"
    )

    data object More : Screen(
        route = "more",
        title = "More",
        selectedIcon = Icons.Filled.Apps,
        unselectedIcon = Icons.Outlined.Apps,
        testTag = "nav_item_more"
    )

    companion object {
        // Keep the primary navigation focused; secondary tools remain available from More.
        // Five destinations fit compact phone navigation without horizontal scrolling.
        // Diagnostics and other secondary destinations are reachable from More.
        val items: List<Screen>
            get() = listOf(Home, Booster, Storage, Usage, More)

        fun isSecondaryRoute(route: String): Boolean = route in setOf(
            Health.route,
            Startup.route,
            SpeedTest.route,
            History.route,
            Privacy.route,
            Diagnostics.route,
            Settings.route
        )
    }

}

