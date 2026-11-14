package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {
    // Users & Auth
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun observeCoach(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // Clients
    @Query("SELECT * FROM clients ORDER BY id ASC")
    fun observeClients(): Flow<List<ClientEntity>>

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun countClients(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    // Exercises (250 catalog)
    @Query("SELECT * FROM exercises ORDER BY id ASC")
    fun observeAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countExercises(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    // Training Programs
    @Query("SELECT * FROM training_programs WHERE clientId = :clientId ORDER BY createdAtMs DESC")
    fun observeProgramsForClient(clientId: Long): Flow<List<TrainingProgramEntity>>

    @Query("SELECT * FROM training_day_exercises WHERE programId = :programId ORDER BY dayIndex ASC, orderIndex ASC")
    fun observeProgramExercises(programId: Long): Flow<List<TrainingDayExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingProgram(program: TrainingProgramEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingDayExercises(items: List<TrainingDayExerciseEntity>)

    @Update
    suspend fun updateTrainingDayExercise(item: TrainingDayExerciseEntity)

    // Generated Workout Sessions & Rows
    @Query("SELECT * FROM workout_sessions WHERE clientId = :clientId ORDER BY createdAtMs DESC")
    fun observeSessionsForClient(clientId: Long): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_session_rows WHERE sessionId = :sessionId ORDER BY rowNumber ASC")
    fun observeSessionRows(sessionId: Long): Flow<List<WorkoutSessionRowEntity>>

    @Query("SELECT * FROM workout_session_rows WHERE sessionId = :sessionId ORDER BY rowNumber ASC")
    suspend fun getSessionRowsOnce(sessionId: Long): List<WorkoutSessionRowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSessionRows(rows: List<WorkoutSessionRowEntity>)

    // Nutrition Programs & Meals
    @Query("SELECT * FROM nutrition_programs WHERE clientId = :clientId ORDER BY updatedAtMs DESC LIMIT 1")
    fun observeNutritionProgram(clientId: Long): Flow<NutritionProgramEntity?>

    @Query("SELECT * FROM meals WHERE nutritionProgramId = :programId ORDER BY orderIndex ASC")
    fun observeMeals(programId: Long): Flow<List<MealEntity>>

    @Query("SELECT * FROM meal_items WHERE mealId IN (SELECT id FROM meals WHERE nutritionProgramId = :programId)")
    fun observeMealItemsForProgram(programId: Long): Flow<List<MealItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionProgram(program: NutritionProgramEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealItems(items: List<MealItemEntity>)

    @Query("DELETE FROM nutrition_programs WHERE clientId = :clientId")
    suspend fun deleteNutritionProgramsForClient(clientId: Long)

    @Query("DELETE FROM meal_items WHERE id = :itemId")
    suspend fun deleteMealItem(itemId: Long)

    // Foods
    @Query("SELECT * FROM foods ORDER BY category ASC, name ASC")
    fun observeAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun countFoods(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    // Supplements
    @Query("SELECT * FROM supplements WHERE clientId = :clientId ORDER BY id ASC")
    fun observeSupplements(clientId: Long): Flow<List<SupplementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: SupplementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(supplements: List<SupplementEntity>)

    @Update
    suspend fun updateSupplement(supplement: SupplementEntity)

    @Delete
    suspend fun deleteSupplement(supplement: SupplementEntity)

    // Weekly Schedule
    @Query("SELECT * FROM schedules WHERE clientId = :clientId ORDER BY dayOfWeek ASC, startTime ASC")
    fun observeSchedules(clientId: Long): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    // Progress Tracking: Assessments & Workout Logs
    @Query("SELECT * FROM assessments WHERE clientId = :clientId ORDER BY dateEpochMs ASC")
    fun observeAssessments(clientId: Long): Flow<List<AssessmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessments(assessments: List<AssessmentEntity>)

    @Delete
    suspend fun deleteAssessment(assessment: AssessmentEntity)

    @Query("SELECT * FROM workout_logs WHERE clientId = :clientId ORDER BY dateEpochMs ASC")
    fun observeWorkoutLogs(clientId: Long): Flow<List<WorkoutLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLogs(logs: List<WorkoutLogEntity>)

    @Delete
    suspend fun deleteWorkoutLog(log: WorkoutLogEntity)

    // Community Feed, Comments & Challenges
    @Query("SELECT * FROM community_posts ORDER BY createdAtMs DESC")
    fun observeCommunityPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT COUNT(*) FROM community_posts")
    suspend fun countCommunityPosts(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPost(post: CommunityPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPosts(posts: List<CommunityPostEntity>)

    @Update
    suspend fun updateCommunityPost(post: CommunityPostEntity)

    @Query("SELECT * FROM community_comments ORDER BY createdAtMs ASC")
    fun observeAllCommunityComments(): Flow<List<CommunityCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityComment(comment: CommunityCommentEntity): Long

    @Query("SELECT * FROM community_challenges ORDER BY id ASC")
    fun observeCommunityChallenges(): Flow<List<CommunityChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityChallenge(challenge: CommunityChallengeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityChallenges(challenges: List<CommunityChallengeEntity>)

    @Update
    suspend fun updateCommunityChallenge(challenge: CommunityChallengeEntity)
}
