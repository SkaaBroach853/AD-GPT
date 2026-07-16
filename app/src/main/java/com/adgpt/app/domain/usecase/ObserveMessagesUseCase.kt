package com.adgpt.app.domain.usecase

import com.adgpt.app.domain.repository.ChatRepository
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    operator fun invoke() = repository.observeMessages()
}
