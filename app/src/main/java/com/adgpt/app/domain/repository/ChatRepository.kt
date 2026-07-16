package com.adgpt.app.domain.repository

import com.adgpt.app.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(content: String)
    suspend fun clear()
}
