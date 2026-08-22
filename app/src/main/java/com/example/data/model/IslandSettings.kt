package com.example.data.model

data class IslandSettings(
    val isOverlayEnabled: Boolean = true,
    val enableNotifications: Boolean = true,
    val enableMusic: Boolean = true,
    val enableCalls: Boolean = true,
    val enableTimer: Boolean = true,
    val enableBatteryCharging: Boolean = true,
    val enableDownloads: Boolean = true,
    val autoCollapseDurationSeconds: Int = 4,
    val islandWidthDp: Int = 200,
    val islandHeightDp: Int = 38,
    val islandYOffsetDp: Int = 12,
    val islandCornerRadiusDp: Int = 24,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = false,
    val animationSpeedMultiplier: Float = 1.0f,
    val cutoutPosition: CutoutPosition = CutoutPosition.CENTER
)

enum class CutoutPosition {
    CENTER,
    LEFT,
    RIGHT
}
