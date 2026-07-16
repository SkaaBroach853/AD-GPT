package com.adgpt.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adgpt.app.domain.model.ChatMessage
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
    val input: String = "",
    val sending: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    observeMessages: ObserveMessagesUseCase,
    private val sendMessage: SendMessageUseCase
) : ViewModel() {
    private val localState = MutableStateFlow(ChatUiState())

    val state: StateFlow<ChatUiState> =
        combine(observeMessages(), localState) { messages, state -> state.copy(messages = messages) }
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
}
