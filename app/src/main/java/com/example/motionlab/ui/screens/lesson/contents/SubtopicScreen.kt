package com.example.motionlab.ui.screens.lesson.contents

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motionlab.R
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.lesson.LessonViewModel
import com.example.motionlab.presentation.lesson.SubtopicViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.components.SubtopicsCarousel
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.theme.ThirdBlue
import com.example.motionlab.ui.components.LessonButton as SubtopicButton


@Composable
fun Subtopics(
    navController: NavController,
    lessonId: Int,
    username: String,
    painter: Painter,
    subtopicViewModel: SubtopicViewModel,
    lessonViewModel: LessonViewModel
)
{
     LockPortrait()

    Log.d("DEBUG", "Subtopics composable called with username=$username, lessonId=$lessonId")

        val subtopics by subtopicViewModel.subtopics.collectAsState()
    Log.d("DEBUG", "Subtopics in UI: $subtopics")
    val lessons by lessonViewModel.lessons.collectAsState()

     val lesson = lessons.find { it.lessonId == lessonId }
     val progress = lesson?.progress
     val isPreTestLocked = progress?.preTestTaken == true
     val isPostTestLocked = !(progress?.preTestTaken == true && subtopics.all { it.videoCompleted && it.problemCompleted && it.simulationCompleted })

     val subtopicProgress = subtopicViewModel.subtopicProgress.collectAsState().value

     var carouselIndex by rememberSaveable { mutableIntStateOf(0) }

     LaunchedEffect(lessonId, lesson?.progress?.preTestTaken) {
         subtopicViewModel.observeSubtopicProgress(username)
         subtopicViewModel.initializeSubtopics(username, lessonId, lesson?.progress?.preTestTaken == true)
     }

     Column(
        Modifier.fillMaxSize().background(MainBlueBg),
    ) {
         BackButton(
             onClick = {
                 navController.navigate(Routes.lessonWithUsername(username)) {
                     popUpTo(0) { inclusive = true }
                 }
             },
             modifier = Modifier.padding(start = 25.dp, top = 40.dp).size(40.dp)
         )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = lesson?.title ?: "TOPIC",
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                color = ThirdBlue,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(250.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(SecondBlueBg)
        ) {
            Column(Modifier.padding(top = 25.dp, start = 20.dp, end = 20.dp)) {
                Text(
                    text = "What is ${lesson?.title ?: "Topic"}?",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Left,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = lesson?.description ?: "No description",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Justify,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 20.dp)
                )

                Spacer(Modifier.height(20.dp))
            }

            // Scrollable content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                // Pre-Test button with consistent sizing
                SubtopicButton(
                    lessonName = "Pre-Test",
                    painter = painterResource(R.drawable.pretest),
                    description = "Pre-Test",
                    onClick = {
                        navController.navigate(Routes.testRulesRoute(username, lessonId, true))
                    },
                    isGreyedOut = isPreTestLocked,
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(130.dp) // Consistent button height
                )

                Spacer(Modifier.height(15.dp))

                SubtopicsCarousel(
                    subtopics = subtopics,
                    currentIndex = carouselIndex,
                    onIndexChange = { carouselIndex = it },
                    modifier = Modifier.padding(horizontal = 15.dp),
                    onSubtopicClick = { subtopic ->
                        navController.navigate(
                            Routes.subtopicContentRoute(username, lessonId, subtopic.subtopicId)
                        )
                    }
                )

                Spacer(Modifier.height(15.dp))

                // Post-Test button with consistent sizing
                SubtopicButton(
                    lessonName = "Post-Test",
                    painter = painterResource(R.drawable.postest),
                    description = "Post-Test",
                    isLocked = isPostTestLocked,
                    modifier = Modifier
                        .padding(horizontal = 15.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(130.dp), // Consistent button height
                    onClick = { 
                        navController.navigate(Routes.testRulesRoute(username, lessonId, false))
                    }
                )
            }
        }
    }

    // Handle device back button to go back to lesson screen
    BackHandler {
        navController.navigate(Routes.lessonWithUsername(username)) {
            popUpTo(0) { inclusive = true }
        }
    }

    }

