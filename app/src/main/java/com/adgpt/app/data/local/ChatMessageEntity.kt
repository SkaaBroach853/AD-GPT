package com.adgpt.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adgpt.app.domain.model.ChatMessage
import com.adgpt.app.domain.model.MessageRole

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: MessageRole,
    val content: String,
    val createdAt: Long
)

fun ChatMessageEntity.toDomain() = ChatMessage(id, role, content, createdAt)
fun ChatMessage.toEntity() = ChatMessageEntity(id, role, content, createdAt)
