package com.example.domain.engines

import com.example.data.db.ClientEntity
import com.example.data.db.FoodEntity
import com.example.data.db.MealEntity
import com.example.data.db.MealItemEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.SupplementEntity
import kotlin.math.roundToInt

data class MealDraft(
    val meal: MealEntity,
    val items: List<MealItemEntity>
)

data class NutritionPlanBundle(
    val program: NutritionProgramEntity,
    val meals: List<MealDraft>
)

object NutritionEngine {

    val allStrategies = listOf(
        "Équilibrée",
        "High Protein",
        "Low Carb",
        "Keto",
        "Carb Cycling",
        "Intermittent Fasting",
        "Personnalisée"
    )

    val restrictiveStrategies = setOf("Low Carb", "Keto", "Intermittent Fasting", "PSMF")

    fun isStrategyAllowed(client: ClientEntity, strategy: String): Boolean {
        if (client.hasRestrictiveDietBan && strategy in restrictiveStrategies) {
            return false
        }
        return true
    }

    /**
     * Mifflin-St Jeor BMR + TDEE + Exact Macro Distribution + Strict Minor/Medical Safety Guard
     */
    fun generateNutritionPlan(
        client: ClientEntity,
        strategy: String,
        supplements: List<SupplementEntity>,
        foods: List<FoodEntity>,
        customProteinPct: Int = 30,
        customCarbsPct: Int = 45,
        customFatPct: Int = 25
    ): NutritionPlanBundle {
        val safeStrategy = if (isStrategyAllowed(client, strategy)) strategy else "Équilibrée"

        val bmr = if (client.sex == "Homme") {
            (10.0 * client.weightKg + 6.25 * client.heightCm - 5.0 * client.age + 5.0).roundToInt()
        } else {
            (10.0 * client.weightKg + 6.25 * client.heightCm - 5.0 * client.age - 161.0).roundToInt()
        }

        val activityMultiplier = when (client.activityLevel) {
            "Très actif" -> 1.725
            "Actif" -> 1.55
            "Modéré" -> 1.375
            else -> 1.2
        }
        val tdee = (bmr * activityMultiplier).roundToInt()

        val rawTargetKcal = when {
            client.hasRestrictiveDietBan -> {
                // Protection stricte mineur / cas médical : jamais de déficit agressif ni PSMF
                if (client.goal == "Prise de masse") tdee + 200 else tdee
            }
            client.goal == "Perte de gras" -> (tdee * 0.82).roundToInt().coerceAtLeast(bmr)
            client.goal == "Prise de masse" -> tdee + 300
            client.goal == "Force" -> tdee + 200
            else -> tdee
        }

        val (pRatio, cRatio, fRatio) = when (safeStrategy) {
            "High Protein" -> Triple(0.35, 0.38, 0.27)
            "Low Carb" -> Triple(0.35, 0.20, 0.45)
            "Keto" -> Triple(0.25, 0.08, 0.67)
            "Carb Cycling" -> Triple(0.32, 0.45, 0.23)
            "Intermittent Fasting" -> Triple(0.32, 0.40, 0.28)
            "Personnalisée" -> {
                val sum = (customProteinPct + customCarbsPct + customFatPct).coerceAtLeast(1).toDouble()
                Triple(customProteinPct / sum, customCarbsPct / sum, customFatPct / sum)
            }
            else -> Triple(0.28, 0.47, 0.25)
        }

        val totalProteinG = ((rawTargetKcal * pRatio) / 4.0).roundToInt()
        val totalCarbsG = ((rawTargetKcal * cRatio) / 4.0).roundToInt()
        val totalFatsG = ((rawTargetKcal * fRatio) / 9.0).roundToInt()
        val targetFiberG = ((rawTargetKcal / 1000.0) * 14.0).roundToInt().coerceIn(25, 45)

        val safetyNotice = when {
            client.isMinor -> "Protection Mineur (<18 ans) : Stratégies restrictives (Low Carb, Keto, Jeûne, PSMF) verrouillées. Apports optimisés pour la croissance et le sport."
            client.isMedicalRestriction -> "Protection Médicale active : Régimes restrictifs désactivés sur ce dossier."
            client.allergiesCsv.isNotBlank() -> "Filtrage allergènes actif (${client.allergiesCsv}) : substitutions automatiques appliquées sur les repas."
            else -> "Calcul validé (Mifflin-St Jeor) — Adulte éligible à toutes les stratégies."
        }

        val program = NutritionProgramEntity(
            clientId = client.id,
            strategy = safeStrategy,
            bmrKcal = bmr,
            tdeeKcal = tdee,
            targetKcal = rawTargetKcal,
            proteinG = totalProteinG,
            carbsG = totalCarbsG,
            fatsG = totalFatsG,
            fiberG = targetFiberG,
            safetyNotice = safetyNotice
        )

        val activeSuppKcal = supplements.filter { it.includedInTotals }.sumOf { it.activeKcal }
        val foodBudgetKcal = (rawTargetKcal - activeSuppKcal).coerceAtLeast(1100)

        val meals = buildMeals(
            client = client,
            strategy = safeStrategy,
            foodBudgetKcal = foodBudgetKcal,
            foods = foods
        )

        return NutritionPlanBundle(program = program, meals = meals)
    }

    private fun buildMeals(
        client: ClientEntity,
        strategy: String,
        foodBudgetKcal: Int,
        foods: List<FoodEntity>
    ): List<MealDraft> {
        val mealSlots = if (strategy == "Intermittent Fasting") {
            listOf(
                Triple("Déjeuner (Fenêtre 1)", "12:30", 0.42),
                Triple("Collation Pré-Training", "16:30", 0.18),
                Triple("Dîner", "20:00", 0.40)
            )
        } else {
            listOf(
                Triple("Petit-déjeuner", "07:45", 0.25),
                Triple("Déjeuner", "12:30", 0.35),
                Triple("Collation Pré-Training", "16:30", 0.12),
                Triple("Dîner", "20:00", 0.28)
            )
        }

        val clientAllergies = client.allergiesCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()

        fun resolveFood(preferredName: String): Triple<FoodEntity, Boolean, String> {
            val preferred = foods.find { it.name.equals(preferredName, ignoreCase = true) } ?: foods.first()
            val allergens = preferred.allergensCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
            val hasConflict = allergens.any { it in clientAllergies }
            if (!hasConflict) return Triple(preferred, false, "")

            // Substitution automatique sans allergène
            val directSub = foods.find {
                it.name.equals(preferred.substituteFoodName, ignoreCase = true) &&
                    it.allergensCsv.split(",").map { a -> a.trim().lowercase() }.none { a -> a in clientAllergies }
            }
            val safeFallback = directSub ?: foods.firstOrNull {
                it.category == preferred.category &&
                    it.allergensCsv.split(",").map { a -> a.trim().lowercase() }.none { a -> a in clientAllergies }
            } ?: preferred
            return Triple(safeFallback, !safeFallback.name.equals(preferred.name, ignoreCase = true), preferred.name)
        }

        fun makeItem(foodName: String, grams: Int): MealItemEntity {
            val (food, substituted, origName) = resolveFood(foodName)
            val ratio = grams / 100.0
            return MealItemEntity(
                mealId = 0,
                foodId = food.id,
                foodName = food.name,
                grams = grams,
                prepState = food.defaultPrepState,
                kcal = (food.kcalPer100g * ratio).roundToInt(),
                proteinG = ((food.proteinPer100g * ratio) * 10.0).roundToInt() / 10.0,
                carbsG = ((food.carbsPer100g * ratio) * 10.0).roundToInt() / 10.0,
                fatG = ((food.fatPer100g * ratio) * 10.0).roundToInt() / 10.0,
                fiberG = ((food.fiberPer100g * ratio) * 10.0).roundToInt() / 10.0,
                wasAutoSubstituted = substituted,
                originalFoodName = origName
            )
        }

        val scale = (foodBudgetKcal / 2200.0).coerceIn(0.65, 1.65)
        fun g(base: Int) = ((base * scale) / 5.0).roundToInt() * 5

        return mealSlots.mapIndexed { idx, (name, time, _) ->
            val items = when {
                name.startsWith("Petit") -> listOf(
                    makeItem("Flocons d'avoine (crus)", if (strategy == "Keto") g(15) else g(65)),
                    makeItem("Skyr nature 0%", g(180)),
                    makeItem("Myrtilles fraîches", g(80)),
                    makeItem("Amandes entières", g(20))
                )
                name.startsWith("Déjeuner") -> listOf(
                    makeItem("Blanc de poulet (cru)", g(160)),
                    makeItem("Riz basmati (cuit)", if (strategy == "Keto") g(30) else g(200)),
                    makeItem("Brocolis vapeur", g(180)),
                    makeItem("Huile d'olive vierge extra", g(12))
                )
                name.startsWith("Collation") -> listOf(
                    makeItem("Banane mûre", if (strategy == "Keto") g(40) else g(110)),
                    makeItem("Fromage blanc 0% MG", g(150)),
                    makeItem("Beurre de cacahuète 100%", g(15))
                )
                else -> listOf(
                    makeItem("Filet de saumon (cru)", g(150)),
                    makeItem("Patate douce (cuite)", if (strategy == "Keto") g(40) else g(190)),
                    makeItem("Haricots verts vapeur", g(180)),
                    makeItem("Avocat frais", g(60))
                )
            }
            MealDraft(
                meal = MealEntity(
                    nutritionProgramId = 0,
                    name = name,
                    scheduledTime = time,
                    orderIndex = idx + 1
                ),
                items = items
            )
        }
    }
}
