package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.example.data.manager.IslandManager
import com.example.data.model.IslandEvent

class BatteryReceiver(private val islandManager: IslandManager) : BroadcastReceiver() {

    private var lastPluggedState: Boolean? = null
    private var lastBatteryLevel: Int = -1

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        when (action) {
            Intent.ACTION_POWER_CONNECTED -> {
                val level = getBatteryLevel(intent)
                lastPluggedState = true
                islandManager.postEvent(
                    IslandEvent.Battery(
                        level = level,
                        isCharging = true,
                        isFullyCharged = level >= 100,
                        estimatedMinutesRemaining = calculateRemainingMinutes(level)
                    ),
                    expandImmediately = true
                )
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                val level = getBatteryLevel(intent)
                lastPluggedState = false
                islandManager.postEvent(
                    IslandEvent.Battery(
                        level = level,
                        isCharging = false,
                        isFullyCharged = false,
                        estimatedMinutesRemaining = 0
                    ),
                    expandImmediately = false
                )
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = getBatteryLevel(intent)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                val isFull = status == BatteryManager.BATTERY_STATUS_FULL || level >= 100

                // If charging state flipped to connected
                if (lastPluggedState == false && isCharging) {
                    lastPluggedState = true
                    islandManager.postEvent(
                        IslandEvent.Battery(
                            level = level,
                            isCharging = true,
                            isFullyCharged = isFull,
                            estimatedMinutesRemaining = calculateRemainingMinutes(level)
                        ),
                        expandImmediately = true
                    )
                } else if (lastPluggedState == true && !isCharging) {
                    lastPluggedState = false
                }
                lastBatteryLevel = level
            }
        }
    }

    private fun getBatteryLevel(intent: Intent): Int {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            78
        }
    }

    private fun calculateRemainingMinutes(level: Int): Int {
        val remainingPercent = (100 - level).coerceAtLeast(0)
        return (remainingPercent * 0.8).toInt().coerceAtLeast(5)
    }
}
