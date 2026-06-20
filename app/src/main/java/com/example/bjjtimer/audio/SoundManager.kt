package com.example.bjjtimer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.bjjtimer.R
import com.example.bjjtimer.data.model.SoundOption
import com.example.bjjtimer.data.model.TimerConfig
import com.example.bjjtimer.data.model.TimerEvent

/**
 * Manages sound playback and vibration for timer events.
 * Uses SoundPool for low-latency short sound clips.
 */
class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val loadedSounds = mutableMapOf<Int, Int>() // resourceId -> soundPool id
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    // Sound option lists per event type
    val roundStartSounds = listOf(
        SoundOption(0, "Boxing Bell", "boxing_bell"),
        SoundOption(1, "Buzzer", "buzzer"),
        SoundOption(2, "Air Horn", "air_horn"),
        SoundOption(3, "Whistle", "whistle")
    )

    val roundEndSounds = listOf(
        SoundOption(0, "Double Bell", "double_bell"),
        SoundOption(1, "Gong", "gong"),
        SoundOption(2, "Buzzer", "buzzer"),
        SoundOption(3, "Whistle", "whistle")
    )

    val minuteWarningSounds = listOf(
        SoundOption(0, "Soft Beep", "soft_beep"),
        SoundOption(1, "Click", "click"),
        SoundOption(2, "Chime", "chime")
    )

    val warningSounds = listOf(
        SoundOption(0, "Double Beep", "double_beep"),
        SoundOption(1, "Alert Tone", "alert_tone"),
        SoundOption(2, "Buzzer", "buzzer")
    )

    val countdownSounds = listOf(
        SoundOption(0, "Tick", "tick"),
        SoundOption(1, "Beep", "soft_beep"),
        SoundOption(2, "Click", "click")
    )

    fun initialize() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load all sound resources
        val resources = mapOf(
            R.raw.boxing_bell to "boxing_bell",
            R.raw.buzzer to "buzzer",
            R.raw.air_horn to "air_horn",
            R.raw.whistle to "whistle",
            R.raw.double_bell to "double_bell",
            R.raw.gong to "gong",
            R.raw.soft_beep to "soft_beep",
            R.raw.click to "click",
            R.raw.chime to "chime",
            R.raw.double_beep to "double_beep",
            R.raw.alert_tone to "alert_tone",
            R.raw.tick to "tick",
            R.raw.session_complete to "session_complete",
            // Ring announcer voice files (1-5 minutes)
            R.raw.announcer_1min to "announcer_1min",
            R.raw.announcer_2min to "announcer_2min",
            R.raw.announcer_3min to "announcer_3min",
            R.raw.announcer_4min to "announcer_4min",
            R.raw.announcer_5min to "announcer_5min"
        )

        resources.forEach { (resId, _) ->
            soundPool?.let { pool ->
                loadedSounds[resId] = pool.load(context, resId, 1)
            }
        }
    }

    fun handleEvent(event: TimerEvent, config: TimerConfig) {
        when (event) {
            is TimerEvent.RoundStarted -> {
                playSoundForOption(roundStartSounds, config.roundStartSoundId)
                if (config.enableVibration) vibratePattern(longArrayOf(0, 300, 100, 300))
            }
            is TimerEvent.RoundEnded -> {
                playSoundForOption(roundEndSounds, config.roundEndSoundId)
                if (config.enableVibration) vibratePattern(longArrayOf(0, 500, 200, 500))
            }
            is TimerEvent.RestStarted -> {
                // Softer notification for rest start
                if (config.enableVibration) vibrateShort()
            }
            is TimerEvent.RestEnded -> {
                playSoundForOption(roundStartSounds, config.roundStartSoundId)
                if (config.enableVibration) vibratePattern(longArrayOf(0, 300, 100, 300))
            }
            is TimerEvent.MinuteWarning -> {
                if (config.useVoiceAnnouncer) {
                    // Play the matching ring announcer voice file
                    val announcerResId = announcerForMinute(event.minutesLeft)
                    if (announcerResId != 0) {
                        playSound(announcerResId)
                    } else {
                        // Fallback to beep for minutes > 5
                        playSoundForOption(minuteWarningSounds, config.minuteWarningSoundId)
                    }
                } else {
                    playSoundForOption(minuteWarningSounds, config.minuteWarningSoundId)
                }
                if (config.enableVibration) vibrateShort()
            }
            is TimerEvent.ThirtySecondWarning -> {
                playSoundForOption(warningSounds, config.warningSoundId)
                if (config.enableVibration) vibratePattern(longArrayOf(0, 200, 100, 200))
            }
            is TimerEvent.HalfTime -> {
                playSoundForOption(minuteWarningSounds, config.minuteWarningSoundId)
                if (config.enableVibration) vibrateShort()
            }
            is TimerEvent.CountdownTick -> {
                playSoundForOption(countdownSounds, config.countdownSoundId, volume = 0.6f)
                if (config.enableVibration) vibrateTick()
            }
            is TimerEvent.SessionComplete -> {
                playSound(R.raw.session_complete)
                if (config.enableVibration) vibratePattern(longArrayOf(0, 400, 200, 400, 200, 400))
            }
        }
    }

    fun previewSound(options: List<SoundOption>, selectedId: Int) {
        playSoundForOption(options, selectedId)
    }

    private fun playSoundForOption(options: List<SoundOption>, selectedId: Int, volume: Float = 1.0f) {
        val option = options.getOrNull(selectedId) ?: options.firstOrNull() ?: return
        val resId = getResourceId(option.resourceName)
        if (resId != 0) {
            playSound(resId, volume)
        }
    }

    private fun playSound(resId: Int, volume: Float = 1.0f) {
        val soundId = loadedSounds[resId] ?: return
        soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    /**
     * Returns the resource ID for the ring announcer voice file
     * matching the given minute. Covers 1-5 minutes.
     */
    private fun announcerForMinute(minutesLeft: Int): Int {
        return when (minutesLeft) {
            1 -> R.raw.announcer_1min
            2 -> R.raw.announcer_2min
            3 -> R.raw.announcer_3min
            4 -> R.raw.announcer_4min
            5 -> R.raw.announcer_5min
            else -> 0 // No voice file for this minute
        }
    }

    private fun getResourceId(name: String): Int {
        return try {
            val field = R.raw::class.java.getField(name)
            field.getInt(null)
        } catch (e: Exception) {
            0
        }
    }

    private fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrateTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loadedSounds.clear()
    }
}
