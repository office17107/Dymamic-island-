package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DynamicIslandApp
import com.example.data.model.IslandEvent
import com.example.data.model.IslandState
import com.example.service.OverlayService
import com.example.ui.island.DynamicIsland
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToActivities: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = DynamicIslandApp.instance
    val islandManager = app.islandManager
    val settingsDataStore = app.settingsDataStore
    val scope = rememberCoroutineScope()

    val settings by islandManager.settings.collectAsState()
    val activeEvent by islandManager.activeEvent.collectAsState()
    val islandState by islandManager.islandState.collectAsState()

    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    LaunchedEffect(Unit) {
        hasOverlayPermission = Settings.canDrawOverlays(context)
        if (hasOverlayPermission && settings.isOverlayEnabled) {
            OverlayService.start(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp, 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF38BDF8))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Dynamic Island",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
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
            // 1. Live Interactive Island Sandbox
            item {
                Text(
                    text = "LIVE ISLAND SANDBOX",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF181824), Color(0xFF0D0D14))
                            )
                        )
                        .border(1.dp, Color(0xFF27273A), RoundedCornerShape(24.dp)),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Interactive Live Preview (Tap or Drag)",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dynamic Island in sandbox preview
                        DynamicIsland(
                            event = activeEvent,
                            state = if (islandState == IslandState.Hidden) IslandState.Compact else islandState,
                            settings = settings,
                            onExpand = { islandManager.expandIsland() },
                            onCollapse = { islandManager.collapseIsland() },
                            onDismiss = { islandManager.dismissActiveEvent() },
                            onToggleMusic = { islandManager.toggleMusicPlayback() },
                            onNextMusic = { islandManager.nextMusicTrack() },
                            onPrevMusic = { islandManager.previousMusicTrack() },
                            onPauseTimer = { islandManager.pauseTimer() },
                            onResumeTimer = { islandManager.resumeTimer() },
                            onResetTimer = { islandManager.resetTimer() },
                            onToggleMuteCall = { islandManager.toggleMuteCall() },
                            onToggleSpeakerCall = { islandManager.toggleSpeakerCall() },
                            onEndCall = { islandManager.endCall() },
                            onPauseDownload = { islandManager.pauseDownload() },
                            onResumeDownload = { islandManager.resumeDownload() },
                            onCancelDownload = { islandManager.cancelDownload() }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sandbox control buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = { islandManager.toggleExpandCollapse() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (islandState == IslandState.Expanded) "Collapse" else "Expand",
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { islandManager.dismissActiveEvent() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Dismiss", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Permission Status Banner (if overlay not granted)
            item {
                if (!hasOverlayPermission) {
                    ElevatedCard(
                        onClick = onNavigateToPermissions,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFF3B1E1E)
                        ),
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
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Overlay Permission Required",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Allow overlay to float over all applications",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFFF87171)
                            )
                        }
                    }
                } else {
                    // Overlay Service Master Switch Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (settings.isOverlayEnabled) Color(0xFF10B981) else Color(0xFF64748B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "System Overlay",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (settings.isOverlayEnabled) "Floating service active" else "Service paused",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = settings.isOverlayEnabled,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        settingsDataStore.updateSettings { it.copy(isOverlayEnabled = enabled) }
                                        if (enabled) {
                                            OverlayService.start(context)
                                        } else {
                                            OverlayService.stop(context)
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("overlay_master_switch")
                            )
                        }
                    }
                }
            }

            // 3. Quick Scenario Simulator Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUICK SIMULATION TRIGGERS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tap to test",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Simulation items
            val triggerItems = listOf(
                TriggerItem(
                    title = "WhatsApp",
                    subtitle = "Message from Ahmed",
                    icon = Icons.Default.Notifications,
                    color = Color(0xFF25D366),
                    onClick = {
                        islandManager.postEvent(
                            IslandEvent.Notification(
                                appName = "WhatsApp",
                                packageName = "com.whatsapp",
                                title = "Ahmed",
                                message = "Hey, are you free for the meeting tonight?"
                            ),
                            expandImmediately = true
                        )
                    }
                ),
                TriggerItem(
                    title = "Spotify Music",
                    subtitle = "Blinding Lights",
                    icon = Icons.Default.MusicNote,
                    color = Color(0xFF1DB954),
                    onClick = {
                        islandManager.setMusicPlaying(
                            title = "Blinding Lights",
                            artist = "The Weeknd",
                            isPlaying = true
                        )
                    }
                ),
                TriggerItem(
                    title = "Phone Call",
                    subtitle = "Sarah Connor",
                    icon = Icons.Default.Call,
                    color = Color(0xFF3B82F6),
                    onClick = {
                        islandManager.startCall("Sarah Connor", isIncoming = true)
                    }
                ),
                TriggerItem(
                    title = "Focus Timer",
                    subtitle = "5:00 countdown",
                    icon = Icons.Default.Timer,
                    color = Color(0xFFF97316),
                    onClick = {
                        islandManager.startTimer(300, "Focus Session")
                    }
                ),
                TriggerItem(
                    title = "Fast Charging",
                    subtitle = "84% • 22 min left",
                    icon = Icons.Default.Bolt,
                    color = Color(0xFF10B981),
                    onClick = {
                        islandManager.postEvent(
                            IslandEvent.Battery(
                                level = 84,
                                isCharging = true,
                                isFullyCharged = false,
                                estimatedMinutesRemaining = 22
                            ),
                            expandImmediately = true
                        )
                    }
                ),
                TriggerItem(
                    title = "Download APK",
                    subtitle = "Cyberpunk v2.4 (650MB)",
                    icon = Icons.Default.Download,
                    color = Color(0xFF6366F1),
                    onClick = {
                        islandManager.startDownload("Cyberpunk_v2.4_Patch.apk", 650.0, 24.5)
                    }
                ),
                TriggerItem(
                    title = "Navigation",
                    subtitle = "Grand Ave • 250m",
                    icon = Icons.Default.Navigation,
                    color = Color(0xFF06B6D4),
                    onClick = {
                        islandManager.postEvent(
                            IslandEvent.Navigation(
                                instruction = "Turn right onto Grand Ave in 250m",
                                distanceRemaining = "3.2 km",
                                eta = "7 min"
                            ),
                            expandImmediately = true
                        )
                    }
                ),
                TriggerItem(
                    title = "Stopwatch",
                    subtitle = "Live elapsed timer",
                    icon = Icons.Default.HourglassTop,
                    color = Color(0xFF38BDF8),
                    onClick = {
                        islandManager.startStopwatch()
                    }
                )
            )

            items(triggerItems.chunked(2).size) { chunkIndex ->
                val chunk = triggerItems.chunked(2)[chunkIndex]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (item in chunk) {
                        QuickTriggerCard(
                            item = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (chunk.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 4. Quick navigation shortcuts
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToActivities,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Live Activities", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onNavigateToSettings,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Customize", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

data class TriggerItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickTriggerCard(
    item: TriggerItem,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = item.onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
