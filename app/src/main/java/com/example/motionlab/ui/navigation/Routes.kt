@file:Suppress("NAME_SHADOWING")

package com.example.motionlab.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.motionlab.MotionLab
import com.example.motionlab.R
import com.example.motionlab.presentation.lesson.LessonViewModel
import com.example.motionlab.presentation.lesson.SubtopicViewModel
import com.example.motionlab.presentation.prepost_test.TestViewModel
import com.example.motionlab.presentation.profile.ProfileViewModel
import com.example.motionlab.ui.screens.exercise.HandsOnActivity
import com.example.motionlab.ui.screens.leaderboards.LeaderboardsScreen
import com.example.motionlab.ui.screens.lesson.TopicScreen
import com.example.motionlab.ui.screens.lesson.contents.SubtopicContent
import com.example.motionlab.ui.screens.lesson.contents.Subtopics
import com.example.motionlab.ui.screens.login.SignInScreen
import com.example.motionlab.ui.screens.profile.ProfileScreen
import com.example.motionlab.ui.screens.settings.Settings
import com.example.motionlab.ui.screens.signup.SignUpScreen
import com.example.motionlab.ui.screens.test.AnswerReviewScreen
import com.example.motionlab.ui.screens.test.TestRulesScreen
import com.example.motionlab.ui.screens.test.TestScreen
import com.example.motionlab.ui.screens.test.TestType
import com.example.motionlab.ui.screens.video_ui.VideoLessonScreen
import com.example.motionlab.ui.screens.simulation.UnitySimulationScreen
import com.example.motionlab.ui.screens.simulation.SimulationShortcut
import com.example.motionlab.ui.theme.MainBlueBg


// ROUTE DEFINITIONS
object Routes {

    // ─── Authentication ──────────────────────────────────────────────
    const val SIGN_IN = "signin"
    const val SIGN_UP = "signup"

    // ─── Main Screens ────────────────────────────────────────────────
    const val LESSON = "lesson/{username}"
    fun lessonWithUsername(username: String) = "lesson/$username"

    const val LESSON_CONTENT = "lessonsContent/{username}/{lessonId}"
    fun lessonContentWith(username: String, lessonId: Int) = "lessonsContent/$username/$lessonId"

    const val PROFILE = "profile/{username}"
    fun profileWithUsername(username: String) = "profile/$username"

    const val SETTINGS = "settings/{username}"
    fun settingsWithUsername(username: String) = "settings/$username"

    // ─── Leaderboards ────────────────────────────────────────────
    const val LEADERBOARDS = "leaderboards/{username}"
    fun leaderboardWithUsername(username: String) = "leaderboards/$username"

    // ─── App Shell ──────────────────────────────────────────────
    const val MAIN_APP = "mainApp/{username}"
    fun mainAppWithUsername(username: String) = "mainApp/$username"


    // ─── Subtopic Screens ────────────────────────────────────────────
    const val SUBTOPIC_CONTENT = "subtopicContent/{username}/{lessonId}/{subtopicId}"
    fun subtopicContentRoute(username: String, lessonId: Int, subtopicId: Int) =
        "subtopicContent/$username/$lessonId/$subtopicId"

    // ─── Media Screens ───────────────────────────────────────────────
    const val VIDEO_LESSON = "videoLesson/{username}/{lessonId}/{subtopicId}"
    fun videoLessonRoute(username: String, lessonId: Int, subtopicId: Int) =
        "videoLesson/$username/$lessonId/$subtopicId"

    const val SIMULATION = "simulation/{username}"
    fun simulationRoute(username: String) = "simulation/$username"



    // ─── Test & Quiz Screens ─────────────────────────────────────────
    const val TEST_RULES_SCREEN = "testRulesScreen/{username}/{lessonId}/{isPreTest}"
    fun testRulesRoute(username: String, lessonId: Int, isPreTest: Boolean) =
        "testRulesScreen/$username/$lessonId/$isPreTest"

    const val TEST_SCREEN = "testScreen/{username}/{lessonId}/{isPreTest}"
    fun testScreenRoute(username: String, lessonId: Int, isPreTest: Boolean): String =
        "testScreen/$username/$lessonId/$isPreTest"

    const val ANSWER_REVIEW_SCREEN = "answerReviewScreen"
    const val ANSWER_REVIEW_SCREEN_WITH_ARGS = "answerReviewScreen/{username}/{lessonId}/{isPreTest}"
    fun answerReviewRoute(username: String, lessonId: Int, isPreTest: Boolean): String =
        "answerReviewScreen/$username/$lessonId/$isPreTest"

    const val TEST_RESULT_SCREEN = "testResultScreen/{score}/{testType}/{username}/{lessonId}"
    fun testResultScreenRoute(score: Int, testType: String, username: String, lessonId: Int) =
        "testResultScreen/$score/$testType/$username/$lessonId"

    // ─── Hands-On Exercise ───────────────────────────────────────────────
    const val HANDS_ON_EXERCISE = "hands_on/{username}/{lessonId}/{subtopicId}/{contentPath}"
    fun handsOnRoute(username: String, lessonId: Int, subtopicId: Int, contentPath: String): String {
        val encodedPath = java.net.URLEncoder.encode(contentPath, "UTF-8")
        return "hands_on/$username/$lessonId/$subtopicId/$encodedPath"
    }

    // ─── Unity Simulation ────────────────────────────────────────────────
    const val UNITY_SIMULATION = "unity_simulation/{sceneId}"
    fun unitySimulationRoute(sceneId: String) = "unity_simulation/$sceneId"


}

// ROOT NAVIGATION HOST
@Composable
fun RootNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Routes.SIGN_IN, modifier = modifier) {
        composable(Routes.SIGN_IN) {
            SignInScreen(viewModel = hiltViewModel(), navController = navController)
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(viewModel = hiltViewModel(), navController = navController)
        }
        composable(
            route = Routes.MAIN_APP,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            MotionLab(username = username)
        }
    }
}

// APP SHELL NAVIGATION HOST (for main app screens)
@Composable
fun AppShellNavGraph(navController: NavHostController, username: String, modifier: Modifier = Modifier) {
    // Only register the placeholder route for lessons and set as startDestination
    NavHost(
        navController = navController,
        startDestination = Routes.lessonWithUsername("{username}"),
        modifier = modifier
    ) {
        composable(
            route = Routes.lessonWithUsername("{username}"),
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
            val viewModel: LessonViewModel = hiltViewModel(backStackEntry)
            TopicScreen(navController, usernameArg, viewModel)
        }
        composable(Routes.profileWithUsername(username)) { _ ->
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(navController = navController, viewModel = viewModel, username = username, firstname = "", lastname = "")
        }
        composable(
            route = Routes.LEADERBOARDS,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            LeaderboardsScreen(
                navController = navController
            )
        }
        composable(
            route = Routes.SIMULATION,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            SimulationShortcut(
                username = username,
                viewModel = hiltViewModel()
            )
        }
        // Subroutes
        composable(
            route = Routes.LESSON_CONTENT,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val usernameArg = backStackEntry.arguments?.getString("username") ?: return@composable
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: return@composable
            android.util.Log.d("DEBUG", "Navigating to Subtopics with username=$usernameArg, lessonId=$lessonId")
            val lessonViewModel: LessonViewModel = hiltViewModel()
            val subtopicViewModel: SubtopicViewModel = hiltViewModel()
            LaunchedEffect(lessonId, usernameArg) {
                val lesson = lessonViewModel.lessons.value.find { it.lessonId == lessonId }
                val preTestTaken = lesson?.progress?.preTestTaken == true
                subtopicViewModel.loadSubtopicProgress(usernameArg, preTestTaken)
            }
            val lesson = lessonViewModel.lessons.value.find { it.lessonId == lessonId }
            val painter = painterResource(id = lesson?.iconRes ?: R.drawable.logo_bg)
            Subtopics(
                navController = navController,
                lessonId = lessonId,
                username = usernameArg,
                painter = painter,
                subtopicViewModel = subtopicViewModel,
                lessonViewModel = lessonViewModel
            )
        }
        composable(
            route = Routes.SUBTOPIC_CONTENT,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("subtopicId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val usernameArg = backStackEntry.arguments?.getString("username") ?: return@composable
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: return@composable
            val subtopicId = backStackEntry.arguments?.getInt("subtopicId") ?: return@composable
            val viewModel: SubtopicViewModel = hiltViewModel(backStackEntry)
            SubtopicContent(
                navController = navController,
                username = usernameArg,
                lessonId = lessonId,
                subtopicId = subtopicId,
                viewModel = viewModel
            )
        }
        composable(
            route = Routes.VIDEO_LESSON,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("subtopicId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val usernameArg = backStackEntry.arguments?.getString("username") ?: return@composable
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: return@composable
            val subtopicId = backStackEntry.arguments?.getInt("subtopicId") ?: return@composable
            val viewModel: SubtopicViewModel = hiltViewModel(backStackEntry)
            val lessonViewModel: LessonViewModel = hiltViewModel(backStackEntry)
            val lesson = lessonViewModel.lessons.collectAsState().value.find { it.lessonId == lessonId }
            val preTestTaken = lesson?.progress?.preTestTaken == true
            VideoLessonScreen(
                navController = navController,
                username = usernameArg,
                lessonId = lessonId,
                subtopicId = subtopicId,
                preTestTaken = preTestTaken,
                viewModel = viewModel
            )
        }
        composable(
            route = Routes.TEST_RULES_SCREEN,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("isPreTest") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val usernameArg = backStackEntry.arguments?.getString("username") ?: return@composable
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: return@composable
            val isPreTest = backStackEntry.arguments?.getBoolean("isPreTest") ?: true
            TestRulesScreen(navController, usernameArg, lessonId, isPreTest)
        }
        composable(
            route = Routes.TEST_SCREEN,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("isPreTest") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.TEST_SCREEN)
            }
            val viewModel: TestViewModel = hiltViewModel(parentEntry)
            LaunchedEffect(Unit) {
                viewModel.loadQuestions()
                viewModel.startTimer {
                    viewModel.submit {
                        navController.navigate(Routes.TEST_RESULT_SCREEN)
                    }
                }
            }
            TestScreen(navController = navController, viewModel = viewModel)
        }
        composable(
            route = Routes.ANSWER_REVIEW_SCREEN_WITH_ARGS,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("isPreTest") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.TEST_SCREEN)
            }
            val viewModel: TestViewModel = hiltViewModel(parentEntry)
            val questions = viewModel.questions.value
            val selectedAnswers = viewModel.selectedAnswers.toList()
            val isPreTest = backStackEntry.arguments?.getBoolean("isPreTest") ?: true
            val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 0
            AnswerReviewScreen(
                navController = navController,
                questions = questions,
                selectedAnswers = selectedAnswers,
                onSubmitFinal = {
                    viewModel.submit {
                        navController.navigate(Routes.TEST_RESULT_SCREEN) {
                            popUpTo(Routes.SIGN_IN) { inclusive = false }
                        }
                    }
                },
                onChangeAnswer = { index ->
                    viewModel.jumpToQuestion(index)
                    navController.popBackStack() // return to TestScreen showing that index
                },
                testType = if (isPreTest) TestType.PRE else TestType.POST,
                username = usernameArg,
                lessonId = lessonId,
                testViewModel = viewModel
            )
        }
        composable(
            route = Routes.TEST_RESULT_SCREEN,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("testType") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val testType = backStackEntry.arguments?.getString("testType") ?: "PRE"
            val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 0
            val subtopicViewModel: SubtopicViewModel = hiltViewModel(backStackEntry)
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.TEST_SCREEN)
            }
            val testViewModel: TestViewModel = hiltViewModel(parentEntry)
            com.example.motionlab.ui.screens.test.TestResultScreen(
                navController = navController,
                score = score,
                testType = if (testType == "POST") TestType.POST else TestType.PRE,
                username = usernameArg,
                lessonId = lessonId,
                testViewModel = testViewModel
            )
        }
        composable(
            route = Routes.HANDS_ON_EXERCISE,
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("subtopicId") { type = NavType.IntType },
                navArgument("contentPath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val usernameArg = backStackEntry.arguments?.getString("username") ?: return@composable
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: return@composable
            val subtopicId = backStackEntry.arguments?.getInt("subtopicId") ?: return@composable
            val contentPath = try {
                java.net.URLDecoder.decode(backStackEntry.arguments?.getString("contentPath") ?: "", "UTF-8")
            } catch (e: Exception) {
                ""
            }
            HandsOnActivity(
                navController = navController,
                username = usernameArg,
                lessonId = lessonId,
                subtopicId = subtopicId,
                contentPath = contentPath,
                viewModel = hiltViewModel()
            )
        }
        composable(
            route = Routes.SETTINGS,
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            Settings(navController = navController, username = username)
        }
        composable(
            route = Routes.UNITY_SIMULATION,
            arguments = listOf(navArgument("sceneId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
            UnitySimulationScreen(navController = navController, sceneId = sceneId)
        }
    }
}

// Remove old AppNavGraph if not needed

// BOTTOM NAVIGATION DATA CLASS
data class BottomNavItem(
    val label: String,
    val icon: Painter,
    val route: String
)

// BOTTOM NAVIGATION BAR UI
@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    selectedRoute: String,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .background(MainBlueBg)
            .border(1.dp, color = Color.Black)
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.Start, // No extra space between items
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item.route == selectedRoute

            Column(
                modifier = Modifier
                    .weight(1f) // Each item takes equal width
                    .fillMaxHeight()
                    .clickable { onItemSelected(item) }
                    .padding(vertical = 10.dp), // Only vertical padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(40.dp),
                    alpha = if (isSelected) 1f else 0.5f
                )
                Text(
                    text = item.label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
        }
    }
}
