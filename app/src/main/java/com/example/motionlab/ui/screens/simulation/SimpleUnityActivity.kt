package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * Simple Unity Activity that handles Unity launch with proper back navigation
 * This is the simplest approach that should work reliably
 */
class SimpleUnityActivity : Activity() {
    
    companion object {
        private const val TAG = "SimpleUnity"
        const val EXTRA_SCENE_ID = "scene_id"
        const val EXTRA_RETURN_TO_COMPOSE = "return_to_compose"
    }
    
    private var sceneId: String? = null
    private var returnToCompose: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sceneId = intent.getStringExtra(EXTRA_SCENE_ID)
        returnToCompose = intent.getBooleanExtra(EXTRA_RETURN_TO_COMPOSE, false)
        
        Log.d(TAG, "Simple Unity Activity created with sceneId: $sceneId, returnToCompose: $returnToCompose")
        
        launchUnityDirectly()
    }
    
    private fun launchUnityDirectly() {
        try {
            val unityIntent = Intent().apply {
                setClassName(packageName, "com.unity3d.player.UnityPlayerActivity")
                
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    Log.d(TAG, "Passing scene ID to Unity: $sceneId")
                }

                putExtra(EXTRA_RETURN_TO_COMPOSE, returnToCompose)
            }
            
            Log.d(TAG, "Launching Unity with proper activity stack")
            startActivityForResult(unityIntent, 1001)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Unity: ${e.message}")
            finish()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        Log.d(TAG, "SimpleUnityActivity.onActivityResult - requestCode: $requestCode, resultCode: $resultCode")
        
        if (requestCode == 1001) {
            Log.d(TAG, "Unity returned with result code: $resultCode")
            
            val resultIntent = Intent().apply {
                putExtra("unity_finished", true)
                putExtra("unity_result_code", resultCode)
                putExtra("unity_timestamp", System.currentTimeMillis())
                
                data?.extras?.let { extras ->
                    Log.d(TAG, "Passing through Unity data: $extras")
                    putExtras(extras)
                } ?: run {
                    Log.d(TAG, "No Unity data to pass through")
                }
            }
            
            Log.d(TAG, "Setting RESULT_OK with Unity completion data")
            setResult(RESULT_OK, resultIntent)
            
            Log.d(TAG, "Finishing SimpleUnityActivity NOW to return to Compose")
            finish()
        } else {
            Log.w(TAG, "Unexpected request code in SimpleUnityActivity: $requestCode")
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "SimpleUnityActivity resumed")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "SimpleUnityActivity paused")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "SimpleUnityActivity stopped")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SimpleUnityActivity destroyed")
    }
    
    override fun onBackPressed() {
        Log.d(TAG, "Back pressed in Simple Unity Activity")
        finish()
    }
}






