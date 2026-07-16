package com.adgpt.app.data.provider

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRegistry @Inject constructor(
    private val defaultProvider: AiProvider
) {
    fun defaultProvider(): AiProvider = defaultProvider
    fun providerById(id: String): AiProvider = defaultProvider.takeIf { it.id == id } ?: defaultProvider
}
