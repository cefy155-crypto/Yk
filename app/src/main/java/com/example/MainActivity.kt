package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.db.ExerciseEntity
import com.example.ui.components.AddClientDialog
import com.example.ui.components.AiAssistantBottomSheet
import com.example.ui.components.ExportDocumentDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExerciseLibraryScreen
import com.example.ui.screens.LiveCoachScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.ProfileManagementScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.TrainingHubScreen
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OnElectricLime
import com.example.ui.theme.SportCoachProTheme
import com.example.ui.viewmodel.MainViewModel

sealed class NavDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : NavDestination("dashboard", "Accueil", Icons.Default.Dashboard)
    data object Training : NavDestination("training", "Séances", Icons.Default.FitnessCenter)
    data object Exercises : NavDestination("exercises", "Exercices", Icons.AutoMirrored.Filled.MenuBook)
    data object Nutrition : NavDestination("nutrition", "Nutrition", Icons.Default.Restaurant)
    data object Progress : NavDestination("progress", "Progrès", Icons.AutoMirrored.Filled.ShowChart)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportCoachProTheme(darkTheme = true) {
                SportCoachProApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportCoachProApp(viewModel: MainViewModel = viewModel()) {
    val isDbReady by viewModel.isDbReady.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    if (!isDbReady) {
        Box(
            modifier = Modifier.fillMaxSize().background(ObsidianBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(color = ElectricLime)
                Text("Initialisation Sport Coach Pro (250 exercices & BDD)...", style = MaterialTheme.typography.bodyMedium)
            }
        }
        return
    }

    if (currentUser == null) {
        AuthScreen(
            errorMessage = authError,
            onClearError = viewModel::clearAuthError,
            onLogin = viewModel::login,
            onDemoLogin = viewModel::loginDemoCoach,
            onSignUp = viewModel::signUp
        )
        return
    }

    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavDestination.Dashboard.route

    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val activeClient by viewModel.activeClient.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val allFoods by viewModel.allFoods.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val sessionRows by viewModel.activeSessionRows.collectAsStateWithLifecycle()
    val activeProgram by viewModel.activeProgram.collectAsStateWithLifecycle()
    val programExercises by viewModel.activeProgramExercises.collectAsStateWithLifecycle()
    val activeNutrition by viewModel.activeNutritionProgram.collectAsStateWithLifecycle()
    val meals by viewModel.activeMeals.collectAsStateWithLifecycle()
    val mealItems by viewModel.activeMealItems.collectAsStateWithLifecycle()
    val supplements by viewModel.activeSupplements.collectAsStateWithLifecycle()
    val schedules by viewModel.activeSchedules.collectAsStateWithLifecycle()
    val assessments by viewModel.activeAssessments.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.activeWorkoutLogs.collectAsStateWithLifecycle()
    val liveCoach by viewModel.liveCoach.collectAsStateWithLifecycle()
    val aiReply by viewModel.aiReply.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val communityComments by viewModel.communityComments.collectAsStateWithLifecycle()
    val communityChallenges by viewModel.communityChallenges.collectAsStateWithLifecycle()

    var trainingSubTab by remember { mutableIntStateOf(0) }
    var showProfile by remember { mutableStateOf(false) }
    var showCommunity by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var preselectedExerciseToLog by remember { mutableStateOf<ExerciseEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        statusBanner?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusBanner()
        }
    }

    if (liveCoach.isActive) {
        LiveCoachScreen(
            state = liveCoach,
            onTogglePause = viewModel::toggleLiveCoachPause,
            onNext = viewModel::liveCoachNext,
            onPrev = viewModel::liveCoachPrev,
            onToggleSound = viewModel::toggleLiveCoachSound,
            onClose = viewModel::stopLiveCoach
        )
        return
    }

    if (showProfile && currentUser != null) {
        ProfileManagementScreen(
            user = currentUser!!,
            onSaveProfile = viewModel::updateProfile,
            onLogout = {
                showProfile = false
                viewModel.logout()
            },
            onClose = { showProfile = false }
        )
        return
    }

    if (showCommunity) {
        CommunityScreen(
            posts = communityPosts,
            comments = communityComments,
            challenges = communityChallenges,
            onPublishPost = viewModel::publishCommunityPost,
            onToggleLike = viewModel::toggleLikePost,
            onAddComment = viewModel::addCommunityComment,
            onCreateChallenge = viewModel::createChallenge,
            onToggleJoinChallenge = viewModel::toggleJoinChallenge,
            onClose = { showCommunity = false }
        )
        return
    }

    val destinations = listOf(
        NavDestination.Dashboard,
        NavDestination.Training,
        NavDestination.Exercises,
        NavDestination.Nutrition,
        NavDestination.Progress
    )

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(NavDestination.Dashboard.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBg)) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            containerColor = ObsidianBg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CharcoalSurface,
                        titleContentColor = ElectricLime
                    ),
                    title = {
                        Column {
                            Text("SPORT COACH PRO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(
                                activeClient?.fullName ?: "Aucun pratiquant",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showCommunity = true }, modifier = Modifier.testTag("topbar_community_btn")) {
                            Icon(Icons.Default.Groups, contentDescription = "Communauté & Défis", tint = CyberCyan)
                        }
                        IconButton(onClick = { showAiSheet = true }, modifier = Modifier.testTag("topbar_ai_btn")) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Assistant IA", tint = ElectricLime)
                        }
                        IconButton(onClick = { showExportDialog = true }, modifier = Modifier.testTag("topbar_export_btn")) {
                            Icon(Icons.Default.Download, contentDescription = "Exporter")
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricLime)
                                .clickable { showProfile = true }
                                .testTag("topbar_profile_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.fullName?.take(2)?.uppercase() ?: "SC",
                                style = MaterialTheme.typography.labelLarge,
                                color = OnElectricLime,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = CharcoalSurface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        destinations.forEach { dest ->
                            NavigationBarItem(
                                selected = currentRoute == dest.route,
                                onClick = { navigateTo(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OnElectricLime,
                                    selectedTextColor = ElectricLime,
                                    indicatorColor = ElectricLime
                                ),
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (useRail) {
                    NavigationRail(
                        containerColor = CharcoalSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        destinations.forEach { dest ->
                            NavigationRailItem(
                                selected = currentRoute == dest.route,
                                onClick = { navigateTo(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = OnElectricLime,
                                    selectedTextColor = ElectricLime,
                                    indicatorColor = ElectricLime
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = NavDestination.Dashboard.route,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    composable(NavDestination.Dashboard.route) {
                        DashboardScreen(
                            user = currentUser,
                            clients = clients,
                            activeClient = activeClient,
                            activeSession = activeSession,
                            activeSessionRows = sessionRows,
                            nutrition = activeNutrition,
                            schedules = schedules,
                            assessments = assessments,
                            workoutLogs = workoutLogs,
                            exerciseCount = allExercises.size,
                            onSelectClient = viewModel::selectClient,
                            onAddClientClick = { showAddClientDialog = true },
                            onLaunchQuickSession = { t, f, d -> viewModel.generateSession(t, f, d) },
                            onStartLiveCoach = {
                                viewModel.startLiveCoach(activeSession?.title ?: "Séance Coach", sessionRows)
                            },
                            onOpenTrainingTab = { sub ->
                                trainingSubTab = sub
                                navigateTo(NavDestination.Training.route)
                            },
                            onOpenNutritionTab = { navigateTo(NavDestination.Nutrition.route) },
                            onOpenProgressTab = { navigateTo(NavDestination.Progress.route) },
                            onOpenCommunity = { showCommunity = true },
                            onOpenAiAssistant = { showAiSheet = true },
                            onOpenExport = { showExportDialog = true }
                        )
                    }
                    composable(NavDestination.Training.route) {
                        TrainingHubScreen(
                            selectedSubTab = trainingSubTab,
                            onSelectSubTab = { trainingSubTab = it },
                            client = activeClient,
                            allExercises = allExercises,
                            activeSession = activeSession,
                            sessionRows = sessionRows,
                            activeProgram = activeProgram,
                            programExercises = programExercises,
                            schedules = schedules,
                            onGenerateSession = viewModel::generateSession,
                            onCreateCircuit = viewModel::createCircuitSession,
                            onStartLiveCoach = viewModel::startLiveCoach,
                            onGenerateProgram = viewModel::generateGymProgram,
                            onBumpLoad = viewModel::bumpExerciseTargetLoad,
                            onAddSchedule = viewModel::addScheduleItem,
                            onToggleSchedule = viewModel::toggleScheduleCompleted,
                            onDeleteSchedule = viewModel::deleteSchedule,
                            onOpenExport = { showExportDialog = true }
                        )
                    }
                    composable(NavDestination.Exercises.route) {
                        ExerciseLibraryScreen(
                            exercises = allExercises,
                            onQuickLogExercise = { ex ->
                                preselectedExerciseToLog = ex
                                navigateTo(NavDestination.Progress.route)
                            }
                        )
                    }
                    composable(NavDestination.Nutrition.route) {
                        NutritionScreen(
                            client = activeClient,
                            program = activeNutrition,
                            meals = meals,
                            mealItems = mealItems,
                            foods = allFoods,
                            supplements = supplements,
                            onApplyStrategy = viewModel::applyNutritionStrategy,
                            onAddFoodToMeal = viewModel::addFoodToMeal,
                            onRemoveMealItem = viewModel::removeMealItem,
                            onAddSupplement = viewModel::addSupplement,
                            onToggleSupplement = viewModel::toggleSupplementInTotals,
                            onDeleteSupplement = viewModel::deleteSupplement,
                            onOpenExport = { showExportDialog = true }
                        )
                    }
                    composable(NavDestination.Progress.route) {
                        ProgressScreen(
                            client = activeClient,
                            nutrition = activeNutrition,
                            exercises = allExercises,
                            assessments = assessments,
                            workoutLogs = workoutLogs,
                            preselectedExerciseToLog = preselectedExerciseToLog,
                            onClearPreselectedExercise = { preselectedExerciseToLog = null },
                            onLogWeight = viewModel::logWeightAndBodyFat,
                            onLogMeasurements = viewModel::logMeasurements,
                            onLogNutrition = viewModel::logNutritionIntake,
                            onLogWorkout = viewModel::logWorkoutPerformance,
                            onDeleteAssessment = viewModel::deleteAssessment,
                            onDeleteWorkoutLog = viewModel::deleteWorkoutLog,
                            onOpenExport = { showExportDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showAddClientDialog) {
        AddClientDialog(
            onDismiss = { showAddClientDialog = false },
            onSave = { client ->
                viewModel.addClient(client)
                showAddClientDialog = false
            }
        )
    }

    if (showExportDialog) {
        ExportDocumentDialog(
            onDismiss = { showExportDialog = false },
            onExport = { fmt, scope -> viewModel.exportDocument(context, fmt, scope) }
        )
    }

    if (showAiSheet) {
        AiAssistantBottomSheet(
            reply = aiReply,
            isLoading = isAiLoading,
            onSendPrompt = viewModel::sendAiPrompt,
            onExecuteAction = viewModel::executeAiAction,
            onDismiss = { showAiSheet = false }
        )
    }
}
