package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.seed.ExerciseCatalogPart1
import com.example.data.seed.ExerciseCatalogPart2
import com.example.data.seed.FoodAndSupplementCatalog
import com.example.domain.engines.GymSplitEngine
import com.example.domain.engines.NutritionEngine
import com.example.domain.engines.WorkoutGeneratorEngine

@Database(
    entities = [
        UserEntity::class,
        ClientEntity::class,
        ExerciseEntity::class,
        TrainingProgramEntity::class,
        TrainingDayExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSessionRowEntity::class,
        NutritionProgramEntity::class,
        MealEntity::class,
        FoodEntity::class,
        MealItemEntity::class,
        SupplementEntity::class,
        ScheduleEntity::class,
        AssessmentEntity::class,
        WorkoutLogEntity::class,
        CommunityPostEntity::class,
        CommunityCommentEntity::class,
        CommunityChallengeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coachDao(): CoachDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sport_coach_pro.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

object DatabaseSeeder {
    suspend fun ensureSeeded(db: AppDatabase) {
        val dao = db.coachDao()
        if (dao.countExercises() == 0) {
            dao.insertExercises(ExerciseCatalogPart1.list + ExerciseCatalogPart2.list)
        }
        if (dao.countFoods() == 0) {
            dao.insertFoods(FoodAndSupplementCatalog.foods)
        }
        if (dao.countClients() > 0) return

        val salt = com.example.data.repository.PasswordHasher.generateSalt()
        val hash = com.example.data.repository.PasswordHasher.hashPassword("Coach2026!", salt)
        dao.insertUser(
            UserEntity(
                fullName = "Coach Marc Delacroix",
                role = "Coach",
                specialty = "Force, Hypertrophie, HIIT & Recomposition",
                certifications = "BPJEPS AF • NSCA-CSCS • Diplôme Nutrition Sportive",
                email = "coach@sportcoachpro.fr",
                passwordHash = hash,
                passwordSalt = salt,
                age = 34,
                sex = "Homme",
                heightCm = 181,
                weightKg = 83.5,
                fitnessLevel = "Avancé",
                fitnessGoal = "Performance & Coaching",
                weeklySessionsTarget = 5,
                bio = "Préparateur physique diplômé spécialisé force, hypertrophie et nutrition sportive."
            )
        )

        val lucasId = dao.insertClient(
            ClientEntity(
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
                medicalConstraints = "Aucune restriction articulaire",
                allergiesCsv = "",
                isMedicalRestriction = false,
                weeklySessionsTarget = 4
            )
        )
        val camilleId = dao.insertClient(
            ClientEntity(
                fullName = "Camille Laurent",
                age = 33,
                sex = "Femme",
                heightCm = 166,
                weightKg = 64.8,
                bodyFatPct = 22.8,
                level = "Intermédiaire",
                goal = "Perte de gras",
                activityLevel = "Modéré",
                availableEquipment = "Haltères,Élastiques,Poids du corps",
                medicalConstraints = "Sensibilité cervicale légère (éviter charge directe nuque)",
                allergiesCsv = "lactose,gluten",
                isMedicalRestriction = false,
                weeklySessionsTarget = 4
            )
        )
        val theoId = dao.insertClient(
            ClientEntity(
                fullName = "Théo Moreau",
                age = 16,
                sex = "Homme",
                heightCm = 174,
                weightKg = 67.5,
                bodyFatPct = 13.5,
                level = "Débutant",
                goal = "Recomposition",
                activityLevel = "Très actif",
                availableEquipment = "Poids du corps,Kettlebell,Élastiques",
                medicalConstraints = "Athlète mineur en croissance — charges progressives maîtrisées",
                allergiesCsv = "arachides",
                isMedicalRestriction = false,
                weeklySessionsTarget = 3
            )
        )

        val allClients = listOf(
            ClientEntity(lucasId, "Lucas Martin", 28, "Homme", 179, 81.2, 14.2, "Intermédiaire", "Prise de masse", "Actif", "Salle complète", "Aucune restriction articulaire", "", false, 4),
            ClientEntity(camilleId, "Camille Laurent", 33, "Femme", 166, 64.8, 22.8, "Intermédiaire", "Perte de gras", "Modéré", "Haltères,Élastiques,Poids du corps", "Sensibilité cervicale légère", "lactose,gluten", false, 4),
            ClientEntity(theoId, "Théo Moreau", 16, "Homme", 174, 67.5, 13.5, "Débutant", "Recomposition", "Très actif", "Poids du corps,Kettlebell,Élastiques", "Athlète mineur", "arachides", false, 3)
        )

        val exercises = ExerciseCatalogPart1.list + ExerciseCatalogPart2.list
        val foods = FoodAndSupplementCatalog.foods

        allClients.forEach { client ->
            // Seed default Gym Split Program
            val (program, dayItems) = GymSplitEngine.generateProgram(
                client = client,
                sessionsPerWeek = client.weeklySessionsTarget,
                splitType = if (client.weeklySessionsTarget == 4) "Haut / Bas" else "Full Body",
                allExercises = exercises
            )
            val progId = dao.insertTrainingProgram(program)
            dao.insertTrainingDayExercises(dayItems.map { it.copy(programId = progId) })

            // Seed default Full-Body 6-phase session
            val (session, rows) = WorkoutGeneratorEngine.generateFullSession(
                client = client,
                sessionType = "Complète 6 Phases",
                focusRegion = "Full Body",
                durationMinutes = 60,
                allExercises = exercises
            )
            val sessionId = dao.insertWorkoutSession(session)
            dao.insertWorkoutSessionRows(rows.map { it.copy(sessionId = sessionId) })

            // Seed Supplements
            val supps = FoodAndSupplementCatalog.defaultSupplementsForClient(
                clientId = client.id,
                hasLactoseAllergy = client.allergiesCsv.contains("lactose"),
                isMinor = client.isMinor
            )
            dao.insertSupplements(supps)

            // Seed Nutrition Program
            val strategy = if (client.isMinor) "Équilibrée" else if (client.goal == "Perte de gras") "High Protein" else "Équilibrée"
            val nutritionBundle = NutritionEngine.generateNutritionPlan(
                client = client,
                strategy = strategy,
                supplements = supps,
                foods = foods
            )
            val nutId = dao.insertNutritionProgram(nutritionBundle.program)
            nutritionBundle.meals.forEach { mealDraft ->
                val mealId = dao.insertMeal(mealDraft.meal.copy(nutritionProgramId = nutId))
                dao.insertMealItems(mealDraft.items.map { it.copy(mealId = mealId) })
            }

            // Seed Weekly Schedule
            dao.insertSchedules(
                listOf(
                    ScheduleEntity(0, client.id, "Séance Haut du Corps Force", "Musculation", 1, "18:00", 60, "Focus tempo 3-1-1-0 sur développé", true),
                    ScheduleEntity(0, client.id, "HIIT Métabolique 30'", "HIIT", 2, "18:30", 35, "Intervals 40s effort / 20s récup", true),
                    ScheduleEntity(0, client.id, "Séance Bas du Corps", "Musculation", 4, "18:00", 60, "Surveiller profondeur squat", false),
                    ScheduleEntity(0, client.id, "LISS Zone 2 + Mobilité", "LISS", 6, "10:00", 45, "Marche inclinée 65% FCmax", false)
                )
            )
        }

        // Seed 21 days of rich Progress Tracking data for Lucas & Camille & Théo
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        val lucasAssessments = listOf(
            AssessmentEntity(0, lucasId, now - 21 * dayMs, "Bilan Complet", 79.4, 14.8, 83.5, 102.0, 36.2, 57.5, 96.0, 2820, 178, 340, 82, 34, 2.8, "Début cycle Prise de masse propre"),
            AssessmentEntity(0, lucasId, now - 17 * dayMs, "Nutrition", null, null, null, null, null, null, null, 2940, 185, 365, 80, 36, 3.1, "Bonne adhérence glucides"),
            AssessmentEntity(0, lucasId, now - 14 * dayMs, "Poids & Compo", 79.9, 14.6, 83.2, 102.4, 36.5, 57.9, 96.1, 2980, 186, 372, 81, 35, 3.0, "Progression poids maîtrisée"),
            AssessmentEntity(0, lucasId, now - 10 * dayMs, "Nutrition", null, null, null, null, null, null, null, 2890, 182, 352, 82, 33, 2.9, "Journée repos"),
            AssessmentEntity(0, lucasId, now - 7 * dayMs, "Mensurations", 80.5, 14.4, 83.0, 103.0, 36.9, 58.3, 96.3, 3020, 190, 380, 80, 37, 3.2, "+1 cm tour de poitrine"),
            AssessmentEntity(0, lucasId, now - 3 * dayMs, "Nutrition", null, null, null, null, null, null, null, 3050, 192, 386, 81, 38, 3.3, "Excellente énergie séance jambes"),
            AssessmentEntity(0, lucasId, now - 1 * dayMs, "Bilan Complet", 81.2, 14.2, 82.8, 103.6, 37.2, 58.7, 96.4, 3010, 189, 378, 82, 36, 3.2, "Record de volume hebdo atteint")
        )
        dao.insertAssessments(lucasAssessments)

        val lucasWorkouts = listOf(
            WorkoutLogEntity(0, lucasId, now - 20 * dayMs, "Haut du Corps", "Développé couché barre olympique", 4, 8, 80.0, 8, 58, false, "Bonne vitesse de barre"),
            WorkoutLogEntity(0, lucasId, now - 18 * dayMs, "Bas du Corps", "Squat barre nuque (Back Squat)", 4, 8, 100.0, 8, 62, false, "Amplitude complète"),
            WorkoutLogEntity(0, lucasId, now - 13 * dayMs, "Haut du Corps", "Développé couché barre olympique", 4, 8, 82.5, 8, 56, false, "+2,5 kg validés"),
            WorkoutLogEntity(0, lucasId, now - 11 * dayMs, "Bas du Corps", "Squat barre nuque (Back Squat)", 4, 8, 105.0, 9, 64, false, "Solide sur les 4 séries"),
            WorkoutLogEntity(0, lucasId, now - 6 * dayMs, "Haut du Corps", "Développé couché barre olympique", 4, 9, 85.0, 8, 59, true, "Nouveau PR à 85 kg !"),
            WorkoutLogEntity(0, lucasId, now - 4 * dayMs, "Bas du Corps", "Squat barre nuque (Back Squat)", 4, 8, 110.0, 8, 63, true, "PR 110 kg x 8 propre"),
            WorkoutLogEntity(0, lucasId, now - 2 * dayMs, "Haut du Corps", "Rowing barre buste penché (Pendlay / Yates)", 4, 10, 75.0, 8, 55, true, "Contraction dorsale nette")
        )
        dao.insertWorkoutLogs(lucasWorkouts)

        val camilleAssessments = listOf(
            AssessmentEntity(0, camilleId, now - 21 * dayMs, "Bilan Complet", 66.8, 24.1, 76.5, 92.0, 29.0, 56.2, 99.0, 1880, 138, 165, 68, 30, 2.2, "Début sèche sans lactose ni gluten"),
            AssessmentEntity(0, camilleId, now - 14 * dayMs, "Poids & Compo", 66.0, 23.6, 75.4, 91.8, 29.1, 55.8, 98.4, 1820, 142, 155, 65, 32, 2.5, "-0,8 kg stable"),
            AssessmentEntity(0, camilleId, now - 7 * dayMs, "Mensurations", 65.4, 23.2, 74.5, 91.5, 29.1, 55.4, 97.8, 1790, 144, 148, 63, 33, 2.6, "-2 cm tour de taille"),
            AssessmentEntity(0, camilleId, now - 1 * dayMs, "Bilan Complet", 64.8, 22.8, 73.8, 91.4, 29.2, 55.0, 97.2, 1810, 146, 152, 64, 34, 2.7, "Excellente tenue énergétique")
        )
        dao.insertAssessments(camilleAssessments)

        val camilleWorkouts = listOf(
            WorkoutLogEntity(0, camilleId, now - 16 * dayMs, "Full Body Haltères", "Goblet Squat haltère ou kettlebell", 4, 12, 20.0, 7, 50, false, ""),
            WorkoutLogEntity(0, camilleId, now - 9 * dayMs, "Full Body Haltères", "Hip Thrust barre olympique", 4, 12, 65.0, 8, 52, false, ""),
            WorkoutLogEntity(0, camilleId, now - 2 * dayMs, "Full Body Haltères", "Hip Thrust barre olympique", 4, 12, 72.5, 8, 54, true, "PR 72,5 kg x 12")
        )
        dao.insertWorkoutLogs(camilleWorkouts)

        val theoAssessments = listOf(
            AssessmentEntity(0, theoId, now - 14 * dayMs, "Bilan Complet", 66.9, 13.8, 75.0, 93.0, 31.5, 53.0, 91.0, 2650, 135, 360, 74, 32, 2.5, "Suivi croissance équilibré"),
            AssessmentEntity(0, theoId, now - 2 * dayMs, "Poids & Compo", 67.5, 13.5, 75.1, 93.8, 32.0, 53.6, 91.2, 2720, 140, 370, 76, 33, 2.7, "Prise de muscle propre")
        )
        dao.insertAssessments(theoAssessments)

        val theoWorkouts = listOf(
            WorkoutLogEntity(0, theoId, now - 10 * dayMs, "Full Body Athlétique", "Goblet Squat haltère ou kettlebell", 3, 12, 18.0, 7, 45, false, "Technique maîtrisée"),
            WorkoutLogEntity(0, theoId, now - 3 * dayMs, "Full Body Athlétique", "Pompes classiques au sol", 4, 15, 0.0, 7, 45, true, "15 reps propres")
        )
        dao.insertWorkoutLogs(theoWorkouts)

        if (dao.countCommunityPosts() == 0) {
            val p1 = dao.insertCommunityPost(
                CommunityPostEntity(
                    authorName = "Lucas Martin",
                    authorRole = "Pratiquant • Prise de masse",
                    category = "Progrès",
                    title = "Nouveau PR au Squat : 110 kg x 8 !",
                    content = "Après 3 semaines sur le split Haut/Bas du Coach Marc avec tempo 3-1-1-0, la barre est montée fluide sans dégrader la profondeur.",
                    highlightBadge = "PR +10 kg • RPE 8",
                    likesCount = 14,
                    createdAtMs = now - 2 * dayMs
                )
            )
            dao.insertCommunityComment(CommunityCommentEntity(0, p1, "Coach Marc Delacroix", "Super gainage en bas du mouvement Lucas ! On vise 112,5 kg la semaine prochaine.", now - 1 * dayMs))

            dao.insertCommunityPost(
                CommunityPostEntity(
                    authorName = "Camille Laurent",
                    authorRole = "Pratiquante • Sans lactose & gluten",
                    category = "Repas Sain",
                    title = "Bowl Saumon Grillé, Quinoa & Avocat",
                    content = "Recette post-training prête en 12 min : 150g saumon, 180g quinoa cuit, 60g avocat, edamame et citron vert. Zéro lactose, zéro gluten !",
                    highlightBadge = "545 kcal • 39g Protéines",
                    likesCount = 21,
                    createdAtMs = now - 3 * dayMs
                )
            )
            dao.insertCommunityPost(
                CommunityPostEntity(
                    authorName = "Coach Marc Delacroix",
                    authorRole = "Coach Certifié",
                    category = "Routine",
                    title = "Circuit HIIT Métabolique 10 Stations (40s/20s)",
                    content = "Routine collective testée ce matin : KB Swing, Thrusters, Battle Ropes, Burpees step-back, Bear Crawl, Mountain Climbers. 3 tours intenses avec 6 phases complètes.",
                    highlightBadge = "HIIT • 380 kcal est.",
                    likesCount = 19,
                    createdAtMs = now - 4 * dayMs
                )
            )

            dao.insertCommunityChallenges(
                listOf(
                    CommunityChallengeEntity(0, "Défi 30 Jours Volume Haut/Bas", "Club Force & Hypertrophie", "Compléter 16 séances structurées en 4 semaines avec suivi des charges.", "12 / 16 séances", 75, 48, true),
                    CommunityChallengeEntity(0, "Challenge 10 000 Pas + LISS Zone 2", "Cardio & Recomposition", "Accumuler 10 000 pas/jour et 2 sessions LISS de 40 min par semaine.", "18 / 30 jours", 60, 92, false),
                    CommunityChallengeEntity(0, "Défi Cuisine Protéinée Sans Ultra-Transformés", "Nutrition Performance", "Partager 5 idées de repas équilibrés avec grammages cru/cuit précis.", "3 / 5 recettes", 60, 64, true)
                )
            )
        }
    }
}
