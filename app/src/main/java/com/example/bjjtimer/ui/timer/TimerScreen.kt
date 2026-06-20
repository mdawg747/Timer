package com.example.bjjtimer.ui.timer

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bjjtimer.data.model.TimerPhase
import com.example.bjjtimer.data.model.TimerState
import com.example.bjjtimer.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TimerScreen(
    state: TimerState,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ── Animated phase colors ────────────────────────────────────────────
    val phaseColor by animateColorAsState(
        targetValue = when {
            state.isRestPhase -> RestBlue
            state.phase == TimerPhase.COMPLETED -> BjjGold
            state.timeRemainingSeconds <= 10 -> TimerRed
            state.timeRemainingSeconds <= 30 -> TimerOrange
            state.timeRemainingSeconds <= 60 -> TimerYellow
            else -> TimerGreen
        },
        animationSpec = tween(600),
        label = "phaseColor"
    )

    val glowColor by animateColorAsState(
        targetValue = when {
            state.isRestPhase -> RestBlueGlow
            state.phase == TimerPhase.COMPLETED -> BjjGoldLight
            state.timeRemainingSeconds <= 10 -> TimerRedGlow
            state.timeRemainingSeconds <= 30 -> TimerOrange
            state.timeRemainingSeconds <= 60 -> TimerYellowGlow
            else -> TimerGreenGlow
        },
        animationSpec = tween(600),
        label = "glowColor"
    )

    // ── Pulse animation for last 10s ─────────────────────────────────────
    val pulseScale by animateFloatAsState(
        targetValue = if (state.timeRemainingSeconds <= 10 && state.isRunning) 1.06f else 1.0f,
        animationSpec = if (state.timeRemainingSeconds <= 10 && state.isRunning) {
            infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(400)
        },
        label = "pulseScale"
    )

    // ── Slow rotating glow ───────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "bgAnim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // ── Breathing glow alpha ─────────────────────────────────────────────
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GradientStart, DarkBackground, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Background effects: rotating gradient + radial glow ──────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = if (isLandscape) size.width * 0.35f else size.width / 2f
            val cy = if (isLandscape) size.height / 2f else size.height * 0.38f

            // Rotating outer halo
            rotate(rotationAngle, pivot = Offset(cx, cy)) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            phaseColor.copy(alpha = breathAlpha * 0.5f),
                            Color.Transparent,
                            glowColor.copy(alpha = breathAlpha * 0.4f),
                            Color.Transparent,
                            phaseColor.copy(alpha = breathAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy)
                    ),
                    radius = if (isLandscape) size.height * 0.7f else size.width * 0.65f,
                    center = Offset(cx, cy)
                )
            }

            // Central radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = breathAlpha),
                        phaseColor.copy(alpha = breathAlpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = if (isLandscape) size.height * 0.45f else size.width * 0.45f
                )
            )

            // Floating particles
            drawParticles(
                phaseColor = phaseColor,
                alpha = breathAlpha * 0.6f,
                time = rotationAngle,
                center = Offset(cx, cy),
                radius = if (isLandscape) size.height * 0.5f else size.width * 0.5f
            )
        }

        if (isLandscape) {
            LandscapeTimerLayout(
                state = state,
                phaseColor = phaseColor,
                glowColor = glowColor,
                pulseScale = pulseScale,
                rotationAngle = rotationAngle,
                breathAlpha = breathAlpha,
                onTogglePause = onTogglePause,
                onSkip = onSkip,
                onStop = onStop,
                onRestart = onRestart
            )
        } else {
            PortraitTimerLayout(
                state = state,
                phaseColor = phaseColor,
                glowColor = glowColor,
                pulseScale = pulseScale,
                rotationAngle = rotationAngle,
                breathAlpha = breathAlpha,
                onTogglePause = onTogglePause,
                onSkip = onSkip,
                onStop = onStop,
                onRestart = onRestart
            )
        }
    }
}

// ── Particle drawing ─────────────────────────────────────────────────────────
private fun DrawScope.drawParticles(
    phaseColor: Color,
    alpha: Float,
    time: Float,
    center: Offset,
    radius: Float
) {
    val particleCount = 8
    for (i in 0 until particleCount) {
        val angle = (time * 0.5f + i * (360f / particleCount)) * (PI.toFloat() / 180f)
        val dist = radius * (0.6f + 0.3f * sin(angle * 1.5f + i))
        val x = center.x + cos(angle) * dist
        val y = center.y + sin(angle) * dist
        val particleSize = 2.dp.toPx() + sin(time * 0.02f + i.toFloat()) * 1.5.dp.toPx()

        drawCircle(
            color = phaseColor.copy(alpha = alpha * (0.5f + 0.5f * sin(angle + i.toFloat()))),
            radius = particleSize,
            center = Offset(x, y)
        )
    }
}

// ── PORTRAIT LAYOUT ──────────────────────────────────────────────────────────
@Composable
private fun PortraitTimerLayout(
    state: TimerState,
    phaseColor: Color,
    glowColor: Color,
    pulseScale: Float,
    rotationAngle: Float,
    breathAlpha: Float,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Phase label with glow
        Text(
            text = state.phaseLabel,
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 6.sp,
                fontSize = 16.sp
            ),
            color = phaseColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Round counter
        if (state.phase != TimerPhase.IDLE) {
            Text(
                text = "Round ${state.currentRound} / ${state.totalRounds}",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(52.dp))
        }

        // Timer ring
        TimerRing(
            state = state,
            phaseColor = phaseColor,
            glowColor = glowColor,
            pulseScale = pulseScale,
            rotationAngle = rotationAngle,
            breathAlpha = breathAlpha,
            ringSize = 280.dp
        )

        Spacer(modifier = Modifier.height(48.dp))

        TimerControls(
            state = state,
            phaseColor = phaseColor,
            glowColor = glowColor,
            onTogglePause = onTogglePause,
            onSkip = onSkip,
            onStop = onStop,
            onRestart = onRestart
        )

        Spacer(modifier = Modifier.height(32.dp))

        SessionInfoBar(state = state, phaseColor = phaseColor)
    }
}

// ── LANDSCAPE LAYOUT ─────────────────────────────────────────────────────────
@Composable
private fun LandscapeTimerLayout(
    state: TimerState,
    phaseColor: Color,
    glowColor: Color,
    pulseScale: Float,
    rotationAngle: Float,
    breathAlpha: Float,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Timer ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            TimerRing(
                state = state,
                phaseColor = phaseColor,
                glowColor = glowColor,
                pulseScale = pulseScale,
                rotationAngle = rotationAngle,
                breathAlpha = breathAlpha,
                ringSize = 200.dp
            )
        }

        Spacer(modifier = Modifier.width(24.dp))

        // Right side: Info + controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = state.phaseLabel,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 6.sp,
                    fontSize = 16.sp
                ),
                color = phaseColor,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            if (state.phase != TimerPhase.IDLE) {
                Text(
                    text = "Round ${state.currentRound} / ${state.totalRounds}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            TimerControls(
                state = state,
                phaseColor = phaseColor,
                glowColor = glowColor,
                onTogglePause = onTogglePause,
                onSkip = onSkip,
                onStop = onStop,
                onRestart = onRestart
            )

            Spacer(modifier = Modifier.height(20.dp))

            SessionInfoBar(state = state, phaseColor = phaseColor)
        }
    }
}

// ── TIMER RING ───────────────────────────────────────────────────────────────
@Composable
private fun TimerRing(
    state: TimerState,
    phaseColor: Color,
    glowColor: Color,
    pulseScale: Float,
    rotationAngle: Float,
    breathAlpha: Float,
    ringSize: Dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size((ringSize.value * pulseScale).dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val glowStroke = 28.dp.toPx()
            val diameter = size.minDimension - glowStroke
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )

            // ── Outer glow ring (blurred effect via thick transparent stroke)
            if (state.phase != TimerPhase.IDLE && state.phase != TimerPhase.COMPLETED) {
                drawArc(
                    color = phaseColor.copy(alpha = breathAlpha * 0.4f),
                    startAngle = -90f,
                    sweepAngle = 360f * state.progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = glowStroke, cap = StrokeCap.Round)
                )
            }

            // ── Track ring (subtle)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        DarkSurfaceVariant.copy(alpha = 0.6f),
                        DarkSurfaceVariant.copy(alpha = 0.3f),
                        DarkSurfaceVariant.copy(alpha = 0.6f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // ── Tick marks
            val tickCount = 60
            val tickCenter = Offset(size.width / 2, size.height / 2)
            val tickRadius = diameter / 2 + 2.dp.toPx()
            for (i in 0 until tickCount) {
                val angle = (i * 6f - 90f) * (PI.toFloat() / 180f)
                val isMain = i % 5 == 0
                val innerR = tickRadius - if (isMain) 8.dp.toPx() else 4.dp.toPx()
                val outerR = tickRadius
                val tickAlpha = if (isMain) 0.3f else 0.12f

                drawLine(
                    color = TextTertiary.copy(alpha = tickAlpha),
                    start = Offset(
                        tickCenter.x + cos(angle) * innerR,
                        tickCenter.y + sin(angle) * innerR
                    ),
                    end = Offset(
                        tickCenter.x + cos(angle) * outerR,
                        tickCenter.y + sin(angle) * outerR
                    ),
                    strokeWidth = if (isMain) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // ── Progress arc with gradient
            if (state.phase != TimerPhase.IDLE && state.phase != TimerPhase.COMPLETED) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            glowColor,
                            phaseColor,
                            phaseColor.copy(alpha = 0.8f),
                            glowColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * state.progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Leading dot at the tip of progress
                val progressAngle = (-90f + 360f * state.progress) * (PI.toFloat() / 180f)
                val dotCenter = Offset(
                    tickCenter.x + cos(progressAngle) * (diameter / 2),
                    tickCenter.y + sin(progressAngle) * (diameter / 2)
                )
                // Glow halo
                drawCircle(
                    color = glowColor.copy(alpha = 0.4f),
                    radius = 12.dp.toPx(),
                    center = dotCenter
                )
                // Solid dot
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = dotCenter
                )
            }

            // ── Completed: full gold ring
            if (state.phase == TimerPhase.COMPLETED) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(BjjGold, BjjGoldLight, BjjGold)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Timer digits
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (state.phase == TimerPhase.COMPLETED) "✓" else state.formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (state.phase == TimerPhase.COMPLETED) 64.sp else 72.sp,
                    letterSpacing = if (state.phase == TimerPhase.COMPLETED) 0.sp else 2.sp
                ),
                color = if (state.phase == TimerPhase.COMPLETED) BjjGold else TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (state.isPaused) {
                Text(
                    text = "PAUSED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 4.sp
                    ),
                    color = BjjGold
                )
            }
        }
    }
}

// ── CONTROL BUTTONS ──────────────────────────────────────────────────────────
@Composable
private fun TimerControls(
    state: TimerState,
    phaseColor: Color,
    glowColor: Color,
    onTogglePause: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit
) {
    when (state.phase) {
        TimerPhase.COMPLETED -> {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BjjGold,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(58.dp)
            ) {
                Text(
                    "RESTART",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 3.sp,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onStop,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(48.dp)
            ) {
                Text(
                    "BACK TO HOME",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp)
                )
            }
        }
        TimerPhase.IDLE -> { /* no controls */ }
        else -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop — glass button
                FilledTonalButton(
                    onClick = onStop,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GlassWhite,
                        contentColor = TimerRed
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("■", fontSize = 20.sp)
                }

                // Play/Pause — main action with glow
                Button(
                    onClick = onTogglePause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = phaseColor,
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(84.dp)
                        .drawBehind {
                            // Button glow
                            drawCircle(
                                color = phaseColor.copy(alpha = 0.3f),
                                radius = size.minDimension / 2 + 8.dp.toPx()
                            )
                        },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (state.isRunning) "⏸" else "▶",
                        fontSize = 30.sp
                    )
                }

                // Skip — glass button
                FilledTonalButton(
                    onClick = onSkip,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GlassWhite,
                        contentColor = TextPrimary
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("⏭", fontSize = 20.sp)
                }
            }
        }
    }
}

// ── SESSION INFO BAR ─────────────────────────────────────────────────────────
@Composable
private fun SessionInfoBar(state: TimerState, phaseColor: Color) {
    if (state.phase != TimerPhase.IDLE) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GlassWhite,
                            GlassWhite.copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(vertical = 14.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "ROUND",
                value = "${state.config.roundDurationSeconds / 60}:${"%02d".format(state.config.roundDurationSeconds % 60)}",
                accent = phaseColor
            )
            StatItem(
                label = "REST",
                value = "${state.config.restDurationSeconds / 60}:${"%02d".format(state.config.restDurationSeconds % 60)}",
                accent = RestBlue
            )
            StatItem(
                label = "ELAPSED",
                value = formatElapsed(state.totalElapsedMillis),
                accent = BjjGold
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
            color = TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = accent.copy(alpha = 0.9f)
        )
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
