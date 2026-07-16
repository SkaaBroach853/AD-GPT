package com.adgpt.app.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    val provider: String,
    val messages: List<NetworkMessageDto>
)

@Serializable
data class NetworkMessageDto(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponseDto(
    val content: String
)
