package com.example.motionlab.ui.screens.test

import android.util.Log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.motionlab.presentation.lesson.LessonViewModel
import com.example.motionlab.presentation.prepost_test.TestViewModel
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg

enum class TestType { PRE, POST }

@Composable
fun TestResultScreen(
    navController: NavController,
    score: Int,
    testType: TestType,
    username: String,
    lessonId: Int,
    testViewModel: TestViewModel
) {
    val lessonViewModel: LessonViewModel = hiltViewModel()
    val (message, remark) = when (testType) {
        TestType.PRE -> when {
            score == 15 -> "Perfect! Keep building on your knowledge" to "Perfect"
            score in 6..14 -> "Good effort! Keep working on your understanding." to "Passed"
            else -> "Failed. Keep practicing and review the concepts." to "Failed"
        }
        TestType.POST -> when {
            score == 15 -> "Aced! You have mastered the key concepts. Keep exploring and applying what you've learned." to "Perfect"
            score in 8..14 -> "Good job! You have a solid understanding, but there's still room for improvement." to "Passed"
            else -> "Try Again. Review the lessons, practice more, and don't hesitate to try again" to "Failed"
        }
    }

    val timeUsed = (if (testType == TestType.PRE) 30 * 60 * 1000L else 25 * 60 * 1000L) - testViewModel.timeRemaining.value

    Column(
        modifier = Modifier.fillMaxSize().background(MainBlueBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your Score: $score/15", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Text("Time Used: ${formatMillis(timeUsed)}", fontSize = 18.sp, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(message, fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Remark: $remark", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        Spacer(Modifier.height(32.dp))
        Button(onClick = {
            if (testType == TestType.PRE) {
                val lessonContentRoute = Routes.lessonContentWith(username, lessonId)
                Log.d("TestResultScreen", "Navigating to lessonContentRoute: $lessonContentRoute, username: $username, lessonId: $lessonId")
                navController.navigate(lessonContentRoute) {
                    popUpTo(lessonContentRoute) { inclusive = true }
                    launchSingleTop = true
                }
            } else if (testType == TestType.POST && score >= 8) {
                lessonViewModel.onPostTestPassed(username, lessonId, score, timeUsed)
                val lessonContentRoute = Routes.lessonContentWith(username, lessonId)
                Log.d("TestResultScreen", "Navigating to lessonContentRoute: $lessonContentRoute, username: $username, lessonId: $lessonId")
                val lessonContentEntry = try {
                    navController.getBackStackEntry(lessonContentRoute)
                } catch (e: Exception) {
                    null
                }
                lessonContentEntry?.savedStateHandle?.set("shouldRefresh", true)
                navController.navigate(lessonContentRoute) {
                    popUpTo(Routes.LESSON) { inclusive = false }
                    launchSingleTop = true
                }
            } else {
                val lessonContentRoute = Routes.lessonContentWith(username, lessonId)
                navController.navigate(lessonContentRoute) {
                    popUpTo(Routes.LESSON) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }) {
            Text("Next")
        }
    }


}