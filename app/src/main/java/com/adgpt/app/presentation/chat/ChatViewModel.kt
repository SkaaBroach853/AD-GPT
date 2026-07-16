package com.adgpt.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole
import com.adgpt.app.data.provider.ApiKeyStore
import com.adgpt.app.data.provider.SavedApiKey
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
    val activeModel: String = "AD-GPT",
    val savedApis: List<SavedApiUi> = emptyList(),
    val activeApiId: String? = null,
    val attachments: List<AttachmentUi> = emptyList(),
    val apiStatus: ApiStatus = ApiStatus.Idle,
    val sending: Boolean = false,
    val selectedHistoryId: String? = null,
    val sidebarCollapsed: Boolean = true,
    val chatMinimized: Boolean = false,
    val darkTheme: Boolean = true
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

data class AttachmentUi(
    val id: String,
    val name: String,
    val type: String
)

data class SavedApiUi(
    val id: String,
    val providerName: String,
    val model: String,
    val maskedKey: String
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
    private val clearChat: ClearChatUseCase,
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {
    private val localState = MutableStateFlow(ChatUiState())

    val state: StateFlow<ChatUiState> =
        combine(observeMessages(), localState, apiKeyStore.keys, apiKeyStore.activeKeyId) { messages, state, keys, activeKeyId ->
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
                selectedHistoryId = state.selectedHistoryId ?: history.firstOrNull()?.id,
                savedApis = keys.map { it.toUi() },
                activeApiId = activeKeyId,
                activeProvider = keys.firstOrNull { it.id == activeKeyId }?.toProviderUi() ?: state.activeProvider,
                activeModel = keys.firstOrNull { it.id == activeKeyId }?.model ?: state.activeModel
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
                    else -> ApiStatus.Detected
                }
            )
        }
    }

    fun activateDetectedProvider() {
        val provider = localState.value.detectedProvider ?: return
        val rawKey = localState.value.apiKeyInput.trim()
        if (rawKey.isBlank() || rawKey.contains("•")) return
        apiKeyStore.addOrUpdate(
            SavedApiKey(
                id = provider.id + "-" + rawKey.takeLast(6),
                providerId = provider.id,
                providerName = provider.name,
                model = provider.model,
                key = rawKey,
                maskedKey = maskKey(rawKey)
            )
        )
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
        localState.value.activeApiId?.let(apiKeyStore::remove)
        localState.update {
            it.copy(
                apiKeyInput = "",
                detectedProvider = null,
                activeProvider = null,
                activeModel = "AD-GPT",
                apiStatus = ApiStatus.Idle
            )
        }
    }

    fun send() {
        val prompt = localState.value.input.trim()
        val attachments = localState.value.attachments
        if ((prompt.isBlank() && attachments.isEmpty()) || localState.value.sending) return
        viewModelScope.launch {
            val attachmentSummary = attachments.joinToString(prefix = "\n\nAttachments: ") { "${it.name} (${it.type})" }
            localState.update { it.copy(input = "", attachments = emptyList(), sending = true) }
            runCatching { sendMessage(prompt + attachmentSummary) }
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

    fun addAttachment(name: String, type: String) {
        localState.update {
            it.copy(
                attachments = it.attachments + AttachmentUi(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    type = type
                )
            )
        }
    }

    fun removeAttachment(id: String) {
        localState.update { state -> state.copy(attachments = state.attachments.filterNot { it.id == id }) }
    }

    fun selectApi(id: String) {
        apiKeyStore.select(id)
    }

    fun toggleSidebar() {
        localState.update { it.copy(sidebarCollapsed = !it.sidebarCollapsed) }
    }

    fun toggleChatMinimized() {
        localState.update { it.copy(chatMinimized = !it.chatMinimized) }
    }

    fun toggleTheme() {
        localState.update { it.copy(darkTheme = !it.darkTheme) }
    }

    private fun detectProvider(key: String): ProviderUi? {
        if (key.length < 8 || key.contains(" ")) return null
        return when {
            key.startsWith("nvapi-", ignoreCase = true) ->
                ProviderUi("nvidia", "NVIDIA", "meta/llama-3.1-8b-instruct", "NVIDIA")
            key.startsWith("glm-", ignoreCase = true) || key.count { it == '.' } == 1 ->
                ProviderUi("glm", "GLM / Zhipu", "glm-4-flash", "GLM")
            key.startsWith("sk-ant-", ignoreCase = true) ->
                ProviderUi("anthropic", "Anthropic", "Claude", "CLAUDE")
            key.startsWith("AIza", ignoreCase = false) ->
                ProviderUi("google", "Google Gemini", "Gemini Pro", "GEMINI")
            key.startsWith("gsk_", ignoreCase = true) ->
                ProviderUi("groq", "Groq", "Llama / Mixtral", "GROQ")
            key.startsWith("xai-", ignoreCase = true) ->
                ProviderUi("xai", "xAI", "Grok", "XAI")
            key.startsWith("hf_", ignoreCase = true) ->
                ProviderUi("huggingface", "Hugging Face", "HF Inference", "HF")
            key.startsWith("co-", ignoreCase = true) ->
                ProviderUi("cohere", "Cohere", "Command", "COHERE")
            key.startsWith("pplx-", ignoreCase = true) ->
                ProviderUi("perplexity", "Perplexity", "Sonar", "PPLX")
            key.startsWith("replicate_", ignoreCase = true) || key.startsWith("r8_", ignoreCase = true) ->
                ProviderUi("replicate", "Replicate", "Replicate", "REPL")
            key.startsWith("sk-or-", ignoreCase = true) ->
                ProviderUi("openrouter", "OpenRouter", "OpenRouter Auto", "ROUTER")
            key.startsWith("sk-", ignoreCase = true) ->
                ProviderUi("openai", "OpenAI", "GPT", "OPENAI")
            key.startsWith("mistral_", ignoreCase = true) ->
                ProviderUi("mistral", "Mistral", "Mistral", "MISTRAL")
            else -> ProviderUi("custom", "Custom Provider", "OpenAI-compatible", "API")
        }
    }

    private fun maskKey(key: String): String {
        val normalized = key.trim()
        if (normalized.length <= 10) return "••••"
        return normalized.take(6) + "••••••" + normalized.takeLast(4)
    }
}

private fun SavedApiKey.toUi() = SavedApiUi(id, providerName, model, maskedKey)

private fun SavedApiKey.toProviderUi() = ProviderUi(providerId, providerName, model, providerName.uppercase().take(8))
