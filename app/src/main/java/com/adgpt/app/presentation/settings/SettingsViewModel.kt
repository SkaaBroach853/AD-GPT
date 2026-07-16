package com.adgpt.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adgpt.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val reduceMotion = settingsRepository.reduceMotion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val playIntroOnStart = settingsRepository.playIntroOnStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReduceMotion(enabled) }
    }

    fun setPlayIntroOnStart(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPlayIntroOnStart(enabled) }
    }
}
