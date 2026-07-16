package com.adgpt.app.data.provider

import com.adgpt.app.domain.model.ChatMessage

interface AiProvider {
    val id: String
    suspend fun complete(messages: List<ChatMessage>): String
}
