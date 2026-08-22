package com.example.service

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.DynamicIslandApp
import com.example.data.model.IslandEvent

class DynamicNotificationListener : NotificationListenerService() {

    private val islandManager by lazy { DynamicIslandApp.instance.islandManager }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkgName = sbn.packageName ?: return

        // Filter out own overlay service notification to prevent self-triggering loops
        if (pkgName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        // Skip blank notifications
        if (title.isBlank() && text.isBlank()) return

        // Skip ongoing persistent background notifications with zero user interest
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        val isMediaStyle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.extras.getString(Notification.EXTRA_TEMPLATE) == "android.app.Notification\$MediaStyle"
        } else false

        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(pkgName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            pkgName.substringAfterLast('.')
        }

        val appIconBitmap: Bitmap? = try {
            val drawable = pm.getApplicationIcon(pkgName)
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }

        if (isMediaStyle) {
            val isPlaying = !isOngoing || extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
            islandManager.postEvent(
                IslandEvent.Music(
                    title = title.ifBlank { "Playing Media" },
                    artist = text.ifBlank { appName },
                    isPlaying = isPlaying,
                    appName = appName
                ),
                expandImmediately = false
            )
        } else if (!isOngoing || notification.priority >= Notification.PRIORITY_HIGH) {
            islandManager.postEvent(
                IslandEvent.Notification(
                    id = "notif_${sbn.id}_${sbn.postTime}",
                    appName = appName,
                    packageName = pkgName,
                    title = title,
                    message = text,
                    appIcon = appIconBitmap,
                    timestamp = sbn.postTime
                ),
                expandImmediately = true
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Can optionally react if needed
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 64
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 64
        val bitmap = Bitmap.createBitmap(width.coerceAtMost(128), height.coerceAtMost(128), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
