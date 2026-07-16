package com.adgpt.app.data.local

import kotlinx.coroutines.flow.Flow

interface ChatMessageDao {
    fun observeMessages(): Flow<List<ChatMessageEntity>>

    suspend fun insert(message: ChatMessageEntity)

    suspend fun clear()
}
