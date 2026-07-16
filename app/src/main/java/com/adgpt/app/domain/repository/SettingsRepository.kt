package com.adgpt.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val reduceMotion: Flow<Boolean>
    val playIntroOnStart: Flow<Boolean>
    suspend fun setReduceMotion(enabled: Boolean)
    suspend fun setPlayIntroOnStart(enabled: Boolean)
}
