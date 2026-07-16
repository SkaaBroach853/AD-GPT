package com.adgpt.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adgpt.app.presentation.chat.ChatRoute
import com.adgpt.app.presentation.settings.SettingsRoute

@Composable
fun ADGPTNavHost(readyForEntrance: Boolean) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppDestination.Chat.route) {
        composable(AppDestination.Chat.route) {
            ChatRoute(
                readyForEntrance = readyForEntrance,
                onSettingsClick = { navController.navigate(AppDestination.Settings.route) }
            )
        }
        composable(AppDestination.Settings.route) {
            SettingsRoute(onBack = { navController.popBackStack() })
        }
    }
}
