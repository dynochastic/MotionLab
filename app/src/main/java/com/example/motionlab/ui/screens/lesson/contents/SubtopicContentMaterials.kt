package com.example.motionlab.ui.screens.lesson.contents


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
import com.example.motionlab.R
import com.example.motionlab.data.local.entity.MaterialType
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.lesson.SubtopicViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.components.LessonButton
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.screens.simulation.UnityLauncherV2
import com.example.motionlab.ui.screens.simulation.SimulationMapper
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun SubtopicContent(
    navController: NavController,
    username: String,
    lessonId: Int,
    subtopicId: Int,
    viewModel: SubtopicViewModel
) {
    LockPortrait()

    LaunchedEffect(subtopicId) {
        viewModel.loadSubtopicDetails(subtopicId)
        viewModel.observeSubtopicProgress(username)
    }

    val subtopicDetails = viewModel.subtopicDetails.collectAsState().value
    val subtopicProgressList = viewModel.subtopicProgress.collectAsState().value

    if (subtopicDetails == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MainBlueBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading subtopic...", color = Color.White, fontSize = 18.sp)
        }
        return
    }

    val subtopic = subtopicDetails.subtopic
    val materials = subtopicDetails.materials
    val progress = subtopicProgressList.find { it.subtopicId == subtopic.subtopicId }



    Column(
        Modifier.fillMaxSize().background(MainBlueBg)
    ) {
        BackButton(
            onClick = {
                navController.navigate(Routes.lessonContentWith(username, lessonId)) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier.padding(start = 25.dp, top = 40.dp).size(40.dp)
        )
        Spacer(Modifier.height(17.dp))

        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(
                text = subtopic.title,
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily,
                color = ThirdBlue,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.align(Alignment.Center).width(300.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(10.dp, RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(SecondBlueBg)
        ) {
            Column(Modifier.padding(top = 25.dp, start = 20.dp, end = 20.dp)) {
                Text(
                    text = "What is ${subtopic.title}?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Left,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = subtopic.content,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = poppinsFontFamily,
                    textAlign = TextAlign.Justify,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(start = 20.dp)
                )

                Spacer(Modifier.height(20.dp))
            }

            // Scrollable content area for materials
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 20.dp)
            ) {
                materials.forEach { material ->
                    val isLocked = when (material.type) {
                        MaterialType.VIDEO -> false
                        MaterialType.HANDS_ON -> progress?.videoCompleted != true
                        MaterialType.SIMULATION -> progress?.problemCompleted != true
                    }

                    val onClick: () -> Unit = when (material.type) {
                        MaterialType.VIDEO -> {
                            { navController.navigate(Routes.videoLessonRoute(username, lessonId, subtopicId)) }
                        }

                        MaterialType.HANDS_ON -> {
                            {
                                if (!isLocked) {
                                    val route = Routes.handsOnRoute(
                                        username = username,
                                        lessonId = lessonId,
                                        subtopicId = subtopicId,
                                        contentPath = material.contentPath // pass the JSON file name here
                                    )
                                    println("🔍 Navigating to hands-on route: $route")
                                    println("🔍 Content path: ${material.contentPath}")
                                    navController.navigate(route)
                                }
                            }
                        }

                        MaterialType.SIMULATION -> {
                            {
                                if (!isLocked) {
                                    // Map subtopic to the correct Unity scene identifier
                                    val sceneId = SimulationMapper.getSceneId(subtopic.title)
                                    if (sceneId != null) {
                                        println("🎮 Launching Unity simulation in Compose: $sceneId for subtopic: ${subtopic.title}")
                                        
                                        // Update progress to mark simulation as completed
                                        // This will unlock the next subtopic
                                        viewModel.updateSubtopicProgress(
                                            username = username,
                                            subtopicId = subtopic.subtopicId,
                                            simulationDone = true
                                        )
                                        println("✅ Simulation progress updated for subtopic: ${subtopic.title}")
                                        
                                        // Force refresh of subtopics to update post-test unlock status
                                        viewModel.initializeSubtopics(username, lessonId, true)
                                        
                                        // Direct Unity launch approach with scene-specific activity
                                        try {
                                            val intent = android.content.Intent().apply {
                                                setClassName(navController.context.packageName, "com.example.motionlab.ui.screens.simulation.SceneSpecificUnityActivity")
                                                // Remove problematic flags that cause app restart
                                                // addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                // addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                // addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                // addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                
                                                // Pass essential parameters for proper return navigation
                                                putExtra("scene_id", sceneId)
                                                putExtra("username", username)
                                                putExtra("lesson_id", lessonId)
                                                putExtra("subtopic_id", subtopic.subtopicId)
                                                putExtra("subtopic_title", subtopic.title)
                                                putExtra("return_to_screen", "subtopic_content")
                                                putExtra("launch_timestamp", System.currentTimeMillis())
                                            }
                                            navController.context.startActivity(intent)
                                            println("✅ Scene-specific Unity simulation launched successfully: $sceneId")
                                        } catch (e: Exception) {
                                            println("❌ Scene-specific Unity launch failed: ${e.message}")
                                            // Fallback to standard Unity activity
                                            try {
                                                val fallbackIntent = android.content.Intent().apply {
                                                    setClassName(navController.context.packageName, "com.unity3d.player.UnityPlayerActivity")
                                                    // Remove problematic flags that cause app restart
                                                    // addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                    // addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                    // addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    // addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    
                                                    // Pass essential parameters for proper return navigation
                                                    putExtra("scene_id", sceneId)
                                                    putExtra("username", username)
                                                    putExtra("lesson_id", lessonId)
                                                    putExtra("subtopic_id", subtopic.subtopicId)
                                                    putExtra("subtopic_title", subtopic.title)
                                                    putExtra("return_to_screen", "subtopic_content")
                                                    putExtra("launch_timestamp", System.currentTimeMillis())
                                                }
                                                navController.context.startActivity(fallbackIntent)
                                                println("✅ Fallback Unity simulation launched successfully: $sceneId")
                                            } catch (fallbackException: Exception) {
                                                println("❌ Fallback Unity launch failed: ${fallbackException.message}")
                                                // Final fallback: Navigate to Unity simulation screen in Compose
                                                navController.navigate("unity_simulation/$sceneId")
                                            }
                                        }
                                    } else {
                                        println("❌ No simulation found for subtopic: ${subtopic.title}")
                                        // You could show a toast or dialog here
                                    }
                                }
                            }
                        }
                    }
                    LessonButton(
                        lessonName = when (material.type) {
                            MaterialType.VIDEO -> "Video Lesson"
                            MaterialType.HANDS_ON -> "Hands-On Exercise"
                            MaterialType.SIMULATION -> "Simulation"
                        },
                        painter = when (material.type) {
                            MaterialType.VIDEO -> painterResource(R.drawable.video_lesson)
                            MaterialType.HANDS_ON -> painterResource(R.drawable.hands_on_exercise)
                            MaterialType.SIMULATION -> painterResource(R.drawable.simulation)
                        },
                        description = material.title,
                        isLocked = isLocked,
                        modifier = Modifier
                            .padding(horizontal = 15.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .height(140.dp), // Consistent button height
                        onClick = {
                            if (!isLocked) {
                                onClick()
                            }
                        }
                    )
                }
            }
        }
    }

    // Handle device back button to go back to subtopics screen
    BackHandler {
        navController.navigate(Routes.lessonContentWith(username, lessonId)) {
            popUpTo(0) { inclusive = true }
        }
    }
}
