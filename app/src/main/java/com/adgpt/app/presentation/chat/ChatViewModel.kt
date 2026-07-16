package com.adgpt.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole
import com.adgpt.app.domain.usecase.ClearChatUseCase
import com.adgpt.app.domain.usecase.ObserveMessagesUseCase
import com.adgpt.app.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val history: List<ChatHistoryItem> = emptyList(),
    val input: String = "",
    val sending: Boolean = false,
    val selectedHistoryId: String? = null
)

data class ChatHistoryItem(
    val id: String,
    val title: String,
    val subtitle: String
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val clearChat: ClearChatUseCase
) : ViewModel() {
    private val localState = MutableStateFlow(ChatUiState())

    val state: StateFlow<ChatUiState> =
        combine(observeMessages(), localState) { messages, state ->
            val history = messages
                .filter { it.role == MessageRole.User }
                .takeLast(12)
                .reversed()
                .mapIndexed { index, message ->
                    ChatHistoryItem(
                        id = message.id,
                        title = message.content.take(38).ifBlank { "New conversation" },
                        subtitle = if (index == 0) "Latest prompt" else "Earlier prompt"
                    )
                }
            state.copy(
                messages = messages,
                history = history,
                selectedHistoryId = state.selectedHistoryId ?: history.firstOrNull()?.id
            )
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    fun updateInput(value: String) {
        localState.update { it.copy(input = value) }
    }

    fun send() {
        val prompt = localState.value.input.trim()
        if (prompt.isBlank() || localState.value.sending) return
        viewModelScope.launch {
            localState.update { it.copy(input = "", sending = true) }
            runCatching { sendMessage(prompt) }
            localState.update { it.copy(sending = false) }
        }
    }

    fun startNewChat() {
        viewModelScope.launch {
            clearChat()
            localState.update { it.copy(selectedHistoryId = null, input = "") }
        }
    }

    fun selectHistory(id: String) {
        localState.update { it.copy(selectedHistoryId = id) }
    }

    fun useQuickPrompt(prompt: String) {
        localState.update { it.copy(input = prompt) }
    }
}
