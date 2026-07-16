package com.adgpt.app.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryChatMessageDao @Inject constructor() : ChatMessageDao {
    private val messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

    override fun observeMessages(): Flow<List<ChatMessageEntity>> = messages

    override suspend fun insert(message: ChatMessageEntity) {
        messages.update { current ->
            (current.filterNot { it.id == message.id } + message).sortedBy { it.createdAt }
        }
    }

    override suspend fun clear() {
        messages.value = emptyList()
    }
}
