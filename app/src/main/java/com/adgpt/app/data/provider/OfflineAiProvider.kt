package com.adgpt.app.data.provider

import com.adgpt.app.domain.model.ChatMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineAiProvider @Inject constructor() : AiProvider {
    override val id: String = "offline"

    override suspend fun complete(messages: List<ChatMessage>): String {
        val prompt = messages.lastOrNull()?.content.orEmpty()
        return if (prompt.isBlank()) {
            "AD-GPT is ready. Ask me anything."
        } else {
            "You said: $prompt"
        }
    }
}
