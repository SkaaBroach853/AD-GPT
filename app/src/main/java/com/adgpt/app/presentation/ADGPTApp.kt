package com.adgpt.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.adgpt.app.presentation.navigation.ADGPTNavHost
import com.adgpt.app.presentation.startup.StartupOverlay
import com.adgpt.app.presentation.startup.StartupViewModel
import com.adgpt.app.presentation.theme.ADGPTTheme

private val PremiumEase = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

@Composable
fun ADGPTApp(
    startupViewModel: StartupViewModel = hiltViewModel()
) {
    val state by startupViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        startupViewModel.beginStartup()
    }

    ADGPTTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AnimatedVisibility(
                visible = state.mainVisible,
                enter = fadeIn(animationSpec = tween(260, easing = PremiumEase))
            ) {
                ADGPTNavHost(readyForEntrance = state.mainEntranceReady)
            }

            if (state.showIntro) {
                StartupOverlay(
                    videoAssetPath = state.videoAssetPath,
                    transitionActive = state.transitionActive,
                    onVideoFinished = startupViewModel::finishIntro,
                    onSkip = startupViewModel::skipIntro
                )
            }
        }
    }
}
