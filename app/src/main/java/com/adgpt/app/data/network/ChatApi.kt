package com.adgpt.app.data.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    @POST("v1/chat")
    suspend fun sendMessage(@Body request: ChatRequestDto): ChatResponseDto
}
