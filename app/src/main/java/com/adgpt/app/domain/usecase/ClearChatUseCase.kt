package com.adgpt.app.domain.usecase

import com.adgpt.app.domain.repository.ChatRepository
import javax.inject.Inject

class ClearChatUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke() = repository.clear()
}
