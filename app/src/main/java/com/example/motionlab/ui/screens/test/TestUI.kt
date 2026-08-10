package com.example.motionlab.ui.screens.test

import ChoicePill
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.motionlab.dmSansFontFamily
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.components.CollapsibleCard
import com.example.motionlab.ui.components.NextButton
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun PrePostTest(
    navController: NavController,
    questionText: String?,
    choices: List<String>?,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    isPreTest: Boolean,
    onProceed: () -> Unit,
    timerText: String,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    username: String,
    lessonId: Int
) {
    LockPortrait()

    var showDialog by remember { mutableStateOf(false) }

    // Add BackHandler to trigger the dialog
    BackHandler(enabled = true) {
        showDialog = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MainBlueBg)
    ) {
        BackButton(
            onClick = { showDialog = true },
            modifier = Modifier.padding(start = 25.dp, top = 50.dp).size(40.dp)
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(
                        text = if (isPreTest) "❌ Cancel Pre-Test?" else "❌ Cancel Post-Test?",
                        fontWeight = FontWeight.Bold,
                        fontFamily = dmSansFontFamily,
                        fontSize = 22.sp,
                        color = Color.Black
                    )
                },
                text = {
                    Text(
                        text = if (isPreTest)
                            "Are you sure you want to cancel the pre-test? Your progress will not be saved."
                        else
                            "Are you sure you want to cancel the post-test? Your progress will not be saved.",
                        fontFamily = dmSansFontFamily,
                        color = Color.Black
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        navController.navigate(
                            Routes.lessonContentWith(
                                username,
                                lessonId
                            )
                        ) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Text(
                            "Yes",
                            fontFamily = dmSansFontFamily,
                            color = Color.Black
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(
                            "No",
                            fontFamily = dmSansFontFamily,
                            color = Color.Black


                        )
                    }
                },
                containerColor = Color.White,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
        }

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isPreTest) "Pre-Test" else "Post-Test",
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                color = ThirdBlue,
                fontSize = 30.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isPreTest) "Test your knowledge" else "Show what you've learned!",
                fontFamily = dmSansFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(20.dp))

        // Use CollapsibleCard with timer as pill header and question content
        CollapsibleCard(
            pillTitle = timerText,
            pillFontSize = 20,
            isExpandable = false, // Disable expandable functionality
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            collapsedContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Question number indicator
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of $totalQuestions",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontFamily = poppinsFontFamily,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 12.dp)
                    )

                    // Question text
                    Text(
                        text = questionText ?: "",
                        fontSize = 20.sp,
                        textAlign = TextAlign.Justify,
                        color = Color.White,
                        fontFamily = dmSansFontFamily
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Choices
                    choices?.forEach { choice ->
                        ChoicePill(
                            text = choice,
                            isSelected = (selectedAnswer == choice),
                            onClick = { onAnswerSelected(choice) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Navigation buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentQuestionIndex > 0) {
                            NextButton(
                                text = "Previous",
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                onClick = onPrevious
                            )
                        }

                        NextButton(
                            text = if (currentQuestionIndex == totalQuestions - 1) "Review" else "Next",
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            onClick = {
                                if (currentQuestionIndex == totalQuestions - 1) {
                                    onProceed()
                                } else {
                                    onNext()
                                }
                            }
                        )
                    }
                }
            },
            expandedContent = {
                // This won't be shown since isExpandable = false, but it's required
                Text("Timer", color = Color.White)
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PrePostTestPreview() {
    PrePostTest(
        navController = rememberNavController(),
        questionText = "What is the unit of force?",
        choices = listOf("Joule", "Newton", "Watt", "Pascal"),
        selectedAnswer = "Newton",
        onAnswerSelected = {},
        isPreTest = true,
        onProceed = {},
        timerText = "00:45",
        currentQuestionIndex = 1,
        totalQuestions = 15,
        onNext = {},
        onPrevious = {},
        username = "TestUser",
        lessonId = 1
    )
}
