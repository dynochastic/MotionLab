package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.motionlab.data.local.db.AppDatabase
import com.example.motionlab.MainActivity

/**
 * Scene-specific Unity launcher that handles scene_id parameter
 * This activity launches Unity with the appropriate scene_id parameter
 */
class SceneSpecificUnityActivity : Activity() {
    
    companion object {
        private const val TAG = "SceneSpecificUnity"
        const val EXTRA_SCENE_ID = "scene_id"
        const val EXTRA_USERNAME = "username"
        const val EXTRA_LESSON_ID = "lesson_id"
        const val EXTRA_SUBTOPIC_ID = "subtopic_id"
        const val EXTRA_RETURN_TO_SCREEN = "return_to_screen"
    }
    
    private var sceneId: String? = null
    private var username: String? = null
    private var lessonId: Int = -1
    private var subtopicId: Int = -1
    private var returnToScreen: String? = null
    private var unityLaunched = false
    private var unityReturnHandled = false
    private var wasPaused = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sceneId = intent.getStringExtra(EXTRA_SCENE_ID)
        username = intent.getStringExtra(EXTRA_USERNAME)
        lessonId = intent.getIntExtra(EXTRA_LESSON_ID, -1)
        subtopicId = intent.getIntExtra(EXTRA_SUBTOPIC_ID, -1)
        returnToScreen = intent.getStringExtra(EXTRA_RETURN_TO_SCREEN)
        
        Log.d(TAG, "Scene ID received: $sceneId, username: $username, lessonId: $lessonId, subtopicId: $subtopicId, returnToScreen: $returnToScreen")
        
        launchUnityWithSceneId()
    }
    
    private fun launchUnityWithSceneId() {
        try {
            val unityIntent = Intent().apply {
                setClassName(packageName, "com.unity3d.player.UnityPlayerActivity")
                
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    Log.d(TAG, "Passing scene ID to Unity: $sceneId")
                }
                
                if (!sceneId.isNullOrEmpty()) {
                    putExtra("unity", "-scene_id $sceneId")
                    Log.d(TAG, "Setting Unity command line: -scene_id $sceneId")
                }
            }
            
            Log.d(TAG, "Launching Unity with scene ID: $sceneId")
            unityLaunched = true
            unityReturnHandled = false
            startActivityForResult(unityIntent, 1002)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Unity: ${e.message}")
            showErrorAndFinish("Failed to launch Unity simulation: ${e.message}")
        }
    }
    
    private fun showErrorAndFinish(message: String) {
        val textView = TextView(this).apply {
            text = "Error: $message\n\nScene ID: $sceneId"
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }
        setContentView(textView)
        
        textView.postDelayed({
            finish()
        }, 3000)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        Log.d(TAG, "SceneSpecificUnityActivity.onActivityResult - requestCode: $requestCode, resultCode: $resultCode")
        
        if (requestCode == 1002) {
            val hasBackPressedFlag = data?.getBooleanExtra("unity_back_pressed", false) == true
            val isResultCanceled = resultCode == Activity.RESULT_CANCELED
            
            Log.d(TAG, "✅ onActivityResult called - Unity returned with result code: $resultCode")
            Log.d(TAG, "   hasBackPressedFlag: $hasBackPressedFlag")
            Log.d(TAG, "   isResultCanceled: $isResultCanceled")
            Log.d(TAG, "   Current subtopicId: $subtopicId")
            
            val wasBackPressed = hasBackPressedFlag || isResultCanceled
            
            if (wasBackPressed) {
                Log.d(TAG, "⚠️ User pressed back in simulation - returning to previous screen")
                Log.d(TAG, "   returnToScreen: $returnToScreen")
                
                if (returnToScreen == "simulation_shortcut" && username != null) {
                    Log.d(TAG, "   → Returning to simulation shortcut page")
                    navigateToSimulationShortcut()
                } else {
                    Log.d(TAG, "   → Just finishing (will return to previous screen)")
                    unityReturnHandled = true
                    finish()
                }
            } else {
                Log.d(TAG, "✅ Simulation was completed - handling return and navigation")
                handleUnityReturn()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SceneSpecificUnityActivity.onResume - unityLaunched: $unityLaunched, unityReturnHandled: $unityReturnHandled, wasPaused: $wasPaused")
        
        if (unityLaunched && wasPaused && !unityReturnHandled && !isFinishing) {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (unityLaunched && wasPaused && !unityReturnHandled && !isFinishing) {
                    Log.d(TAG, "⚠️ FALLBACK: onActivityResult not called, treating as back press")
                    if (returnToScreen == "simulation_shortcut" && username != null) {
                        Log.d(TAG, "   → Returning to simulation shortcut page")
                        navigateToSimulationShortcut()
                    } else {
                        Log.d(TAG, "   → Just finishing")
                        unityReturnHandled = true
                        finish()
                    }
                }
            }, 800)
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "SceneSpecificUnityActivity.onPause")
        if (unityLaunched) {
            wasPaused = true
            Log.d(TAG, "Marked wasPaused = true (Unity is running)")
        }
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "SceneSpecificUnityActivity.onStop")
    }
    
    private fun handleUnityReturn() {
        Log.d(TAG, "🔄🔄🔄 handleUnityReturn() CALLED 🔄🔄🔄")
        Log.d(TAG, "   unityReturnHandled: $unityReturnHandled")
        Log.d(TAG, "   username: $username")
        Log.d(TAG, "   subtopicId: $subtopicId")
        Log.d(TAG, "   lessonId: $lessonId")
        
        if (unityReturnHandled) {
            Log.d(TAG, "⚠️ Unity return already handled, skipping")
            return
        }
        
        if (username == null || subtopicId == -1) {
            Log.e(TAG, "❌ Missing required parameters for navigation: username=$username, subtopicId=$subtopicId")
            unityReturnHandled = true
            finish()
            return
        }
        
        unityReturnHandled = true
        Log.d(TAG, "✅ Starting Unity return handling - username: $username, subtopicId: $subtopicId")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.instance
                if (database == null) {
                    Log.e(TAG, "Database instance is null")
                    runOnUiThread { finish() }
                    return@launch
                }
                
                val actualLessonId = database.subtopicDao().getLessonIdForSubtopic(subtopicId)
                if (actualLessonId == -1) {
                    Log.e(TAG, "Could not find lessonId for subtopic $subtopicId")
                    runOnUiThread { finish() }
                    return@launch
                }
                
                val currentSubtopic = database.subtopicDao().getSubtopicById(subtopicId)
                Log.d(TAG, "Current subtopic: ${currentSubtopic.title} (ID: $subtopicId), belongs to lessonId: $actualLessonId")
                Log.d(TAG, "Intent provided lessonId: $lessonId, actual lessonId from DB: $actualLessonId")
                
                val subtopics = database.subtopicDao().getSubtopicsForLesson(actualLessonId).sortedBy { it.order }
                Log.d(TAG, "Found ${subtopics.size} subtopics for lesson $actualLessonId (sorted by order)")
                subtopics.forEachIndexed { index, subtopic ->
                    Log.d(TAG, "  Subtopic $index: ${subtopic.title} (ID: ${subtopic.subtopicId}, order: ${subtopic.order})")
                }
                
                val currentIndex = subtopics.indexOfFirst { it.subtopicId == subtopicId }
                
                if (currentIndex == -1) {
                    Log.e(TAG, "Current subtopic $subtopicId not found in lesson $actualLessonId")
                    runOnUiThread { finish() }
                    return@launch
                }
                
                Log.d(TAG, "Current subtopic index: $currentIndex (out of ${subtopics.size - 1} total)")
                
                val isLastSubtopic = currentIndex == subtopics.size - 1
                val currentSubtopicOrder = currentSubtopic.order
                
                val isKnownLastSubtopic = when (subtopicId) {
                    3 -> {
                        Log.d(TAG, "✅✅✅ Detected Momentum (ID 3) - last subtopic in Mechanics (Lesson $actualLessonId)")
                        Log.d(TAG, "   → Will navigate to POST-TEST for Lesson $actualLessonId (Mechanics)")
                        true
                    }
                    6 -> {
                        Log.d(TAG, "✅✅✅ Detected Action-Reaction (ID 6) - last subtopic in Newton's Laws (Lesson $actualLessonId)")
                        Log.d(TAG, "   → Will navigate to POST-TEST for Lesson $actualLessonId (Newton's Laws)")
                        true
                    }
                    9 -> {
                        Log.d(TAG, "✅✅✅ Detected Energy (ID 9) - last subtopic in Work/Power/Energy (Lesson $actualLessonId)")
                        Log.d(TAG, "   → Will navigate to POST-TEST for Lesson $actualLessonId (Work/Power/Energy)")
                        true
                    }
                    else -> {
                        val normalizedTitle = currentSubtopic.title.lowercase().trim()
                        val isTitleMatch = normalizedTitle in listOf("momentum", "action-reaction", "energy")
                        if (isTitleMatch) {
                            val maxOrder = subtopics.maxOfOrNull { it.order } ?: -1
                            val isMaxOrder = currentSubtopicOrder == maxOrder
                            Log.d(TAG, "✅ Detected by title: '$normalizedTitle' (order: $currentSubtopicOrder, max: $maxOrder, isMax: $isMaxOrder)")
                            isMaxOrder
                        } else {
                            false
                        }
                    }
                }
                
                val shouldNavigateToPostTest = isLastSubtopic || isKnownLastSubtopic
                
                Log.d(TAG, "=== NAVIGATION CHECK ===")
                Log.d(TAG, "Current subtopic: '${currentSubtopic.title}' (ID: $subtopicId, order: $currentSubtopicOrder)")
                Log.d(TAG, "Current index: $currentIndex of ${subtopics.size - 1}")
                Log.d(TAG, "Is last subtopic (by index): $isLastSubtopic")
                Log.d(TAG, "Is known last subtopic (Momentum/Action-Reaction/Energy): $isKnownLastSubtopic")
                Log.d(TAG, "Should navigate to post-test: $shouldNavigateToPostTest")
                Log.d(TAG, "Total subtopics in lesson: ${subtopics.size}")
                
                if (subtopicId == 3 || subtopicId == 6 || subtopicId == 9) {
                    Log.d(TAG, "🎯 CRITICAL: Detected last subtopic by ID!")
                    Log.d(TAG, "   - Subtopic ID: $subtopicId")
                    Log.d(TAG, "   - Title: '${currentSubtopic.title}'")
                    Log.d(TAG, "   - Will navigate to post-test: $shouldNavigateToPostTest")
                }
                
                if (!shouldNavigateToPostTest) {
                    val nextSubtopic = subtopics[currentIndex + 1]
                    Log.d(TAG, "Next subtopic will be: '${nextSubtopic.title}' (ID: ${nextSubtopic.subtopicId}, order: ${nextSubtopic.order})")
                } else {
                    Log.d(TAG, "✅✅✅ THIS IS THE LAST SUBTOPIC - WILL NAVIGATE TO POST-TEST ✅✅✅")
                }
                
                val currentProgress = database.subtopicProgressDao().getProgress(username!!, subtopicId)
                val wasAlreadyCompleted = currentProgress?.simulationCompleted == true
                
                if (wasAlreadyCompleted) {
                    Log.d(TAG, "⚠️ Simulation was already completed - this is NOT the first time")
                    Log.d(TAG, "   → Just returning to previous screen, NOT navigating")
                    runOnUiThread {
                        if (returnToScreen == "simulation_shortcut" && username != null) {
                            navigateToSimulationShortcut()
                        } else {
                            finish()
                        }
                    }
                    return@launch
                }
                
                Log.d(TAG, "✅ FIRST TIME completing simulation - updating progress and navigating")
                
                val updatedProgress = if (currentProgress != null) {
                    currentProgress.copy(simulationCompleted = true)
                } else {
                    Log.w(TAG, "Progress entry doesn't exist for subtopic $subtopicId, creating new entry")
                    com.example.motionlab.data.local.entity.SubtopicProgressEntity(
                        username = username!!,
                        subtopicId = subtopicId,
                        videoCompleted = true,
                        problemCompleted = true,
                        simulationCompleted = true
                    )
                }
                database.subtopicProgressDao().insertOrUpdateProgress(updatedProgress)
                Log.d(TAG, "✅ Marked simulation as completed for subtopic $subtopicId. Progress: video=${updatedProgress.videoCompleted}, problem=${updatedProgress.problemCompleted}, simulation=${updatedProgress.simulationCompleted}")
                
                val isCurrentSubtopicCompleted = updatedProgress.videoCompleted && 
                                                 updatedProgress.problemCompleted && 
                                                 updatedProgress.simulationCompleted
                
                Log.d(TAG, "Current subtopic completed status: $isCurrentSubtopicCompleted")
                
                if (isCurrentSubtopicCompleted && !shouldNavigateToPostTest) {
                    val nextSubtopic = subtopics[currentIndex + 1]
                    val nextProgress = database.subtopicProgressDao().getProgress(username!!, nextSubtopic.subtopicId)
                    
                    if (nextProgress == null) {
                        val nextSubtopicProgress = com.example.motionlab.data.local.entity.SubtopicProgressEntity(
                            username = username!!,
                            subtopicId = nextSubtopic.subtopicId,
                            videoCompleted = false,
                            problemCompleted = false,
                            simulationCompleted = false
                        )
                        database.subtopicProgressDao().insertOrUpdateProgress(nextSubtopicProgress)
                        Log.d(TAG, "✅ Created progress entry for next subtopic: ${nextSubtopic.title} (ID: ${nextSubtopic.subtopicId}) - Subtopic is now unlocked")
                    } else {
                        Log.d(TAG, "Next subtopic already has progress entry: ${nextSubtopic.title} (ID: ${nextSubtopic.subtopicId})")
                    }
                }
                
                runOnUiThread {
                    if (shouldNavigateToPostTest) {
                        Log.d(TAG, "✅ LAST SUBTOPIC DETECTED - Navigating to post-test for lesson $actualLessonId")
                        Log.d(TAG, "   Subtopic: '${currentSubtopic.title}' (ID: $subtopicId)")
                        navigateToPostTest(actualLessonId)
                    } else {
                        val nextSubtopic = subtopics[currentIndex + 1]
                        Log.d(TAG, "➡️ NOT LAST SUBTOPIC - Navigating to next subtopic content: ${nextSubtopic.title} (ID: ${nextSubtopic.subtopicId}) in lesson $actualLessonId")
                        navigateToNextSubtopicContent(nextSubtopic.subtopicId, actualLessonId)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling Unity return: ${e.message}", e)
                e.printStackTrace()
                runOnUiThread { finish() }
            }
        }
    }
    
    private fun navigateToSimulationShortcut() {
        Log.d(TAG, "🔄 Navigating back to simulation shortcut page")
        Log.d(TAG, "   → Username: $username")
        Log.d(TAG, "   → Unity return handled: $unityReturnHandled")
        
        if (username == null) {
            Log.e(TAG, "❌ Missing username for simulation shortcut navigation")
            unityReturnHandled = true
            finish()
            return
        }
        
        unityReturnHandled = true
        
        try {
            val intent = Intent().apply {
                setClassName(packageName, "com.example.motionlab.MainActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(MainActivity.EXTRA_NAVIGATE_TO, "simulation_shortcut")
                putExtra(MainActivity.EXTRA_USERNAME, username)
            }
            Log.d(TAG, "✅ Starting MainActivity to return to simulation shortcut")
            Log.d(TAG, "   → Intent extras: navigateTo=simulation_shortcut, username=$username")
            startActivity(intent)
            Log.d(TAG, "✅ MainActivity started, finishing SceneSpecificUnityActivity")
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error navigating to simulation shortcut: ${e.message}", e)
            e.printStackTrace()
            finish()
        }
    }
    
    private fun navigateToPostTest(actualLessonId: Int) {
        Log.d(TAG, "🚀🚀🚀 NAVIGATING TO POST-TEST 🚀🚀🚀")
        Log.d(TAG, "   Lesson ID: $actualLessonId")
        Log.d(TAG, "   Username: $username")
        Log.d(TAG, "   Route will be: testRulesScreen/$username/$actualLessonId/false")
        
        if (username.isNullOrEmpty()) {
            Log.e(TAG, "❌ CRITICAL: Username is null or empty! Cannot navigate to post-test.")
            Log.e(TAG, "   This would cause navigation crash. Finishing activity instead.")
            finish()
            return
        }
        
        try {
            val intent = Intent().apply {
                setClassName(packageName, "com.example.motionlab.MainActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(MainActivity.EXTRA_NAVIGATE_TO, MainActivity.NAV_POST_TEST)
                putExtra(MainActivity.EXTRA_USERNAME, username!!)
                putExtra(MainActivity.EXTRA_LESSON_ID, actualLessonId)
            }
            Log.d(TAG, "✅ Starting MainActivity with post-test navigation intent")
            Log.d(TAG, "   Intent extras: username=${intent.getStringExtra(MainActivity.EXTRA_USERNAME)}, lessonId=${intent.getIntExtra(MainActivity.EXTRA_LESSON_ID, -1)}")
            startActivity(intent)
            Log.d(TAG, "✅ Finished SceneSpecificUnityActivity - should now be at post-test")
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error navigating to post-test: ${e.message}", e)
            e.printStackTrace()
            finish()
        }
    }
    
    private fun navigateToNextSubtopicContent(nextSubtopicId: Int, actualLessonId: Int) {
        Log.d(TAG, "Navigating to next subtopic content: $nextSubtopicId in lesson $actualLessonId")
        try {
            val intent = Intent().apply {
                setClassName(packageName, "com.example.motionlab.MainActivity")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra(MainActivity.EXTRA_NAVIGATE_TO, MainActivity.NAV_NEXT_SUBTOPIC_CONTENT)
                putExtra(MainActivity.EXTRA_USERNAME, username)
                putExtra(MainActivity.EXTRA_LESSON_ID, actualLessonId)
                putExtra(MainActivity.EXTRA_SUBTOPIC_ID, nextSubtopicId)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to next subtopic content: ${e.message}", e)
            e.printStackTrace()
            finish()
        }
    }
    
    /**
     * Method to get the current scene ID
     */
    fun getCurrentSceneId(): String? = sceneId
}
