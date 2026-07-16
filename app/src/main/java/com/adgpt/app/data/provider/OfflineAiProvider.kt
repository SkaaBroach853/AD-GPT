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
            "AD-GPT is ready. Ask me anything and I’ll help shape it into something useful."
        } else {
            "I’m running in local demo mode. You said: “$prompt”. Connect a real provider in the provider layer to stream production responses."
        }
    }
}
