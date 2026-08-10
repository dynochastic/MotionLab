package com.example.motionlab.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motionlab.dmSansFontFamily
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.components.CollapsibleCard
import com.example.motionlab.ui.components.ProceedButton
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun TestRulesScreen(
    navController: NavController,
    username: String,
    lessonId: Int,
    isPreTest: Boolean
) {
    LockPortrait()

    val testTitle = if (isPreTest) "Pre-Test" else "Post-Test"
    val timerDuration = if (isPreTest) "30" else "25"

    Column(
        Modifier
            .fillMaxSize()
            .background(MainBlueBg)
    ) {
        BackButton(
            onClick = {
                navController.navigate(Routes.lessonContentWith(username, lessonId)) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.padding(start = 25.dp, top = 50.dp).size(40.dp)
        )

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = testTitle,
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                color = ThirdBlue,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(200.dp)
            )
            Text(
                text = "Test your knowledge",
                fontFamily = dmSansFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(200.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Use CollapsibleCard with disabled expandable functionality
        CollapsibleCard(
            pillTitle = "Instructions",
            pillFontSize = 20,
            isExpandable = false, // Disable expandable functionality
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ,collapsedContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp), // Fixed height box
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Scrollable text content area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BulletItem(
                            text = "Start the $testTitle by tapping \"Proceed\". The $timerDuration-minute timer begins immediately.",
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = "The $testTitle will consist of 15 multiple-choice questions.",
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = "You can change answers before submitting.",
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = "Use \"Next\" and \"Previous\" to navigate between questions.",
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = "Submit once all questions are answered or before time runs out.",
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = "If time ends, the $testTitle will auto-submit and mark unanswered questions incorrect.",
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = if (isPreTest) {
                                "You need a minimum score of 6 out of 15 to pass the Pre-Test."
                            } else {
                                "You need a minimum score of 8 out of 15 to pass the Post-Test."
                            },
                            color = Color.White
                        )
                        Spacer(Modifier.height(15.dp))
                        BulletItem(
                            text = "Passing the test will unlock the next lesson and mark the current lesson as complete.",
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Fixed Proceed button at the bottom
                    ProceedButton(
                        onClick = {
                            navController.navigate(
                                Routes.testScreenRoute(username, lessonId, isPreTest)
                            ) {
                                // Clear the back stack up to the lesson content screen
                                // This preserves the path: Lesson -> Subtopic, but removes Test Rules
                                popUpTo(Routes.lessonContentWith(username, lessonId)) {
                                    inclusive = false
                                }
                            }
                        }
                    )
                }
            },
            expandedContent = {
                // This won't be shown since isExpandable = false, but it's required
                Text("Instructions", color = Color.White)
            }
        )
    }
}

@Composable
private fun BulletItem(
    text: String,
    color: Color,
    bullet: String = "\u2022",
    gutterWidth: Int = 20,
    fontSizeSp: Int = 20
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = bullet,
            color = color,
            fontFamily = dmSansFontFamily,
            fontSize = fontSizeSp.sp,
            modifier = Modifier.width(gutterWidth.dp)
        )
        Text(
            text = text,
            color = color,
            fontFamily = dmSansFontFamily,
            fontSize = fontSizeSp.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}
