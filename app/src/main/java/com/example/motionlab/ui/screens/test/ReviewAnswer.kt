package com.example.motionlab.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motionlab.data.local.entity.QuestionEntity
import com.example.motionlab.dmSansFontFamily
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.prepost_test.TestViewModel
import com.example.motionlab.ui.LockPortrait

import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.SecondBlueBg
import kotlinx.coroutines.launch

@Composable
fun AnswerReviewScreen(
    navController: NavController,
    questions: List<QuestionEntity>,
    selectedAnswers: List<String?>,
    onSubmitFinal: () -> Unit,
    onChangeAnswer: (Int) -> Unit,
    testType: TestType,
    username: String,
    lessonId: Int,
    testViewModel: TestViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    LockPortrait()

    val allAnswered = selectedAnswers.none { it.isNullOrEmpty() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBlueBg)
            .padding(16.dp)
    ) {
        
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Review Your Answers",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(questions) { index, question ->
                ReviewCard(
                    index = index,
                    question = question,
                    selectedAnswer = selectedAnswers.getOrNull(index),
                    onChangeAnswerClick = { onChangeAnswer(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (allAnswered) {
                    val score = questions.zip(selectedAnswers).count { (q, ans) -> q.answer == ans }
                    coroutineScope.launch {
                        testViewModel.submit {
                            navController.navigate(Routes.testResultScreenRoute(score, testType.name, username, lessonId)) {
                                // Clear the back stack up to the lesson content screen
                                // This preserves the path: Lesson -> Subtopic, but removes Answer Review
                                popUpTo(Routes.lessonContentWith(username, lessonId)) {
                                    inclusive = false
                                }
                            }
                        }
                    }
                }
            },
            enabled = allAnswered,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allAnswered) Color(0xFF003366) else Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(50))
                .border(2.dp, Color(0xFFB9D9F6), RoundedCornerShape(50))
        ) {
            Text(
                text = "Final Submit",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ReviewCard(
    index: Int,
    question: QuestionEntity,
    selectedAnswer: String?,
    onChangeAnswerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SecondBlueBg)
            .padding(12.dp)
    ) {
        Text(
            text = "${index + 1}: ${question.question}",
            color = Color.White,
            fontFamily = poppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your Answer: ${selectedAnswer ?: "No answer selected"}",
            color = if (selectedAnswer == null) Color(0xfff42e3c) else Color(0xff6fe630),
            fontFamily = dmSansFontFamily,
            fontSize = 17.sp,
            modifier = Modifier.padding(start = 5.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onChangeAnswerClick,
            modifier = Modifier
                .align(Alignment.End)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(15.dp)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor),
            shape = RoundedCornerShape(15.dp)
        ) {
            Text("Change Answer", color = Color.White)
        }

    }
}

