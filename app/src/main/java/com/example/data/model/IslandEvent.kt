package com.example.data.model

import android.graphics.Bitmap

sealed interface IslandEvent {
    val id: String
    val priority: Int // Higher number = higher priority

    data class Notification(
        override val id: String = "notification_${System.currentTimeMillis()}",
        override val priority: Int = 10,
        val appName: String,
        val packageName: String,
        val title: String,
        val message: String,
        val appIcon: Bitmap? = null,
        val iconResName: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : IslandEvent

    data class Music(
        override val id: String = "music_activity",
        override val priority: Int = 5,
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val progressMs: Long = 0,
        val durationMs: Long = 210000,
        val albumArtUrl: String? = null,
        val appName: String = "Spotify"
    ) : IslandEvent

    data class PhoneCall(
        override val id: String = "phone_call",
        override val priority: Int = 20,
        val callerName: String,
        val callerNumber: String = "+1 (555) 019-2834",
        val durationSeconds: Int = 0,
        val isMuted: Boolean = false,
        val isSpeakerOn: Boolean = false,
        val isIncoming: Boolean = false
    ) : IslandEvent

    data class Timer(
        override val id: String = "timer_activity",
        override val priority: Int = 8,
        val label: String = "Timer",
        val totalSeconds: Int = 300,
        val remainingSeconds: Int = 300,
        val isRunning: Boolean = false
    ) : IslandEvent

    data class Stopwatch(
        override val id: String = "stopwatch_activity",
        override val priority: Int = 7,
        val elapsedSeconds: Int = 0,
        val isRunning: Boolean = false
    ) : IslandEvent

    data class Battery(
        override val id: String = "battery_activity",
        override val priority: Int = 9,
        val level: Int,
        val isCharging: Boolean,
        val isFullyCharged: Boolean = false,
        val estimatedMinutesRemaining: Int = 28
    ) : IslandEvent

    data class Download(
        override val id: String = "download_${System.currentTimeMillis()}",
        override val priority: Int = 6,
        val fileName: String,
        val progressPercent: Int,
        val speedMbPerSec: Double = 14.2,
        val totalSizeMb: Double = 450.0,
        val isPaused: Boolean = false
    ) : IslandEvent

    data class Navigation(
        override val id: String = "nav_activity",
        override val priority: Int = 12,
        val directionIcon: String = "turn_right",
        val instruction: String = "In 250m turn right onto Grand Ave",
        val distanceRemaining: String = "3.2 km",
        val eta: String = "8 min"
    ) : IslandEvent
}
