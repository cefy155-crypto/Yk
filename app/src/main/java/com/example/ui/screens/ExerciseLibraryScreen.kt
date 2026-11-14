package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.ExerciseEntity
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.OnElectricLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    exercises: List<ExerciseEntity>,
    onQuickLogExercise: (ExerciseEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("Toutes") }
    var selectedEquipment by remember { mutableStateOf("Tous") }
    var inspectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }

    val regions = remember(exercises) {
        listOf("Toutes") + exercises.map { it.region }.distinct()
    }
    val equipments = remember(exercises) {
        listOf("Tous") + exercises.map { it.equipment }.distinct()
    }

    val filtered = remember(exercises, searchQuery, selectedRegion, selectedEquipment) {
        exercises.filter { ex ->
            val matchQuery = searchQuery.isBlank() ||
                ex.name.contains(searchQuery, ignoreCase = true) ||
                ex.primaryMuscle.contains(searchQuery, ignoreCase = true) ||
                ex.code.contains(searchQuery, ignoreCase = true)
            val matchRegion = selectedRegion == "Toutes" || ex.region == selectedRegion
            val matchEquip = selectedEquipment == "Tous" || ex.equipment == selectedEquipment
            matchQuery && matchRegion && matchEquip
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Bibliothèque 250 Exercices", style = MaterialTheme.typography.headlineMedium, color = ElectricLime)
                Surface(
                    color = ElectricLime.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${filtered.size} / ${exercises.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher exercice, muscle, code EX-...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("exercise_search_input")
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                regions.forEach { reg ->
                    FilterChip(
                        selected = selectedRegion == reg,
                        onClick = { selectedRegion = reg },
                        label = { Text(reg) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricLime,
                            selectedLabelColor = OnElectricLime
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                equipments.forEach { eq ->
                    FilterChip(
                        selected = selectedEquipment == eq,
                        onClick = { selectedEquipment = eq },
                        label = { Text(eq) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { ex ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { inspectedExercise = ex },
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(CharcoalElevated)
                                .border(1.dp, CharcoalBorder, MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(ex.code, style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                Text("• ${ex.region}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(ex.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${ex.primaryMuscle} • ${ex.equipment} • ${ex.level}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    val selected = inspectedExercise
    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { inspectedExercise = null },
            containerColor = CharcoalSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${selected.code} • ${selected.category}", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                        Text(selected.name, style = MaterialTheme.typography.headlineMedium, color = ElectricLime)
                    }
                    IconButton(onClick = { inspectedExercise = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                // Technical Diagram / Anatomy Badge Card
                Surface(
                    color = CharcoalElevated,
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Fiche Technique Biomécanique", style = MaterialTheme.typography.titleSmall, color = CyberCyan)
                        Text("Région : ${selected.region} | Matériel : ${selected.equipment}")
                        Text("Muscle principal : ${selected.primaryMuscle}")
                        Text("Synergistes : ${selected.secondaryMuscles}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tempo recommandé : ${selected.defaultTempo} • Niveau : ${selected.level}", style = MaterialTheme.typography.labelMedium, color = ElectricLime)
                    }
                }

                Text("Exécution précise", style = MaterialTheme.typography.titleSmall)
                Text(selected.executionCues, style = MaterialTheme.typography.bodyMedium)

                Surface(
                    color = AmberKinetic.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.small,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberKinetic.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AmberKinetic, modifier = Modifier.size(18.dp))
                        Text("Sécurité : ${selected.safetyNote}", style = MaterialTheme.typography.bodySmall, color = AmberKinetic)
                    }
                }

                Text("Variantes & Progressions", style = MaterialTheme.typography.titleSmall)
                Text("• Régression : ${selected.easyVariant}", style = MaterialTheme.typography.bodySmall)
                Text("• Progression : ${selected.hardVariant}", style = MaterialTheme.typography.bodySmall)

                Button(
                    onClick = {
                        onQuickLogExercise(selected)
                        inspectedExercise = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Logger une performance sur cet exercice", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
