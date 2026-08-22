package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DynamicIslandApp
import com.example.data.model.IslandEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveActivitiesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val islandManager = DynamicIslandApp.instance.islandManager
    val activeEvent by islandManager.activeEvent.collectAsState()

    var selectedTimerDuration by remember { mutableIntStateOf(300) } // 5 min
    var downloadSpeed by remember { mutableStateOf(18.5f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Activities",
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
            // 1. Music Player Controller
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF10B981))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Music Controller", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Real-time MediaSession live pill", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        val isMusicActive = activeEvent is IslandEvent.Music
                        val musicEvent = activeEvent as? IslandEvent.Music

                        Text(
                            text = if (isMusicActive) "${musicEvent?.title} — ${musicEvent?.artist}" else "No track playing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isMusicActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilledTonalButton(
                                onClick = { islandManager.previousMusicTrack() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null)
                            }

                            Button(
                                onClick = {
                                    if (!isMusicActive) {
                                        islandManager.setMusicPlaying()
                                    } else {
                                        islandManager.toggleMusicPlayback()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(
                                    imageVector = if (musicEvent?.isPlaying == true) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (musicEvent?.isPlaying == true) "Pause" else "Play",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalButton(
                                onClick = { islandManager.nextMusicTrack() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null)
                            }
                        }
                    }
                }
            }

            // 2. Timer & Countdown
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF97316).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFF97316))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("In-App Timer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Runs in background with live updates", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Duration Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(60 to "1m", 180 to "3m", 300 to "5m", 600 to "10m", 1500 to "25m").forEach { (sec, label) ->
                                FilterChip(
                                    selected = selectedTimerDuration == sec,
                                    onClick = { selectedTimerDuration = sec },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFF97316),
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        val isTimerActive = activeEvent is IslandEvent.Timer
                        val timerEvent = activeEvent as? IslandEvent.Timer

                        if (isTimerActive && timerEvent != null) {
                            val min = timerEvent.remainingSeconds / 60
                            val sec = timerEvent.remainingSeconds % 60
                            Text(
                                text = "Running: ${String.format("%02d:%02d", min, sec)} remaining",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF97316),
                                fontSize = 15.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { islandManager.resetTimer() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Reset")
                            }

                            Button(
                                onClick = {
                                    if (isTimerActive && timerEvent?.isRunning == true) {
                                        islandManager.pauseTimer()
                                    } else if (isTimerActive && timerEvent?.isRunning == false) {
                                        islandManager.resumeTimer()
                                    } else {
                                        islandManager.startTimer(selectedTimerDuration, "${selectedTimerDuration / 60}m Timer")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isTimerActive && timerEvent?.isRunning == true) "Pause" else "Start Timer",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Download Progress Simulator
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6366F1).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF6366F1))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("File Download Simulator", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Simulate multi-megabyte app downloads", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        val isDownloadActive = activeEvent is IslandEvent.Download
                        val downloadEvent = activeEvent as? IslandEvent.Download

                        if (isDownloadActive && downloadEvent != null) {
                            Text(
                                text = "${downloadEvent.fileName} (${downloadEvent.progressPercent}%)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF818CF8)
                            )
                        } else {
                            Text(
                                text = "Idle • Ready to download",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { islandManager.cancelDownload() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    islandManager.startDownload(
                                        fileName = "Android_15_Preview_Image.zip",
                                        totalSizeMb = 1250.0,
                                        speedMbPerSec = 32.0
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Start 1.2GB File", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Phone Call Simulator
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF3B82F6))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Phone Call Live Pill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Simulate active phone calls & audio controls", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { islandManager.startCall("Sarah Connor", isIncoming = true) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Incoming Call", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { islandManager.startCall("John Matrix", isIncoming = false) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Active Call", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
