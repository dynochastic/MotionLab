package com.example.motionlab.ui.screens.lesson

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.lesson.LessonViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.HamburgerButton
import com.example.motionlab.ui.components.LessonButton
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun TopicScreen(
    navController: NavController,
    username: String,
    viewModel: LessonViewModel
) {
    Log.d("NAV_DEBUG", "TopicScreen: username=$username")
    LockPortrait()

    // Refresh lessons when screen becomes visible
    LaunchedEffect(username) {
        Log.d("LESSON_SCREEN", "LaunchedEffect triggered with username: $username (lesson screen composed or recomposed)")
        viewModel.refreshLessons(username)
    }

    val lessonUiState by viewModel.lessons.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .background(MainBlueBg),
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Welcome to \nMotionLab",
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                color = ThirdBlue,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(180.dp)
                    .offset(y = 20.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f) // Use weight instead of fillMaxHeight to account for bottom navigation
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(SecondBlueBg)
        ) {
            Column(Modifier.padding(top = 25.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                Text(
                    text = "What is Physics?",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Physics is the scientific study of matter, its fundamental constituents, its motion and behavior through space and time, and the related entities of energy and force.",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Justify,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 20.dp)
                )
                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Topics",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(15.dp))

            // Scrollable content area for lessons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                lessonUiState.forEach { lesson ->
                    val isUnlocked = lesson.isUnlocked
                    Log.d("UI", "Lesson: ${lesson.title}, isUnlocked=$isUnlocked, user=$username")

                    val progressPercent = lesson.progress?.let {
                        val hasPostTest = it.postTestScore != null && it.postTestScore > 0
                        val totalItems = 2 // Only preTestTaken and postTestScore
                        val completedItems = listOf(
                            it.preTestTaken,
                            hasPostTest
                        ).count { done -> done }

                        (completedItems * 100) / totalItems
                    }

                    LessonButton(
                        lessonName = lesson.title,
                        painter = painterResource(id = lesson.iconRes),
                        description = lesson.title,
                        isLocked = !isUnlocked,
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .height(140.dp), // Consistent button height
                        onClick = {
                            Log.d("LESSON_SCREEN", "Navigating to lesson content: username=$username, lessonId=${lesson.lessonId}")
                            navController.navigate(Routes.lessonContentWith(username, lesson.lessonId))
                        }
                    )
                }
            }
        }
    }
}
