package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DynamicIslandApp
import com.example.data.model.CutoutPosition
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = DynamicIslandApp.instance
    val settingsDataStore = app.settingsDataStore
    val settings by app.islandManager.settings.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Island Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Permissions Shortcut
            item {
                Card(
                    onClick = onNavigateToPermissions,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Permissions & Access",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Manage overlay & notification listeners",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Dimensions & Position
            item {
                Text(
                    text = "DIMENSIONS & POSITION",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Camera Cutout Position
                        Text(
                            text = "Camera Cutout Alignment",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CutoutPosition.values().forEach { pos ->
                                FilterChip(
                                    selected = settings.cutoutPosition == pos,
                                    onClick = {
                                        scope.launch {
                                            settingsDataStore.updateSettings { it.copy(cutoutPosition = pos) }
                                        }
                                    },
                                    label = { Text(pos.name) }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Width Slider
                        Text(
                            text = "Compact Width: ${settings.islandWidthDp} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = settings.islandWidthDp.toFloat(),
                            onValueChange = { value ->
                                scope.launch {
                                    settingsDataStore.updateSettings { it.copy(islandWidthDp = value.toInt()) }
                                }
                            },
                            valueRange = 160f..280f,
                            steps = 12
                        )

                        Spacer(Modifier.height(8.dp))

                        // Height Slider
                        Text(
                            text = "Compact Height: ${settings.islandHeightDp} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = settings.islandHeightDp.toFloat(),
                            onValueChange = { value ->
                                scope.launch {
                                    settingsDataStore.updateSettings { it.copy(islandHeightDp = value.toInt()) }
                                }
                            },
                            valueRange = 30f..48f,
                            steps = 9
                        )

                        Spacer(Modifier.height(8.dp))

                        // Y Offset Slider
                        Text(
                            text = "Top Offset (Margin): ${settings.islandYOffsetDp} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = settings.islandYOffsetDp.toFloat(),
                            onValueChange = { value ->
                                scope.launch {
                                    settingsDataStore.updateSettings { it.copy(islandYOffsetDp = value.toInt()) }
                                }
                            },
                            valueRange = 0f..40f,
                            steps = 20
                        )

                        Spacer(Modifier.height(8.dp))

                        // Corner Radius Slider
                        Text(
                            text = "Corner Radius: ${settings.islandCornerRadiusDp} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = settings.islandCornerRadiusDp.toFloat(),
                            onValueChange = { value ->
                                scope.launch {
                                    settingsDataStore.updateSettings { it.copy(islandCornerRadiusDp = value.toInt()) }
                                }
                            },
                            valueRange = 14f..30f,
                            steps = 8
                        )
                    }
                }
            }

            // Auto-collapse duration
            item {
                Text(
                    text = "BEHAVIOR & TIMING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Auto-Collapse Duration: ${settings.autoCollapseDurationSeconds}s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Automatically returns from expanded card to compact pill",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.autoCollapseDurationSeconds.toFloat(),
                            onValueChange = { value ->
                                scope.launch {
                                    settingsDataStore.updateSettings {
                                        it.copy(autoCollapseDurationSeconds = value.toInt())
                                    }
                                }
                            },
                            valueRange = 2f..10f,
                            steps = 8
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Haptic Feedback", fontWeight = FontWeight.Medium)
                                    Text("Vibrate on expand / tap", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = settings.enableVibration,
                                onCheckedChange = { v ->
                                    scope.launch {
                                        settingsDataStore.updateSettings { it.copy(enableVibration = v) }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Feature Toggles
            item {
                Text(
                    text = "FEATURE INTEGRATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FeatureToggleRow(
                            title = "Notifications",
                            subtitle = "Show incoming app notifications",
                            icon = Icons.Default.Notifications,
                            checked = settings.enableNotifications,
                            onCheckedChange = { v ->
                                scope.launch { settingsDataStore.updateSettings { it.copy(enableNotifications = v) } }
                            }
                        )

                        FeatureToggleRow(
                            title = "Music Player",
                            subtitle = "Show MediaSession music controls",
                            icon = Icons.Default.MusicNote,
                            checked = settings.enableMusic,
                            onCheckedChange = { v ->
                                scope.launch { settingsDataStore.updateSettings { it.copy(enableMusic = v) } }
                            }
                        )

                        FeatureToggleRow(
                            title = "Phone Calls",
                            subtitle = "Show ongoing call status & audio actions",
                            icon = Icons.Default.Call,
                            checked = settings.enableCalls,
                            onCheckedChange = { v ->
                                scope.launch { settingsDataStore.updateSettings { it.copy(enableCalls = v) } }
                            }
                        )

                        FeatureToggleRow(
                            title = "Timer & Stopwatch",
                            subtitle = "Show countdown and elapsed time",
                            icon = Icons.Default.Timer,
                            checked = settings.enableTimer,
                            onCheckedChange = { v ->
                                scope.launch { settingsDataStore.updateSettings { it.copy(enableTimer = v) } }
                            }
                        )

                        FeatureToggleRow(
                            title = "Battery Charging",
                            subtitle = "Animate on power plug-in",
                            icon = Icons.Default.Bolt,
                            checked = settings.enableBatteryCharging,
                            onCheckedChange = { v ->
                                scope.launch { settingsDataStore.updateSettings { it.copy(enableBatteryCharging = v) } }
                            }
                        )

                        FeatureToggleRow(
                            title = "Downloads",
                            subtitle = "Show active download progress bar",
                            icon = Icons.Default.Download,
                            checked = settings.enableDownloads,
                            onCheckedChange = { v ->
                                scope.launch { settingsDataStore.updateSettings { it.copy(enableDownloads = v) } }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
