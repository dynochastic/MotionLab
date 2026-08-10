package com.example.motionlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.motionlab.ui.navigation.RootNavGraph
import com.example.motionlab.ui.theme.MotionLabTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val EXTRA_USERNAME = "username"
        const val EXTRA_LESSON_ID = "lesson_id"
        const val EXTRA_SUBTOPIC_ID = "subtopic_id"
        const val NAV_POST_TEST = "post_test"
        const val NAV_NEXT_SUBTOPIC_CONTENT = "next_subtopic_content"
    }
    
    private var isUnityRunning = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        
        // Disable Firebase App Check completely for development
        try {
            // Don't install any App Check provider - this disables it
            Log.d(TAG, "Firebase App Check disabled for development")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase App Check not available, continuing without it", e)
        }
        
        enableEdgeToEdge()
        setContent {
            MotionLabTheme {
                val navController = rememberNavController()
                RootNavGraph(navController = navController)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity onResume - Unity running: $isUnityRunning")
        
        if (isUnityRunning) {
            Log.d(TAG, "Returning from Unity - restoring Compose state")
            isUnityRunning = false
            // The Compose UI should automatically restore its state
            // since we're not calling finish() on MainActivity
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity onStop - Unity may be running")
        // Don't finish() here - we need to preserve the activity for return
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity onDestroy")
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "MainActivity onSaveInstanceState")
        // Save any important state here
        outState.putBoolean("isUnityRunning", isUnityRunning)
    }
    
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "MainActivity onRestoreInstanceState")
        isUnityRunning = savedInstanceState.getBoolean("isUnityRunning", false)
    }
    
    /**
     * Called when Unity is launched - marks that Unity is running
     */
    fun setUnityRunning(running: Boolean) {
        isUnityRunning = running
        Log.d(TAG, "Unity running state set to: $running")
    }
    
    /**
     * Check if Unity is currently running
     */
    fun isUnityCurrentlyRunning(): Boolean = isUnityRunning
}
