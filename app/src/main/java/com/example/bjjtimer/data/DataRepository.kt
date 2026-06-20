package com.example.bjjtimer.data

import com.example.bjjtimer.data.model.Preset
import com.example.bjjtimer.data.model.SessionRecord
import com.example.bjjtimer.data.model.TimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * In-memory repository for presets and session history.
 * Can be upgraded to Room DB later.
 */
class PresetRepository {

    private val _presets = MutableStateFlow(createBuiltInPresets())
    val presets: Flow<List<Preset>> = _presets.asStateFlow()

    private val _sessions = MutableStateFlow<List<SessionRecord>>(emptyList())
    val sessions: Flow<List<SessionRecord>> = _sessions.asStateFlow()

    fun getPreset(id: String): Preset? {
        return _presets.value.find { it.id == id }
    }

    fun savePreset(preset: Preset): Preset {
        val withId = if (preset.id.isEmpty()) {
            preset.copy(id = UUID.randomUUID().toString())
        } else preset

        val current = _presets.value.toMutableList()
        val index = current.indexOfFirst { it.id == withId.id }
        if (index >= 0) {
            current[index] = withId
        } else {
            current.add(withId)
        }
        _presets.value = current
        return withId
    }

    fun deletePreset(id: String) {
        _presets.value = _presets.value.filter { it.id != id || it.isBuiltIn }
    }

    fun addSession(session: SessionRecord) {
        val withId = if (session.id.isEmpty()) {
            session.copy(id = UUID.randomUUID().toString())
        } else session
        _sessions.value = listOf(withId) + _sessions.value
    }

    fun clearHistory() {
        _sessions.value = emptyList()
    }

    companion object {
        fun createBuiltInPresets(): List<Preset> = listOf(
            Preset(
                id = "ibjjf_white",
                name = "IBJJF White Belt",
                config = TimerConfig(
                    name = "IBJJF White Belt",
                    roundDurationSeconds = 300,
                    restDurationSeconds = 60,
                    numberOfRounds = 5
                ),
                isBuiltIn = true,
                beltColor = "white"
            ),
            Preset(
                id = "ibjjf_blue",
                name = "IBJJF Blue Belt",
                config = TimerConfig(
                    name = "IBJJF Blue Belt",
                    roundDurationSeconds = 360,
                    restDurationSeconds = 60,
                    numberOfRounds = 5
                ),
                isBuiltIn = true,
                beltColor = "blue"
            ),
            Preset(
                id = "ibjjf_purple",
                name = "IBJJF Purple Belt",
                config = TimerConfig(
                    name = "IBJJF Purple Belt",
                    roundDurationSeconds = 420,
                    restDurationSeconds = 60,
                    numberOfRounds = 5
                ),
                isBuiltIn = true,
                beltColor = "purple"
            ),
            Preset(
                id = "ibjjf_brown_black",
                name = "IBJJF Brown/Black",
                config = TimerConfig(
                    name = "IBJJF Brown/Black",
                    roundDurationSeconds = 600,
                    restDurationSeconds = 60,
                    numberOfRounds = 5
                ),
                isBuiltIn = true,
                beltColor = "brown"
            ),
            Preset(
                id = "adcc_qualifiers",
                name = "ADCC Qualifiers",
                config = TimerConfig(
                    name = "ADCC Qualifiers",
                    roundDurationSeconds = 300,
                    restDurationSeconds = 60,
                    numberOfRounds = 1
                ),
                isBuiltIn = true,
                beltColor = "gold"
            ),
            Preset(
                id = "training",
                name = "Training Rounds",
                config = TimerConfig(
                    name = "Training Rounds",
                    roundDurationSeconds = 420,
                    restDurationSeconds = 60,
                    numberOfRounds = 8
                ),
                isBuiltIn = true,
                beltColor = "blue"
            ),
            Preset(
                id = "drilling",
                name = "Drilling",
                config = TimerConfig(
                    name = "Drilling",
                    roundDurationSeconds = 180,
                    restDurationSeconds = 30,
                    numberOfRounds = 10
                ),
                isBuiltIn = true,
                beltColor = "white"
            ),
            Preset(
                id = "sub_only",
                name = "Sub Only",
                config = TimerConfig(
                    name = "Sub Only",
                    roundDurationSeconds = 600,
                    restDurationSeconds = 120,
                    numberOfRounds = 3
                ),
                isBuiltIn = true,
                beltColor = "black"
            ),
            Preset(
                id = "open_mat",
                name = "Open Mat",
                config = TimerConfig(
                    name = "Open Mat",
                    roundDurationSeconds = 360,
                    restDurationSeconds = 60,
                    numberOfRounds = 12
                ),
                isBuiltIn = true,
                beltColor = "blue"
            )
        )
    }
}
