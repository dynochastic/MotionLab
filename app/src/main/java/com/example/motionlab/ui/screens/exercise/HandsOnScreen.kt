package com.example.motionlab.ui.screens.exercise


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.motionlab.domain.model.local.HandsOnExercise
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.exercise.HandsOnExerciseViewModel
import com.example.motionlab.presentation.lesson.SubtopicViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.components.PillHeader
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.ThirdBlue

/**
 * Main Hands-on Activity Screen
 * This is the entry point for the hands-on exercise interface
 */
@Composable
fun HandsOnActivity(
    navController: NavController,
    username: String,
    lessonId: Int,
    subtopicId: Int,
    contentPath: String,
    viewModel: HandsOnExerciseViewModel
) {
    LockPortrait()

    // Collect state from ViewModel
    val exercises by viewModel.exercises.collectAsState()
    val currentExercise by viewModel.currentExercise.collectAsState()
    val subtopicViewModel: SubtopicViewModel = hiltViewModel()

    // Load exercises when the screen is first displayed
    LaunchedEffect(username, lessonId, subtopicId, contentPath) {
        println("🔄 Loading exercises from: $contentPath")
        println("   - For user: $username")
        println("   - Lesson: $lessonId, Subtopic: $subtopicId")
        viewModel.loadExercisesFromAssets(contentPath)
        subtopicViewModel.observeSubtopicProgress(username)
    }



    Box(Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .background(MainBlueBg),
        ) {

            BackButton(
                onClick = {
                    navController.navigate(Routes.subtopicContentRoute(username, lessonId, subtopicId)) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(start = 25.dp, top = 50.dp).size(50.dp)
            )
            Spacer(Modifier.height(10.dp))


            // Header with title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Hands-on Activity",
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily,
                    color = ThirdBlue,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )
            }

            Spacer(Modifier.height(40.dp))


            // Main content area with rounded top corners
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                        clip = false
                    )
                    .border(
                        width = 1.dp,
                        Color.Black,
                        RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp)
                    )
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(BlueButtonColor)
            ) {
                Column(Modifier.padding(top = 25.dp, start = 10.dp, end = 20.dp)) {

                    // Instructions text
                    Text(
                        text = "Select the correct values and place them in the right formula positions",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = poppinsFontFamily,
                        textAlign = TextAlign.Justify,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 20.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    if (currentExercise != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            EquationScreen(
                                exercise = currentExercise!!,
                                navController = navController,
                                username = username,
                                lessonId = lessonId,
                                subtopicId = subtopicId,
                                subtopicViewModel = subtopicViewModel
                            )
                        }
                    } else if (exercises.isNotEmpty()) {
                        Text(
                            "Loading exercise...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        Text(
                            "No exercises available",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

            }
        }            // Pill Header positioned at the top of the column
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 160.dp),
            contentAlignment = Alignment.Center
        ) {
            PillHeader("Exercise", fontSize = 25)
        }

    }
    // Handle device back button to go back to subtopic material content
    androidx.activity.compose.BackHandler {
        navController.navigate(Routes.subtopicContentRoute(username, lessonId, subtopicId)) {
            popUpTo(0) { inclusive = true }
        }
    }
}

data class ChoiceItem(val id: Int, val value: String)

/**
 * Main Equation Screen Component
 * Handles the interactive drag-and-drop (click-to-place) functionality
 */
@Composable
fun EquationScreen(
    exercise: HandsOnExercise,
    navController: NavController,
    username: String,
    lessonId: Int,
    subtopicId: Int,
    subtopicViewModel: SubtopicViewModel
) {
    val template = exercise.template
    val answers = exercise.answers
    val blankRegex = Regex("__")
    val answerMap = answers.associate { "slot_${it.blankIndex}" to it.value }
    // Create a list of ChoiceItem with unique IDs for each choice
    val initialChoices = remember {
        exercise.choices.mapIndexed { idx, value -> ChoiceItem(idx, value) }
    }
    // State management for the interactive elements
    val availableChoices =
        remember { mutableStateListOf<ChoiceItem>().apply { addAll(initialChoices) } }
    // Now droppedAnswers maps slotKey to a ChoiceItem (not just String)
    val droppedAnswers = remember { mutableStateMapOf<String, ChoiceItem>() }

    // UI state variables
    var showDialog by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var didSubmit by remember { mutableStateOf(false) }

    // Click-to-drag state - tracks what user has selected
    var selectedChoice by remember { mutableStateOf<ChoiceItem?>(null) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }

    // Generate slot keys for each blank space in the template
    val slotKeys = remember {
        val keys = mutableListOf<String>()
        var slotIndex = 0
        val blankRegex = Regex("__")
        template.forEach { line ->
            blankRegex.findAll(line).forEach {
                keys.add("slot_$slotIndex")
                slotIndex++
            }
        }
        keys
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Problem/question text - LEFT ALIGNED and properly sized
            Text(
                exercise.question,
                fontSize = 16.sp, //
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Left, // Left align the problem text
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
            // Reduce the gap between problem and equation
            // Equation template
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp), // Reduce vertical gap
                modifier = Modifier.fillMaxWidth()
            ) {
                var currentSlotIndex = 0
                template.forEach { templateLine ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val words = blankRegex.split(templateLine)
                        words.forEachIndexed { i, word ->
                            // Render the text segment
                            Text(
                                word,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            // After each segment except the last, render a slot
                            if (i < words.size - 1) {
                                val key = slotKeys[currentSlotIndex]
                                val dropped = droppedAnswers[key]
                                val isSelected = selectedSlot == key
                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.1f else 1.0f,
                                    animationSpec = tween(durationMillis = 200),
                                    label = "slotScale"
                                )
                                Box(
                                    modifier = Modifier
                                        .scale(scale)
                                        .size(60.dp, 40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF4CAF50)
                                                dropped != null -> MainBlueBg
                                                else -> Color.White
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 3.dp else 2.dp,
                                            color = if (isSelected) Color(0xFF4CAF50) else Color.Black,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (selectedChoice != null) {
                                                val choice = selectedChoice!!
                                                val replaced = droppedAnswers[key]
                                                if (replaced != null && !availableChoices.contains(
                                                        replaced
                                                    )
                                                ) {
                                                    availableChoices.add(replaced)
                                                }
                                                droppedAnswers[key] = choice
                                                availableChoices.remove(choice)
                                                selectedChoice = null
                                                selectedSlot = null
                                            } else if (dropped != null) {
                                                selectedSlot = key
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dropped != null) {
                                        Text(
                                            dropped.value,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 16.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.clickable {
                                                droppedAnswers.remove(key)
                                                if (!availableChoices.contains(dropped)) {
                                                    availableChoices.add(dropped)
                                                }
                                            }
                                        )
                                    }
                                }
                                currentSlotIndex++
                            }
                        }
                    }
                }
            }

            // Draggable choices in horizontal scrollable row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(50.dp)
            ) {
                items(availableChoices, key = { it.id }) { item ->
                    val isSelected = selectedChoice == item

                    // Animated scale for selected choice - provides visual feedback
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1.0f,
                        animationSpec = tween(durationMillis = 200),
                        label = "choiceScale"
                    )

                    // Individual choice item
                    Box(
                        Modifier
                            .scale(scale)
                            .size(70.dp, 50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF4CAF50) else MainBlueBg
                            )
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) Color(0xFF4CAF50) else Color.Black,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (selectedSlot != null) {
                                    // Place this choice in selected slot
                                    val slotKey = selectedSlot!!

                                    // If slot occupied, return old item to choices
                                    val replaced = droppedAnswers[slotKey]
                                    if (replaced != null && !availableChoices.contains(replaced)) {
                                        availableChoices.add(replaced)
                                    }

                                    // Place new choice in slot
                                    droppedAnswers[slotKey] = item
                                    availableChoices.remove(item)

                                    // Clear selection
                                    selectedChoice = null
                                    selectedSlot = null
                                } else {
                                    // Select this choice
                                    selectedChoice = item
                                    selectedSlot = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.value,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Action buttons row - compact design
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cancel button (visible only when dragging or selected)
                if (selectedChoice != null || selectedSlot != null) {
                    Button(
                        onClick = {
                            selectedChoice = null
                            selectedSlot = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336) // Red
                        ),
                        shape = RoundedCornerShape(20),
                        modifier = Modifier
                            .width(100.dp)
                            .height(50.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(20))
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Maintain layout spacing
                    Spacer(modifier = Modifier.width(100.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Submit button (always visible)
                Button(
                    onClick = {
                        val allSlotKeys = answerMap.keys.toList()
                        val userAnswers = allSlotKeys.map { droppedAnswers[it]?.value }
                        val expectedAnswers = allSlotKeys.map { answerMap[it] }

                        // Require all slots to be filled
                        val allFilled = userAnswers.all { it != null }

                        isCorrect = if (!allFilled) {
                            false
                        } else if (exercise.commutative) {
                            // For commutative operations, check if all values are present
                            // allows for different ordering in multiplication contexts
                            val userValues = userAnswers.filterNotNull()
                            val expectedValues = expectedAnswers.filterNotNull()

                            // Check if all expected values are present in user answers
                            val allExpectedPresent = expectedValues.all { expected ->
                                userValues.contains(expected)
                            }

                            // Check if all user values are expected (no extra wrong values)
                            val noExtraValues = userValues.all { user ->
                                expectedValues.contains(user)
                            }

                            // Additional check: ensure the count of each value matches
                            val userCounts = userValues.groupingBy { it }.eachCount()
                            val expectedCounts = expectedValues.groupingBy { it }.eachCount()
                            val countsMatch = userCounts == expectedCounts

                            allExpectedPresent && noExtraValues && countsMatch
                        } else {
                            // For non-commutative operations, require exact position matching
                            answerMap.entries.all { (key, expectedValue) ->
                                droppedAnswers[key]?.value == expectedValue
                            }
                        }
                        showDialog = true
                        didSubmit = true
                        if (isCorrect) {
                            subtopicViewModel.updateSubtopicProgress(
                                username = username,
                                subtopicId = subtopicId,
                                problemDone = true
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF003366) // Dark blue
                    ),
                    shape = RoundedCornerShape(20),
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(20))
                ) {
                    Text(
                        text = "Submit",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }


            // Result Dialog - shows success or failure
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDialog = false
                                if (isCorrect && didSubmit) {
                                    // Update simulation progress to unlock next subtopic
                                    subtopicViewModel.updateSubtopicProgress(
                                        username = username,
                                        subtopicId = subtopicId,
                                        simulationDone = true
                                    )
                                    println("✅ Simulation progress updated for subtopic: $subtopicId")
                                    
                                    // Force refresh of subtopics to update post-test unlock status
                                    subtopicViewModel.initializeSubtopics(username, lessonId, true)
                                    
                                    // Navigate directly to Unity simulation instead of Compose simulation
                                    // Get the subtopic title to map to the correct Unity scene
                                    val subtopicTitle = subtopicViewModel.subtopicDetails.value?.subtopic?.title
                                    if (subtopicTitle != null) {
                                        val sceneId = com.example.motionlab.ui.screens.simulation.SimulationMapper.getSceneId(subtopicTitle)
                                        if (sceneId != null) {
                                            println("🎮 Launching Unity simulation from hands-on: $sceneId for subtopic: $subtopicTitle")
                                            try {
                                                val intent = android.content.Intent().apply {
                                                    setClassName(navController.context.packageName, "com.example.motionlab.ui.screens.simulation.SceneSpecificUnityActivity")
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    putExtra("scene_id", sceneId)
                                                }
                                                navController.context.startActivity(intent)
                                                println("✅ Unity simulation launched successfully from hands-on: $sceneId")
                                            } catch (e: Exception) {
                                                println("❌ Unity launch failed from hands-on: ${e.message}")
                                                // Fallback to standard Unity activity
                                                try {
                                                    val fallbackIntent = android.content.Intent().apply {
                                                        setClassName(navController.context.packageName, "com.unity3d.player.UnityPlayerActivity")
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        putExtra("scene_id", sceneId)
                                                    }
                                                    navController.context.startActivity(fallbackIntent)
                                                    println("✅ Fallback Unity simulation launched successfully: $sceneId")
                                                } catch (fallbackException: Exception) {
                                                    println("❌ Fallback Unity launch failed: ${fallbackException.message}")
                                                }
                                            }
                                        } else {
                                            println("❌ No simulation found for subtopic: $subtopicTitle")
                                        }
                                    }
                                }
                                didSubmit = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCorrect) Color(0xFF4CAF50) else Color(
                                    0xFF2196F3
                                )
                            )
                        ) {
                            Text(
                                if (isCorrect) "Continue" else "Try Again",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    title = {
                        Text(
                            if (isCorrect) "🎉 Excellent!" else "❌ Try Again",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    },
                    text = {
                        Column {
                            Text(
                                if (isCorrect)
                                    "Perfect! You've solved the equation correctly. You can try another exercise or continue with the lesson."
                                else
                                    "Some answers are incorrect. Check your work and try again.",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Justify,
                                color = Color.Black
                            )
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}