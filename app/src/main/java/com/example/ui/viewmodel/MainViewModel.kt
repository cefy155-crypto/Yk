package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.AssessmentEntity
import com.example.data.db.ClientEntity
import com.example.data.db.CommunityChallengeEntity
import com.example.data.db.CommunityCommentEntity
import com.example.data.db.CommunityPostEntity
import com.example.data.db.DatabaseSeeder
import com.example.data.db.ExerciseEntity
import com.example.data.db.FoodEntity
import com.example.data.db.MealEntity
import com.example.data.db.MealItemEntity
import com.example.data.db.NutritionProgramEntity
import com.example.data.db.ScheduleEntity
import com.example.data.db.SupplementEntity
import com.example.data.db.TrainingDayExerciseEntity
import com.example.data.db.TrainingProgramEntity
import com.example.data.db.UserEntity
import com.example.data.db.WorkoutLogEntity
import com.example.data.db.WorkoutSessionEntity
import com.example.data.db.WorkoutSessionRowEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthResult
import com.example.data.repository.SportCoachRepository
import com.example.domain.engines.AiActionType
import com.example.domain.engines.AiAssistantReply
import com.example.domain.engines.AiCoachRepository
import com.example.domain.engines.DocumentExporter
import com.example.domain.engines.ExportFormat
import com.example.domain.engines.ExportScope
import com.example.domain.engines.PlaceholderAiCoachRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LiveCoachState(
    val isActive: Boolean = false,
    val sessionTitle: String = "",
    val rows: List<WorkoutSessionRowEntity> = emptyList(),
    val currentIndex: Int = 0,
    val secondsRemaining: Int = 45,
    val totalStepSeconds: Int = 45,
    val isRunning: Boolean = false,
    val isRestPhase: Boolean = false,
    val soundEnabled: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val dao = db.coachDao()
    private val repo = SportCoachRepository(dao)
    private val authRepo = AuthRepository(application, dao)
    private val aiRepo: AiCoachRepository = PlaceholderAiCoachRepository()

    private val _isDbReady = MutableStateFlow(false)
    val isDbReady: StateFlow<Boolean> = _isDbReady.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    val clients: StateFlow<List<ClientEntity>> = repo.clients.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _selectedClientId = MutableStateFlow<Long?>(null)
    val activeClient: StateFlow<ClientEntity?> = combine(clients, _selectedClientId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allExercises: StateFlow<List<ExerciseEntity>> = repo.exercises.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allFoods: StateFlow<List<FoodEntity>> = repo.foods.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val sessions: StateFlow<List<WorkoutSessionEntity>> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observeSessions(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    val activeSession: StateFlow<WorkoutSessionEntity?> = combine(sessions, _selectedSessionId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSessionRows: StateFlow<List<WorkoutSessionRowEntity>> = activeSession.flatMapLatest { s ->
        if (s == null) flowOf(emptyList()) else repo.observeSessionRows(s.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val programs: StateFlow<List<TrainingProgramEntity>> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observePrograms(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProgram: StateFlow<TrainingProgramEntity?> = programs.flatMapLatest { list ->
        flowOf(list.firstOrNull())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeProgramExercises: StateFlow<List<TrainingDayExerciseEntity>> = activeProgram.flatMapLatest { p ->
        if (p == null) flowOf(emptyList()) else repo.observeProgramExercises(p.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNutritionProgram: StateFlow<NutritionProgramEntity?> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(null) else repo.observeNutritionProgram(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMeals: StateFlow<List<MealEntity>> = activeNutritionProgram.flatMapLatest { np ->
        if (np == null) flowOf(emptyList()) else repo.observeMeals(np.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeMealItems: StateFlow<List<MealItemEntity>> = activeNutritionProgram.flatMapLatest { np ->
        if (np == null) flowOf(emptyList()) else repo.observeMealItems(np.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSupplements: StateFlow<List<SupplementEntity>> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observeSupplements(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSchedules: StateFlow<List<ScheduleEntity>> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observeSchedules(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAssessments: StateFlow<List<AssessmentEntity>> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observeAssessments(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkoutLogs: StateFlow<List<WorkoutLogEntity>> = activeClient.flatMapLatest { c ->
        if (c == null) flowOf(emptyList()) else repo.observeWorkoutLogs(c.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repo.communityPosts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val communityComments: StateFlow<List<CommunityCommentEntity>> = repo.communityComments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val communityChallenges: StateFlow<List<CommunityChallengeEntity>> = repo.communityChallenges.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _liveCoach = MutableStateFlow(LiveCoachState())
    val liveCoach: StateFlow<LiveCoachState> = _liveCoach.asStateFlow()
    private var timerJob: Job? = null

    private val _aiReply = MutableStateFlow<AiAssistantReply?>(null)
    val aiReply: StateFlow<AiAssistantReply?> = _aiReply.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                DatabaseSeeder.ensureSeeded(db)
            }
            _isDbReady.value = true
        }
    }

    fun clearStatusBanner() { _statusBanner.value = null }
    fun clearAuthError() { _authError.value = null }
    fun selectClient(clientId: Long) { _selectedClientId.value = clientId }

    // --- Authentication & Profile ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            when (val res = authRepo.login(email, password)) {
                is AuthResult.Success -> _authError.value = null
                is AuthResult.Error -> _authError.value = res.message
            }
        }
    }

    fun loginDemoCoach() {
        viewModelScope.launch {
            when (val res = authRepo.loginDemoCoach()) {
                is AuthResult.Success -> _authError.value = null
                is AuthResult.Error -> _authError.value = res.message
            }
        }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        role: String,
        age: Int,
        sex: String,
        heightCm: Int,
        weightKg: Double,
        fitnessLevel: String,
        fitnessGoal: String,
        weeklyTarget: Int
    ) {
        viewModelScope.launch {
            val res = authRepo.signUp(
                fullName, email, password, role, age, sex,
                heightCm, weightKg, fitnessLevel, fitnessGoal, weeklyTarget
            )
            when (res) {
                is AuthResult.Success -> {
                    _authError.value = null
                    // Si c'est un pratiquant, créer aussi son dossier suivi client lié
                    if (role == "Pratiquant") {
                        val newClientId = repo.saveClient(
                            ClientEntity(
                                fullName = fullName.trim(),
                                age = age,
                                sex = sex,
                                heightCm = heightCm,
                                weightKg = weightKg,
                                bodyFatPct = 18.0,
                                level = fitnessLevel,
                                goal = fitnessGoal,
                                activityLevel = "Actif",
                                availableEquipment = "Salle complète",
                                medicalConstraints = "Aucune",
                                allergiesCsv = "",
                                weeklySessionsTarget = weeklyTarget
                            )
                        )
                        _selectedClientId.value = newClientId
                    }
                    _statusBanner.value = "Bienvenue ${res.user.fullName} ! Profil créé."
                }
                is AuthResult.Error -> _authError.value = res.message
            }
        }
    }

    fun updateProfile(updated: UserEntity) {
        viewModelScope.launch {
            authRepo.updateProfile(updated)
            _statusBanner.value = "Profil mis à jour avec succès."
        }
    }

    fun logout() {
        stopLiveCoach()
        authRepo.logout()
    }

    // --- Clients ---
    fun addClient(client: ClientEntity) {
        viewModelScope.launch {
            val id = repo.saveClient(client)
            val created = client.copy(id = id)
            val exList = allExercises.value
            val foodList = allFoods.value
            if (exList.isNotEmpty()) {
                repo.generateAndSaveSession(created, "Complète 6 Phases", "Full Body", 60, exList)
                repo.generateAndSaveProgram(created, created.weeklySessionsTarget, "Haut / Bas", exList)
            }
            if (foodList.isNotEmpty()) {
                repo.regenerateNutritionPlan(created, "Équilibrée", emptyList(), foodList)
            }
            _selectedClientId.value = id
            _statusBanner.value = "Pratiquant ${client.fullName} ajouté avec programmes initialisés."
        }
    }

    // --- Sessions & Circuits ---
    fun generateSession(sessionType: String, focusRegion: String, durationMinutes: Int, customMainCount: Int? = null) {
        val client = activeClient.value ?: return
        val exList = allExercises.value
        if (exList.isEmpty()) return
        viewModelScope.launch {
            val sid = repo.generateAndSaveSession(client, sessionType, focusRegion, durationMinutes, exList, customMainCount)
            _selectedSessionId.value = sid
            _statusBanner.value = "Séance générée ($sessionType • $focusRegion)."
        }
    }

    fun createCircuitSession(title: String, rounds: Int, workSec: Int, restSec: Int, stations: List<ExerciseEntity>) {
        val client = activeClient.value ?: return
        val exList = allExercises.value
        viewModelScope.launch {
            val sid = repo.saveCustomCircuitSession(client, title, rounds, workSec, restSec, stations, exList)
            _selectedSessionId.value = sid
            _statusBanner.value = "Circuit '$title' ($rounds tours) enregistré."
        }
    }

    fun selectSession(sessionId: Long) { _selectedSessionId.value = sessionId }

    // --- Gym Training Program ---
    fun generateGymProgram(sessionsPerWeek: Int, splitType: String) {
        val client = activeClient.value ?: return
        val exList = allExercises.value
        if (exList.isEmpty()) return
        viewModelScope.launch {
            repo.generateAndSaveProgram(client, sessionsPerWeek, splitType, exList)
            _statusBanner.value = "Programme $splitType ($sessionsPerWeek j/sem.) généré."
        }
    }

    fun bumpExerciseTargetLoad(item: TrainingDayExerciseEntity, deltaKg: Double) {
        viewModelScope.launch {
            val nextLoad = (item.targetLoadKg + deltaKg).coerceAtLeast(0.0)
            repo.updateProgramDayExercise(item.copy(targetLoadKg = nextLoad))
            _statusBanner.value = "Charge cible ajustée : ${item.exerciseName} → $nextLoad kg"
        }
    }

    // --- Nutrition & Supplements ---
    fun applyNutritionStrategy(strategy: String, customP: Int = 30, customC: Int = 45, customF: Int = 25) {
        val client = activeClient.value ?: return
        viewModelScope.launch {
            repo.regenerateNutritionPlan(client, strategy, activeSupplements.value, allFoods.value, customP, customC, customF)
            _statusBanner.value = if (client.hasRestrictiveDietBan && strategy in setOf("Low Carb", "Keto", "Intermittent Fasting")) {
                "Sécurité active : stratégie basculée sur Équilibrée."
            } else {
                "Plan nutrition '$strategy' recalculé."
            }
        }
    }

    fun addFoodToMeal(mealId: Long, food: FoodEntity, grams: Int, prepState: String) {
        viewModelScope.launch {
            repo.addMealItem(mealId, food, grams, prepState)
            _statusBanner.value = "${food.name} ($grams g $prepState) ajouté au repas."
        }
    }

    fun removeMealItem(itemId: Long) {
        viewModelScope.launch { repo.removeMealItem(itemId) }
    }

    fun addSupplement(supp: SupplementEntity) {
        val client = activeClient.value ?: return
        viewModelScope.launch {
            repo.addSupplement(supp.copy(clientId = client.id))
            _statusBanner.value = "${supp.name} ajouté au Supplement Planner."
        }
    }

    fun toggleSupplementInTotals(supp: SupplementEntity) {
        viewModelScope.launch {
            repo.updateSupplement(supp.copy(includedInTotals = !supp.includedInTotals))
        }
    }

    fun deleteSupplement(supp: SupplementEntity) {
        viewModelScope.launch { repo.deleteSupplement(supp) }
    }

    // --- Weekly Schedule ---
    fun addScheduleItem(title: String, activityType: String, dayOfWeek: Int, startTime: String, durationMin: Int, notes: String) {
        val client = activeClient.value ?: return
        viewModelScope.launch {
            repo.addSchedule(
                ScheduleEntity(
                    clientId = client.id,
                    title = title,
                    activityType = activityType,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    durationMin = durationMin,
                    sharedNotes = notes,
                    isCompleted = false
                )
            )
            _statusBanner.value = "Créneau planifié au calendrier."
        }
    }

    fun toggleScheduleCompleted(item: ScheduleEntity) {
        viewModelScope.launch { repo.updateSchedule(item.copy(isCompleted = !item.isCompleted)) }
    }

    fun deleteSchedule(item: ScheduleEntity) {
        viewModelScope.launch { repo.deleteSchedule(item) }
    }

    // --- Progress Tracking Module ---
    fun logWeightAndBodyFat(weightKg: Double, bodyFatPct: Double?, notes: String) {
        val client = activeClient.value ?: return
        viewModelScope.launch {
            repo.addAssessment(
                AssessmentEntity(
                    clientId = client.id,
                    dateEpochMs = System.currentTimeMillis(),
                    entryType = "Poids & Compo",
                    weightKg = weightKg,
                    bodyFatPct = bodyFatPct,
                    waistCm = null, chestCm = null, armCm = null, thighCm = null, hipCm = null,
                    caloriesConsumed = null, proteinConsumedG = null, carbsConsumedG = null,
                    fatConsumedG = null, fiberConsumedG = null, hydrationLiters = null,
                    notes = notes
                )
            )
            repo.updateClient(client.copy(weightKg = weightKg, bodyFatPct = bodyFatPct ?: client.bodyFatPct))
            _statusBanner.value = "Pesée enregistrée : $weightKg kg"
        }
    }

    fun logMeasurements(waistCm: Double?, chestCm: Double?, armCm: Double?, thighCm: Double?, hipCm: Double?, notes: String) {
        val client = activeClient.value ?: return
        viewModelScope.launch {
            repo.addAssessment(
                AssessmentEntity(
                    clientId = client.id,
                    dateEpochMs = System.currentTimeMillis(),
                    entryType = "Mensurations",
                    weightKg = null, bodyFatPct = null,
                    waistCm = waistCm, chestCm = chestCm, armCm = armCm, thighCm = thighCm, hipCm = hipCm,
                    caloriesConsumed = null, proteinConsumedG = null, carbsConsumedG = null,
                    fatConsumedG = null, fiberConsumedG = null, hydrationLiters = null,
                    notes = notes
                )
            )
            _statusBanner.value = "Mensurations enregistrées."
        }
    }

    fun logNutritionIntake(kcal: Int, proteinG: Int, carbsG: Int, fatG: Int, fiberG: Int, hydrationL: Double, notes: String) {
        val client = activeClient.value ?: return
        viewModelScope.launch {
            repo.addAssessment(
                AssessmentEntity(
                    clientId = client.id,
                    dateEpochMs = System.currentTimeMillis(),
                    entryType = "Nutrition",
                    weightKg = null, bodyFatPct = null,
                    waistCm = null, chestCm = null, armCm = null, thighCm = null, hipCm = null,
                    caloriesConsumed = kcal, proteinConsumedG = proteinG, carbsConsumedG = carbsG,
                    fatConsumedG = fatG, fiberConsumedG = fiberG, hydrationLiters = hydrationL,
                    notes = notes
                )
            )
            _statusBanner.value = "Apport nutritionnel logué ($kcal kcal)."
        }
    }

    fun logWorkoutPerformance(
        sessionTitle: String,
        exerciseName: String,
        sets: Int,
        reps: Int,
        loadKg: Double,
        rpe: Int,
        durationMin: Int,
        notes: String
    ) {
        val client = activeClient.value ?: return
        val previousBest = activeWorkoutLogs.value
            .filter { it.exerciseName.equals(exerciseName, ignoreCase = true) }
            .maxOfOrNull { it.estimatedOneRmKg } ?: 0.0
        val newEst = if (reps <= 1) loadKg else loadKg * (1.0 + reps / 30.0)
        val isPr = loadKg > 0.0 && newEst > previousBest

        viewModelScope.launch {
            repo.addWorkoutLog(
                WorkoutLogEntity(
                    clientId = client.id,
                    dateEpochMs = System.currentTimeMillis(),
                    sessionTitle = sessionTitle,
                    exerciseName = exerciseName,
                    setsCompleted = sets,
                    repsCompleted = reps,
                    loadKg = loadKg,
                    rpe = rpe,
                    durationMin = durationMin,
                    isPr = isPr,
                    notes = notes
                )
            )
            _statusBanner.value = if (isPr) "Nouveau Record Personnel (PR) sur $exerciseName !" else "Séance enregistrée dans le suivi."
        }
    }

    fun deleteAssessment(item: AssessmentEntity) {
        viewModelScope.launch {
            repo.deleteAssessment(item)
            _statusBanner.value = "Mesure supprimée."
        }
    }

    fun deleteWorkoutLog(log: WorkoutLogEntity) {
        viewModelScope.launch {
            repo.deleteWorkoutLog(log)
            _statusBanner.value = "Log d'entraînement supprimé."
        }
    }

    // --- Live Coach Mode ---
    fun startLiveCoach(sessionTitle: String, rows: List<WorkoutSessionRowEntity>) {
        if (rows.isEmpty()) return
        val firstSec = rows.first().durationSecondsEstimate.coerceAtLeast(15)
        _liveCoach.value = LiveCoachState(
            isActive = true,
            sessionTitle = sessionTitle,
            rows = rows,
            currentIndex = 0,
            secondsRemaining = firstSec,
            totalStepSeconds = firstSec,
            isRunning = true,
            isRestPhase = false
        )
        runTimerLoop()
    }

    fun toggleLiveCoachPause() {
        val cur = _liveCoach.value
        val nextRunning = !cur.isRunning
        _liveCoach.value = cur.copy(isRunning = nextRunning)
        if (nextRunning) runTimerLoop() else timerJob?.cancel()
    }

    fun toggleLiveCoachSound() {
        _liveCoach.value = _liveCoach.value.copy(soundEnabled = !_liveCoach.value.soundEnabled)
    }

    fun liveCoachNext() {
        val cur = _liveCoach.value
        val nextIdx = cur.currentIndex + 1
        if (nextIdx >= cur.rows.size) {
            stopLiveCoach()
            _statusBanner.value = "Séance terminée ! Bravo !"
            return
        }
        val sec = cur.rows[nextIdx].durationSecondsEstimate.coerceAtLeast(15)
        _liveCoach.value = cur.copy(
            currentIndex = nextIdx,
            secondsRemaining = sec,
            totalStepSeconds = sec,
            isRestPhase = false
        )
        playBeep(ToneGenerator.TONE_PROP_BEEP2)
    }

    fun liveCoachPrev() {
        val cur = _liveCoach.value
        val prevIdx = (cur.currentIndex - 1).coerceAtLeast(0)
        val sec = cur.rows[prevIdx].durationSecondsEstimate.coerceAtLeast(15)
        _liveCoach.value = cur.copy(
            currentIndex = prevIdx,
            secondsRemaining = sec,
            totalStepSeconds = sec,
            isRestPhase = false
        )
    }

    fun stopLiveCoach() {
        timerJob?.cancel()
        _liveCoach.value = LiveCoachState(isActive = false)
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_liveCoach.value.isActive && _liveCoach.value.isRunning) {
                delay(1000L)
                val cur = _liveCoach.value
                if (!cur.isActive || !cur.isRunning) break
                val rem = cur.secondsRemaining - 1
                if (rem in 1..3) {
                    playBeep(ToneGenerator.TONE_PROP_BEEP)
                }
                if (rem <= 0) {
                    liveCoachNext()
                } else {
                    _liveCoach.value = cur.copy(secondsRemaining = rem)
                }
            }
        }
    }

    private fun playBeep(tone: Int) {
        if (!_liveCoach.value.soundEnabled) return
        runCatching {
            val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
            tg.startTone(tone, 160)
        }
    }

    // --- Assistant Intelligent ---
    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiReply.value = aiRepo.processNaturalCommand(prompt, activeClient.value)
            _isAiLoading.value = false
        }
    }

    fun executeAiAction(actionType: AiActionType) {
        when (actionType) {
            AiActionType.GENERATE_FULL_BODY_SESSION -> generateSession("Complète 6 Phases", "Full Body", 60, 13)
            AiActionType.APPLY_HIGH_PROTEIN_MACROS -> applyNutritionStrategy("High Protein")
            AiActionType.SWITCH_SPLIT_UPPER_LOWER -> generateGymProgram(4, "Haut / Bas")
            AiActionType.CREATE_HIIT_CIRCUIT -> generateSession("HIIT", "HIIT / Métabolique", 40, 10)
        }
    }

    // --- Export Multi-Formats ---
    fun exportDocument(context: Context, format: ExportFormat, scope: ExportScope) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = DocumentExporter.exportAndShare(
                context = context,
                format = format,
                scope = scope,
                client = activeClient.value,
                session = activeSession.value,
                sessionRows = activeSessionRows.value,
                programTitle = activeProgram.value?.title ?: "Programme Musculation",
                programRows = activeProgramExercises.value,
                nutrition = activeNutritionProgram.value,
                mealItems = activeMealItems.value,
                assessments = activeAssessments.value,
                workoutLogs = activeWorkoutLogs.value
            )
            withContext(Dispatchers.Main) {
                _statusBanner.value = res.fold(
                    onSuccess = { "Export ${format.label} prêt : ${it.name}" },
                    onFailure = { "Erreur d'export : ${it.localizedMessage}" }
                )
            }
        }
    }

    // --- Community ---
    fun publishCommunityPost(category: String, title: String, content: String, badge: String) {
        val user = currentUser.value
        viewModelScope.launch {
            repo.createCommunityPost(
                CommunityPostEntity(
                    authorName = user?.fullName ?: "Coach",
                    authorRole = "${user?.role ?: "Coach"} • ${user?.fitnessGoal ?: "Performance"}",
                    category = category,
                    title = title.trim(),
                    content = content.trim(),
                    highlightBadge = badge.trim().ifBlank { category }
                )
            )
            _statusBanner.value = "Publication partagée avec la communauté !"
        }
    }

    fun toggleLikePost(post: CommunityPostEntity) {
        viewModelScope.launch { repo.toggleLikePost(post) }
    }

    fun addCommunityComment(postId: Long, text: String) {
        if (text.isBlank()) return
        val user = currentUser.value
        viewModelScope.launch {
            repo.addCommunityComment(
                CommunityCommentEntity(
                    postId = postId,
                    authorName = user?.fullName ?: "Coach",
                    text = text.trim()
                )
            )
        }
    }

    fun createChallenge(title: String, groupName: String, description: String, targetLabel: String) {
        viewModelScope.launch {
            repo.createChallenge(
                CommunityChallengeEntity(
                    title = title.trim(),
                    groupName = groupName.trim(),
                    description = description.trim(),
                    targetLabel = targetLabel.trim(),
                    currentProgressPct = 10,
                    membersCount = 1,
                    isJoined = true
                )
            )
            _statusBanner.value = "Défi '$title' créé !"
        }
    }

    fun toggleJoinChallenge(challenge: CommunityChallengeEntity) {
        viewModelScope.launch {
            repo.toggleJoinChallenge(challenge)
            _statusBanner.value = if (!challenge.isJoined) "Vous avez rejoint '${challenge.title}'" else "Défi quitté."
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
