package com.adgpt.app.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adgpt.app.domain.repository.ChatRepository
import com.adgpt.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val startupVideoResolver: StartupVideoResolver,
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(StartupState())
    val state = _state.asStateFlow()
    private var transitionJob: Job? = null
    private var startupStarted = false

    fun beginStartup() {
        if (startupStarted) return
        startupStarted = true
        viewModelScope.launch {
            val shouldPlayIntro = settingsRepository.playIntroOnStart.first()
            val video = if (shouldPlayIntro) startupVideoResolver.findStartupVideo() else null
            _state.update {
                it.copy(
                    videoAssetPath = video,
                    showIntro = video != null,
                    mainVisible = video == null,
                    mainEntranceReady = video == null
                )
            }
            preloadApplication()
            _state.update { it.copy(appPreloaded = true) }
            if (video == null) revealMainImmediately()
        }
    }

    fun skipIntro() = finishIntro()

    fun finishIntro() {
        if (transitionJob?.isActive == true || !_state.value.showIntro) return
        transitionJob = viewModelScope.launch {
            _state.update { it.copy(transitionActive = true, mainVisible = true) }
            delay(360)
            _state.update { it.copy(showIntro = false, mainEntranceReady = true) }
        }
    }

    private suspend fun preloadApplication() {
        settingsRepository.reduceMotion.first()
        settingsRepository.playIntroOnStart.first()
        chatRepository.observeMessages().first()
    }

    private fun revealMainImmediately() {
        _state.update {
            it.copy(showIntro = false, transitionActive = false, mainVisible = true, mainEntranceReady = true)
        }
    }
}
