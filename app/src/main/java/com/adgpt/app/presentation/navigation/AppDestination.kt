package com.adgpt.app.presentation.navigation

sealed class AppDestination(val route: String) {
    data object Chat : AppDestination("chat")
    data object Settings : AppDestination("settings")
}
