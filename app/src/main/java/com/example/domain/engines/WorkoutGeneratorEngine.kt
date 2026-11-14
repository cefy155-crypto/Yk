package com.example.domain.engines

import com.example.data.db.ClientEntity
import com.example.data.db.ExerciseEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.db.WorkoutSessionRowEntity

object WorkoutGeneratorEngine {

    /**
     * Generates a complete session strictly adhering to the 6 mandatory phases:
     * 1. Échauffement général (5-8 min)
     * 2. Échauffement articulaire (4-6 min, sans forcer le cou)
     * 3. Activation spécifique (3-5 min)
     * 4. Partie principale : Minimum 10 exercices (12-16 pour Full Body) couvrant toutes les régions
     * 5. Retour au calme (3-5 min)
     * 6. Étirements (5-8 min avec consignes de sécurité)
     */
    fun generateFullSession(
        client: ClientEntity,
        sessionType: String,
        focusRegion: String,
        durationMinutes: Int,
        allExercises: List<ExerciseEntity>,
        customMainCount: Int? = null
    ): Pair<WorkoutSessionEntity, List<WorkoutSessionRowEntity>> {
        val rows = mutableListOf<WorkoutSessionRowEntity>()
        var num = 1

        fun addRow(
            phase: String,
            exName: String,
            region: String,
            sets: String,
            reps: String,
            duration: String,
            rest: String,
            cues: String,
            estSeconds: Int
        ) {
            rows += WorkoutSessionRowEntity(
                sessionId = 0,
                rowNumber = num++,
                phase = phase,
                exerciseName = exName,
                region = region,
                sets = sets,
                reps = reps,
                durationText = duration,
                restText = rest,
                executionCues = cues,
                durationSecondsEstimate = estSeconds
            )
        }

        // Phase 1: Échauffement général (5-8 min)
        val p1 = allExercises.filter { it.category == "Cardio / HIIT" }.take(2)
        p1.forEachIndexed { idx, ex ->
            addRow(
                phase = "1. Échauffement général (6 min)",
                exName = ex.name,
                region = ex.region,
                sets = "1",
                reps = "Continu",
                duration = if (idx == 0) "3 min" else "3 min",
                rest = "30 s",
                cues = ex.executionCues,
                estSeconds = 180
            )
        }

        // Phase 2: Échauffement articulaire (4-6 min, sans forcer le cou)
        val p2 = listOfNotNull(
            allExercises.find { it.code == "EX-031" }, // Mobilité cervicale douce sans forcer le cou
            allExercises.find { it.code == "EX-033" }, // Cercles d'épaules
            allExercises.find { it.code == "EX-035" }, // Rotations thoraciques
            allExercises.find { it.code == "EX-039" }  // 90/90 Hanches
        )
        p2.forEach { ex ->
            addRow(
                phase = "2. Échauffement articulaire (5 min)",
                exName = ex.name,
                region = ex.region,
                sets = "1",
                reps = "8-10 lents",
                duration = "75 s",
                rest = "15 s",
                cues = "${ex.executionCues} • ${ex.safetyNote}",
                estSeconds = 75
            )
        }

        // Phase 3: Activation spécifique (3-5 min)
        val p3 = listOfNotNull(
            allExercises.find { it.code == "EX-054" }, // Band Pull-Apart
            allExercises.find { it.code == "EX-053" }, // Pont fessier activation
            allExercises.find { it.code == "EX-056" }  // Dead Bug
        )
        p3.forEach { ex ->
            addRow(
                phase = "3. Activation spécifique (4 min)",
                exName = ex.name,
                region = ex.region,
                sets = "2",
                reps = "12-15",
                duration = "80 s",
                rest = "20 s",
                cues = ex.executionCues,
                estSeconds = 80
            )
        }

        // Phase 4: Partie principale (Minimum 10 exercices ; 12-16 pour Full Body)
        val isFullBody = focusRegion.equals("Full Body", ignoreCase = true)
        val targetMainCount = (customMainCount ?: if (isFullBody) 13 else 10)
            .coerceIn(if (isFullBody) 12 else 10, if (isFullBody) 16 else 14)

        val mainPool = selectMainExercises(
            allExercises = allExercises,
            sessionType = sessionType,
            focusRegion = focusRegion,
            equipmentCsv = client.availableEquipment,
            targetCount = targetMainCount
        )

        val isHiit = sessionType.contains("HIIT", ignoreCase = true) || sessionType.contains("Circuit", ignoreCase = true)
        val isLiss = sessionType.contains("LISS", ignoreCase = true)

        mainPool.forEach { ex ->
            val sets = when {
                isHiit -> "3 tours"
                isLiss -> "1 bloc"
                else -> if (client.level == "Débutant") "3" else "4"
            }
            val reps = when {
                isHiit -> "40 s effort"
                isLiss -> "3 min"
                ex.defaultTempo.contains("Maintien") -> "35-45 s"
                client.goal == "Force" -> "5-6"
                client.goal == "Perte de gras" -> "12-15"
                else -> "8-12"
            }
            val dur = when {
                isHiit -> "40 s"
                isLiss -> "3 min"
                else -> "90 s"
            }
            val rest = when {
                isHiit -> "20 s"
                isLiss -> "15 s"
                client.goal == "Force" -> "120 s"
                else -> "75 s"
            }
            val estSec = when {
                isHiit -> 40
                isLiss -> 180
                else -> 60
            }
            addRow(
                phase = "4. Partie principale (${mainPool.size} ex.)",
                exName = ex.name,
                region = ex.region,
                sets = sets,
                reps = reps,
                duration = dur,
                rest = rest,
                cues = "${ex.executionCues} (Tempo ${ex.defaultTempo})",
                estSeconds = estSec
            )
        }

        // Phase 5: Retour au calme (3-5 min)
        val p5 = listOfNotNull(
            allExercises.find { it.code == "EX-241" },
            allExercises.find { it.code == "EX-242" }
        )
        p5.forEach { ex ->
            addRow(
                phase = "5. Retour au calme (4 min)",
                exName = ex.name,
                region = ex.region,
                sets = "1",
                reps = "Continu",
                duration = "2 min",
                rest = "0 s",
                cues = ex.executionCues,
                estSeconds = 120
            )
        }

        // Phase 6: Étirements (5-8 min avec consignes de sécurité)
        val p6 = listOfNotNull(
            allExercises.find { it.code == "EX-243" },
            allExercises.find { it.code == "EX-244" },
            allExercises.find { it.code == "EX-245" },
            allExercises.find { it.code == "EX-247" },
            allExercises.find { it.code == "EX-250" } // Relâchement trapèzes sans forcer le cou
        )
        p6.forEach { ex ->
            addRow(
                phase = "6. Étirements (6 min)",
                exName = ex.name,
                region = ex.region,
                sets = "1",
                reps = "2 x 30 s",
                duration = "70 s",
                rest = "10 s",
                cues = "${ex.executionCues} • ${ex.safetyNote}",
                estSeconds = 70
            )
        }

        val session = WorkoutSessionEntity(
            clientId = client.id,
            title = "$sessionType — $focusRegion",
            sessionType = sessionType,
            focusRegion = focusRegion,
            durationMinutes = durationMinutes,
            roundsCount = if (isHiit) 3 else 1,
            workSeconds = if (isHiit) 40 else 60,
            restSeconds = if (isHiit) 20 else 60
        )
        return session to rows
    }

    private fun selectMainExercises(
        allExercises: List<ExerciseEntity>,
        sessionType: String,
        focusRegion: String,
        equipmentCsv: String,
        targetCount: Int
    ): List<ExerciseEntity> {
        val allowedCategories = when {
            sessionType.contains("HIIT", ignoreCase = true) -> setOf("Cardio / HIIT", "Collectif", "Force", "Hypertrophie")
            sessionType.contains("Collectif", ignoreCase = true) -> setOf("Collectif", "Cardio / HIIT", "Hypertrophie")
            sessionType.contains("LISS", ignoreCase = true) -> setOf("Cardio / HIIT", "Mobilité")
            else -> setOf("Force", "Hypertrophie", "Cardio / HIIT")
        }

        val equipFiltered = allExercises.filter { ex ->
            ex.category in allowedCategories && isEquipmentAllowed(ex.equipment, equipmentCsv)
        }.ifEmpty { allExercises.filter { it.category in allowedCategories } }

        val targetRegions = when (focusRegion) {
            "Full Body" -> listOf("Jambes", "Pectoraux", "Dos", "Fessiers", "Épaules", "Bras", "Sangle abdominale", "Cardio / Full Body")
            "Haut du Corps" -> listOf("Pectoraux", "Dos", "Épaules", "Bras", "Sangle abdominale")
            "Bas du Corps" -> listOf("Jambes", "Fessiers", "Sangle abdominale")
            "HIIT / Métabolique" -> listOf("Cardio / Full Body", "Jambes", "Pectoraux", "Sangle abdominale")
            else -> listOf(focusRegion, "Sangle abdominale", "Cardio / Full Body")
        }

        val picked = mutableListOf<ExerciseEntity>()
        val usedCodes = mutableSetOf<String>()
        var regionCursor = 0
        var guard = 0
        while (picked.size < targetCount && guard < 200) {
            guard++
            val reg = targetRegions[regionCursor % targetRegions.size]
            val candidate = equipFiltered.firstOrNull { it.region == reg && it.code !in usedCodes }
                ?: equipFiltered.firstOrNull { it.code !in usedCodes }
            if (candidate != null) {
                picked += candidate
                usedCodes += candidate.code
            }
            regionCursor++
        }
        return picked
    }

    private fun isEquipmentAllowed(exEquipment: String, clientEquipmentCsv: String): Boolean {
        if (clientEquipmentCsv.contains("Salle complète", ignoreCase = true) || clientEquipmentCsv.isBlank()) {
            return true
        }
        if (exEquipment == "Poids du corps") return true
        return clientEquipmentCsv.split(",").any { token ->
            exEquipment.contains(token.trim(), ignoreCase = true) ||
                token.trim().contains(exEquipment, ignoreCase = true)
        }
    }
}
