package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.CutoutPosition
import com.example.data.model.IslandSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "island_settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val CALLS_ENABLED = booleanPreferencesKey("calls_enabled")
        val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")
        val BATTERY_ENABLED = booleanPreferencesKey("battery_enabled")
        val DOWNLOADS_ENABLED = booleanPreferencesKey("downloads_enabled")
        val AUTO_COLLAPSE_DURATION = intPreferencesKey("auto_collapse_duration")
        val ISLAND_WIDTH = intPreferencesKey("island_width")
        val ISLAND_HEIGHT = intPreferencesKey("island_height")
        val ISLAND_Y_OFFSET = intPreferencesKey("island_y_offset")
        val ISLAND_CORNER_RADIUS = intPreferencesKey("island_corner_radius")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
        val CUTOUT_POSITION = stringPreferencesKey("cutout_position")
    }

    val settingsFlow: Flow<IslandSettings> = context.dataStore.data.map { preferences ->
        val cutoutString = preferences[PreferencesKeys.CUTOUT_POSITION] ?: CutoutPosition.CENTER.name
        val cutout = try {
            CutoutPosition.valueOf(cutoutString)
        } catch (e: Exception) {
            CutoutPosition.CENTER
        }

        IslandSettings(
            isOverlayEnabled = preferences[PreferencesKeys.OVERLAY_ENABLED] ?: true,
            enableNotifications = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            enableMusic = preferences[PreferencesKeys.MUSIC_ENABLED] ?: true,
            enableCalls = preferences[PreferencesKeys.CALLS_ENABLED] ?: true,
            enableTimer = preferences[PreferencesKeys.TIMER_ENABLED] ?: true,
            enableBatteryCharging = preferences[PreferencesKeys.BATTERY_ENABLED] ?: true,
            enableDownloads = preferences[PreferencesKeys.DOWNLOADS_ENABLED] ?: true,
            autoCollapseDurationSeconds = preferences[PreferencesKeys.AUTO_COLLAPSE_DURATION] ?: 4,
            islandWidthDp = preferences[PreferencesKeys.ISLAND_WIDTH] ?: 200,
            islandHeightDp = preferences[PreferencesKeys.ISLAND_HEIGHT] ?: 38,
            islandYOffsetDp = preferences[PreferencesKeys.ISLAND_Y_OFFSET] ?: 12,
            islandCornerRadiusDp = preferences[PreferencesKeys.ISLAND_CORNER_RADIUS] ?: 24,
            enableVibration = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            enableSound = preferences[PreferencesKeys.SOUND_ENABLED] ?: false,
            animationSpeedMultiplier = preferences[PreferencesKeys.ANIMATION_SPEED] ?: 1.0f,
            cutoutPosition = cutout
        )
    }

    suspend fun updateSettings(update: (IslandSettings) -> IslandSettings) {
        context.dataStore.edit { preferences ->
            val current = IslandSettings(
                isOverlayEnabled = preferences[PreferencesKeys.OVERLAY_ENABLED] ?: true,
                enableNotifications = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                enableMusic = preferences[PreferencesKeys.MUSIC_ENABLED] ?: true,
                enableCalls = preferences[PreferencesKeys.CALLS_ENABLED] ?: true,
                enableTimer = preferences[PreferencesKeys.TIMER_ENABLED] ?: true,
                enableBatteryCharging = preferences[PreferencesKeys.BATTERY_ENABLED] ?: true,
                enableDownloads = preferences[PreferencesKeys.DOWNLOADS_ENABLED] ?: true,
                autoCollapseDurationSeconds = preferences[PreferencesKeys.AUTO_COLLAPSE_DURATION] ?: 4,
                islandWidthDp = preferences[PreferencesKeys.ISLAND_WIDTH] ?: 200,
                islandHeightDp = preferences[PreferencesKeys.ISLAND_HEIGHT] ?: 38,
                islandYOffsetDp = preferences[PreferencesKeys.ISLAND_Y_OFFSET] ?: 12,
                islandCornerRadiusDp = preferences[PreferencesKeys.ISLAND_CORNER_RADIUS] ?: 24,
                enableVibration = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                enableSound = preferences[PreferencesKeys.SOUND_ENABLED] ?: false,
                animationSpeedMultiplier = preferences[PreferencesKeys.ANIMATION_SPEED] ?: 1.0f,
                cutoutPosition = try {
                    CutoutPosition.valueOf(preferences[PreferencesKeys.CUTOUT_POSITION] ?: CutoutPosition.CENTER.name)
                } catch (e: Exception) {
                    CutoutPosition.CENTER
                }
            )
            val updated = update(current)
            preferences[PreferencesKeys.OVERLAY_ENABLED] = updated.isOverlayEnabled
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = updated.enableNotifications
            preferences[PreferencesKeys.MUSIC_ENABLED] = updated.enableMusic
            preferences[PreferencesKeys.CALLS_ENABLED] = updated.enableCalls
            preferences[PreferencesKeys.TIMER_ENABLED] = updated.enableTimer
            preferences[PreferencesKeys.BATTERY_ENABLED] = updated.enableBatteryCharging
            preferences[PreferencesKeys.DOWNLOADS_ENABLED] = updated.enableDownloads
            preferences[PreferencesKeys.AUTO_COLLAPSE_DURATION] = updated.autoCollapseDurationSeconds
            preferences[PreferencesKeys.ISLAND_WIDTH] = updated.islandWidthDp
            preferences[PreferencesKeys.ISLAND_HEIGHT] = updated.islandHeightDp
            preferences[PreferencesKeys.ISLAND_Y_OFFSET] = updated.islandYOffsetDp
            preferences[PreferencesKeys.ISLAND_CORNER_RADIUS] = updated.islandCornerRadiusDp
            preferences[PreferencesKeys.VIBRATION_ENABLED] = updated.enableVibration
            preferences[PreferencesKeys.SOUND_ENABLED] = updated.enableSound
            preferences[PreferencesKeys.ANIMATION_SPEED] = updated.animationSpeedMultiplier
            preferences[PreferencesKeys.CUTOUT_POSITION] = updated.cutoutPosition.name
        }
    }
}
