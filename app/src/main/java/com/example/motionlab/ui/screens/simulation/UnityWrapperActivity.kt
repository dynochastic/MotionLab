package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Unity wrapper activity that handles proper back navigation to Compose app
 * This activity ensures Unity can return to the previous Compose activity
 */
class UnityWrapperActivity : Activity() {
    
    companion object {
        private const val TAG = "UnityWrapper"
        const val EXTRA_SCENE_ID = "scene_id"
        const val EXTRA_RETURN_TO_COMPOSE = "return_to_compose"
    }
    
    private var sceneId: String? = null
    private var returnToCompose: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sceneId = intent.getStringExtra(EXTRA_SCENE_ID)
        returnToCompose = intent.getBooleanExtra(EXTRA_RETURN_TO_COMPOSE, false)
        
        Log.d(TAG, "Unity wrapper created with sceneId: $sceneId, returnToCompose: $returnToCompose")
        
        launchUnityWithBackNavigation()
    }
    
    private fun launchUnityWithBackNavigation() {
        try {
               val unityIntent = Intent().apply {
                   setClassName(packageName, "com.example.motionlab.ui.screens.simulation.SimpleUnityActivity")
                   
                   if (!sceneId.isNullOrEmpty()) {
                       putExtra(EXTRA_SCENE_ID, sceneId)
                       Log.d(TAG, "Passing scene ID to Unity: $sceneId")
                   }

                   putExtra(EXTRA_RETURN_TO_COMPOSE, returnToCompose)
               }
            
            Log.d(TAG, "Launching Custom Unity Player with proper back navigation")
            startActivity(unityIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Custom Unity Player: ${e.message}")
            try {
                val fallbackIntent = Intent().apply {
                    setClassName(packageName, "com.unity3d.player.UnityPlayerActivity")
                    if (!sceneId.isNullOrEmpty()) {
                        putExtra(EXTRA_SCENE_ID, sceneId)
                    }
                    putExtra(EXTRA_RETURN_TO_COMPOSE, returnToCompose)
                }
                startActivity(fallbackIntent)
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Fallback Unity launch also failed: ${fallbackException.message}")
                finish()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Unity wrapper paused")
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Unity wrapper resumed")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Unity wrapper stopped - keeping alive for back navigation")
    }
    
    override fun onBackPressed() {
        Log.d(TAG, "Back pressed in Unity wrapper")
        super.onBackPressed()
    }
}