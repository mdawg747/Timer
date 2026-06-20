package com.example.bjjtimer.ui.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bjjtimer.data.model.Preset
import com.example.bjjtimer.data.model.TimerConfig
import com.example.bjjtimer.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsScreen(
    presets: List<Preset>,
    currentConfig: TimerConfig,
    onPresetSelected: (Preset) -> Unit,
    onPresetSave: (Preset) -> Unit,
    onPresetDelete: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, DarkBackground, GradientEnd)
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    "Presets",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("←", fontSize = 24.sp, color = BjjGold)
                }
            },
            actions = {
                TextButton(onClick = { showSaveDialog = true }) {
                    Text("+ Save Current", color = BjjGold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Built-in section
            item {
                Text(
                    "BUILT-IN",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                    color = TextTertiary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(presets.filter { it.isBuiltIn }) { preset ->
                PresetCard(
                    preset = preset,
                    onSelect = { onPresetSelected(preset) },
                    onDelete = null // Can't delete built-in
                )
            }

            // Custom section
            val customPresets = presets.filter { !it.isBuiltIn }
            if (customPresets.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "CUSTOM",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 3.sp),
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(customPresets) { preset ->
                    PresetCard(
                        preset = preset,
                        onSelect = { onPresetSelected(preset) },
                        onDelete = { onPresetDelete(preset.id) }
                    )
                }
            }
        }
    }

    if (showSaveDialog) {
        SavePresetDialog(
            config = currentConfig,
            onSave = { name ->
                onPresetSave(
                    Preset(
                        name = name,
                        config = currentConfig.copy(name = name),
                        isBuiltIn = false,
                        beltColor = "gold"
                    )
                )
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }
}

@Composable
private fun PresetCard(
    preset: Preset,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)?
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Belt color bar with glow
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(beltColor, beltColor.copy(alpha = 0.5f))
                            )
                        )
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip(
                            label = "Round",
                            value = formatDuration(preset.config.roundDurationSeconds)
                        )
                        InfoChip(
                            label = "Rest",
                            value = formatDuration(preset.config.restDurationSeconds)
                        )
                        InfoChip(
                            label = "Rounds",
                            value = "${preset.config.numberOfRounds}"
                        )
                    }
                }
            }

            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("✕", color = TimerRed, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun SavePresetDialog(
    config: TimerConfig,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(1.dp, GlassWhiteBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Save Preset",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Save your current configuration as a reusable preset.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset Name") },
                    placeholder = { Text("e.g., Tuesday Night Rounds") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BjjGold,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = BjjGold,
                        cursorColor = BjjGold,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Config summary
                Text(
                    text = "Round: ${formatDuration(config.roundDurationSeconds)} · Rest: ${formatDuration(config.restDurationSeconds)} · ${config.numberOfRounds} rounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name) },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BjjGold,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (s == 0) "${m}:00" else "${m}:${"%02d".format(s)}"
}
