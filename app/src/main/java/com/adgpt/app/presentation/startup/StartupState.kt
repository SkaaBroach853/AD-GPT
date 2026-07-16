package com.adgpt.app.presentation.startup

data class StartupState(
    val videoAssetPath: String? = null,
    val appPreloaded: Boolean = false,
    val showIntro: Boolean = true,
    val transitionActive: Boolean = false,
    val mainVisible: Boolean = false,
    val mainEntranceReady: Boolean = false
)
