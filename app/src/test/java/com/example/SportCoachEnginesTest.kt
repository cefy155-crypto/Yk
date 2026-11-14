package com.example

import com.example.data.db.ClientEntity
import com.example.data.repository.PasswordHasher
import com.example.data.seed.ExerciseCatalogPart1
import com.example.data.seed.ExerciseCatalogPart2
import com.example.data.seed.FoodAndSupplementCatalog
import com.example.domain.engines.GymSplitEngine
import com.example.domain.engines.NutritionEngine
import com.example.domain.engines.WorkoutGeneratorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SportCoachEnginesTest {

    @Test
    fun exerciseCatalogHasExactly250UniqueExercises() {
        val all = ExerciseCatalogPart1.list + ExerciseCatalogPart2.list
        assertEquals(250, all.size)
        assertEquals(250, all.map { it.code }.distinct().size)
    }

    @Test
    fun fullBodySessionGeneratorRespects6PhasesAnd12To16MainExercises() {
        val all = ExerciseCatalogPart1.list + ExerciseCatalogPart2.list
        val adult = ClientEntity(
            id = 1,
            fullName = "Lucas Martin",
            age = 28,
            sex = "Homme",
            heightCm = 179,
            weightKg = 81.2,
            bodyFatPct = 14.2,
            level = "Intermédiaire",
            goal = "Prise de masse",
            activityLevel = "Actif",
            availableEquipment = "Salle complète",
            medicalConstraints = "",
            allergiesCsv = ""
        )
        val (_, rows) = WorkoutGeneratorEngine.generateFullSession(
            client = adult,
            sessionType = "Complète 6 Phases",
            focusRegion = "Full Body",
            durationMinutes = 60,
            allExercises = all
        )
        val mainRows = rows.filter { it.phase.startsWith("4.") }
        assertTrue("Full Body main phase must have 12..16 exercises", mainRows.size in 12..16)
        assertTrue(rows.any { it.phase.startsWith("1.") })
        assertTrue(rows.any { it.phase.startsWith("2.") && it.executionCues.contains("cou", ignoreCase = true) })
        assertTrue(rows.any { it.phase.startsWith("3.") })
        assertTrue(rows.any { it.phase.startsWith("5.") })
        assertTrue(rows.any { it.phase.startsWith("6.") && it.executionCues.contains("Sécurité", ignoreCase = true) })
    }

    @Test
    fun nutritionEngineBlocksRestrictiveDietsForMinorsAndSubstitutesAllergens() {
        val minor = ClientEntity(
            id = 3,
            fullName = "Théo Moreau",
            age = 16,
            sex = "Homme",
            heightCm = 174,
            weightKg = 67.5,
            bodyFatPct = 13.5,
            level = "Débutant",
            goal = "Perte de gras",
            activityLevel = "Très actif",
            availableEquipment = "Poids du corps",
            medicalConstraints = "",
            allergiesCsv = "lactose,gluten"
        )
        assertFalse(NutritionEngine.isStrategyAllowed(minor, "Keto"))
        assertFalse(NutritionEngine.isStrategyAllowed(minor, "Low Carb"))
        assertFalse(NutritionEngine.isStrategyAllowed(minor, "Intermittent Fasting"))
        assertTrue(NutritionEngine.isStrategyAllowed(minor, "Équilibrée"))

        val bundle = NutritionEngine.generateNutritionPlan(
            client = minor,
            strategy = "Keto",
            supplements = FoodAndSupplementCatalog.defaultSupplementsForClient(3, hasLactoseAllergy = true, isMinor = true),
            foods = FoodAndSupplementCatalog.foods
        )
        assertEquals("Équilibrée", bundle.program.strategy)
        val items = bundle.meals.flatMap { it.items }
        assertTrue(items.any { it.wasAutoSubstituted })
    }

    @Test
    fun gymSplitEngineGeneratesRequestedDays() {
        val all = ExerciseCatalogPart1.list + ExerciseCatalogPart2.list
        val adult = ClientEntity(
            id = 1, fullName = "Test", age = 26, sex = "Homme", heightCm = 175, weightKg = 75.0,
            bodyFatPct = 15.0, level = "Intermédiaire", goal = "Force", activityLevel = "Actif",
            availableEquipment = "Salle complète", medicalConstraints = "", allergiesCsv = ""
        )
        val (program, exercises) = GymSplitEngine.generateProgram(adult, 4, "Haut / Bas", all)
        assertEquals(4, program.sessionsPerWeek)
        assertEquals(4, exercises.map { it.dayIndex }.distinct().size)
    }

    @Test
    fun passwordHasherVerifiesSaltedHash() {
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hashPassword("Secret123!", salt)
        assertTrue(PasswordHasher.verify("Secret123!", salt, hash))
        assertFalse(PasswordHasher.verify("WrongPass", salt, hash))
    }
}
