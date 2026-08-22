package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import com.example.data.datastore.SettingsDataStore
import com.example.data.manager.IslandManager
import com.example.service.BatteryReceiver

class DynamicIslandApp : Application() {

    lateinit var settingsDataStore: SettingsDataStore
        private set

    lateinit var islandManager: IslandManager
        private set

    private var batteryReceiver: BatteryReceiver? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        settingsDataStore = SettingsDataStore(this)
        islandManager = IslandManager(this, settingsDataStore)

        createNotificationChannels()
        registerBatteryReceiver()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_OVERLAY,
                "Dynamic Island Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the Dynamic Island overlay floating smoothly"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun registerBatteryReceiver() {
        try {
            batteryReceiver = BatteryReceiver(islandManager)
            val filter = IntentFilter().apply {
                addAction(android.content.Intent.ACTION_BATTERY_CHANGED)
                addAction(android.content.Intent.ACTION_POWER_CONNECTED)
                addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val CHANNEL_ID_OVERLAY = "dynamic_island_overlay_channel"
        lateinit var instance: DynamicIslandApp
            private set
    }
}
