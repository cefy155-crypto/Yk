package com.example.domain.engines

import com.example.data.db.ClientEntity
import com.example.data.db.ExerciseEntity
import com.example.data.db.TrainingDayExerciseEntity
import com.example.data.db.TrainingProgramEntity

object GymSplitEngine {

    fun availableSplitsForFrequency(sessionsPerWeek: Int): List<String> = when (sessionsPerWeek) {
        2 -> listOf("Full Body 2j", "Haut / Bas Alterné")
        3 -> listOf("Full Body 3j", "Push / Pull / Legs", "Split Personnalisé")
        4 -> listOf("Haut / Bas", "Push / Pull / Jambes / Full", "Split Personnalisé")
        5 -> listOf("Hybride PPL + Haut/Bas", "Split 5 Jours Ciblé", "Split Personnalisé")
        6 -> listOf("Push / Pull / Legs x2", "Haut / Bas x3", "Split Personnalisé")
        else -> listOf("Full Body")
    }

    fun generateProgram(
        client: ClientEntity,
        sessionsPerWeek: Int,
        splitType: String,
        allExercises: List<ExerciseEntity>
    ): Pair<TrainingProgramEntity, List<TrainingDayExerciseEntity>> {
        val freq = sessionsPerWeek.coerceIn(2, 6)
        val dayBlueprints = buildDayBlueprints(freq, splitType)

        val dayExercises = mutableListOf<TrainingDayExerciseEntity>()
        val baseSets = if (client.level == "Débutant") 3 else 4
        val (repScheme, restSec) = when (client.goal) {
            "Force" -> "4-6" to 150
            "Perte de gras" -> "12-15" to 60
            "Endurance / HIIT" -> "15-20" to 45
            else -> "8-12" to 90
        }

        dayBlueprints.forEachIndexed { dayIdx, (dayLabel, targetRegions) ->
            val usedInDay = mutableSetOf<String>()
            targetRegions.forEachIndexed { orderIdx, region ->
                val pool = allExercises.filter {
                    it.region == region &&
                        (it.category == "Force" || it.category == "Hypertrophie") &&
                        it.code !in usedInDay &&
                        (client.availableEquipment.contains("Salle complète", true) ||
                            it.equipment == "Poids du corps" ||
                            client.availableEquipment.split(",").any { eq ->
                                it.equipment.contains(eq.trim(), true)
                            })
                }.ifEmpty {
                    allExercises.filter { it.region == region && it.code !in usedInDay }
                }
                val ex = pool.drop(dayIdx % (pool.size.coerceAtLeast(1))).firstOrNull() ?: pool.firstOrNull()
                if (ex != null) {
                    usedInDay += ex.code
                    val suggestedLoad = estimateStartingLoadKg(client, ex.equipment, orderIdx)
                    dayExercises += TrainingDayExerciseEntity(
                        programId = 0,
                        dayIndex = dayIdx + 1,
                        dayLabel = dayLabel,
                        orderIndex = orderIdx + 1,
                        exerciseName = ex.name, // Nom précis issu du catalogue 250 exercices
                        muscleGroups = "${ex.primaryMuscle} (${ex.secondaryMuscles})",
                        sets = if (orderIdx >= targetRegions.size - 1) 3 else baseSets,
                        reps = repScheme,
                        restSeconds = restSec,
                        tempo = ex.defaultTempo,
                        targetLoadKg = suggestedLoad,
                        coachingCues = "${ex.executionCues} • Sécurité : ${ex.safetyNote}",
                        variant = "Facile : ${ex.easyVariant} | Avancé : ${ex.hardVariant}"
                    )
                }
            }
        }

        val program = TrainingProgramEntity(
            clientId = client.id,
            title = "$splitType ($freq séances/sem.)",
            splitType = splitType,
            sessionsPerWeek = freq,
            level = client.level,
            goal = client.goal,
            durationWeeks = 8
        )
        return program to dayExercises
    }

    private fun buildDayBlueprints(freq: Int, splitType: String): List<Pair<String, List<String>>> {
        if (splitType.contains("Full Body", true)) {
            return (1..freq).map { d ->
                "Jour $d — Full Body Complet" to listOf("Jambes", "Pectoraux", "Dos", "Épaules", "Fessiers", "Bras", "Sangle abdominale")
            }
        }
        if (splitType.contains("Haut / Bas", true)) {
            return (1..freq).map { d ->
                if (d % 2 == 1) {
                    "Jour $d — Haut du Corps" to listOf("Pectoraux", "Dos", "Pectoraux", "Dos", "Épaules", "Bras", "Bras")
                } else {
                    "Jour $d — Bas du Corps & Gainage" to listOf("Jambes", "Fessiers", "Jambes", "Jambes", "Fessiers", "Sangle abdominale")
                }
            }
        }
        if (splitType.contains("Push", true) && freq >= 3) {
            val base = listOf(
                "Push (Poussée)" to listOf("Pectoraux", "Pectoraux", "Épaules", "Épaules", "Bras", "Bras"),
                "Pull (Tirage)" to listOf("Dos", "Dos", "Dos", "Épaules", "Bras", "Bras"),
                "Legs (Jambes & Core)" to listOf("Jambes", "Jambes", "Fessiers", "Jambes", "Sangle abdominale", "Sangle abdominale")
            )
            return (0 until freq).map { idx ->
                val (label, regs) = base[idx % 3]
                "Jour ${idx + 1} — $label" to regs
            }
        }
        // Hybride / Personnalisé 5-6 jours
        val customPool = listOf(
            "Jour 1 — Pectoraux & Triceps" to listOf("Pectoraux", "Pectoraux", "Pectoraux", "Bras", "Bras", "Sangle abdominale"),
            "Jour 2 — Dos & Biceps" to listOf("Dos", "Dos", "Dos", "Bras", "Bras"),
            "Jour 3 — Jambes & Fessiers" to listOf("Jambes", "Jambes", "Fessiers", "Jambes", "Fessiers"),
            "Jour 4 — Épaules & Rappels" to listOf("Épaules", "Épaules", "Épaules", "Pectoraux", "Dos", "Sangle abdominale"),
            "Jour 5 — Haut du Corps Athlétique" to listOf("Pectoraux", "Dos", "Épaules", "Bras", "Sangle abdominale"),
            "Jour 6 — Chaîne Postérieure & Core" to listOf("Fessiers", "Jambes", "Dos", "Sangle abdominale", "Cardio / Full Body")
        )
        return customPool.take(freq)
    }

    private fun estimateStartingLoadKg(client: ClientEntity, equipment: String, orderIdx: Int): Double {
        if (equipment == "Poids du corps" || equipment == "Élastique") return 0.0
        val factor = when (client.level) {
            "Avancé" -> 0.85
            "Intermédiaire" -> 0.65
            else -> 0.45
        }
        val base = client.weightKg * factor * if (orderIdx < 2) 1.0 else 0.45
        return ((base / 2.5).toInt() * 2.5).coerceAtLeast(5.0)
    }
}
