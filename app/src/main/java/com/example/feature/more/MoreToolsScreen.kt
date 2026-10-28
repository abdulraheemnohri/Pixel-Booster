package com.example.feature.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ListItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Screen

private data class ToolLink(val title: String, val description: String, val screen: Screen, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreToolsScreen(onNavigate: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val tools = listOf(
        ToolLink("Health", "Battery and device health findings", Screen.Health, Icons.Filled.Security),
        ToolLink("Startup & apps", "Review apps and Android-supported app settings", Screen.Startup, Icons.Filled.Bolt),
        ToolLink("Network speed test", "Run an optional network test", Screen.SpeedTest, Icons.Filled.Speed),
        ToolLink("History", "Review locally saved activity and results", Screen.History, Icons.Filled.BarChart),
        ToolLink("Privacy & permissions", "Review permissions and privacy information", Screen.Privacy, Icons.Filled.Lock),
        ToolLink("Diagnostics", "Run individual device checks and review honest results", Screen.Diagnostics, Icons.Filled.Build),
        ToolLink("Settings", "Theme, monitoring and app preferences", Screen.Settings, Icons.Filled.Settings)
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("More tools", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Your secondary tools stay available here, keeping the main navigation simple.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(tools) { tool ->
            Card(
                onClick = { onNavigate(tool.screen) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                ListItem(
                    headlineContent = { Text(tool.title) },
                    supportingContent = { Text(tool.description) },
                    leadingContent = { Icon(tool.icon, contentDescription = null) },
                    trailingContent = { Icon(Icons.Filled.Apps, contentDescription = "Open") }
                )
            }
        }
    }
}
