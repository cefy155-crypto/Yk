package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnElectricLime
import com.example.ui.viewmodel.LiveCoachState

@Composable
fun LiveCoachScreen(
    state: LiveCoachState,
    onTogglePause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onToggleSound: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val currentRow = state.rows.getOrNull(state.currentIndex)
    val nextRow = state.rows.getOrNull(state.currentIndex + 1)
    val progress = if (state.totalStepSeconds > 0) {
        state.secondsRemaining.toFloat() / state.totalStepSeconds.toFloat()
    } else 0f

    Scaffold(containerColor = ObsidianBg) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Image(
                painter = painterResource(R.drawable.img_coach_live),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.22f)
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(ObsidianBg.copy(alpha = 0.7f), ObsidianBg.copy(alpha = 0.96f)))
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MODE COACH EN DIRECT", style = MaterialTheme.typography.labelLarge, color = ElectricLime)
                        Text(state.sessionTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onToggleSound) {
                            Icon(
                                if (state.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = "Alertes sonores",
                                tint = if (state.soundEnabled) CyberCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onClose, modifier = Modifier.testTag("close_live_coach_btn")) {
                            Icon(Icons.Default.Close, contentDescription = "Quitter mode direct")
                        }
                    }
                }

                // Circular Countdown
                Box(
                    modifier = Modifier.size(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = CharcoalElevated,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 18f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = ElectricLime,
                            startAngle = -90f,
                            sweepAngle = 360f * progress.coerceIn(0f, 1f),
                            useCenter = false,
                            style = Stroke(width = 18f, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.currentIndex + 1} / ${state.rows.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan
                        )
                        val mins = state.secondsRemaining / 60
                        val secs = state.secondsRemaining % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp),
                            color = ElectricLime,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isRunning) "EN COURS" else "PAUSE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Current Exercise
                if (currentRow != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(color = CyberCyan.copy(alpha = 0.16f), shape = MaterialTheme.shapes.small) {
                            Text(
                                currentRow.phase,
                                style = MaterialTheme.typography.labelLarge,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = currentRow.exerciseName,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${currentRow.sets} séries • ${currentRow.reps} • Récup : ${currentRow.restText}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricLime
                        )
                        Text(
                            text = currentRow.executionCues,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = onPrev,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Précédent")
                    }
                    Button(
                        onClick = onTogglePause,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                        modifier = Modifier.size(72.dp).testTag("live_coach_play_pause_btn")
                    ) {
                        Icon(
                            if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Lecture / Pause",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    OutlinedIconButton(
                        onClick = onNext,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Suivant")
                    }
                }

                // Next Exercise Preview
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("APERÇU EXERCICE SUIVANT", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                        if (nextRow != null) {
                            Text(nextRow.exerciseName, style = MaterialTheme.typography.titleMedium)
                            Text("${nextRow.region} • ${nextRow.sets} x ${nextRow.reps} (${nextRow.durationText})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("Dernier bloc de la séance — Fin après ce chrono !", style = MaterialTheme.typography.titleSmall, color = ElectricLime)
                        }
                    }
                }
            }
        }
    }
}
