package com.adgpt.app.domain.model

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val createdAt: Long
)

enum class MessageRole {
    User,
    Assistant,
    System
}
