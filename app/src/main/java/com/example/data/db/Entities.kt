package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val role: String, // "Coach" | "Pratiquant"
    val specialty: String,
    val certifications: String,
    val email: String,
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val age: Int = 30,
    val sex: String = "Homme",
    val heightCm: Int = 178,
    val weightKg: Double = 78.0,
    val fitnessLevel: String = "Avancé",
    val fitnessGoal: String = "Performance & Force",
    val weeklySessionsTarget: Int = 4,
    val bio: String = ""
)

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val age: Int,
    val sex: String, // "Homme" | "Femme"
    val heightCm: Int,
    val weightKg: Double,
    val bodyFatPct: Double,
    val level: String, // "Débutant" | "Intermédiaire" | "Avancé"
    val goal: String, // "Prise de masse" | "Perte de gras" | "Force" | "Endurance / HIIT" | "Recomposition"
    val activityLevel: String, // "Sédentaire" | "Modéré" | "Actif" | "Très actif"
    val availableEquipment: String, // CSV: "Salle complète", "Haltères", "Élastiques", "Poids du corps", "Kettlebells"
    val medicalConstraints: String,
    val allergiesCsv: String, // CSV: "lactose,gluten,arachides,oeufs,soja,fruits_a_coque,poissons"
    val isMedicalRestriction: Boolean = false,
    val weeklySessionsTarget: Int = 4
) {
    val isMinor: Boolean get() = age < 18
    val hasRestrictiveDietBan: Boolean get() = isMinor || isMedicalRestriction
}

@Entity(
    tableName = "exercises",
    indices = [Index("region"), Index("category"), Index("equipment")]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val category: String, // "Force", "Hypertrophie", "Cardio / HIIT", "Mobilité", "Activation", "Étirement", "Collectif"
    val region: String, // "Pectoraux", "Dos", "Épaules", "Bras", "Jambes", "Fessiers", "Sangle abdominale", "Cardio / Full Body", "Mobilité & Cou"
    val primaryMuscle: String,
    val secondaryMuscles: String,
    val equipment: String, // "Barre", "Haltères", "Machine", "Poulie", "Kettlebell", "Poids du corps", "Élastique", "Cardio"
    val level: String, // "Débutant", "Intermédiaire", "Avancé"
    val defaultTempo: String,
    val executionCues: String,
    val safetyNote: String,
    val easyVariant: String,
    val hardVariant: String
)

@Entity(tableName = "training_programs")
data class TrainingProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val title: String,
    val splitType: String, // "Full Body", "Haut / Bas", "Push Pull Legs", "Hybride 5j", "Split Personnalisé"
    val sessionsPerWeek: Int,
    val level: String,
    val goal: String,
    val durationWeeks: Int,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "training_day_exercises",
    indices = [Index("programId")]
)
data class TrainingDayExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val dayIndex: Int,
    val dayLabel: String,
    val orderIndex: Int,
    val exerciseName: String,
    val muscleGroups: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int,
    val tempo: String,
    val targetLoadKg: Double,
    val coachingCues: String,
    val variant: String
)

@Entity(
    tableName = "workout_sessions",
    indices = [Index("clientId")]
)
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val title: String,
    val sessionType: String, // "Complète 6 Phases", "HIIT", "LISS", "Collectif", "Circuit"
    val focusRegion: String,
    val durationMinutes: Int,
    val roundsCount: Int = 1,
    val workSeconds: Int = 40,
    val restSeconds: Int = 20,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workout_session_rows",
    indices = [Index("sessionId")]
)
data class WorkoutSessionRowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val rowNumber: Int,
    val phase: String, // "1. Échauffement général" ... "6. Étirements"
    val exerciseName: String,
    val region: String,
    val sets: String,
    val reps: String,
    val durationText: String,
    val restText: String,
    val executionCues: String,
    val durationSecondsEstimate: Int = 45
)

@Entity(
    tableName = "nutrition_programs",
    indices = [Index("clientId")]
)
data class NutritionProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val strategy: String, // "Équilibrée", "High Protein", "Low Carb", "Keto", "Carb Cycling", "Intermittent Fasting", "Personnalisée"
    val bmrKcal: Int,
    val tdeeKcal: Int,
    val targetKcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatsG: Int,
    val fiberG: Int,
    val safetyNotice: String,
    val updatedAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "meals",
    indices = [Index("nutritionProgramId")]
)
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nutritionProgramId: Long,
    val name: String, // "Petit-déjeuner", "Déjeuner", "Collation Pré-Training", "Dîner"
    val scheduledTime: String,
    val orderIndex: Int
)

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Protéines", "Glucides", "Lipides", "Légumes & Fibres", "Fruits", "Produits Laitiers & Alt."
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double,
    val allergensCsv: String, // "lactose", "gluten", "arachides", "oeufs", "soja", "fruits_a_coque", "poissons"
    val defaultPrepState: String, // "Cru", "Cuit"
    val substituteFoodName: String
)

@Entity(
    tableName = "meal_items",
    indices = [Index("mealId")]
)
data class MealItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val foodId: Long,
    val foodName: String,
    val grams: Int,
    val prepState: String, // "Cru" | "Cuit"
    val kcal: Int,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val fiberG: Double,
    val wasAutoSubstituted: Boolean = false,
    val originalFoodName: String = ""
)

@Entity(
    tableName = "supplements",
    indices = [Index("clientId")]
)
data class SupplementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val name: String,
    val category: String, // "Protéine", "Performance", "Santé & Récupération"
    val powderDoseG: Double, // Poids brut de poudre / prise
    val activeProteinG: Double, // Macronutriment actif réel
    val activeCarbsG: Double,
    val activeFatG: Double,
    val activeKcal: Int,
    val creatineG: Double,
    val caffeineMg: Int,
    val timing: String, // "Post-entraînement", "Matin", "Pré-entraînement", "Coucher"
    val includedInTotals: Boolean = true
)

@Entity(
    tableName = "schedules",
    indices = [Index("clientId")]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val title: String,
    val activityType: String, // "Musculation", "HIIT", "LISS", "Collectif", "Bilan", "Récupération"
    val dayOfWeek: Int, // 1 = Lundi .. 7 = Dimanche
    val startTime: String,
    val durationMin: Int,
    val sharedNotes: String,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "assessments",
    indices = [Index("clientId"), Index("dateEpochMs")]
)
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val dateEpochMs: Long,
    val entryType: String, // "Poids & Compo", "Mensurations", "Nutrition", "Bilan Complet"
    val weightKg: Double?,
    val bodyFatPct: Double?,
    val waistCm: Double?,
    val chestCm: Double?,
    val armCm: Double?,
    val thighCm: Double?,
    val hipCm: Double?,
    val caloriesConsumed: Int?,
    val proteinConsumedG: Int?,
    val carbsConsumedG: Int?,
    val fatConsumedG: Int?,
    val fiberConsumedG: Int?,
    val hydrationLiters: Double?,
    val notes: String = ""
)

@Entity(
    tableName = "workout_logs",
    indices = [Index("clientId"), Index("dateEpochMs")]
)
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val dateEpochMs: Long,
    val sessionTitle: String,
    val exerciseName: String,
    val setsCompleted: Int,
    val repsCompleted: Int,
    val loadKg: Double,
    val rpe: Int, // 1..10
    val durationMin: Int,
    val isPr: Boolean = false,
    val notes: String = ""
) {
    val totalVolumeKg: Double get() = setsCompleted * repsCompleted * loadKg
    val estimatedOneRmKg: Double get() = if (repsCompleted <= 1) loadKg else loadKg * (1.0 + repsCompleted / 30.0)
}

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorRole: String,
    val category: String, // "Routine" | "Progrès" | "Repas Sain"
    val title: String,
    val content: String,
    val highlightBadge: String, // ex: "PR 110 kg", "520 kcal • 46g P", "6 Phases"
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "community_comments",
    indices = [Index("postId")]
)
data class CommunityCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val text: String,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_challenges")
data class CommunityChallengeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val groupName: String,
    val description: String,
    val targetLabel: String,
    val currentProgressPct: Int,
    val membersCount: Int,
    val isJoined: Boolean = false
)

