package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.db.AssessmentEntity
import com.example.data.db.ClientEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.ScheduleEntity
import com.example.data.db.UserEntity
import com.example.data.db.WorkoutLogEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.db.WorkoutSessionRowEntity
import com.example.ui.components.ChartPoint
import com.example.ui.components.ClientSelectorBar
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.TrendLineChart
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnElectricLime
import com.example.ui.theme.VioletSplit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    user: UserEntity?,
    clients: List<ClientEntity>,
    activeClient: ClientEntity?,
    activeSession: WorkoutSessionEntity?,
    activeSessionRows: List<WorkoutSessionRowEntity>,
    nutrition: NutritionProgramEntity?,
    schedules: List<ScheduleEntity>,
    assessments: List<AssessmentEntity>,
    workoutLogs: List<WorkoutLogEntity>,
    exerciseCount: Int,
    onSelectClient: (Long) -> Unit,
    onAddClientClick: () -> Unit,
    onLaunchQuickSession: (type: String, focus: String, duration: Int) -> Unit,
    onStartLiveCoach: () -> Unit,
    onOpenTrainingTab: (subTab: Int) -> Unit,
    onOpenNutritionTab: () -> Unit,
    onOpenProgressTab: () -> Unit,
    onOpenCommunity: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenExport: () -> Unit
) {
    val totalVolumeKg = workoutLogs.sumOf { it.totalVolumeKg }.toInt()
    val completedWeek = schedules.count { it.isCompleted }
    val df = SimpleDateFormat("dd/MM", Locale.FRANCE)
    val weightPoints = assessments.filter { it.weightKg != null }.map {
        ChartPoint(df.format(Date(it.dateEpochMs)), it.weightKg!!)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(174.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, CharcoalBorder, MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_hero_coach),
                    contentDescription = "Bannière Sport Coach Pro",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.65f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ObsidianBg.copy(alpha = 0.94f), ObsidianBg.copy(alpha = 0.45f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ElectricLime.copy(alpha = 0.16f),
                            shape = MaterialTheme.shapes.small,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricLime.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "TABLEAU DE BORD UNIFIÉ",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricLime,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "$exerciseCount exercices • Offline BDD",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }

                    Column {
                        Text(
                            text = "Bonjour, ${user?.fullName ?: "Coach"}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${user?.role ?: "Coach"} • ${user?.fitnessGoal ?: "Performance"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onStartLiveCoach,
                            enabled = activeSessionRows.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                            modifier = Modifier.testTag("dashboard_live_coach_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mode Coach Direct", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(onClick = onOpenAiAssistant, modifier = Modifier.testTag("dashboard_ai_btn")) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assistant IA")
                        }
                    }
                }
            }
        }

        // Client Selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pratiquant suivi", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ClientSelectorBar(
                    clients = clients,
                    activeClient = activeClient,
                    onSelectClient = onSelectClient,
                    onAddClientClick = onAddClientClick
                )
            }
        }

        // KPI Grid 2x2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiMetricCard(
                        title = "Poids & Compo",
                        value = "${activeClient?.weightKg ?: 0.0} kg",
                        subtitle = "${activeClient?.bodyFatPct ?: 0.0}% MG • ${activeClient?.goal ?: ""}",
                        accent = ElectricLime,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Cible Nutrition",
                        value = "${nutrition?.targetKcal ?: 0} kcal",
                        subtitle = "${nutrition?.proteinG ?: 0}P / ${nutrition?.carbsG ?: 0}G / ${nutrition?.fatsG ?: 0}L",
                        accent = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    KpiMetricCard(
                        title = "Volume Cumulé",
                        value = "$totalVolumeKg kg",
                        subtitle = "${workoutLogs.size} logs • ${workoutLogs.count { it.isPr }} PR",
                        accent = AmberKinetic,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Planning Hebdo",
                        value = "$completedWeek / ${schedules.size}",
                        subtitle = "Séances validées cette sem.",
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Module Launchers (HIIT / LISS / Collectif / Circuit / Export / Communauté)
        item {
            Text("Modules & Générateurs Rapides", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickModuleTile(
                        title = "HIIT Cardio",
                        subtitle = "40s/20s • 10 ex.",
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        accent = CoralCompat,
                        modifier = Modifier.weight(1f)
                    ) {
                        onLaunchQuickSession("HIIT", "HIIT / Métabolique", 35)
                        onOpenTrainingTab(0)
                    }
                    QuickModuleTile(
                        title = "LISS Zone 2",
                        subtitle = "Endurance douce",
                        icon = Icons.Default.Timer,
                        accent = CyberCyan,
                        modifier = Modifier.weight(1f)
                    ) {
                        onLaunchQuickSession("LISS", "Cardio / Full Body", 45)
                        onOpenTrainingTab(0)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickModuleTile(
                        title = "Activité Collective",
                        subtitle = "Circuit Cross-Fit",
                        icon = Icons.Default.Groups,
                        accent = ElectricLime,
                        modifier = Modifier.weight(1f)
                    ) {
                        onLaunchQuickSession("Collectif", "Full Body", 50)
                        onOpenTrainingTab(2)
                    }
                    QuickModuleTile(
                        title = "Split Musculation",
                        subtitle = "2 à 6 séances/sem",
                        icon = Icons.Default.FitnessCenter,
                        accent = VioletSplit,
                        modifier = Modifier.weight(1f)
                    ) { onOpenTrainingTab(1) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    QuickModuleTile(
                        title = "Communauté & Défis",
                        subtitle = "Routines, repas, défis",
                        icon = Icons.Default.Groups,
                        accent = AmberKinetic,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenCommunity
                    )
                    QuickModuleTile(
                        title = "Exports PDF/DOCX/XLSX",
                        subtitle = "Partage client direct",
                        icon = Icons.Default.Download,
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenExport
                    )
                }
            }
        }

        // Progress Chart Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tendance Poids — ${activeClient?.fullName ?: ""}", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onOpenProgressTab) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ouvrir Suivi")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            TrendLineChart(
                points = weightPoints,
                lineColor = ElectricLime,
                unit = "kg",
                emptyMessage = "Ajoutez une pesée dans Progrès"
            )
        }

        // Active Session Card
        if (activeSession != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier.fillMaxWidth().clickable { onOpenTrainingTab(0) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Séance Active (6 Phases)", style = MaterialTheme.typography.labelLarge, color = ElectricLime)
                            Text(activeSession.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${activeSessionRows.size} lignes • ${activeSession.durationMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = onOpenNutritionTab) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Repas")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CharcoalElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

private val CoralCompat = Color(0xFFFB7185)
