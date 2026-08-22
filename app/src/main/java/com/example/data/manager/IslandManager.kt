package com.example.data.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.datastore.SettingsDataStore
import com.example.data.model.IslandEvent
import com.example.data.model.IslandSettings
import com.example.data.model.IslandState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IslandManager(
    private val context: Context,
    private val settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val settings: StateFlow<IslandSettings> = settingsDataStore.settingsFlow.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = IslandSettings()
    )

    private val _activeEvent = MutableStateFlow<IslandEvent?>(null)
    val activeEvent: StateFlow<IslandEvent?> = _activeEvent.asStateFlow()

    private val _islandState = MutableStateFlow<IslandState>(IslandState.Hidden)
    val islandState: StateFlow<IslandState> = _islandState.asStateFlow()

    // Persistent activities stored when interrupted by transient notifications
    private var persistentMusicEvent: IslandEvent.Music? = null
    private var persistentTimerEvent: IslandEvent.Timer? = null
    private var persistentCallEvent: IslandEvent.PhoneCall? = null
    private var persistentDownloadEvent: IslandEvent.Download? = null

    private var autoCollapseJob: Job? = null
    private var timerTickerJob: Job? = null
    private var stopwatchTickerJob: Job? = null
    private var downloadTickerJob: Job? = null
    private var callTickerJob: Job? = null

    // Haptics
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        // Observe settings changes
        scope.launch {
            settings.collect { s ->
                if (!s.isOverlayEnabled && _islandState.value != IslandState.Hidden) {
                    // overlay disabled
                }
            }
        }
    }

    fun postEvent(event: IslandEvent, expandImmediately: Boolean = false) {
        val currentSettings = settings.value

        // Check if event type is enabled
        when (event) {
            is IslandEvent.Notification -> if (!currentSettings.enableNotifications) return
            is IslandEvent.Music -> if (!currentSettings.enableMusic) return
            is IslandEvent.PhoneCall -> if (!currentSettings.enableCalls) return
            is IslandEvent.Timer -> if (!currentSettings.enableTimer) return
            is IslandEvent.Stopwatch -> if (!currentSettings.enableTimer) return
            is IslandEvent.Battery -> if (!currentSettings.enableBatteryCharging) return
            is IslandEvent.Download -> if (!currentSettings.enableDownloads) return
            is IslandEvent.Navigation -> {}
        }

        // Cache persistent activities
        when (event) {
            is IslandEvent.Music -> persistentMusicEvent = event
            is IslandEvent.Timer -> persistentTimerEvent = event
            is IslandEvent.PhoneCall -> persistentCallEvent = event
            is IslandEvent.Download -> persistentDownloadEvent = event
            else -> {}
        }

        _activeEvent.value = event

        if (expandImmediately) {
            _islandState.value = IslandState.Expanded
            scheduleAutoCollapse(currentSettings.autoCollapseDurationSeconds)
        } else {
            _islandState.value = IslandState.Compact
        }

        triggerHapticFeedback()

        // If it's a temporary notification or battery flash, schedule collapse/dismiss
        if (event is IslandEvent.Notification || event is IslandEvent.Battery) {
            scheduleAutoDismiss(currentSettings.autoCollapseDurationSeconds + 3)
        }
    }

    fun expandIsland() {
        if (_activeEvent.value != null) {
            autoCollapseJob?.cancel()
            _islandState.value = IslandState.Expanded
            triggerHapticFeedback()
            scheduleAutoCollapse(settings.value.autoCollapseDurationSeconds + 2)
        }
    }

    fun collapseIsland() {
        if (_activeEvent.value != null) {
            autoCollapseJob?.cancel()
            _islandState.value = IslandState.Compact
            triggerHapticFeedback()
        } else {
            _islandState.value = IslandState.Hidden
        }
    }

    fun toggleExpandCollapse() {
        if (_islandState.value == IslandState.Expanded || _islandState.value == IslandState.Interactive) {
            collapseIsland()
        } else if (_islandState.value == IslandState.Compact) {
            expandIsland()
        }
    }

    fun setInteractiveState() {
        autoCollapseJob?.cancel()
        _islandState.value = IslandState.Interactive
        triggerHapticFeedback()
    }

    fun dismissActiveEvent() {
        autoCollapseJob?.cancel()
        _islandState.value = IslandState.Dismissing
        triggerHapticFeedback()

        scope.launch {
            delay(280)
            // Restore any running background persistent activity
            val restored = persistentCallEvent
                ?: persistentTimerEvent?.takeIf { it.isRunning }
                ?: persistentDownloadEvent?.takeIf { !it.isPaused && it.progressPercent < 100 }
                ?: persistentMusicEvent?.takeIf { it.isPlaying }

            if (restored != null) {
                _activeEvent.value = restored
                _islandState.value = IslandState.Compact
            } else {
                _activeEvent.value = null
                _islandState.value = IslandState.Hidden
            }
        }
    }

    private fun scheduleAutoCollapse(seconds: Int) {
        autoCollapseJob?.cancel()
        if (seconds <= 0) return
        autoCollapseJob = scope.launch {
            delay(seconds * 1000L)
            if (_islandState.value == IslandState.Expanded) {
                _islandState.value = IslandState.Compact
            }
        }
    }

    private fun scheduleAutoDismiss(seconds: Int) {
        autoCollapseJob?.cancel()
        if (seconds <= 0) return
        autoCollapseJob = scope.launch {
            delay(seconds * 1000L)
            dismissActiveEvent()
        }
    }

    fun triggerHapticFeedback() {
        if (!settings.value.enableVibration) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (e: Exception) {
            // Ignore if vibration unavailable
        }
    }

    // ==========================================
    // MUSIC PLAYER CONTROLLER
    // ==========================================
    fun setMusicPlaying(
        title: String = "Blinding Lights",
        artist: String = "The Weeknd",
        isPlaying: Boolean = true,
        progressMs: Long = 45000,
        durationMs: Long = 200000
    ) {
        val music = IslandEvent.Music(
            title = title,
            artist = artist,
            isPlaying = isPlaying,
            progressMs = progressMs,
            durationMs = durationMs
        )
        persistentMusicEvent = music
        postEvent(music, expandImmediately = false)
    }

    fun toggleMusicPlayback() {
        val current = _activeEvent.value as? IslandEvent.Music ?: persistentMusicEvent
        if (current != null) {
            val updated = current.copy(isPlaying = !current.isPlaying)
            persistentMusicEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()
        }
    }

    fun nextMusicTrack() {
        val tracks = listOf(
            Pair("Starboy", "The Weeknd, Daft Punk"),
            Pair("As It Was", "Harry Styles"),
            Pair("Levitating", "Dua Lipa"),
            Pair("Midnight City", "M83"),
            Pair("Blinding Lights", "The Weeknd")
        )
        val current = _activeEvent.value as? IslandEvent.Music ?: persistentMusicEvent
        val currentIndex = tracks.indexOfFirst { it.first == current?.title }.let { if (it >= 0) it else 0 }
        val nextTrack = tracks[(currentIndex + 1) % tracks.size]

        val updated = IslandEvent.Music(
            title = nextTrack.first,
            artist = nextTrack.second,
            isPlaying = true,
            progressMs = 0,
            durationMs = 215000
        )
        persistentMusicEvent = updated
        _activeEvent.value = updated
        triggerHapticFeedback()
    }

    fun previousMusicTrack() {
        val tracks = listOf(
            Pair("Starboy", "The Weeknd, Daft Punk"),
            Pair("As It Was", "Harry Styles"),
            Pair("Levitating", "Dua Lipa"),
            Pair("Midnight City", "M83"),
            Pair("Blinding Lights", "The Weeknd")
        )
        val current = _activeEvent.value as? IslandEvent.Music ?: persistentMusicEvent
        val currentIndex = tracks.indexOfFirst { it.first == current?.title }.let { if (it >= 0) it else 0 }
        val prevIndex = if (currentIndex - 1 < 0) tracks.size - 1 else currentIndex - 1
        val prevTrack = tracks[prevIndex]

        val updated = IslandEvent.Music(
            title = prevTrack.first,
            artist = prevTrack.second,
            isPlaying = true,
            progressMs = 0,
            durationMs = 198000
        )
        persistentMusicEvent = updated
        _activeEvent.value = updated
        triggerHapticFeedback()
    }

    // ==========================================
    // TIMER CONTROLLER
    // ==========================================
    fun startTimer(totalSeconds: Int = 300, label: String = "Focus Timer") {
        timerTickerJob?.cancel()
        val timer = IslandEvent.Timer(
            label = label,
            totalSeconds = totalSeconds,
            remainingSeconds = totalSeconds,
            isRunning = true
        )
        persistentTimerEvent = timer
        postEvent(timer, expandImmediately = false)

        timerTickerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                val updated = timer.copy(remainingSeconds = remaining, isRunning = true)
                persistentTimerEvent = updated
                if (_activeEvent.value is IslandEvent.Timer) {
                    _activeEvent.value = updated
                }
            }
            // Finished
            val finished = timer.copy(remainingSeconds = 0, isRunning = false)
            persistentTimerEvent = null
            postEvent(IslandEvent.Notification(
                appName = "Clock",
                packageName = "com.google.android.deskclock",
                title = "⏱ Timer Finished",
                message = "$label (${totalSeconds / 60}m) has ended."
            ), expandImmediately = true)
        }
    }

    fun pauseTimer() {
        val current = _activeEvent.value as? IslandEvent.Timer ?: persistentTimerEvent
        if (current != null && current.isRunning) {
            timerTickerJob?.cancel()
            val updated = current.copy(isRunning = false)
            persistentTimerEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()
        }
    }

    fun resumeTimer() {
        val current = _activeEvent.value as? IslandEvent.Timer ?: persistentTimerEvent
        if (current != null && !current.isRunning && current.remainingSeconds > 0) {
            val updated = current.copy(isRunning = true)
            persistentTimerEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()

            timerTickerJob?.cancel()
            timerTickerJob = scope.launch {
                var remaining = current.remainingSeconds
                while (remaining > 0) {
                    delay(1000)
                    remaining--
                    val step = current.copy(remainingSeconds = remaining, isRunning = true)
                    persistentTimerEvent = step
                    if (_activeEvent.value is IslandEvent.Timer) {
                        _activeEvent.value = step
                    }
                }
                persistentTimerEvent = null
                postEvent(IslandEvent.Notification(
                    appName = "Clock",
                    packageName = "com.google.android.deskclock",
                    title = "⏱ Timer Finished",
                    message = "Timer has ended."
                ), expandImmediately = true)
            }
        }
    }

    fun resetTimer() {
        timerTickerJob?.cancel()
        persistentTimerEvent = null
        if (_activeEvent.value is IslandEvent.Timer) {
            dismissActiveEvent()
        }
    }

    // ==========================================
    // STOPWATCH CONTROLLER
    // ==========================================
    fun startStopwatch() {
        stopwatchTickerJob?.cancel()
        val stopwatch = IslandEvent.Stopwatch(elapsedSeconds = 0, isRunning = true)
        postEvent(stopwatch, expandImmediately = false)

        stopwatchTickerJob = scope.launch {
            var elapsed = 0
            while (true) {
                delay(1000)
                elapsed++
                val updated = IslandEvent.Stopwatch(elapsedSeconds = elapsed, isRunning = true)
                if (_activeEvent.value is IslandEvent.Stopwatch) {
                    _activeEvent.value = updated
                }
            }
        }
    }

    fun pauseStopwatch() {
        val current = _activeEvent.value as? IslandEvent.Stopwatch
        if (current != null && current.isRunning) {
            stopwatchTickerJob?.cancel()
            _activeEvent.value = current.copy(isRunning = false)
            triggerHapticFeedback()
        }
    }

    fun resetStopwatch() {
        stopwatchTickerJob?.cancel()
        if (_activeEvent.value is IslandEvent.Stopwatch) {
            dismissActiveEvent()
        }
    }

    // ==========================================
    // DOWNLOAD SIMULATOR
    // ==========================================
    fun startDownload(
        fileName: String = "Cyberpunk_Update_v2.4.apk",
        totalSizeMb: Double = 650.0,
        speedMbPerSec: Double = 18.5
    ) {
        downloadTickerJob?.cancel()
        val download = IslandEvent.Download(
            fileName = fileName,
            progressPercent = 0,
            speedMbPerSec = speedMbPerSec,
            totalSizeMb = totalSizeMb,
            isPaused = false
        )
        persistentDownloadEvent = download
        postEvent(download, expandImmediately = true)

        downloadTickerJob = scope.launch {
            var progress = 0
            while (progress < 100) {
                delay(600)
                progress += 5
                if (progress > 100) progress = 100
                val updated = download.copy(progressPercent = progress)
                persistentDownloadEvent = updated
                if (_activeEvent.value is IslandEvent.Download) {
                    _activeEvent.value = updated
                }
            }
            delay(1500)
            persistentDownloadEvent = null
            postEvent(IslandEvent.Notification(
                appName = "Downloads",
                packageName = "com.android.providers.downloads",
                title = "✓ Download Complete",
                message = "$fileName has finished downloading."
            ), expandImmediately = true)
        }
    }

    fun pauseDownload() {
        val current = _activeEvent.value as? IslandEvent.Download ?: persistentDownloadEvent
        if (current != null && !current.isPaused) {
            downloadTickerJob?.cancel()
            val updated = current.copy(isPaused = true)
            persistentDownloadEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()
        }
    }

    fun resumeDownload() {
        val current = _activeEvent.value as? IslandEvent.Download ?: persistentDownloadEvent
        if (current != null && current.isPaused) {
            val updated = current.copy(isPaused = false)
            persistentDownloadEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()

            downloadTickerJob?.cancel()
            downloadTickerJob = scope.launch {
                var progress = current.progressPercent
                while (progress < 100) {
                    delay(600)
                    progress += 5
                    if (progress > 100) progress = 100
                    val step = current.copy(progressPercent = progress, isPaused = false)
                    persistentDownloadEvent = step
                    if (_activeEvent.value is IslandEvent.Download) {
                        _activeEvent.value = step
                    }
                }
                persistentDownloadEvent = null
                postEvent(IslandEvent.Notification(
                    appName = "Downloads",
                    packageName = "com.android.providers.downloads",
                    title = "✓ Download Complete",
                    message = "${current.fileName} has finished downloading."
                ), expandImmediately = true)
            }
        }
    }

    fun cancelDownload() {
        downloadTickerJob?.cancel()
        persistentDownloadEvent = null
        if (_activeEvent.value is IslandEvent.Download) {
            dismissActiveEvent()
        }
    }

    // ==========================================
    // PHONE CALL SIMULATOR
    // ==========================================
    fun startCall(callerName: String = "Sarah Connor", isIncoming: Boolean = false) {
        callTickerJob?.cancel()
        val call = IslandEvent.PhoneCall(
            callerName = callerName,
            durationSeconds = 0,
            isIncoming = isIncoming
        )
        persistentCallEvent = call
        postEvent(call, expandImmediately = isIncoming)

        if (!isIncoming) {
            callTickerJob = scope.launch {
                var seconds = 0
                while (true) {
                    delay(1000)
                    seconds++
                    val updated = call.copy(durationSeconds = seconds, isIncoming = false)
                    persistentCallEvent = updated
                    if (_activeEvent.value is IslandEvent.PhoneCall) {
                        _activeEvent.value = updated
                    }
                }
            }
        }
    }

    fun answerCall() {
        val current = _activeEvent.value as? IslandEvent.PhoneCall ?: persistentCallEvent
        if (current != null) {
            val updated = current.copy(isIncoming = false, durationSeconds = 1)
            persistentCallEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()

            callTickerJob?.cancel()
            callTickerJob = scope.launch {
                var seconds = 1
                while (true) {
                    delay(1000)
                    seconds++
                    val step = current.copy(durationSeconds = seconds, isIncoming = false)
                    persistentCallEvent = step
                    if (_activeEvent.value is IslandEvent.PhoneCall) {
                        _activeEvent.value = step
                    }
                }
            }
        }
    }

    fun toggleMuteCall() {
        val current = _activeEvent.value as? IslandEvent.PhoneCall ?: persistentCallEvent
        if (current != null) {
            val updated = current.copy(isMuted = !current.isMuted)
            persistentCallEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()
        }
    }

    fun toggleSpeakerCall() {
        val current = _activeEvent.value as? IslandEvent.PhoneCall ?: persistentCallEvent
        if (current != null) {
            val updated = current.copy(isSpeakerOn = !current.isSpeakerOn)
            persistentCallEvent = updated
            _activeEvent.value = updated
            triggerHapticFeedback()
        }
    }

    fun endCall() {
        callTickerJob?.cancel()
        persistentCallEvent = null
        if (_activeEvent.value is IslandEvent.PhoneCall) {
            dismissActiveEvent()
        }
    }
}
