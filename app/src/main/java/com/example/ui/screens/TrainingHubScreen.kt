package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.ClientEntity
import com.example.data.db.ExerciseEntity
import com.example.data.db.ScheduleEntity
import com.example.data.db.TrainingDayExerciseEntity
import com.example.data.db.TrainingProgramEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.db.WorkoutSessionRowEntity
import com.example.domain.engines.GymSplitEngine
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OnElectricLime

@Composable
fun TrainingHubScreen(
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    client: ClientEntity?,
    allExercises: List<ExerciseEntity>,
    activeSession: WorkoutSessionEntity?,
    sessionRows: List<WorkoutSessionRowEntity>,
    activeProgram: TrainingProgramEntity?,
    programExercises: List<TrainingDayExerciseEntity>,
    schedules: List<ScheduleEntity>,
    onGenerateSession: (type: String, focus: String, durationMin: Int, mainCount: Int) -> Unit,
    onCreateCircuit: (title: String, rounds: Int, workSec: Int, restSec: Int, stations: List<ExerciseEntity>) -> Unit,
    onStartLiveCoach: (title: String, rows: List<WorkoutSessionRowEntity>) -> Unit,
    onGenerateProgram: (freq: Int, split: String) -> Unit,
    onBumpLoad: (TrainingDayExerciseEntity, Double) -> Unit,
    onAddSchedule: (title: String, type: String, day: Int, time: String, dur: Int, notes: String) -> Unit,
    onToggleSchedule: (ScheduleEntity) -> Unit,
    onDeleteSchedule: (ScheduleEntity) -> Unit,
    onOpenExport: () -> Unit
) {
    val tabs = listOf("Générateur 6 Phases", "Programme Muscu", "Circuits & HIIT", "Calendrier")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab.coerceIn(0, 3),
            containerColor = CharcoalSurface,
            contentColor = ElectricLime,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedSubTab == idx,
                    onClick = { onSelectSubTab(idx) },
                    text = { Text(label) }
                )
            }
        }

        when (selectedSubTab) {
            0 -> SessionGeneratorTab(
                client = client,
                activeSession = activeSession,
                rows = sessionRows,
                onGenerateSession = onGenerateSession,
                onStartLiveCoach = onStartLiveCoach,
                onOpenExport = onOpenExport
            )
            1 -> GymPlannerTab(
                client = client,
                program = activeProgram,
                dayExercises = programExercises,
                onGenerateProgram = onGenerateProgram,
                onBumpLoad = onBumpLoad,
                onOpenExport = onOpenExport
            )
            2 -> CircuitBuilderTab(
                allExercises = allExercises,
                onCreateCircuit = onCreateCircuit,
                onStartLiveCoach = { onSelectSubTab(0) }
            )
            else -> WeeklyScheduleTab(
                schedules = schedules,
                onAddSchedule = onAddSchedule,
                onToggleSchedule = onToggleSchedule,
                onDeleteSchedule = onDeleteSchedule
            )
        }
    }
}

@Composable
private fun SessionGeneratorTab(
    client: ClientEntity?,
    activeSession: WorkoutSessionEntity?,
    rows: List<WorkoutSessionRowEntity>,
    onGenerateSession: (String, String, Int, Int) -> Unit,
    onStartLiveCoach: (String, List<WorkoutSessionRowEntity>) -> Unit,
    onOpenExport: () -> Unit
) {
    var sessionType by remember { mutableStateOf("Complète 6 Phases") }
    var focusRegion by remember { mutableStateOf("Full Body") }
    var mainCount by remember(focusRegion) { mutableIntStateOf(if (focusRegion == "Full Body") 13 else 10) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Générateur Intelligent (6 Phases Strictes)", style = MaterialTheme.typography.titleMedium, color = ElectricLime)
                    Text(
                        "Structure : 1. Échauffement général • 2. Articulaire (sans forcer le cou) • 3. Activation • 4. Principale (10+ ex. / 12-16 Full Body) • 5. Retour au calme • 6. Étirements.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Complète 6 Phases", "HIIT", "LISS", "Collectif").forEach { t ->
                            FilterChip(selected = sessionType == t, onClick = { sessionType = t }, label = { Text(t) })
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Full Body", "Haut du Corps", "Bas du Corps", "HIIT / Métabolique").forEach { f ->
                            FilterChip(
                                selected = focusRegion == f,
                                onClick = {
                                    focusRegion = f
                                    mainCount = if (f == "Full Body") 13 else 10
                                },
                                label = { Text(f) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val minC = if (focusRegion == "Full Body") 12 else 10
                        val maxC = if (focusRegion == "Full Body") 16 else 14
                        Text("Exercices principaux : $mainCount", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(onClick = { if (mainCount > minC) mainCount-- }) { Text("-") }
                            OutlinedButton(onClick = { if (mainCount < maxC) mainCount++ }) { Text("+") }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onGenerateSession(sessionType, focusRegion, 60, mainCount) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                            modifier = Modifier.weight(1f).testTag("generate_session_btn")
                        ) {
                            Text("Générer Séance (${client?.fullName?.split(" ")?.firstOrNull() ?: ""})", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(onClick = onOpenExport) {
                            Icon(Icons.Default.Download, contentDescription = "Exporter", modifier = Modifier.size(18.dp))
                        }
                    }
                    if (rows.isNotEmpty()) {
                        Button(
                            onClick = { onStartLiveCoach(activeSession?.title ?: "Séance Live", rows) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = OnElectricLime),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lancer en Mode Coach En Direct", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Tableau 9 colonnes obligatoires (scrollable horizontalement pour fidélité tabulaire complète)
        item {
            Text("Tableau de Séance (9 Colonnes Réglementaires)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium)
                    .background(CharcoalSurface, MaterialTheme.shapes.medium)
                    .horizontalScroll(scroll)
            ) {
                Row(
                    modifier = Modifier
                        .background(CharcoalElevated)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TableHeaderCell("N°", 32)
                    TableHeaderCell("Phase", 170)
                    TableHeaderCell("Exercice", 210)
                    TableHeaderCell("Région", 120)
                    TableHeaderCell("Séries", 68)
                    TableHeaderCell("Répétitions", 90)
                    TableHeaderCell("Durée", 68)
                    TableHeaderCell("Récup", 68)
                    TableHeaderCell("Exécution", 320)
                }
                rows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .border(0.5.dp, CharcoalBorder)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        TableCell("${row.rowNumber}", 32, ElectricLime)
                        TableCell(row.phase, 170, CyberCyan)
                        TableCell(row.exerciseName, 210, MaterialTheme.colorScheme.onSurface, bold = true)
                        TableCell(row.region, 120, MaterialTheme.colorScheme.onSurfaceVariant)
                        TableCell(row.sets, 68, MaterialTheme.colorScheme.onSurface)
                        TableCell(row.reps, 90, MaterialTheme.colorScheme.onSurface)
                        TableCell(row.durationText, 68, AmberKinetic)
                        TableCell(row.restText, 68, MaterialTheme.colorScheme.onSurfaceVariant)
                        TableCell(row.executionCues, 320, MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, widthDp: Int) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = ElectricLime, modifier = Modifier.width(widthDp.dp))
}

@Composable
private fun TableCell(
    text: String,
    widthDp: Int,
    color: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.width(widthDp.dp)
    )
}

@Composable
private fun GymPlannerTab(
    client: ClientEntity?,
    program: TrainingProgramEntity?,
    dayExercises: List<TrainingDayExerciseEntity>,
    onGenerateProgram: (Int, String) -> Unit,
    onBumpLoad: (TrainingDayExerciseEntity, Double) -> Unit,
    onOpenExport: () -> Unit
) {
    var freq by remember(client?.id) { mutableIntStateOf(client?.weeklySessionsTarget?.coerceIn(2, 6) ?: 4) }
    val splitOptions = remember(freq) { GymSplitEngine.availableSplitsForFrequency(freq) }
    var selectedSplit by remember(freq) { mutableStateOf(splitOptions.first()) }
    var selectedDayIdx by remember { mutableIntStateOf(1) }

    val days = remember(dayExercises) {
        dayExercises.groupBy { it.dayIndex }.toSortedMap()
    }
    val activeDayItems = days[selectedDayIdx] ?: days.values.firstOrNull().orEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Programme de Musculation (Gym Training Planner)", style = MaterialTheme.typography.titleMedium, color = ElectricLime)
                    if (client != null) {
                        Text(
                            "Pratiquant : ${client.fullName} • Niveau ${client.level} • Objectif ${client.goal} • Matériel : ${client.availableEquipment}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("Fréquence : $freq séances / semaine", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (2..6).forEach { f ->
                            FilterChip(
                                selected = freq == f,
                                onClick = { freq = f },
                                label = { Text("${f}j") }
                            )
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        splitOptions.forEach { sp ->
                            FilterChip(selected = selectedSplit == sp, onClick = { selectedSplit = sp }, label = { Text(sp) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onGenerateProgram(freq, selectedSplit) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                            modifier = Modifier.weight(1f).testTag("generate_gym_program_btn")
                        ) { Text("Générer Programme", fontWeight = FontWeight.Bold) }
                        OutlinedButton(onClick = onOpenExport) {
                            Icon(Icons.Default.Download, contentDescription = "Exporter", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        if (days.isNotEmpty()) {
            item {
                Text(program?.title ?: "Split Actif", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    days.keys.forEach { dIdx ->
                        FilterChip(
                            selected = selectedDayIdx == dIdx,
                            onClick = { selectedDayIdx = dIdx },
                            label = { Text("Jour $dIdx") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricLime,
                                selectedLabelColor = OnElectricLime
                            )
                        )
                    }
                }
            }

            items(activeDayItems, key = { it.id }) { ex ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${ex.orderIndex}. ${ex.exerciseName}", style = MaterialTheme.typography.titleSmall, color = ElectricLime, modifier = Modifier.weight(1f))
                            Surface(color = CharcoalElevated, shape = MaterialTheme.shapes.small) {
                                Text("Tempo ${ex.tempo}", style = MaterialTheme.typography.labelSmall, color = CyberCyan, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }
                        }
                        Text("Groupes : ${ex.muscleGroups}", style = MaterialTheme.typography.bodySmall, color = CyberCyan)
                        Text("${ex.sets} séries x ${ex.reps} • Repos ${ex.restSeconds}s", style = MaterialTheme.typography.labelMedium)
                        Text("Consignes : ${ex.coachingCues}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(ex.variant, style = MaterialTheme.typography.bodySmall, color = AmberKinetic)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (ex.targetLoadKg > 0.0) "Charge cible : ${ex.targetLoadKg} kg" else "Poids du corps / Élastique",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldSuccess
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onBumpLoad(ex, -2.5) }) { Text("-2.5 kg") }
                                Button(onClick = { onBumpLoad(ex, 2.5) }) { Text("+2.5 kg") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircuitBuilderTab(
    allExercises: List<ExerciseEntity>,
    onCreateCircuit: (title: String, rounds: Int, workSec: Int, restSec: Int, stations: List<ExerciseEntity>) -> Unit,
    onStartLiveCoach: () -> Unit
) {
    var title by remember { mutableStateOf("Circuit Métabolique 10 Stations") }
    var rounds by remember { mutableIntStateOf(3) }
    var workSec by remember { mutableIntStateOf(40) }
    var restSec by remember { mutableIntStateOf(20) }
    val defaultStations = remember(allExercises) {
        allExercises.filter { it.category == "Cardio / HIIT" || it.category == "Collectif" }.take(10)
    }
    var selectedStations by remember(defaultStations) { mutableStateOf(defaultStations) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Créateur de Circuits & Collectifs", style = MaterialTheme.typography.titleMedium, color = ElectricLime)
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nom du circuit") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(color = CharcoalElevated, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tours", style = MaterialTheme.typography.labelSmall)
                                Text("$rounds", style = MaterialTheme.typography.titleLarge, color = ElectricLime)
                                Row { TextButton(onClick = { if (rounds > 1) rounds-- }) { Text("-") }; TextButton(onClick = { if (rounds < 8) rounds++ }) { Text("+") } }
                            }
                        }
                        Surface(color = CharcoalElevated, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Effort", style = MaterialTheme.typography.labelSmall)
                                Text("${workSec}s", style = MaterialTheme.typography.titleLarge, color = CyberCyan)
                                Row { TextButton(onClick = { if (workSec > 15) workSec -= 5 }) { Text("-") }; TextButton(onClick = { if (workSec < 90) workSec += 5 }) { Text("+") } }
                            }
                        }
                        Surface(color = CharcoalElevated, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Récup", style = MaterialTheme.typography.labelSmall)
                                Text("${restSec}s", style = MaterialTheme.typography.titleLarge, color = AmberKinetic)
                                Row { TextButton(onClick = { if (restSec > 10) restSec -= 5 }) { Text("-") }; TextButton(onClick = { if (restSec < 60) restSec += 5 }) { Text("+") } }
                            }
                        }
                    }
                    Button(
                        onClick = {
                            onCreateCircuit(title, rounds, workSec, restSec, selectedStations)
                            onStartLiveCoach()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Enregistrer Circuit (${selectedStations.size} stations) & Voir 6 Phases", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Text("Sélection des Stations (${selectedStations.size} actives)", style = MaterialTheme.typography.titleSmall) }
        items(allExercises.filter { it.category in setOf("Cardio / HIIT", "Collectif", "Force") }.take(24), key = { it.id }) { ex ->
            val isPicked = selectedStations.any { it.id == ex.id }
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isPicked) CharcoalElevated else CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isPicked) ElectricLime else CharcoalBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ex.name, style = MaterialTheme.typography.titleSmall)
                        Text("${ex.region} • ${ex.equipment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        selectedStations = if (isPicked) selectedStations.filterNot { it.id == ex.id } else selectedStations + ex
                    }) {
                        Icon(
                            if (isPicked) Icons.Default.CheckCircle else Icons.Default.Add,
                            contentDescription = "Basculer station",
                            tint = if (isPicked) ElectricLime else CyberCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyScheduleTab(
    schedules: List<ScheduleEntity>,
    onAddSchedule: (String, String, Int, String, Int, String) -> Unit,
    onToggleSchedule: (ScheduleEntity) -> Unit,
    onDeleteSchedule: (ScheduleEntity) -> Unit
) {
    val dayNames = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
    var selectedDay by remember { mutableIntStateOf(0) } // 0 = Tous
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(schedules, selectedDay) {
        if (selectedDay == 0) schedules else schedules.filter { it.dayOfWeek == selectedDay }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Calendrier Partagé Hebdo", style = MaterialTheme.typography.titleMedium, color = ElectricLime)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Planifier")
            }
        }

        Row(
            modifier = Modifier.padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(selected = selectedDay == 0, onClick = { selectedDay = 0 }, label = { Text("Toute la sem.") })
            dayNames.forEachIndexed { idx, d ->
                FilterChip(selected = selectedDay == idx + 1, onClick = { selectedDay = idx + 1 }, label = { Text(d) })
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isCompleted) EmeraldSuccess else CharcoalBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onToggleSchedule(item) }) {
                            Icon(
                                if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Statut séance",
                                tint = if (item.isCompleted) EmeraldSuccess else CyberCyan
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${dayNames.getOrElse(item.dayOfWeek - 1) { "Jour" }} • ${item.startTime} (${item.durationMin} min)", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                            Text(item.title, style = MaterialTheme.typography.titleSmall)
                            if (item.sharedNotes.isNotBlank()) {
                                Text("Note partagée : ${item.sharedNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onDeleteSchedule(item) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("Séance Force / Cardio") }
        var type by remember { mutableStateOf("Musculation") }
        var day by remember { mutableIntStateOf(3) }
        var time by remember { mutableStateOf("18:30") }
        var notes by remember { mutableStateOf("Hydratation + échauffement 15 min") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Planifier une séance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, singleLine = true)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Musculation", "HIIT", "LISS", "Collectif", "Bilan").forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                        }
                    }
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        dayNames.forEachIndexed { idx, d ->
                            FilterChip(selected = day == idx + 1, onClick = { day = idx + 1 }, label = { Text(d) })
                        }
                    }
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Heure (HH:mm)") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Note coach / pratiquant") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onAddSchedule(title, type, day, time, 60, notes)
                        showAddDialog = false
                    }
                }) { Text("Ajouter") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Annuler") } }
        )
    }
}
