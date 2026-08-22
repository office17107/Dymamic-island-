package com.example.ui.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IslandEvent

@Composable
fun ExpandedIsland(
    event: IslandEvent?,
    onDismiss: () -> Unit,
    onToggleMusic: () -> Unit,
    onNextMusic: () -> Unit,
    onPrevMusic: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onToggleMuteCall: () -> Unit,
    onToggleSpeakerCall: () -> Unit,
    onEndCall: () -> Unit,
    onPauseDownload: () -> Unit,
    onResumeDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        AnimatedContent(
            targetState = event,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "expanded_content"
        ) { currentEvent ->
            if (currentEvent == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dynamic Island Active",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                when (currentEvent) {
                    is IslandEvent.Notification -> {
                        NotificationIslandContent(
                            event = currentEvent,
                            onDismiss = onDismiss
                        )
                    }
                    is IslandEvent.Music -> {
                        MusicIslandContent(
                            event = currentEvent,
                            onTogglePlay = onToggleMusic,
                            onNext = onNextMusic,
                            onPrev = onPrevMusic
                        )
                    }
                    is IslandEvent.PhoneCall -> {
                        CallIslandContent(
                            event = currentEvent,
                            onToggleMute = onToggleMuteCall,
                            onToggleSpeaker = onToggleSpeakerCall,
                            onEndCall = onEndCall
                        )
                    }
                    is IslandEvent.Timer -> {
                        TimerIslandContent(
                            event = currentEvent,
                            onPause = onPauseTimer,
                            onResume = onResumeTimer,
                            onReset = onResetTimer
                        )
                    }
                    is IslandEvent.Stopwatch -> {
                        ExpandedStopwatch(
                            event = currentEvent,
                            onPause = onPauseTimer,
                            onResume = onResumeTimer,
                            onReset = onResetTimer
                        )
                    }
                    is IslandEvent.Battery -> {
                        BatteryIslandContent(event = currentEvent)
                    }
                    is IslandEvent.Download -> {
                        DownloadIslandContent(
                            event = currentEvent,
                            onPause = onPauseDownload,
                            onResume = onResumeDownload,
                            onCancel = onCancelDownload
                        )
                    }
                    is IslandEvent.Navigation -> {
                        ExpandedNavigation(
                            event = currentEvent,
                            onDismiss = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedStopwatch(
    event: IslandEvent.Stopwatch,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Stopwatch",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val minutes = event.elapsedSeconds / 60
            val seconds = event.elapsedSeconds % 60
            val timeString = String.format("%02d:%02d", minutes, seconds)

            Text(
                text = timeString,
                color = Color(0xFF38BDF8),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = onReset,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reset", fontSize = 12.sp)
            }
            ElevatedButton(
                onClick = if (event.isRunning) onPause else onResume,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = if (event.isRunning) Color(0xFF0284C7) else Color(0xFF16A34A),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    if (event.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (event.isRunning) "Pause" else "Start", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ExpandedNavigation(
    event: IslandEvent.Navigation,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
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
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = event.instruction,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ETA: ${event.eta} • ${event.distanceRemaining}",
                        color = Color(0xFF22D3EE),
                        fontSize = 12.sp
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF94A3B8))
            }
        }
    }
}
