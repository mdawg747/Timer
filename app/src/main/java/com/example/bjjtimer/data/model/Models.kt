package com.example.bjjtimer.data.model

import kotlinx.serialization.Serializable

/**
 * Core configuration for a timer session.
 */
@Serializable
data class TimerConfig(
    val name: String = "Custom",
    val roundDurationSeconds: Int = 300,      // 5 min default
    val restDurationSeconds: Int = 60,         // 1 min default
    val numberOfRounds: Int = 5,
    val enableMinuteAlerts: Boolean = true,
    val useVoiceAnnouncer: Boolean = true,   // Use ring announcer voice files at minute marks
    val enable30SecWarning: Boolean = true,
    val enable10SecCountdown: Boolean = true,
    val enableHalfTimeAlert: Boolean = false,
    val enableVibration: Boolean = true,
    val roundStartSoundId: Int = 0,   // Index into SoundOption lists
    val roundEndSoundId: Int = 0,
    val restEndSoundId: Int = 0,
    val minuteWarningSoundId: Int = 0,
    val warningSoundId: Int = 0,      // 30-sec warning
    val countdownSoundId: Int = 0,    // 10-sec ticks
) {
    val roundDurationMinutes: Int get() = roundDurationSeconds / 60
    val roundDurationExtraSeconds: Int get() = roundDurationSeconds % 60
    val restDurationMinutes: Int get() = restDurationSeconds / 60
    val restDurationExtraSeconds: Int get() = restDurationSeconds % 60
}

/**
 * Represents a saved preset configuration.
 */
@Serializable
data class Preset(
    val id: String = "",
    val name: String,
    val config: TimerConfig,
    val isBuiltIn: Boolean = false,
    val beltColor: String = "white" // white, blue, purple, brown, black, gold
)

/**
 * Available sound options for each event type.
 */
data class SoundOption(
    val id: Int,
    val name: String,
    val resourceName: String // raw resource name
)

/**
 * The current phase of the timer.
 */
enum class TimerPhase {
    IDLE,
    ROUND_RUNNING,
    REST_RUNNING,
    PAUSED_ROUND,
    PAUSED_REST,
    COMPLETED
}

/**
 * Full timer state emitted to the UI.
 */
data class TimerState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val currentRound: Int = 0,
    val totalRounds: Int = 0,
    val timeRemainingMillis: Long = 0L,
    val totalPhaseMillis: Long = 0L,
    val totalElapsedMillis: Long = 0L,
    val config: TimerConfig = TimerConfig()
) {
    val timeRemainingSeconds: Int get() = (timeRemainingMillis / 1000).toInt()
    val minutesRemaining: Int get() = timeRemainingSeconds / 60
    val secondsRemaining: Int get() = timeRemainingSeconds % 60
    val progress: Float get() = if (totalPhaseMillis > 0) {
        1f - (timeRemainingMillis.toFloat() / totalPhaseMillis.toFloat())
    } else 0f

    val isRunning: Boolean get() = phase == TimerPhase.ROUND_RUNNING || phase == TimerPhase.REST_RUNNING
    val isPaused: Boolean get() = phase == TimerPhase.PAUSED_ROUND || phase == TimerPhase.PAUSED_REST
    val isRoundPhase: Boolean get() = phase == TimerPhase.ROUND_RUNNING || phase == TimerPhase.PAUSED_ROUND
    val isRestPhase: Boolean get() = phase == TimerPhase.REST_RUNNING || phase == TimerPhase.PAUSED_REST

    val formattedTime: String get() {
        val mins = minutesRemaining
        val secs = secondsRemaining
        return "%d:%02d".format(mins, secs)
    }

    val phaseLabel: String get() = when (phase) {
        TimerPhase.ROUND_RUNNING, TimerPhase.PAUSED_ROUND -> "ROUND"
        TimerPhase.REST_RUNNING, TimerPhase.PAUSED_REST -> "REST"
        TimerPhase.COMPLETED -> "DONE"
        else -> "READY"
    }
}

/**
 * Events the timer engine fires for sound/vibration triggers.
 */
sealed class TimerEvent {
    data class RoundStarted(val round: Int) : TimerEvent()
    data class RoundEnded(val round: Int) : TimerEvent()
    data class RestStarted(val round: Int) : TimerEvent()
    data class RestEnded(val round: Int) : TimerEvent()
    data class MinuteWarning(val minutesLeft: Int) : TimerEvent()
    object ThirtySecondWarning : TimerEvent()
    object HalfTime : TimerEvent()
    data class CountdownTick(val secondsLeft: Int) : TimerEvent()
    object SessionComplete : TimerEvent()
}

/**
 * A completed training session record.
 */
@Serializable
data class SessionRecord(
    val id: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val presetName: String = "",
    val roundsCompleted: Int = 0,
    val totalRounds: Int = 0,
    val totalTimeSeconds: Int = 0,
    val roundDurationSeconds: Int = 0,
    val restDurationSeconds: Int = 0
)
