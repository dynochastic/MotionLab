package com.example.motionlab.ui.screens.simulation

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.R
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.lesson.SubtopicViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.ShortcutButton
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun SimulationShortcut(
    username: String,
    viewModel: SubtopicViewModel
){
    LockPortrait()
    val context = LocalContext.current
    
    // Load progress for all subtopics
    LaunchedEffect(username) {
        viewModel.observeSubtopicProgress(username)
    }
    
    val subtopicProgressList = viewModel.subtopicProgress.collectAsState().value
    
    // Helper function to check if a simulation is unlocked based on subtopic progress
    // This matches the logic in SubtopicContentMaterials.kt line 145: progress?.problemCompleted != true
    fun isSimulationUnlocked(subtopicTitle: String): Boolean {
        // Create a mapping of simulation titles to their corresponding subtopic IDs
        // This is based on the order of subtopics in the database
        val simulationToSubtopicIdMap = mapOf(
            "uniformly accelerated motion" to 1,  // First subtopic in mechanics
            "projectile motion" to 2,             // Second subtopic in mechanics  
            "momentum" to 3,                     // Third subtopic in mechanics
            "law of inertia" to 4,               // First Newton's law
            "force = mass x acceleration" to 5, // Second Newton's law
            "action-reaction" to 6,             // Third Newton's law
            "work" to 7,                         // First in work/power/energy
            "power" to 8,                       // Second in work/power/energy
            "energy" to 9                       // Third in work/power/energy
        )
        
        // Get the expected subtopic ID for this simulation
        val expectedSubtopicId = simulationToSubtopicIdMap[subtopicTitle.lowercase().trim()]
        
        if (expectedSubtopicId == null) {
            println("❌ No subtopic ID found for simulation: $subtopicTitle")
            return false
        }
        
        // Find the progress for this specific subtopic
        val progress = subtopicProgressList.find { it.subtopicId == expectedSubtopicId }
        
        if (progress == null) {
            println("❌ No progress found for subtopic ID: $expectedSubtopicId (title: $subtopicTitle)")
            return false
        }
        
        val isUnlocked = progress.problemCompleted == true
        println("🔍 Simulation unlock check - Title: $subtopicTitle, ID: $expectedSubtopicId, Progress: $progress, Unlocked: $isUnlocked")
        
        return isUnlocked
    }
    
    // Helper function to launch Unity simulation
    fun launchUnitySimulation(subtopicTitle: String) {
        val sceneId = SimulationMapper.getSceneId(subtopicTitle)
        if (sceneId != null) {
            // Update progress to mark simulation as completed
            // This will unlock the next subtopic
            val simulationToSubtopicIdMap = mapOf(
                "uniformly accelerated motion" to 1,
                "projectile motion" to 2,
                "momentum" to 3,
                "law of inertia" to 4,
                "force = mass x acceleration" to 5,
                "action-reaction" to 6,
                "work" to 7,
                "power" to 8,
                "energy" to 9
            )
            
            val subtopicId = simulationToSubtopicIdMap[subtopicTitle.lowercase().trim()]
            if (subtopicId != null) {
                viewModel.updateSubtopicProgress(
                    username = username,
                    subtopicId = subtopicId,
                    simulationDone = true
                )
                println("✅ Simulation progress updated for subtopic: $subtopicTitle (ID: $subtopicId)")
            }
            
            try {
                val intent = android.content.Intent().apply {
                    setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.SceneSpecificUnityActivity")
                    // Remove problematic flags that cause app restart
                    // addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    // addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    // addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    // addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("scene_id", sceneId)
                    putExtra("username", username)
                    putExtra("subtopic_title", subtopicTitle)
                    putExtra("subtopic_id", subtopicId ?: -1)
                    putExtra("return_to_screen", "simulation_shortcut")
                    putExtra("launch_timestamp", System.currentTimeMillis())
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to standard Unity activity
                try {
                    val fallbackIntent = android.content.Intent().apply {
                        setClassName(context.packageName, "com.unity3d.player.UnityPlayerActivity")
                        // Remove problematic flags that cause app restart
                        // addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        // addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        // addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        // addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("scene_id", sceneId)
                    }
                    context.startActivity(fallbackIntent)
                } catch (fallbackException: Exception) {
                    Toast.makeText(context, "Simulation not available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    // Helper function to show locked simulation toast
    fun showLockedToast(subtopicTitle: String) {
        Toast.makeText(context, "Complete all materials in $subtopicTitle to unlock this simulation", Toast.LENGTH_LONG).show()
    }
    
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

        Spacer(Modifier.fillMaxHeight(0.02f))

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
            LazyColumn(
                modifier = Modifier.padding(top = 25.dp, start = 20.dp, end = 20.dp, bottom = 5.dp)
            ) {
                // HEADER SECTION
                item {
                    Text(
                        text = "Simulation",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = poppinsFontFamily,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Experience the laws of physics firsthand as you simulate and explore the forces that govern everything around us.",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontFamily = poppinsFontFamily,
                        textAlign = TextAlign.Justify,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                }
                
                // MECHANICS SECTION
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = ThirdBlue,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = "🔧 MECHANICS",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    Column {
                        Text(
                            text = "Uniformly Accelerated Motion",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )

                        val uamUnlocked = isSimulationUnlocked("uniformly accelerated motion")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.uam_tb),
                            contentDescription = if (uamUnlocked) "Unlocked" else "Locked",
                            isLocked = !uamUnlocked,
                            onClick = { 
                                if (uamUnlocked) {
                                    launchUnitySimulation("uniformly accelerated motion")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Uniformly Accelerated Motion")
                            }
                        )
                        Spacer(Modifier.height(13.dp))
                        Text(
                            text = "Projectile Motion",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val pmUnlocked = isSimulationUnlocked("projectile motion")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.projectile_motion_tb),
                            contentDescription = if (pmUnlocked) "Unlocked" else "Locked",
                            isLocked = !pmUnlocked,
                            onClick = { 
                                if (pmUnlocked) {
                                    launchUnitySimulation("projectile motion")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Projectile Motion")
                            }
                        )
                        Spacer(Modifier.height(13.dp))

                        Text(
                            text = "Momentum",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val momentumUnlocked = isSimulationUnlocked("momentum")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.momentum_tb),
                            contentDescription = if (momentumUnlocked) "Unlocked" else "Locked",
                            isLocked = !momentumUnlocked,
                            onClick = { 
                                if (momentumUnlocked) {
                                    launchUnitySimulation("momentum")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Momentum")
                            }
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = ThirdBlue,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                             Text(
                                 text = "⚖️ NEWTON'S LAWS",
                                 color = Color.White,
                                 fontSize = 18.sp,
                                 fontFamily = poppinsFontFamily,
                                 fontWeight = FontWeight.Bold,
                                 modifier = Modifier.align(Alignment.CenterStart)
                             )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Law of Inertia",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val firstLawUnlocked = isSimulationUnlocked("law of inertia")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.first_law_tb),
                            contentDescription = if (firstLawUnlocked) "Unlocked" else "Locked",
                            isLocked = !firstLawUnlocked,
                            onClick = { 
                                if (firstLawUnlocked) {
                                    launchUnitySimulation("law of inertia")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Law of Inertia")
                            }
                        )
                        Spacer(Modifier.height(13.dp))

                        Text(
                            text = "Law of Force and Acceleration",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val secondLawUnlocked = isSimulationUnlocked("force = mass x acceleration")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.second_law_tb),
                            contentDescription = if (secondLawUnlocked) "Unlocked" else "Locked",
                            isLocked = !secondLawUnlocked,
                            onClick = { 
                                if (secondLawUnlocked) {
                                    launchUnitySimulation("force = mass x acceleration")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Law of Force and Acceleration")
                            }
                        )
                        Spacer(Modifier.height(13.dp))

                        Text(
                            text = "Action-Reaction",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val thirdLawUnlocked = isSimulationUnlocked("action-reaction")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.third_law_tb),
                            contentDescription = if (thirdLawUnlocked) "Unlocked" else "Locked",
                            isLocked = !thirdLawUnlocked,
                            onClick = { 
                                if (thirdLawUnlocked) {
                                    launchUnitySimulation("action-reaction")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Action-Reaction")
                            }
                        )
                    }
                    Column {
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = ThirdBlue,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                text = "⚡ WORK, POWER & ENERGY",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = "Work",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val workUnlocked = isSimulationUnlocked("work")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.work_tb),
                            contentDescription = if (workUnlocked) "Unlocked" else "Locked",
                            isLocked = !workUnlocked,
                            onClick = { 
                                if (workUnlocked) {
                                    launchUnitySimulation("work")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Work")
                            }
                        )
                        Spacer(Modifier.height(13.dp))

                        Text(
                            text = "Power",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        Spacer(Modifier.height(13.dp))

                        val powerUnlocked = isSimulationUnlocked("power")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.power_tb),
                            contentDescription = if (powerUnlocked) "Unlocked" else "Locked",
                            isLocked = !powerUnlocked,
                            onClick = { 
                                if (powerUnlocked) {
                                    launchUnitySimulation("power")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Power")
                            }
                        )
                        Spacer(Modifier.height(13.dp))

                        Text(
                            text = "Energy",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                        val energyUnlocked = isSimulationUnlocked("energy")
                        ShortcutButton(
                            painter = painterResource(id = R.drawable.energy_tb),
                            contentDescription = if (energyUnlocked) "Unlocked" else "Locked",
                            isLocked = !energyUnlocked,
                            onClick = { 
                                if (energyUnlocked) {
                                    launchUnitySimulation("energy")
                                }
                            },
                            onLockedClick = { 
                                showLockedToast("Energy")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview (showBackground = true)
@Composable
fun SimPreview(){
    // Note: Preview doesn't work with ViewModel parameters
    // SimulationShortcut("test_user", viewModel)
}