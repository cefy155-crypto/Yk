package com.example.data.repository

import com.example.data.db.AssessmentEntity
import com.example.data.db.ClientEntity
import com.example.data.db.CoachDao
import com.example.data.db.CommunityChallengeEntity
import com.example.data.db.CommunityCommentEntity
import com.example.data.db.CommunityPostEntity
import com.example.data.db.ExerciseEntity
import com.example.data.db.FoodEntity
import com.example.data.db.MealItemEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.ScheduleEntity
import com.example.data.db.SupplementEntity
import com.example.data.db.TrainingDayExerciseEntity
import com.example.data.db.TrainingProgramEntity
import com.example.data.db.WorkoutLogEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.db.WorkoutSessionRowEntity
import com.example.data.seed.FoodAndSupplementCatalog
import com.example.domain.engines.GymSplitEngine
import com.example.domain.engines.NutritionEngine
import com.example.domain.engines.WorkoutGeneratorEngine
import kotlinx.coroutines.flow.Flow

class SportCoachRepository(private val dao: CoachDao) {

    val clients: Flow<List<ClientEntity>> = dao.observeClients()
    val exercises: Flow<List<ExerciseEntity>> = dao.observeAllExercises()
    val foods: Flow<List<FoodEntity>> = dao.observeAllFoods()

    fun observePrograms(clientId: Long): Flow<List<TrainingProgramEntity>> = dao.observeProgramsForClient(clientId)
    fun observeProgramExercises(programId: Long): Flow<List<TrainingDayExerciseEntity>> = dao.observeProgramExercises(programId)

    fun observeSessions(clientId: Long): Flow<List<WorkoutSessionEntity>> = dao.observeSessionsForClient(clientId)
    fun observeSessionRows(sessionId: Long): Flow<List<WorkoutSessionRowEntity>> = dao.observeSessionRows(sessionId)

    fun observeNutritionProgram(clientId: Long): Flow<NutritionProgramEntity?> = dao.observeNutritionProgram(clientId)
    fun observeMeals(programId: Long) = dao.observeMeals(programId)
    fun observeMealItems(programId: Long): Flow<List<MealItemEntity>> = dao.observeMealItemsForProgram(programId)

    fun observeSupplements(clientId: Long): Flow<List<SupplementEntity>> = dao.observeSupplements(clientId)
    fun observeSchedules(clientId: Long): Flow<List<ScheduleEntity>> = dao.observeSchedules(clientId)

    fun observeAssessments(clientId: Long): Flow<List<AssessmentEntity>> = dao.observeAssessments(clientId)
    fun observeWorkoutLogs(clientId: Long): Flow<List<WorkoutLogEntity>> = dao.observeWorkoutLogs(clientId)

    suspend fun saveClient(client: ClientEntity): Long {
        val id = dao.insertClient(client)
        val savedClient = client.copy(id = id)
        if (client.id == 0L) {
            val supps = FoodAndSupplementCatalog.defaultSupplementsForClient(
                clientId = id,
                hasLactoseAllergy = client.allergiesCsv.contains("lactose", true),
                isMinor = client.isMinor
            )
            dao.insertSupplements(supps)
        }
        return id
    }

    suspend fun updateClient(client: ClientEntity) = dao.updateClient(client)

    suspend fun generateAndSaveSession(
        client: ClientEntity,
        sessionType: String,
        focusRegion: String,
        durationMinutes: Int,
        allExercises: List<ExerciseEntity>,
        customMainCount: Int? = null
    ): Long {
        val (session, rows) = WorkoutGeneratorEngine.generateFullSession(
            client = client,
            sessionType = sessionType,
            focusRegion = focusRegion,
            durationMinutes = durationMinutes,
            allExercises = allExercises,
            customMainCount = customMainCount
        )
        val id = dao.insertWorkoutSession(session)
        dao.insertWorkoutSessionRows(rows.map { it.copy(sessionId = id) })
        return id
    }

    suspend fun saveCustomCircuitSession(
        client: ClientEntity,
        title: String,
        roundsCount: Int,
        workSeconds: Int,
        restSeconds: Int,
        stationExercises: List<ExerciseEntity>,
        allExercises: List<ExerciseEntity>
    ): Long {
        // Respecte les 6 phases même pour un circuit personnalisé !
        val (_, baseRows) = WorkoutGeneratorEngine.generateFullSession(
            client = client,
            sessionType = "Circuit",
            focusRegion = "Full Body",
            durationMinutes = ((roundsCount * stationExercises.size * (workSeconds + restSeconds)) / 60 + 22).coerceAtLeast(30),
            allExercises = allExercises,
            customMainCount = stationExercises.size.coerceAtLeast(10)
        )
        // Remplacer la partie principale par les stations choisies (complétées à min 10 si besoin)
        val prePhases = baseRows.filter { it.phase.startsWith("1.") || it.phase.startsWith("2.") || it.phase.startsWith("3.") }
        val postPhases = baseRows.filter { it.phase.startsWith("5.") || it.phase.startsWith("6.") }
        val paddedStations = if (stationExercises.size >= 10) stationExercises else {
            val extras = allExercises.filter { ex -> stationExercises.none { it.code == ex.code } && ex.category == "Cardio / HIIT" }
            stationExercises + extras.take(10 - stationExercises.size)
        }

        val renumbered = mutableListOf<WorkoutSessionRowEntity>()
        var idx = 1
        prePhases.forEach { renumbered += it.copy(rowNumber = idx++) }
        paddedStations.forEach { ex ->
            renumbered += WorkoutSessionRowEntity(
                sessionId = 0,
                rowNumber = idx++,
                phase = "4. Partie principale (Circuit ${paddedStations.size} stations)",
                exerciseName = ex.name,
                region = ex.region,
                sets = "$roundsCount tours",
                reps = "$workSeconds s effort",
                durationText = "$workSeconds s",
                restText = "$restSeconds s",
                executionCues = ex.executionCues,
                durationSecondsEstimate = workSeconds
            )
        }
        postPhases.forEach { renumbered += it.copy(rowNumber = idx++) }

        val sessionId = dao.insertWorkoutSession(
            WorkoutSessionEntity(
                clientId = client.id,
                title = title.ifBlank { "Circuit Personnalisé" },
                sessionType = "Circuit",
                focusRegion = "Full Body",
                durationMinutes = (renumbered.sumOf { it.durationSecondsEstimate } / 60).coerceAtLeast(30),
                roundsCount = roundsCount,
                workSeconds = workSeconds,
                restSeconds = restSeconds
            )
        )
        dao.insertWorkoutSessionRows(renumbered.map { it.copy(sessionId = sessionId) })
        return sessionId
    }

    suspend fun generateAndSaveProgram(
        client: ClientEntity,
        sessionsPerWeek: Int,
        splitType: String,
        allExercises: List<ExerciseEntity>
    ): Long {
        val (prog, items) = GymSplitEngine.generateProgram(client, sessionsPerWeek, splitType, allExercises)
        val progId = dao.insertTrainingProgram(prog)
        dao.insertTrainingDayExercises(items.map { it.copy(programId = progId) })
        return progId
    }

    suspend fun updateProgramDayExercise(item: TrainingDayExerciseEntity) = dao.updateTrainingDayExercise(item)

    suspend fun regenerateNutritionPlan(
        client: ClientEntity,
        strategy: String,
        supplements: List<SupplementEntity>,
        foods: List<FoodEntity>,
        customP: Int = 30,
        customC: Int = 45,
        customF: Int = 25
    ) {
        val bundle = NutritionEngine.generateNutritionPlan(client, strategy, supplements, foods, customP, customC, customF)
        dao.deleteNutritionProgramsForClient(client.id)
        val nutId = dao.insertNutritionProgram(bundle.program)
        bundle.meals.forEach { draft ->
            val mealId = dao.insertMeal(draft.meal.copy(nutritionProgramId = nutId))
            dao.insertMealItems(draft.items.map { it.copy(mealId = mealId) })
        }
    }

    suspend fun addMealItem(mealId: Long, food: FoodEntity, grams: Int, prepState: String) {
        val ratio = grams / 100.0
        dao.insertMealItems(
            listOf(
                MealItemEntity(
                    mealId = mealId,
                    foodId = food.id,
                    foodName = food.name,
                    grams = grams,
                    prepState = prepState,
                    kcal = (food.kcalPer100g * ratio).toInt(),
                    proteinG = ((food.proteinPer100g * ratio) * 10.0).toInt() / 10.0,
                    carbsG = ((food.carbsPer100g * ratio) * 10.0).toInt() / 10.0,
                    fatG = ((food.fatPer100g * ratio) * 10.0).toInt() / 10.0,
                    fiberG = ((food.fiberPer100g * ratio) * 10.0).toInt() / 10.0
                )
            )
        )
    }

    suspend fun removeMealItem(itemId: Long) = dao.deleteMealItem(itemId)

    suspend fun addSupplement(supplement: SupplementEntity) = dao.insertSupplement(supplement)
    suspend fun updateSupplement(supplement: SupplementEntity) = dao.updateSupplement(supplement)
    suspend fun deleteSupplement(supplement: SupplementEntity) = dao.deleteSupplement(supplement)

    suspend fun addSchedule(schedule: ScheduleEntity) = dao.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: ScheduleEntity) = dao.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleEntity) = dao.deleteSchedule(schedule)

    // Progress Tracking
    suspend fun addAssessment(assessment: AssessmentEntity) {
        dao.insertAssessment(assessment)
        // Synchroniser poids et % MG sur le dossier client si renseignés
        // (géré au niveau ViewModel si souhaité)
    }

    suspend fun deleteAssessment(assessment: AssessmentEntity) = dao.deleteAssessment(assessment)

    suspend fun addWorkoutLog(log: WorkoutLogEntity) = dao.insertWorkoutLog(log)
    suspend fun deleteWorkoutLog(log: WorkoutLogEntity) = dao.deleteWorkoutLog(log)

    // Community
    val communityPosts = dao.observeCommunityPosts()
    val communityComments = dao.observeAllCommunityComments()
    val communityChallenges = dao.observeCommunityChallenges()

    suspend fun createCommunityPost(post: CommunityPostEntity) = dao.insertCommunityPost(post)
    suspend fun toggleLikePost(post: CommunityPostEntity) {
        val nextLiked = !post.isLikedByMe
        val nextCount = if (nextLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        dao.updateCommunityPost(post.copy(isLikedByMe = nextLiked, likesCount = nextCount))
    }
    suspend fun addCommunityComment(comment: CommunityCommentEntity) = dao.insertCommunityComment(comment)
    suspend fun createChallenge(challenge: CommunityChallengeEntity) = dao.insertCommunityChallenge(challenge)
    suspend fun toggleJoinChallenge(challenge: CommunityChallengeEntity) {
        val joined = !challenge.isJoined
        val count = if (joined) challenge.membersCount + 1 else (challenge.membersCount - 1).coerceAtLeast(1)
        dao.updateCommunityChallenge(challenge.copy(isJoined = joined, membersCount = count))
    }
}
