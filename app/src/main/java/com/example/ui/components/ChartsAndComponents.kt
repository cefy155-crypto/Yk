package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.ClientEntity
import com.example.domain.engines.AiActionType
import com.example.domain.engines.AiAssistantReply
import com.example.domain.engines.ExportFormat
import com.example.domain.engines.ExportScope
import com.example.ui.theme.AmberKinetic
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OnElectricLime

data class ChartPoint(
    val label: String,
    val value: Double
)

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color = ElectricLime,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TrendLineChart(
    points: List<ChartPoint>,
    lineColor: Color = ElectricLime,
    unit: String = "",
    emptyMessage: String = "Aucune donnée sur la période",
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(CharcoalElevated)
                .border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val minVal = points.minOf { it.value }
    val maxVal = points.maxOf { it.value }
    val range = (maxVal - minVal).takeIf { it > 0.001 } ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(CharcoalElevated)
            .border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min : ${"%.1f".format(minVal)} $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Max : ${"%.1f".format(maxVal)} $unit",
                style = MaterialTheme.typography.labelSmall,
                color = lineColor
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
        ) {
            val w = size.width
            val h = size.height
            val padX = 16f
            val padY = 14f
            val plotW = (w - padX * 2).coerceAtLeast(1f)
            val plotH = (h - padY * 2).coerceAtLeast(1f)

            // Grid lines
            for (i in 0..3) {
                val gy = padY + (plotH / 3f) * i
                drawLine(
                    color = CharcoalBorder,
                    start = Offset(padX, gy),
                    end = Offset(w - padX, gy),
                    strokeWidth = 1.5f
                )
            }

            val coords = points.mapIndexed { idx, pt ->
                val x = if (points.size == 1) w / 2f else padX + (idx.toFloat() / (points.size - 1)) * plotW
                val norm = ((pt.value - minVal) / range).toFloat()
                val y = padY + plotH - (norm * plotH)
                Offset(x, y)
            }

            if (coords.size >= 2) {
                val fillPath = Path().apply {
                    moveTo(coords.first().x, padY + plotH)
                    coords.forEach { lineTo(it.x, it.y) }
                    lineTo(coords.last().x, padY + plotH)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0.02f))
                    )
                )

                val strokePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        val prev = coords[i - 1]
                        val curr = coords[i]
                        val cx = (prev.x + curr.x) / 2f
                        cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )
            }

            coords.forEach { pt ->
                drawCircle(color = CharcoalSurface, radius = 9f, center = pt)
                drawCircle(color = lineColor, radius = 5.5f, center = pt)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(points.first().label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (points.size > 2) {
                Text(points[points.size / 2].label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(points.last().label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BarMetricChart(
    points: List<ChartPoint>,
    barColor: Color = CyberCyan,
    targetValue: Double? = null,
    unit: String = "",
    emptyMessage: String = "Aucune donnée enregistrée",
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(CharcoalElevated)
                .border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxVal = maxOf(points.maxOf { it.value }, targetValue ?: 0.0).coerceAtLeast(1.0)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(CharcoalElevated)
            .border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium)
            .padding(14.dp)
    ) {
        if (targetValue != null) {
            Text(
                text = "Cible : ${targetValue.toInt()} $unit",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricLime
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val w = size.width
            val h = size.height
            val slotW = w / points.size.coerceAtLeast(1)
            val barW = (slotW * 0.56f).coerceIn(10f, 44f)

            points.forEachIndexed { idx, pt ->
                val ratio = (pt.value / maxVal).toFloat().coerceIn(0f, 1f)
                val barH = (h * 0.9f) * ratio
                val x = idx * slotW + (slotW - barW) / 2f
                val y = h - barH
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }

            if (targetValue != null && targetValue > 0) {
                val tRatio = (targetValue / maxVal).toFloat().coerceIn(0f, 1f)
                val ty = h - (h * 0.9f) * tRatio
                drawLine(
                    color = ElectricLime,
                    start = Offset(0f, ty),
                    end = Offset(w, ty),
                    strokeWidth = 3f
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            points.takeLast(7).forEach {
                Text(it.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun MacroRingChart(
    proteinG: Int,
    carbsG: Int,
    fatG: Int,
    totalKcal: Int,
    modifier: Modifier = Modifier
) {
    val pKcal = proteinG * 4f
    val cKcal = carbsG * 4f
    val fKcal = fatG * 9f
    val sum = (pKcal + cKcal + fKcal).coerceAtLeast(1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(CharcoalElevated)
            .border(1.dp, CharcoalBorder, MaterialTheme.shapes.medium)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.size(92.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 16f, cap = StrokeCap.Butt)
                var startAngle = -90f
                val pSweep = (pKcal / sum) * 360f
                val cSweep = (cKcal / sum) * 360f
                val fSweep = (fKcal / sum) * 360f
                drawArc(color = CyberCyan, startAngle = startAngle, sweepAngle = pSweep, useCenter = false, style = stroke)
                startAngle += pSweep
                drawArc(color = ElectricLime, startAngle = startAngle, sweepAngle = cSweep, useCenter = false, style = stroke)
                startAngle += cSweep
                drawArc(color = AmberKinetic, startAngle = startAngle, sweepAngle = fSweep, useCenter = false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$totalKcal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            MacroLegendRow("Protéines", "${proteinG}g", "${((pKcal / sum) * 100).toInt()}%", CyberCyan)
            MacroLegendRow("Glucides", "${carbsG}g", "${((cKcal / sum) * 100).toInt()}%", ElectricLime)
            MacroLegendRow("Lipides", "${fatG}g", "${((fKcal / sum) * 100).toInt()}%", AmberKinetic)
        }
    }
}

@Composable
private fun MacroLegendRow(label: String, grams: String, pct: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text("$grams ($pct)", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun ClientSelectorBar(
    clients: List<ClientEntity>,
    activeClient: ClientEntity?,
    onSelectClient: (Long) -> Unit,
    onAddClientClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        clients.forEach { client ->
            val selected = client.id == activeClient?.id
            FilterChip(
                selected = selected,
                onClick = { onSelectClient(client.id) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(client.fullName)
                        if (client.isMinor) {
                            Icon(Icons.Default.Shield, contentDescription = "Mineur protégé", modifier = Modifier.size(14.dp))
                        }
                    }
                },
                leadingIcon = {
                    Icon(
                        if (selected) Icons.Default.Check else Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricLime,
                    selectedLabelColor = OnElectricLime,
                    selectedLeadingIconColor = OnElectricLime
                )
            )
        }
        OutlinedButton(
            onClick = onAddClientClick,
            modifier = Modifier.testTag("add_client_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nouveau pratiquant", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Pratiquant")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onSave: (ClientEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("26") }
    var sex by remember { mutableStateOf("Homme") }
    var heightText by remember { mutableStateOf("176") }
    var weightText by remember { mutableStateOf("74.0") }
    var level by remember { mutableStateOf("Intermédiaire") }
    var goal by remember { mutableStateOf("Recomposition") }
    var equipment by remember { mutableStateOf("Salle complète") }
    var allergies by remember { mutableStateOf("") }
    var isMedical by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau dossier pratiquant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom complet") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = ageText, onValueChange = { ageText = it }, label = { Text("Âge") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = heightText, onValueChange = { heightText = it }, label = { Text("Taille (cm)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = weightText, onValueChange = { weightText = it }, label = { Text("Poids (kg)") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Homme", "Femme").forEach { s ->
                        FilterChip(selected = sex == s, onClick = { sex = s }, label = { Text(s) })
                    }
                }
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Débutant", "Intermédiaire", "Avancé").forEach { l ->
                        FilterChip(selected = level == l, onClick = { level = l }, label = { Text(l) })
                    }
                }
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Prise de masse", "Perte de gras", "Force", "Recomposition", "Endurance / HIIT").forEach { g ->
                        FilterChip(selected = goal == g, onClick = { goal = g }, label = { Text(g) })
                    }
                }
                OutlinedTextField(
                    value = equipment,
                    onValueChange = { equipment = it },
                    label = { Text("Matériel (ex: Salle complète, Haltères, Élastiques)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies CSV (lactose,gluten,arachides,oeufs)") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isMedical, onCheckedChange = { isMedical = it })
                    Text("Cas médical / restriction diététique stricte", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ClientEntity(
                                fullName = name.trim(),
                                age = ageText.toIntOrNull()?.coerceIn(12, 95) ?: 26,
                                sex = sex,
                                heightCm = heightText.toIntOrNull()?.coerceIn(120, 230) ?: 176,
                                weightKg = weightText.replace(",", ".").toDoubleOrNull()?.coerceIn(35.0, 220.0) ?: 74.0,
                                bodyFatPct = 18.0,
                                level = level,
                                goal = goal,
                                activityLevel = "Actif",
                                availableEquipment = equipment.ifBlank { "Salle complète" },
                                medicalConstraints = if (isMedical) "Restriction médicale active" else "Aucune",
                                allergiesCsv = allergies.trim().lowercase(),
                                isMedicalRestriction = isMedical,
                                weeklySessionsTarget = 4
                            )
                        )
                    }
                }
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun ExportDocumentDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat, ExportScope) -> Unit
) {
    var selectedScope by remember { mutableStateOf(ExportScope.SESSION_6_PHASES) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Download, contentDescription = null, tint = ElectricLime) },
        title = { Text("Exportation Multi-Formats") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Contenu à exporter :", style = MaterialTheme.typography.labelLarge)
                ExportScope.entries.forEach { scope ->
                    FilterChip(
                        selected = selectedScope == scope,
                        onClick = { selectedScope = scope },
                        label = { Text(scope.label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Format du fichier :", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExportFormat.entries.forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt.extension.uppercase()) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExport(selectedFormat, selectedScope)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_export_button")
            ) { Text("Générer & Partager") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantBottomSheet(
    reply: AiAssistantReply?,
    isLoading: Boolean,
    onSendPrompt: (String) -> Unit,
    onExecuteAction: (AiActionType) -> Unit,
    onDismiss: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricLime)
                    Text("Assistant Intelligent", style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Surface(
                color = CyberCyanContainerCompat,
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Mode aperçu IA : connectez Firebase (AI Logic) pour activer Gemini en direct. Les commandes ci-dessous pilotent déjà les moteurs locaux de l'app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyberCyan,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Génère une séance Full Body",
                    "Recalcule macros High Protein",
                    "Passe en Split Haut / Bas",
                    "Crée un circuit HIIT"
                ).forEach { chip ->
                    OutlinedButton(onClick = { onSendPrompt(chip) }) {
                        Text(chip, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = { Text("Commande en langage naturel...") },
                modifier = Modifier.fillMaxWidth().testTag("ai_prompt_input"),
                trailingIcon = {
                    Button(
                        onClick = {
                            onSendPrompt(prompt)
                            prompt = ""
                        },
                        enabled = prompt.isNotBlank() && !isLoading
                    ) { Text("Envoyer") }
                }
            )

            if (reply != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricLime.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(reply.title, style = MaterialTheme.typography.titleMedium, color = ElectricLime)
                        Text(reply.message, style = MaterialTheme.typography.bodyMedium)
                        val action = reply.actionType
                        val label = reply.actionLabel
                        if (action != null && label != null) {
                            Button(
                                onClick = {
                                    onExecuteAction(action)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricLime, contentColor = OnElectricLime)
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private val CyberCyanContainerCompat = Color(0xFF0C354A)
