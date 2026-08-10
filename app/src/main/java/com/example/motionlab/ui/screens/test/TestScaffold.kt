package com.example.motionlab.ui.screens.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.motionlab.presentation.prepost_test.TestViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.navigation.Routes

@Composable
fun TestScreen(navController: NavController, viewModel: TestViewModel) {
    LockPortrait()

    val questions by viewModel.questions
    val currentIndex by viewModel.currentQuestionIndex
    val timer by viewModel.timeRemaining
    val selectedAnswers = viewModel.selectedAnswers

    // Start timer when test begins
    LaunchedEffect(Unit) {
        viewModel.startTimer {
            // Auto-submit when time runs out
            navController.navigate(
                Routes.answerReviewRoute(
                    viewModel.username,
                    viewModel.lessonId,
                    viewModel.isPreTest
                )
            )
        }
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Loading questions...", color = Color.Black)
        }
        return
    }

    val current = questions[currentIndex]

    PrePostTest(
        navController = navController,
        questionText = current.question,
        choices = current.choices,
        selectedAnswer = selectedAnswers.getOrNull(currentIndex),
        onAnswerSelected = { answer ->
            viewModel.selectAnswer(currentIndex, answer)
        },
        isPreTest = viewModel.isPreTest,
        timerText = formatMillis(timer),
        currentQuestionIndex = currentIndex,
        totalQuestions = questions.size,
        onNext = { viewModel.goToNextQuestion() },
        onPrevious = { viewModel.goToPreviousQuestion() },
        onProceed = {
            navController.navigate(
                Routes.answerReviewRoute(
                    viewModel.username,
                    viewModel.lessonId,
                    viewModel.isPreTest
                )
            )
        },
        username = viewModel.username,
        lessonId = viewModel.lessonId
    )
}

fun formatMillis(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}
