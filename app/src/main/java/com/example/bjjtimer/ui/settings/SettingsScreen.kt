package com.example.bjjtimer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bjjtimer.data.model.SoundOption
import com.example.bjjtimer.data.model.TimerConfig
import com.example.bjjtimer.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: TimerConfig,
    onConfigChange: (TimerConfig) -> Unit,
    onBack: () -> Unit,
    roundStartSounds: List<SoundOption>,
    roundEndSounds: List<SoundOption>,
    minuteWarningSounds: List<SoundOption>,
    warningSounds: List<SoundOption>,
    countdownSounds: List<SoundOption>,
    onPreviewSound: (List<SoundOption>, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, DarkBackground, GradientEnd)
                )
            )
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 24.sp, color = BjjGold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Sound Settings
            SectionHeader("ROUND SOUNDS")

            SoundSelector(
                label = "Round Start",
                options = roundStartSounds,
                selectedId = config.roundStartSoundId,
                onSelect = { onConfigChange(config.copy(roundStartSoundId = it)) },
                onPreview = { onPreviewSound(roundStartSounds, it) }
            )

            SoundSelector(
                label = "Round End",
                options = roundEndSounds,
                selectedId = config.roundEndSoundId,
                onSelect = { onConfigChange(config.copy(roundEndSoundId = it)) },
                onPreview = { onPreviewSound(roundEndSounds, it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("WARNING SOUNDS")

            SoundSelector(
                label = "Minute Warning",
                options = minuteWarningSounds,
                selectedId = config.minuteWarningSoundId,
                onSelect = { onConfigChange(config.copy(minuteWarningSoundId = it)) },
                onPreview = { onPreviewSound(minuteWarningSounds, it) }
            )

            SoundSelector(
                label = "30-Second Warning",
                options = warningSounds,
                selectedId = config.warningSoundId,
                onSelect = { onConfigChange(config.copy(warningSoundId = it)) },
                onPreview = { onPreviewSound(warningSounds, it) }
            )

            SoundSelector(
                label = "Countdown Tick",
                options = countdownSounds,
                selectedId = config.countdownSoundId,
                onSelect = { onConfigChange(config.copy(countdownSoundId = it)) },
                onPreview = { onPreviewSound(countdownSounds, it) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("ALERT OPTIONS")

            ToggleSettingRow(
                label = "Minute Alerts",
                description = "Alert at each minute mark",
                checked = config.enableMinuteAlerts,
                onCheckedChange = { onConfigChange(config.copy(enableMinuteAlerts = it)) }
            )

            ToggleSettingRow(
                label = "Voice Announcer",
                description = "Ring announcer calls out '5 minutes', '4 minutes', etc.",
                checked = config.useVoiceAnnouncer,
                onCheckedChange = { onConfigChange(config.copy(useVoiceAnnouncer = it)) }
            )

            ToggleSettingRow(
                label = "30-Second Warning",
                description = "Alert with 30 seconds remaining",
                checked = config.enable30SecWarning,
                onCheckedChange = { onConfigChange(config.copy(enable30SecWarning = it)) }
            )

            ToggleSettingRow(
                label = "10-Second Countdown",
                description = "Tick for the final 10 seconds",
                checked = config.enable10SecCountdown,
                onCheckedChange = { onConfigChange(config.copy(enable10SecCountdown = it)) }
            )

            ToggleSettingRow(
                label = "Half-Time Alert",
                description = "Alert at the halfway point of each round",
                checked = config.enableHalfTimeAlert,
                onCheckedChange = { onConfigChange(config.copy(enableHalfTimeAlert = it)) }
            )

            ToggleSettingRow(
                label = "Vibration",
                description = "Haptic feedback alongside sounds",
                checked = config.enableVibration,
                onCheckedChange = { onConfigChange(config.copy(enableVibration = it)) }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 3.sp
        ),
        color = TextTertiary,
        modifier = Modifier.padding(bottom = 14.dp, top = 8.dp)
    )
}

@Composable
private fun SoundSelector(
    label: String,
    options: List<SoundOption>,
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onPreview: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = options.getOrNull(selectedId)?.name ?: options.firstOrNull()?.name ?: "None"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(GlassWhite, GlassWhite.copy(alpha = 0.04f)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = selectedName,
                        style = MaterialTheme.typography.bodySmall,
                        color = BjjGold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Preview button
                    FilledTonalButton(
                        onClick = { onPreview(selectedId) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GlassWhite,
                            contentColor = BjjGold
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("▶", fontSize = 14.sp)
                    }

                    // Dropdown toggle
                    FilledTonalButton(
                        onClick = { expanded = !expanded },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GlassWhite,
                            contentColor = TextSecondary
                        ),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(if (expanded) "▲" else "▼", fontSize = 12.sp)
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(8.dp))

                options.forEach { option ->
                    val isSelected = option.id == selectedId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) GlassWhite.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable {
                                onSelect(option.id)
                                onPreview(option.id)
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) BjjGold else TextPrimary
                        )
                        if (isSelected) {
                            Text("✓", color = BjjGold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = BjjGold,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = GlassWhite
            )
        )
    }
}
