package com.example.bjjtimer.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bjjtimer.data.model.Preset
import com.example.bjjtimer.data.model.TimerConfig
import com.example.bjjtimer.theme.*

@Composable
fun HomeScreen(
    config: TimerConfig,
    presets: List<Preset>,
    onConfigChange: (TimerConfig) -> Unit,
    onStart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onPresetSelected: (Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, DarkBackground, GradientEnd)
                )
            )
    ) {
        // Subtle background accent glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BjjGold.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.08f),
                    radius = size.width * 0.5f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 100.dp)
        ) {
            // ── Header with gradient text effect ────────────────────────────
            Text(
                text = "BJJ TIMER",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    letterSpacing = 6.sp
                ),
                color = BjjGold,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Round Timer for Jiu-Jitsu",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            // ── Configure Section ───────────────────────────────────────────
            SectionLabel("CONFIGURE")

            // Round Duration — Glass Card
            GlassDurationCard(
                label = "Round Duration",
                minutes = config.roundDurationMinutes,
                seconds = config.roundDurationExtraSeconds,
                color = TimerGreen,
                onMinutesChange = { mins ->
                    onConfigChange(config.copy(
                        roundDurationSeconds = mins * 60 + config.roundDurationExtraSeconds
                    ))
                },
                onSecondsChange = { secs ->
                    onConfigChange(config.copy(
                        roundDurationSeconds = config.roundDurationMinutes * 60 + secs
                    ))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rest Duration
            GlassDurationCard(
                label = "Rest Duration",
                minutes = config.restDurationMinutes,
                seconds = config.restDurationExtraSeconds,
                color = RestBlue,
                onMinutesChange = { mins ->
                    onConfigChange(config.copy(
                        restDurationSeconds = mins * 60 + config.restDurationExtraSeconds
                    ))
                },
                onSecondsChange = { secs ->
                    onConfigChange(config.copy(
                        restDurationSeconds = config.restDurationMinutes * 60 + secs
                    ))
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rounds
            GlassRoundsCard(
                rounds = config.numberOfRounds,
                onRoundsChange = { onConfigChange(config.copy(numberOfRounds = it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Presets Section ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel("PRESETS", addPadding = false)
                TextButton(onClick = onNavigateToPresets) {
                    Text(
                        "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = BjjGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(presets) { preset ->
                    PresetChip(
                        preset = preset,
                        isSelected = preset.config == config,
                        onClick = { onPresetSelected(preset) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Alerts Section ──────────────────────────────────────────────
            SectionLabel("ALERTS")

            GlassToggleRow(
                label = "Minute Alerts",
                description = "Alert at each minute mark",
                checked = config.enableMinuteAlerts,
                onCheckedChange = { onConfigChange(config.copy(enableMinuteAlerts = it)) }
            )
            GlassToggleRow(
                label = "Voice Announcer",
                description = "Ring announcer calls out minutes remaining",
                checked = config.useVoiceAnnouncer,
                onCheckedChange = { onConfigChange(config.copy(useVoiceAnnouncer = it)) }
            )
            GlassToggleRow(
                label = "30-Second Warning",
                description = "Alert at 30 seconds remaining",
                checked = config.enable30SecWarning,
                onCheckedChange = { onConfigChange(config.copy(enable30SecWarning = it)) }
            )
            GlassToggleRow(
                label = "10-Second Countdown",
                description = "Tick every second in final 10",
                checked = config.enable10SecCountdown,
                onCheckedChange = { onConfigChange(config.copy(enable10SecCountdown = it)) }
            )
            GlassToggleRow(
                label = "Vibration",
                description = "Haptic feedback with alerts",
                checked = config.enableVibration,
                onCheckedChange = { onConfigChange(config.copy(enableVibration = it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Settings link
            OutlinedButton(
                onClick = onNavigateToSettings,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.horizontalGradient(
                        listOf(BorderSubtle, GlassWhiteBorder)
                    )
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "⚙  Sound & Advanced Settings",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // ── Floating Start Button ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, DarkBackground, DarkBackground),
                        startY = 0f,
                        endY = 120f
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(BjjGold, BjjGoldLight, BjjGold)
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .drawBehind {
                        // Glow under button
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    BjjGold.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2, size.height),
                                radius = size.width * 0.5f
                            )
                        )
                    },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "START TIMER",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontSize = 18.sp
                    ),
                    color = DarkBackground
                )
            }
        }
    }
}

// ── Section Label ────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String, addPadding: Boolean = true) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            letterSpacing = 3.sp
        ),
        color = TextTertiary,
        modifier = if (addPadding) Modifier.padding(bottom = 14.dp) else Modifier
    )
}

// ── Glass Duration Card ──────────────────────────────────────────────────────
@Composable
private fun GlassDurationCard(
    label: String,
    minutes: Int,
    seconds: Int,
    color: Color,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(GlassWhite, GlassWhite.copy(alpha = 0.04f)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, GlassWhiteBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                // Glowing dot indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .drawBehind {
                            drawCircle(
                                color = color.copy(alpha = 0.4f),
                                radius = size.minDimension
                            )
                        }
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NumberPicker(
                    value = minutes,
                    range = 0..30,
                    onValueChange = onMinutesChange,
                    label = "min",
                    accent = color
                )

                Text(
                    ":",
                    style = MaterialTheme.typography.displaySmall,
                    color = TextTertiary.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NumberPicker(
                    value = seconds,
                    range = 0..59,
                    step = 5,
                    onValueChange = onSecondsChange,
                    label = "sec",
                    accent = color
                )
            }
        }
    }
}

// ── Number Picker ────────────────────────────────────────────────────────────
@Composable
private fun NumberPicker(
    value: Int,
    range: IntRange,
    step: Int = 1,
    onValueChange: (Int) -> Unit,
    label: String,
    accent: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalButton(
            onClick = {
                val next = value + step
                if (next <= range.last) onValueChange(next)
            },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = GlassWhite,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(50.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("▲", fontSize = 14.sp)
        }

        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        FilledTonalButton(
            onClick = {
                val next = value - step
                if (next >= range.first) onValueChange(next)
            },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = GlassWhite,
                contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(50.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("▼", fontSize = 14.sp)
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = 0.7f)
        )
    }
}

// ── Glass Rounds Card ────────────────────────────────────────────────────────
@Composable
private fun GlassRoundsCard(
    rounds: Int,
    onRoundsChange: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(GlassWhite, GlassWhite.copy(alpha = 0.04f)),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, GlassWhiteBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .drawBehind {
                            drawCircle(
                                color = BjjGold.copy(alpha = 0.4f),
                                radius = size.minDimension
                            )
                        }
                        .clip(CircleShape)
                        .background(BjjGold)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Rounds",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FilledTonalButton(
                    onClick = { if (rounds > 1) onRoundsChange(rounds - 1) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GlassWhite,
                        contentColor = TextPrimary
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("−", fontSize = 20.sp)
                }

                Text(
                    text = "$rounds",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = BjjGold,
                    modifier = Modifier.widthIn(min = 44.dp),
                    textAlign = TextAlign.Center
                )

                FilledTonalButton(
                    onClick = { if (rounds < 20) onRoundsChange(rounds + 1) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GlassWhite,
                        contentColor = TextPrimary
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = 20.sp)
                }
            }
        }
    }
}

// ── Preset Chip ──────────────────────────────────────────────────────────────
@Composable
private fun PresetChip(
    preset: Preset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val beltColor = when (preset.beltColor) {
        "white" -> BeltWhite
        "blue" -> BeltBlue
        "purple" -> BeltPurple
        "brown" -> BeltBrown
        "black" -> BeltBlack
        "gold" -> BjjGold
        else -> BeltWhite
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GlassWhite.copy(alpha = 0.15f) else GlassWhite
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(150.dp)
            .then(
                if (isSelected) Modifier.border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(listOf(BjjGold, BjjGoldDark)),
                    shape = RoundedCornerShape(16.dp)
                )
                else Modifier.border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
            )
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Belt color bar with glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(beltColor, beltColor.copy(alpha = 0.5f))
                        )
                    )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = preset.name,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${preset.config.roundDurationSeconds / 60}min × ${preset.config.numberOfRounds}",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}

// ── Glass Toggle Row ─────────────────────────────────────────────────────────
@Composable
private fun GlassToggleRow(
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
