package com.adgpt.app.domain.usecase

import com.adgpt.app.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(content: String) {
        if (content.isNotBlank()) repository.sendMessage(content)
    }
}
