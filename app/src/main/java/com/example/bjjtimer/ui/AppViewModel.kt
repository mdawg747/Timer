package com.example.bjjtimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bjjtimer.audio.SoundManager
import com.example.bjjtimer.data.PresetRepository
import com.example.bjjtimer.data.model.*
import com.example.bjjtimer.timer.TimerEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PresetRepository()
    val timerEngine = TimerEngine(viewModelScope)
    val soundManager = SoundManager(application)

    // Timer state
    val timerState: StateFlow<TimerState> = timerEngine.state

    // Current config (editable from home screen)
    private val _config = MutableStateFlow(TimerConfig())
    val config: StateFlow<TimerConfig> = _config.asStateFlow()

    // Presets
    val presets: Flow<List<Preset>> = repository.presets

    // Sessions
    val sessions: Flow<List<SessionRecord>> = repository.sessions

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    init {
        soundManager.initialize()

        // Listen for timer events and play sounds
        viewModelScope.launch {
            timerEngine.events.collect { event ->
                soundManager.handleEvent(event, _config.value)

                // Auto-save session on complete
                if (event is TimerEvent.SessionComplete) {
                    val state = timerState.value
                    repository.addSession(
                        SessionRecord(
                            presetName = _config.value.name,
                            roundsCompleted = state.currentRound,
                            totalRounds = state.totalRounds,
                            totalTimeSeconds = (state.totalElapsedMillis / 1000).toInt(),
                            roundDurationSeconds = _config.value.roundDurationSeconds,
                            restDurationSeconds = _config.value.restDurationSeconds
                        )
                    )
                }
            }
        }
    }

    fun updateConfig(config: TimerConfig) {
        _config.value = config
    }

    fun selectPreset(preset: Preset) {
        _config.value = preset.config
    }

    fun startTimer() {
        timerEngine.configure(_config.value)
        timerEngine.start()
        _currentScreen.value = Screen.TIMER
    }

    fun togglePause() = timerEngine.togglePause()
    fun skipToNext() = timerEngine.skipToNext()

    fun stopTimer() {
        timerEngine.stop()
        _currentScreen.value = Screen.HOME
    }

    fun restartTimer() {
        timerEngine.configure(_config.value)
        timerEngine.start()
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun savePreset(preset: Preset) {
        repository.savePreset(preset)
    }

    fun deletePreset(id: String) {
        repository.deletePreset(id)
    }

    fun previewSound(options: List<SoundOption>, selectedId: Int) {
        soundManager.previewSound(options, selectedId)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

enum class Screen {
    HOME,
    TIMER,
    SETTINGS,
    PRESETS
}
