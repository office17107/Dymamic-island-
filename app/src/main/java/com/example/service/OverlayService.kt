package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.DynamicIslandApp
import com.example.MainActivity
import com.example.R
import com.example.data.model.CutoutPosition
import com.example.data.model.IslandState
import com.example.ui.island.DynamicIsland
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val islandManager by lazy { DynamicIslandApp.instance.islandManager }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startAsForeground()
        setupOverlayView()
        observeIslandState()
    }

    private fun startAsForeground() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, DynamicIslandApp.CHANNEL_ID_OVERLAY)
            .setContentTitle("Dynamic Island Active")
            .setContentText("Interactive floating island is running")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun setupOverlayView() {
        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val gravity = when (islandManager.settings.value.cutoutPosition) {
            CutoutPosition.CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            CutoutPosition.LEFT -> Gravity.TOP or Gravity.START
            CutoutPosition.RIGHT -> Gravity.TOP or Gravity.END
        }

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            this.y = (islandManager.settings.value.islandYOffsetDp * resources.displayMetrics.density).toInt()
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                MyApplicationTheme(darkTheme = true) {
                    val currentEvent by islandManager.activeEvent.collectAsState()
                    val currentState by islandManager.islandState.collectAsState()
                    val settings by islandManager.settings.collectAsState()

                    DynamicIsland(
                        event = currentEvent,
                        state = currentState,
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
                }
            }
        }

        try {
            windowManager?.addView(composeView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeIslandState() {
        serviceScope.launch {
            islandManager.islandState.collect { state ->
                updateOverlayWindow(state)
            }
        }

        serviceScope.launch {
            islandManager.settings.collect { settings ->
                if (!settings.isOverlayEnabled) {
                    composeView?.visibility = View.GONE
                } else {
                    composeView?.visibility = View.VISIBLE
                    updateWindowPosition(settings)
                }
            }
        }
    }

    private fun updateWindowPosition(settings: com.example.data.model.IslandSettings) {
        val params = layoutParams ?: return
        val density = resources.displayMetrics.density
        params.y = (settings.islandYOffsetDp * density).toInt()
        params.gravity = when (settings.cutoutPosition) {
            CutoutPosition.CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            CutoutPosition.LEFT -> Gravity.TOP or Gravity.START
            CutoutPosition.RIGHT -> Gravity.TOP or Gravity.END
        }
        try {
            if (composeView != null && composeView?.isAttachedToWindow == true) {
                windowManager?.updateViewLayout(composeView, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayWindow(state: IslandState) {
        val view = composeView ?: return
        val params = layoutParams ?: return

        when (state) {
            IslandState.Hidden -> {
                view.visibility = View.GONE
            }
            IslandState.Dismissing -> {
                // Keep visible for animation then collapse
            }
            else -> {
                view.visibility = View.VISIBLE
            }
        }

        try {
            if (view.isAttachedToWindow) {
                windowManager?.updateViewLayout(view, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()

        try {
            if (composeView != null && composeView?.isAttachedToWindow == true) {
                windowManager?.removeView(composeView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 9110

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }
    }
}
