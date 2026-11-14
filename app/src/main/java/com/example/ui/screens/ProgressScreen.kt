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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Straighten
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
import com.example.data.db.AssessmentEntity
import com.example.data.db.ClientEntity
import com.example.data.db.ExerciseEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.WorkoutLogEntity
import com.example.ui.components.BarMetricChart
import com.example.ui.components.ChartPoint
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.TrendLineChart
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OnElectricLime
import com.example.ui.theme.VioletSplit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    client: ClientEntity?,
    nutrition: NutritionProgramEntity?,
    exercises: List<ExerciseEntity>,
    assessments: List<AssessmentEntity>,
    workoutLogs: List<WorkoutLogEntity>,
    preselectedExerciseToLog: ExerciseEntity?,
    onClearPreselectedExercise: () -> Unit,
    onLogWeight: (weightKg: Double, bodyFatPct: Double?, notes: String) -> Unit,
    onLogMeasurements: (waist: Double?, chest: Double?, arm: Double?, thigh: Double?, hip: Double?, notes: String) -> Unit,
    onLogNutrition: (kcal: Int, p: Int, c: Int, f: Int, fib: Int, hydroL: Double, notes: String) -> Unit,
    onLogWorkout: (title: String, exerciseName: String, sets: Int, reps: Int, loadKg: Double, rpe: Int, durMin: Int, notes: String) -> Unit,
    onDeleteAssessment: (AssessmentEntity) -> Unit,
    onDeleteWorkoutLog: (WorkoutLogEntity) -> Unit,
    onOpenExport: () -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0=Poids, 1=Mensurations, 2=Entraînements, 3=Nutrition
    var daysRange by remember { mutableIntStateOf(30) } // 7, 30, 90, 3650
    var showLogWeightDialog by remember { mutableStateOf(false) }
    var showLogMeasureDialog by remember { mutableStateOf(false) }
    var showLogNutritionDialog by remember { mutableStateOf(false) }
    var showLogWorkoutDialog by remember(preselectedExerciseToLog) { mutableStateOf(preselectedExerciseToLog != null) }

    val cutoffMs = remember(daysRange) { System.currentTimeMillis() - daysRange * 86_400_000L }
    val filteredAssessments = remember(assessments, cutoffMs) { assessments.filter { it.dateEpochMs >= cutoffMs } }
    val filteredWorkouts = remember(workoutLogs, cutoffMs) { workoutLogs.filter { it.dateEpochMs >= cutoffMs } }
    val df = remember { SimpleDateFormat("dd/MM", Locale.FRANCE) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header + Time Range + Quick Log Action Strip
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Suivi & Progrès", style = MaterialTheme.typography.headlineMedium, color = ElectricLime)
                    Text("${client?.fullName ?: ""} • Visualisation & Journaux", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onOpenExport) {
                    Icon(Icons.Default.Download, contentDescription = "Exporter bilan", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bilan")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(7 to "7J", 30 to "30J", 90 to "90J", 3650 to "Tout").forEach { (d, lbl) ->
                    FilterChip(
                        selected = daysRange == d,
                        onClick = { daysRange = d },
                        label = { Text(lbl) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricLime,
                            selectedLabelColor = OnElectricLime
                        )
                    )
                }
            }

            // 4 Quick Log Buttons
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showLogWorkoutDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                    modifier = Modifier.testTag("log_workout_btn")
                ) {
                    Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Séance", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = { showLogWeightDialog = true }, modifier = Modifier.testTag("log_weight_btn")) {
                    Icon(Icons.Default.MonitorWeight, null, modifier = Modifier.size(16.dp), tint = CyberCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Poids")
                }
                OutlinedButton(onClick = { showLogMeasureDialog = true }, modifier = Modifier.testTag("log_measure_btn")) {
                    Icon(Icons.Default.Straighten, null, modifier = Modifier.size(16.dp), tint = VioletSplit)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Mensurations")
                }
                OutlinedButton(onClick = { showLogNutritionDialog = true }, modifier = Modifier.testTag("log_nutrition_btn")) {
                    Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(16.dp), tint = AmberKinetic)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Nutrition")
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = subTab,
            containerColor = CharcoalSurface,
            contentColor = ElectricLime,
            edgePadding = 12.dp
        ) {
            listOf("Poids & Compo", "Mensurations", "Entraînements & PR", "Nutrition Suivie").forEachIndexed { idx, t ->
                Tab(selected = subTab == idx, onClick = { subTab = idx }, text = { Text(t) })
            }
        }

        when (subTab) {
            0 -> {
                val weightList = filteredAssessments.filter { it.weightKg != null }
                val bfList = filteredAssessments.filter { it.bodyFatPct != null }
                val latestW = weightList.lastOrNull()?.weightKg ?: client?.weightKg ?: 0.0
                val deltaW = if (weightList.size >= 2) latestW - weightList.first().weightKg!! else 0.0
                val latestBf = bfList.lastOrNull()?.bodyFatPct ?: client?.bodyFatPct ?: 0.0

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiMetricCard(
                                title = "Poids Actuel",
                                value = "${"%.1f".format(latestW)} kg",
                                subtitle = "${if (deltaW >= 0) "+" else ""}${"%.1f".format(deltaW)} kg sur période",
                                accent = ElectricLime,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMetricCard(
                                title = "Masse Grasse",
                                value = "${"%.1f".format(latestBf)} %",
                                subtitle = "Masse maigre : ${"%.1f".format(latestW * (1 - latestBf / 100.0))} kg",
                                accent = CyberCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Text("Évolution du Poids (kg)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        TrendLineChart(
                            points = weightList.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.weightKg!!) },
                            lineColor = ElectricLime,
                            unit = "kg"
                        )
                    }
                    item {
                        Text("Évolution % Masse Grasse", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        TrendLineChart(
                            points = bfList.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.bodyFatPct!!) },
                            lineColor = CyberCyan,
                            unit = "%"
                        )
                    }
                    items(weightList.reversed(), key = { "w_${it.id}" }) { item ->
                        AssessmentHistoryRow(df.format(Date(item.dateEpochMs)), "${item.weightKg} kg • ${item.bodyFatPct ?: "—"} % MG", item.notes) {
                            onDeleteAssessment(item)
                        }
                    }
                }
            }
            1 -> {
                val measureList = filteredAssessments.filter {
                    it.waistCm != null || it.chestCm != null || it.armCm != null || it.thighCm != null || it.hipCm != null
                }
                val latest = measureList.lastOrNull()
                val first = measureList.firstOrNull()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            val dWaist = if (latest?.waistCm != null && first?.waistCm != null) latest.waistCm - first.waistCm else 0.0
                            val dChest = if (latest?.chestCm != null && first?.chestCm != null) latest.chestCm - first.chestCm else 0.0
                            KpiMetricCard(
                                title = "Tour de Taille",
                                value = "${latest?.waistCm ?: "—"} cm",
                                subtitle = "${if (dWaist >= 0) "+" else ""}${"%.1f".format(dWaist)} cm",
                                accent = VioletSplit,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMetricCard(
                                title = "Tour de Poitrine",
                                value = "${latest?.chestCm ?: "—"} cm",
                                subtitle = "${if (dChest >= 0) "+" else ""}${"%.1f".format(dChest)} cm",
                                accent = CyberCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiMetricCard("Bras", "${latest?.armCm ?: "—"} cm", "Biceps contracté", ElectricLime, Modifier.weight(1f))
                            KpiMetricCard("Cuisses", "${latest?.thighCm ?: "—"} cm", "Hanches : ${latest?.hipCm ?: "—"} cm", AmberKinetic, Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Évolution Tour de Taille (cm)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        TrendLineChart(
                            points = measureList.filter { it.waistCm != null }.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.waistCm!!) },
                            lineColor = VioletSplit,
                            unit = "cm"
                        )
                    }
                    item {
                        Text("Évolution Tour de Bras (cm)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        TrendLineChart(
                            points = measureList.filter { it.armCm != null }.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.armCm!!) },
                            lineColor = ElectricLime,
                            unit = "cm"
                        )
                    }
                    items(measureList.reversed(), key = { "m_${it.id}" }) { item ->
                        AssessmentHistoryRow(
                            df.format(Date(item.dateEpochMs)),
                            "Taille ${item.waistCm ?: "—"} • Poitrine ${item.chestCm ?: "—"} • Bras ${item.armCm ?: "—"} • Cuisse ${item.thighCm ?: "—"} cm",
                            item.notes
                        ) { onDeleteAssessment(item) }
                    }
                }
            }
            2 -> {
                val distinctExNames = remember(filteredWorkouts) { filteredWorkouts.map { it.exerciseName }.distinct() }
                var selectedExFilter by remember(distinctExNames) { mutableStateOf(distinctExNames.firstOrNull() ?: "") }
                val exSeries = filteredWorkouts.filter { it.exerciseName == selectedExFilter }
                val totalVol = filteredWorkouts.sumOf { it.totalVolumeKg }.toInt()
                val totalPrs = filteredWorkouts.count { it.isPr }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiMetricCard("Volume Total", "$totalVol kg", "${filteredWorkouts.size} exercices logués", ElectricLime, Modifier.weight(1f))
                            KpiMetricCard("Records (PR)", "$totalPrs PR", "1RM max estimé", AmberKinetic, Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Volume par Séance (kg)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        BarMetricChart(
                            points = filteredWorkouts.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.totalVolumeKg) },
                            barColor = CyberCyan,
                            unit = "kg"
                        )
                    }
                    if (distinctExNames.isNotEmpty()) {
                        item {
                            Text("Progression 1RM Estimée par Exercice", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                distinctExNames.forEach { exName ->
                                    FilterChip(selected = selectedExFilter == exName, onClick = { selectedExFilter = exName }, label = { Text(exName) })
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            TrendLineChart(
                                points = exSeries.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.estimatedOneRmKg) },
                                lineColor = AmberKinetic,
                                unit = "kg 1RM"
                            )
                        }
                    }
                    items(filteredWorkouts.reversed(), key = { "wl_${it.id}" }) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (log.isPr) AmberKinetic else CharcoalBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(df.format(Date(log.dateEpochMs)), style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                        if (log.isPr) {
                                            Surface(color = AmberKinetic.copy(alpha = 0.2f), shape = MaterialTheme.shapes.small) {
                                                Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.EmojiEvents, null, tint = AmberKinetic, modifier = Modifier.size(12.dp))
                                                    Text(" PR", style = MaterialTheme.typography.labelSmall, color = AmberKinetic)
                                                }
                                            }
                                        }
                                    }
                                    Text(log.exerciseName, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${log.setsCompleted}x${log.repsCompleted} @ ${log.loadKg} kg • Vol ${log.totalVolumeKg.toInt()} kg • RPE ${log.rpe}/10",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElectricLime
                                    )
                                }
                                IconButton(onClick = { onDeleteWorkoutLog(log) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Supprimer log")
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                val nutList = filteredAssessments.filter { it.caloriesConsumed != null }
                val avgKcal = if (nutList.isNotEmpty()) nutList.mapNotNull { it.caloriesConsumed }.average().toInt() else 0
                val avgProt = if (nutList.isNotEmpty()) nutList.mapNotNull { it.proteinConsumedG }.average().toInt() else 0
                val avgHydr = if (nutList.isNotEmpty()) nutList.mapNotNull { it.hydrationLiters }.average() else 0.0

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            KpiMetricCard("Moyenne Calories", "$avgKcal kcal", "Cible : ${nutrition?.targetKcal ?: 0} kcal", ElectricLime, Modifier.weight(1f))
                            KpiMetricCard("Protéines / Eau", "${avgProt}g P", "Hydratation : ${"%.1f".format(avgHydr)} L/j", CyberCyan, Modifier.weight(1f))
                        }
                    }
                    item {
                        Text("Apports Caloriques vs Objectif", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        BarMetricChart(
                            points = nutList.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.caloriesConsumed!!.toDouble()) },
                            barColor = AmberKinetic,
                            targetValue = nutrition?.targetKcal?.toDouble(),
                            unit = "kcal"
                        )
                    }
                    item {
                        Text("Suivi Protéines (g)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        TrendLineChart(
                            points = nutList.filter { it.proteinConsumedG != null }.map { ChartPoint(df.format(Date(it.dateEpochMs)), it.proteinConsumedG!!.toDouble()) },
                            lineColor = CyberCyan,
                            unit = "g"
                        )
                    }
                    items(nutList.reversed(), key = { "n_${it.id}" }) { item ->
                        AssessmentHistoryRow(
                            df.format(Date(item.dateEpochMs)),
                            "${item.caloriesConsumed} kcal • P:${item.proteinConsumedG ?: 0}g G:${item.carbsConsumedG ?: 0}g L:${item.fatConsumedG ?: 0}g • Eau ${item.hydrationLiters ?: 0.0}L",
                            item.notes
                        ) { onDeleteAssessment(item) }
                    }
                }
            }
        }
    }

    // Dialog 1: Log Weight & Body Fat
    if (showLogWeightDialog) {
        var wText by remember { mutableStateOf((client?.weightKg ?: 75.0).toString()) }
        var bfText by remember { mutableStateOf((client?.bodyFatPct ?: 16.0).toString()) }
        var notes by remember { mutableStateOf("Pesée à jeun") }
        AlertDialog(
            onDismissRequest = { showLogWeightDialog = false },
            title = { Text("Logger Pesée & Composition") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = wText, onValueChange = { wText = it }, label = { Text("Poids (kg)") }, singleLine = true)
                    OutlinedTextField(value = bfText, onValueChange = { bfText = it }, label = { Text("Masse Grasse (%)") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val w = wText.replace(",", ".").toDoubleOrNull()
                    if (w != null && w > 25.0) {
                        onLogWeight(w, bfText.replace(",", ".").toDoubleOrNull(), notes)
                        showLogWeightDialog = false
                    }
                }) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { showLogWeightDialog = false }) { Text("Annuler") } }
        )
    }

    // Dialog 2: Log Measurements
    if (showLogMeasureDialog) {
        var waist by remember { mutableStateOf("82.0") }
        var chest by remember { mutableStateOf("103.0") }
        var arm by remember { mutableStateOf("37.0") }
        var thigh by remember { mutableStateOf("58.5") }
        var hip by remember { mutableStateOf("96.0") }
        var notes by remember { mutableStateOf("Mensurations matin") }
        AlertDialog(
            onDismissRequest = { showLogMeasureDialog = false },
            title = { Text("Logger Mensurations (cm)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = waist, onValueChange = { waist = it }, label = { Text("Taille") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = chest, onValueChange = { chest = it }, label = { Text("Poitrine") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = arm, onValueChange = { arm = it }, label = { Text("Bras") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = thigh, onValueChange = { thigh = it }, label = { Text("Cuisse") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = hip, onValueChange = { hip = it }, label = { Text("Hanches") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onLogMeasurements(
                        waist.replace(",", ".").toDoubleOrNull(),
                        chest.replace(",", ".").toDoubleOrNull(),
                        arm.replace(",", ".").toDoubleOrNull(),
                        thigh.replace(",", ".").toDoubleOrNull(),
                        hip.replace(",", ".").toDoubleOrNull(),
                        notes
                    )
                    showLogMeasureDialog = false
                }) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { showLogMeasureDialog = false }) { Text("Annuler") } }
        )
    }

    // Dialog 3: Log Nutrition Intake
    if (showLogNutritionDialog) {
        var kcal by remember { mutableStateOf((nutrition?.targetKcal ?: 2400).toString()) }
        var prot by remember { mutableStateOf((nutrition?.proteinG ?: 160).toString()) }
        var carbs by remember { mutableStateOf((nutrition?.carbsG ?: 260).toString()) }
        var fat by remember { mutableStateOf((nutrition?.fatsG ?: 70).toString()) }
        var fiber by remember { mutableStateOf((nutrition?.fiberG ?: 32).toString()) }
        var water by remember { mutableStateOf("2.8") }
        var notes by remember { mutableStateOf("Journée conforme au plan") }
        AlertDialog(
            onDismissRequest = { showLogNutritionDialog = false },
            title = { Text("Logger Apports Nutritionnels") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = kcal, onValueChange = { kcal = it }, label = { Text("Calories") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = water, onValueChange = { water = it }, label = { Text("Eau (L)") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = prot, onValueChange = { prot = it }, label = { Text("Prot g") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Gluc g") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Lip g") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = fiber, onValueChange = { fiber = it }, label = { Text("Fibres (g)") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    onLogNutrition(
                        kcal.toIntOrNull() ?: 2200,
                        prot.toIntOrNull() ?: 150,
                        carbs.toIntOrNull() ?: 240,
                        fat.toIntOrNull() ?: 70,
                        fiber.toIntOrNull() ?: 30,
                        water.replace(",", ".").toDoubleOrNull() ?: 2.5,
                        notes
                    )
                    showLogNutritionDialog = false
                }) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { showLogNutritionDialog = false }) { Text("Annuler") } }
        )
    }

    // Dialog 4: Log Workout
    if (showLogWorkoutDialog) {
        var title by remember { mutableStateOf("Séance Musculation") }
        var exName by remember(preselectedExerciseToLog) {
            mutableStateOf(preselectedExerciseToLog?.name ?: exercises.firstOrNull { it.code == "EX-167" }?.name ?: "Squat barre nuque (Back Squat)")
        }
        var sets by remember { mutableStateOf("4") }
        var reps by remember { mutableStateOf("8") }
        var load by remember { mutableStateOf("85.0") }
        var rpe by remember { mutableStateOf("8") }
        var dur by remember { mutableStateOf("55") }
        var notes by remember { mutableStateOf("Bonne exécution") }

        AlertDialog(
            onDismissRequest = {
                showLogWorkoutDialog = false
                onClearPreselectedExercise()
            },
            title = { Text("Logger une Performance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Séance") }, singleLine = true)
                    OutlinedTextField(value = exName, onValueChange = { exName = it }, label = { Text("Exercice précis") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sets, onValueChange = { sets = it }, label = { Text("Séries") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = reps, onValueChange = { reps = it }, label = { Text("Reps") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = load, onValueChange = { load = it }, label = { Text("Charge kg") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = rpe, onValueChange = { rpe = it }, label = { Text("RPE /10") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = dur, onValueChange = { dur = it }, label = { Text("Durée min") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Ressenti / Notes") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (exName.isNotBlank()) {
                        onLogWorkout(
                            title,
                            exName.trim(),
                            sets.toIntOrNull() ?: 4,
                            reps.toIntOrNull() ?: 8,
                            load.replace(",", ".").toDoubleOrNull() ?: 0.0,
                            (rpe.toIntOrNull() ?: 8).coerceIn(1, 10),
                            dur.toIntOrNull() ?: 50,
                            notes
                        )
                        showLogWorkoutDialog = false
                        onClearPreselectedExercise()
                    }
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogWorkoutDialog = false
                    onClearPreselectedExercise()
                }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun AssessmentHistoryRow(
    dateLabel: String,
    summary: String,
    notes: String,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dateLabel, style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                Text(summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (notes.isNotBlank()) {
                    Text(notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer entrée")
            }
        }
    }
}
