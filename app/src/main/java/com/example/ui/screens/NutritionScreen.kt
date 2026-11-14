package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.db.ClientEntity
import com.example.data.db.FoodEntity
import com.example.data.db.MealEntity
import com.example.data.db.MealItemEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.SupplementEntity
import com.example.data.seed.FoodAndSupplementCatalog
import com.example.domain.engines.NutritionEngine
import com.example.ui.components.MacroRingChart
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnElectricLime

@Composable
fun NutritionScreen(
    client: ClientEntity?,
    program: NutritionProgramEntity?,
    meals: List<MealEntity>,
    mealItems: List<MealItemEntity>,
    foods: List<FoodEntity>,
    supplements: List<SupplementEntity>,
    onApplyStrategy: (strategy: String, p: Int, c: Int, f: Int) -> Unit,
    onAddFoodToMeal: (mealId: Long, food: FoodEntity, grams: Int, prepState: String) -> Unit,
    onRemoveMealItem: (Long) -> Unit,
    onAddSupplement: (SupplementEntity) -> Unit,
    onToggleSupplement: (SupplementEntity) -> Unit,
    onDeleteSupplement: (SupplementEntity) -> Unit,
    onOpenExport: () -> Unit
) {
    var showAddFoodMealId by remember { mutableStateOf<Long?>(null) }
    var showFoodDbDialog by remember { mutableStateOf(false) }
    var showAddSuppDialog by remember { mutableStateOf(false) }
    var customP by remember { mutableIntStateOf(30) }
    var customC by remember { mutableIntStateOf(45) }
    var customF by remember { mutableIntStateOf(25) }

    val foodKcal = mealItems.sumOf { it.kcal }
    val foodP = mealItems.sumOf { it.proteinG }
    val foodC = mealItems.sumOf { it.carbsG }
    val foodF = mealItems.sumOf { it.fatG }
    val foodFib = mealItems.sumOf { it.fiberG }

    val activeSupps = supplements.filter { it.includedInTotals }
    val suppPowderTotal = activeSupps.sumOf { it.powderDoseG }
    val suppKcal = activeSupps.sumOf { it.activeKcal }
    val suppP = activeSupps.sumOf { it.activeProteinG }
    val suppC = activeSupps.sumOf { it.activeCarbsG }
    val suppF = activeSupps.sumOf { it.activeFatG }

    val grandKcal = foodKcal + suppKcal
    val grandP = (foodP + suppP).toInt()
    val grandC = (foodC + suppC).toInt()
    val grandF = (foodF + suppF).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, CharcoalBorder, MaterialTheme.shapes.large)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_nutrition_banner),
                    contentDescription = "Nutrition Sportive",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.62f)
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(listOf(ObsidianBg.copy(alpha = 0.94f), ObsidianBg.copy(alpha = 0.4f)))
                    )
                )
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("NUTRITION PLANNER & SUPPLÉMENTS", style = MaterialTheme.typography.labelSmall, color = ElectricLime)
                        Text("${client?.fullName ?: ""} — ${program?.strategy ?: "Équilibrée"}", style = MaterialTheme.typography.headlineMedium)
                        Text("BMR ${program?.bmrKcal ?: 0} • TDEE ${program?.tdeeKcal ?: 0} • Fibres cible ${program?.fiberG ?: 30}g", style = MaterialTheme.typography.bodySmall, color = CyberCyan)
                    }
                    OutlinedButton(onClick = onOpenExport) {
                        Icon(Icons.Default.Download, contentDescription = "Exporter", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Strict Safety Guard Notice
        if (client != null && (client.hasRestrictiveDietBan || client.allergiesCsv.isNotBlank())) {
            item {
                val isRestricted = client.hasRestrictiveDietBan
                val bannerColor = if (isRestricted) CoralAlert else AmberKinetic
                Surface(
                    color = bannerColor.copy(alpha = 0.14f),
                    shape = MaterialTheme.shapes.medium,
                    border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = "Sécurité nutritionnelle", tint = bannerColor)
                        Text(program?.safetyNotice ?: "", style = MaterialTheme.typography.bodySmall, color = bannerColor)
                    }
                }
            }
        }

        // Strategy Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Stratégie Nutritionnelle", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NutritionEngine.allStrategies.forEach { strat ->
                            val allowed = client == null || NutritionEngine.isStrategyAllowed(client, strat)
                            val selected = program?.strategy == strat
                            FilterChip(
                                selected = selected,
                                onClick = { if (allowed) onApplyStrategy(strat, customP, customC, customF) },
                                enabled = allowed,
                                label = { Text(strat) },
                                leadingIcon = if (!allowed) {
                                    { Icon(Icons.Default.Lock, contentDescription = "Verrouillé mineur/médical", modifier = Modifier.size(14.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricLime,
                                    selectedLabelColor = OnElectricLime
                                )
                            )
                        }
                    }
                    if (program?.strategy == "Personnalisée") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(value = "$customP", onValueChange = { customP = it.toIntOrNull() ?: 30 }, label = { Text("P %") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = "$customC", onValueChange = { customC = it.toIntOrNull() ?: 45 }, label = { Text("G %") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = "$customF", onValueChange = { customF = it.toIntOrNull() ?: 25 }, label = { Text("L %") }, modifier = Modifier.weight(1f))
                            Button(onClick = { onApplyStrategy("Personnalisée", customP, customC, customF) }) { Text("OK") }
                        }
                    }
                }
            }
        }

        // Daily Totals + Macro Ring (Aliments + Compléments actifs)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroRingChart(
                    proteinG = grandP,
                    carbsG = grandC,
                    fatG = grandF,
                    totalKcal = grandKcal
                )
                Text(
                    text = "Totaux journaliers : $grandKcal / ${program?.targetKcal ?: 0} kcal • Fibres ${"%.1f".format(foodFib)} g • Aliments ($foodKcal kcal) + Compléments actifs ($suppKcal kcal)",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldSuccess
                )
            }
        }

        // Meals List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Structure des Repas (Cru / Cuit)", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { showFoodDbDialog = true }) { Text("Base Aliments (${foods.size})") }
            }
        }

        items(meals, key = { it.id }) { meal ->
            val itemsForMeal = mealItems.filter { it.mealId == meal.id }
            val mealKcal = itemsForMeal.sumOf { it.kcal }
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${meal.name} (${meal.scheduledTime})", style = MaterialTheme.typography.titleSmall, color = ElectricLime)
                            Text("$mealKcal kcal • ${itemsForMeal.sumOf { it.proteinG }.toInt()}g P", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                        }
                        OutlinedButton(onClick = { showAddFoodMealId = meal.id }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aliment")
                        }
                    }

                    itemsForMeal.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(CharcoalElevated)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${item.foodName} — ${item.grams}g (${item.prepState})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    if (item.wasAutoSubstituted) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = "Substitué allergie", tint = AmberKinetic, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    "${item.kcal} kcal • P:${item.proteinG}g G:${item.carbsG}g L:${item.fatG}g Fib:${item.fiberG}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.wasAutoSubstituted) {
                                    Text("Substitué automatiquement (remplace ${item.originalFoodName})", style = MaterialTheme.typography.labelSmall, color = AmberKinetic)
                                }
                            }
                            IconButton(onClick = { onRemoveMealItem(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Retirer", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Supplement Planner (Poudre vs Actifs)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Supplement Planner", style = MaterialTheme.typography.titleMedium, color = CyberCyan)
                    Text(
                        "Poudre totale : ${"%.1f".format(suppPowderTotal)}g → Actifs intégrés : ${"%.1f".format(suppP)}g P, ${"%.1f".format(suppC)}g G, $suppKcal kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showAddSuppDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime),
                    modifier = Modifier.testTag("add_supplement_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter")
                }
            }
        }

        items(supplements, key = { it.id }) { supp ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (supp.includedInTotals) CyberCyan else CharcoalBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(checked = supp.includedInTotals, onCheckedChange = { onToggleSupplement(supp) })
                    Column(modifier = Modifier.weight(1f)) {
                        Text(supp.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Poudre : ${supp.powderDoseG}g | Actifs : ${supp.activeProteinG}g P, ${supp.activeCarbsG}g G, ${supp.activeFatG}g L (${supp.activeKcal} kcal)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricLime
                        )
                        Text(
                            "Timing : ${supp.timing}" + if (supp.creatineG > 0) " • Créatine ${supp.creatineG}g" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteSupplement(supp) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer complément")
                    }
                }
            }
        }
    }

    // Add Food to Meal Dialog
    val mealIdTarget = showAddFoodMealId
    if (mealIdTarget != null) {
        var search by remember { mutableStateOf("") }
        var gramsText by remember { mutableStateOf("150") }
        var prepState by remember { mutableStateOf("Cuit") }
        val filteredFoods = foods.filter {
            search.isBlank() || it.name.contains(search, true) || it.category.contains(search, true)
        }.take(12)

        AlertDialog(
            onDismissRequest = { showAddFoodMealId = null },
            title = { Text("Ajouter un aliment au repas") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Rechercher aliment...") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = gramsText, onValueChange = { gramsText = it }, label = { Text("Grammes") }, modifier = Modifier.weight(1f), singleLine = true)
                        listOf("Cru", "Cuit").forEach { st ->
                            FilterChip(selected = prepState == st, onClick = { prepState = st }, label = { Text(st) })
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        filteredFoods.forEach { f ->
                            Surface(
                                color = CharcoalElevated,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(f.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text("${f.kcalPer100g.toInt()} kcal/100g • ${f.category}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(onClick = {
                                        onAddFoodToMeal(mealIdTarget, f, gramsText.toIntOrNull() ?: 100, prepState)
                                        showAddFoodMealId = null
                                    }) { Text("+") }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showAddFoodMealId = null }) { Text("Fermer") } }
        )
    }

    if (showFoodDbDialog) {
        var filterCat by remember { mutableStateOf("Toutes") }
        val cats = listOf("Toutes") + foods.map { it.category }.distinct()
        AlertDialog(
            onDismissRequest = { showFoodDbDialog = false },
            title = { Text("Base Aliments (${foods.size})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        cats.forEach { c -> FilterChip(selected = filterCat == c, onClick = { filterCat = c }, label = { Text(c) }) }
                    }
                    foods.filter { filterCat == "Toutes" || it.category == filterCat }.take(14).forEach { f ->
                        Text("• ${f.name} (${f.kcalPer100g.toInt()} kcal | P:${f.proteinPer100g} G:${f.carbsPer100g} L:${f.fatPer100g})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFoodDbDialog = false }) { Text("Fermer") } }
        )
    }

    if (showAddSuppDialog) {
        AlertDialog(
            onDismissRequest = { showAddSuppDialog = false },
            title = { Text("Ajouter un complément") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FoodAndSupplementCatalog.supplementTemplates.forEach { tpl ->
                        Card(colors = CardDefaults.cardColors(containerColor = CharcoalElevated)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tpl.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text("Poudre ${tpl.powderDoseG}g → Actif ${tpl.activeProteinG}g P / ${tpl.activeCarbsG}g G (${tpl.activeKcal} kcal)", style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                                }
                                Button(onClick = {
                                    onAddSupplement(tpl)
                                    showAddSuppDialog = false
                                }) { Text("+") }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAddSuppDialog = false }) { Text("Fermer") } }
        )
    }
}
