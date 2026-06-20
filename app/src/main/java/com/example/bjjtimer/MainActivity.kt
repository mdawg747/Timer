package com.example.bjjtimer

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bjjtimer.data.model.TimerPhase
import com.example.bjjtimer.theme.BJJTimerTheme
import com.example.bjjtimer.ui.AppViewModel
import com.example.bjjtimer.ui.Screen
import com.example.bjjtimer.ui.home.HomeScreen
import com.example.bjjtimer.ui.presets.PresetsScreen
import com.example.bjjtimer.ui.settings.SettingsScreen
import com.example.bjjtimer.ui.timer.TimerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            BJJTimerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BJJTimerApp()
                }
            }
        }
    }
}

@Composable
fun BJJTimerApp(
    viewModel: AppViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle(initialValue = emptyList())

    // Keep screen on during active timer
    val activity = androidx.compose.ui.platform.LocalContext.current as? ComponentActivity
    LaunchedEffect(timerState.phase) {
        if (timerState.isRunning || timerState.isPaused) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            when {
                targetState == Screen.TIMER -> {
                    (fadeIn(tween(300)) + slideInVertically { it / 4 }) togetherWith
                            (fadeOut(tween(200)))
                }
                initialState == Screen.TIMER -> {
                    (fadeIn(tween(200))) togetherWith
                            (fadeOut(tween(300)) + slideOutVertically { it / 4 })
                }
                else -> {
                    (fadeIn(tween(200)) + slideInHorizontally { it / 4 }) togetherWith
                            (fadeOut(tween(200)) + slideOutHorizontally { -it / 4 })
                }
            }
        },
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            Screen.HOME -> {
                HomeScreen(
                    config = config,
                    presets = presets,
                    onConfigChange = viewModel::updateConfig,
                    onStart = viewModel::startTimer,
                    onNavigateToSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                    onNavigateToPresets = { viewModel.navigateTo(Screen.PRESETS) },
                    onPresetSelected = viewModel::selectPreset
                )
            }
            Screen.TIMER -> {
                TimerScreen(
                    state = timerState,
                    onTogglePause = viewModel::togglePause,
                    onSkip = viewModel::skipToNext,
                    onStop = viewModel::stopTimer,
                    onRestart = viewModel::restartTimer
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    config = config,
                    onConfigChange = viewModel::updateConfig,
                    onBack = { viewModel.navigateTo(Screen.HOME) },
                    roundStartSounds = viewModel.soundManager.roundStartSounds,
                    roundEndSounds = viewModel.soundManager.roundEndSounds,
                    minuteWarningSounds = viewModel.soundManager.minuteWarningSounds,
                    warningSounds = viewModel.soundManager.warningSounds,
                    countdownSounds = viewModel.soundManager.countdownSounds,
                    onPreviewSound = viewModel::previewSound
                )
            }
            Screen.PRESETS -> {
                PresetsScreen(
                    presets = presets,
                    currentConfig = config,
                    onPresetSelected = { preset ->
                        viewModel.selectPreset(preset)
                        viewModel.navigateTo(Screen.HOME)
                    },
                    onPresetSave = viewModel::savePreset,
                    onPresetDelete = viewModel::deletePreset,
                    onBack = { viewModel.navigateTo(Screen.HOME) }
                )
            }
        }
    }
}
