package com.example.bjjtimer.timer

import com.example.bjjtimer.data.model.TimerConfig
import com.example.bjjtimer.data.model.TimerEvent
import com.example.bjjtimer.data.model.TimerPhase
import com.example.bjjtimer.data.model.TimerState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core timer engine that manages round/rest countdown logic.
 * Emits TimerState for UI and TimerEvent for audio/haptic triggers.
 */
class TimerEngine(private val scope: CoroutineScope) {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var config: TimerConfig = TimerConfig()
    private var currentRound = 0
    private var totalElapsed = 0L

    // Tracks which minute warnings have already fired this phase
    private val firedMinuteWarnings = mutableSetOf<Int>()
    private var fired30SecWarning = false
    private var firedHalfTime = false

    fun configure(timerConfig: TimerConfig) {
        config = timerConfig
        _state.value = TimerState(
            phase = TimerPhase.IDLE,
            currentRound = 0,
            totalRounds = config.numberOfRounds,
            timeRemainingMillis = config.roundDurationSeconds * 1000L,
            totalPhaseMillis = config.roundDurationSeconds * 1000L,
            config = config
        )
    }

    fun start() {
        if (_state.value.phase == TimerPhase.IDLE || _state.value.phase == TimerPhase.COMPLETED) {
            currentRound = 1
            totalElapsed = 0L
            startRound()
        }
    }

    fun resume() {
        val current = _state.value
        when (current.phase) {
            TimerPhase.PAUSED_ROUND -> {
                _state.value = current.copy(phase = TimerPhase.ROUND_RUNNING)
                startCountdown(current.timeRemainingMillis, isRound = true)
            }
            TimerPhase.PAUSED_REST -> {
                _state.value = current.copy(phase = TimerPhase.REST_RUNNING)
                startCountdown(current.timeRemainingMillis, isRound = false)
            }
            else -> {}
        }
    }

    fun pause() {
        timerJob?.cancel()
        val current = _state.value
        _state.value = when (current.phase) {
            TimerPhase.ROUND_RUNNING -> current.copy(phase = TimerPhase.PAUSED_ROUND)
            TimerPhase.REST_RUNNING -> current.copy(phase = TimerPhase.PAUSED_REST)
            else -> current
        }
    }

    fun togglePause() {
        val current = _state.value
        when {
            current.isRunning -> pause()
            current.isPaused -> resume()
            current.phase == TimerPhase.IDLE -> start()
            current.phase == TimerPhase.COMPLETED -> start()
        }
    }

    fun skipToNext() {
        timerJob?.cancel()
        val current = _state.value
        when {
            current.isRoundPhase -> {
                // Skip to rest (or next round if last)
                if (currentRound >= config.numberOfRounds) {
                    completeSession()
                } else {
                    startRest()
                }
            }
            current.isRestPhase -> {
                // Skip to next round
                currentRound++
                startRound()
            }
            else -> {}
        }
    }

    fun stop() {
        timerJob?.cancel()
        currentRound = 0
        totalElapsed = 0L
        _state.value = TimerState(
            phase = TimerPhase.IDLE,
            totalRounds = config.numberOfRounds,
            timeRemainingMillis = config.roundDurationSeconds * 1000L,
            totalPhaseMillis = config.roundDurationSeconds * 1000L,
            config = config
        )
    }

    private fun startRound() {
        val durationMillis = config.roundDurationSeconds * 1000L
        resetPhaseFlags()
        _state.value = TimerState(
            phase = TimerPhase.ROUND_RUNNING,
            currentRound = currentRound,
            totalRounds = config.numberOfRounds,
            timeRemainingMillis = durationMillis,
            totalPhaseMillis = durationMillis,
            totalElapsedMillis = totalElapsed,
            config = config
        )
        scope.launch { _events.emit(TimerEvent.RoundStarted(currentRound)) }
        startCountdown(durationMillis, isRound = true)
    }

    private fun startRest() {
        val durationMillis = config.restDurationSeconds * 1000L
        resetPhaseFlags()
        scope.launch { _events.emit(TimerEvent.RoundEnded(currentRound)) }
        _state.value = TimerState(
            phase = TimerPhase.REST_RUNNING,
            currentRound = currentRound,
            totalRounds = config.numberOfRounds,
            timeRemainingMillis = durationMillis,
            totalPhaseMillis = durationMillis,
            totalElapsedMillis = totalElapsed,
            config = config
        )
        scope.launch { _events.emit(TimerEvent.RestStarted(currentRound)) }
        startCountdown(durationMillis, isRound = false)
    }

    private fun startCountdown(durationMillis: Long, isRound: Boolean) {
        timerJob?.cancel()
        timerJob = scope.launch {
            var remaining = durationMillis
            val tickInterval = 50L // 50ms for smooth UI updates

            while (remaining > 0 && isActive) {
                delay(tickInterval)
                remaining -= tickInterval
                totalElapsed += tickInterval

                if (remaining < 0) remaining = 0

                val currentPhase = if (isRound) TimerPhase.ROUND_RUNNING else TimerPhase.REST_RUNNING
                _state.value = _state.value.copy(
                    phase = currentPhase,
                    timeRemainingMillis = remaining,
                    totalElapsedMillis = totalElapsed
                )

                // Fire events at specific time thresholds
                checkAndFireEvents(remaining, isRound)
            }

            // Phase completed
            if (isActive) {
                onPhaseComplete(isRound)
            }
        }
    }

    private suspend fun checkAndFireEvents(remainingMillis: Long, isRound: Boolean) {
        val remainingSeconds = (remainingMillis / 1000).toInt()

        if (isRound && config.enableMinuteAlerts) {
            // Fire at exact minute boundaries (e.g., 4:00, 3:00, 2:00, 1:00)
            val totalSeconds = config.roundDurationSeconds
            for (minute in 1 until (totalSeconds / 60)) {
                val thresholdSec = minute * 60
                if (remainingSeconds == thresholdSec && !firedMinuteWarnings.contains(minute)) {
                    firedMinuteWarnings.add(minute)
                    _events.emit(TimerEvent.MinuteWarning(minute))
                }
            }
        }

        // Half-time alert
        if (isRound && config.enableHalfTimeAlert) {
            val halfSec = config.roundDurationSeconds / 2
            if (remainingSeconds == halfSec && !firedHalfTime) {
                firedHalfTime = true
                _events.emit(TimerEvent.HalfTime)
            }
        }

        // 30-second warning
        if (isRound && config.enable30SecWarning) {
            if (remainingSeconds == 30 && !fired30SecWarning) {
                fired30SecWarning = true
                _events.emit(TimerEvent.ThirtySecondWarning)
            }
        }

        // 10-second countdown
        if (config.enable10SecCountdown && remainingSeconds <= 10 && remainingSeconds > 0) {
            // Only fire once per second
            val milliInSecond = (remainingMillis % 1000)
            if (milliInSecond in 0..60) {
                _events.emit(TimerEvent.CountdownTick(remainingSeconds))
            }
        }
    }

    private suspend fun onPhaseComplete(wasRound: Boolean) {
        if (wasRound) {
            if (currentRound >= config.numberOfRounds) {
                _events.emit(TimerEvent.RoundEnded(currentRound))
                completeSession()
            } else {
                startRest()
            }
        } else {
            // Rest completed, start next round
            _events.emit(TimerEvent.RestEnded(currentRound))
            currentRound++
            startRound()
        }
    }

    private fun completeSession() {
        scope.launch { _events.emit(TimerEvent.SessionComplete) }
        _state.value = TimerState(
            phase = TimerPhase.COMPLETED,
            currentRound = currentRound,
            totalRounds = config.numberOfRounds,
            timeRemainingMillis = 0,
            totalPhaseMillis = 0,
            totalElapsedMillis = totalElapsed,
            config = config
        )
    }

    private fun resetPhaseFlags() {
        firedMinuteWarnings.clear()
        fired30SecWarning = false
        firedHalfTime = false
    }
}
