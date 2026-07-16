package com.adgpt.app.data.repository

import com.adgpt.app.data.local.ChatMessageDao
import com.adgpt.app.data.local.toDomain
import com.adgpt.app.data.local.toEntity
import com.adgpt.app.data.provider.AiProvider
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole
import com.adgpt.app.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatMessageDao,
    private val aiProvider: AiProvider
) : ChatRepository {
    override fun observeMessages(): Flow<List<ChatMessage>> =
        dao.observeMessages().map { messages -> messages.map { it.toDomain() } }

    override suspend fun sendMessage(content: String) {
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.User,
            content = content.trim(),
            createdAt = System.currentTimeMillis()
        )
        dao.insert(userMessage.toEntity())
        val reply = aiProvider.complete(listOf(userMessage))
        dao.insert(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                role = MessageRole.Assistant,
                content = reply,
                createdAt = System.currentTimeMillis()
            ).toEntity()
        )
    }

    override suspend fun clear() = dao.clear()
}
