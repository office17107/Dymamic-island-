package com.example.data.model

sealed interface IslandState {
    data object Hidden : IslandState
    data object Appearing : IslandState
    data object Compact : IslandState
    data object Expanded : IslandState
    data object Interactive : IslandState
    data object Dismissing : IslandState
}
