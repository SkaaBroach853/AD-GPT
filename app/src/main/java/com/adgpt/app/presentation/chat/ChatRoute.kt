package com.adgpt.app.presentation.chat

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import com.adgpt.app.presentation.theme.CarbonBlack
import com.adgpt.app.presentation.theme.PanelBlack

val EntranceEase = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
fun entranceTween(delay: Int = 0, duration: Int = 620) = tween<Float>(duration, delay, EntranceEase)

@Composable
fun ChatRoute(
    readyForEntrance: Boolean,
    onSettingsClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(PanelBlack, CarbonBlack),
                    radius = 1100f
                )
            )
    ) {
        ChatScreen(
            state = state,
            readyForEntrance = readyForEntrance,
            onInputChange = viewModel::updateInput,
            onApiKeyChange = viewModel::updateApiKey,
            onActivateApi = viewModel::activateDetectedProvider,
            onClearApi = viewModel::clearApiKey,
            onSend = viewModel::send,
            onNewChat = viewModel::startNewChat,
            onHistoryClick = viewModel::selectHistory,
            onQuickPrompt = viewModel::useQuickPrompt,
            onAddAttachment = viewModel::addAttachment,
            onRemoveAttachment = viewModel::removeAttachment,
            onSelectApi = viewModel::selectApi,
            onToggleSidebar = viewModel::toggleSidebar,
            onToggleChatMinimized = viewModel::toggleChatMinimized,
            onToggleTheme = viewModel::toggleTheme,
            onSettingsClick = onSettingsClick
        )
    }
}
