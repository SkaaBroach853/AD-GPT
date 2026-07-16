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
    val apiKeyInput: String = "",
    val detectedProvider: ProviderUi? = null,
    val activeProvider: ProviderUi? = null,
    val activeModel: String = "Local demo",
    val apiStatus: ApiStatus = ApiStatus.Idle,
    val sending: Boolean = false,
    val selectedHistoryId: String? = null,
    val sidebarCollapsed: Boolean = false,
    val chatMinimized: Boolean = false
)

data class ChatHistoryItem(
    val id: String,
    val title: String,
    val subtitle: String
)

data class ProviderUi(
    val id: String,
    val name: String,
    val model: String,
    val badge: String
)

enum class ApiStatus {
    Idle,
    Detected,
    Active,
    Invalid
}

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

    fun updateApiKey(value: String) {
        val normalized = value.trim()
        val provider = detectProvider(normalized)
        localState.update {
            it.copy(
                apiKeyInput = value,
                detectedProvider = provider,
                apiStatus = when {
                    normalized.isBlank() -> ApiStatus.Idle
                    provider == null -> ApiStatus.Invalid
                    else -> ApiStatus.Detected
                }
            )
        }
    }

    fun activateDetectedProvider() {
        val provider = localState.value.detectedProvider ?: return
        localState.update {
            it.copy(
                activeProvider = provider,
                activeModel = provider.model,
                apiStatus = ApiStatus.Active,
                apiKeyInput = maskKey(it.apiKeyInput)
            )
        }
    }

    fun clearApiKey() {
        localState.update {
            it.copy(
                apiKeyInput = "",
                detectedProvider = null,
                activeProvider = null,
                activeModel = "Local demo",
                apiStatus = ApiStatus.Idle
            )
        }
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

    fun toggleSidebar() {
        localState.update { it.copy(sidebarCollapsed = !it.sidebarCollapsed) }
    }

    fun toggleChatMinimized() {
        localState.update { it.copy(chatMinimized = !it.chatMinimized) }
    }

    private fun detectProvider(key: String): ProviderUi? {
        if (key.length < 12 || key.contains(" ")) return null
        return when {
            key.startsWith("sk-ant-", ignoreCase = true) ->
                ProviderUi("anthropic", "Anthropic", "Claude", "CLAUDE")
            key.startsWith("AIza", ignoreCase = false) ->
                ProviderUi("google", "Google Gemini", "Gemini Pro", "GEMINI")
            key.startsWith("gsk_", ignoreCase = true) ->
                ProviderUi("groq", "Groq", "Llama / Mixtral", "GROQ")
            key.startsWith("sk-or-", ignoreCase = true) ->
                ProviderUi("openrouter", "OpenRouter", "OpenRouter Auto", "ROUTER")
            key.startsWith("sk-", ignoreCase = true) ->
                ProviderUi("openai", "OpenAI", "GPT", "OPENAI")
            key.startsWith("mistral_", ignoreCase = true) ->
                ProviderUi("mistral", "Mistral", "Mistral", "MISTRAL")
            else -> null
        }
    }

    private fun maskKey(key: String): String {
        val normalized = key.trim()
        if (normalized.length <= 10) return "••••"
        return normalized.take(6) + "••••••" + normalized.takeLast(4)
    }
}
