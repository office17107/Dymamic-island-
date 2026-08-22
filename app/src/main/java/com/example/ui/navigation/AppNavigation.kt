package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveActivitiesScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val ACTIVITIES = "activities"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToActivities = { navController.navigate(Routes.ACTIVITIES) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) }
            )
        }

        composable(Routes.ACTIVITIES) {
            LiveActivitiesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) }
            )
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
