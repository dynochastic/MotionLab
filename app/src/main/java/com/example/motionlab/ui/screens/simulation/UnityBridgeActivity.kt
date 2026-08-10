package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.motionlab.debug.UnityDebugLogger
import com.example.motionlab.debug.UnityCrashFixer

/**
 * Unity Bridge Activity that works with Back_Button_Handler_Improved.cs
 * This activity launches Unity and handles proper back navigation without extending Unity classes
 * Prevents app stops/restarts by maintaining proper lifecycle management
 */
class UnityBridgeActivity : Activity() {
    
    companion object {
        private const val TAG = "UnityBridge"
        const val EXTRA_SCENE_ID = "scene_id"
        const val EXTRA_RETURN_TO_COMPOSE = "return_to_compose"
    }
    
    private var sceneId: String? = null
    private var returnToCompose: Boolean = false
    private var isFinishing = false
    private var unityLaunched = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        UnityDebugLogger.initializeFileLogging(this)
        UnityDebugLogger.logSystemInfo(this)
        
        UnityCrashFixer.applyAllFixes(this)
        UnityCrashFixer.applyDeviceSpecificFixes(this)
        
        Log.d(TAG, "UnityBridgeActivity onCreate")
        
        sceneId = intent.getStringExtra(EXTRA_SCENE_ID)
        returnToCompose = intent.getBooleanExtra(EXTRA_RETURN_TO_COMPOSE, false)
        
        Log.d(TAG, "Unity bridge created with sceneId: $sceneId, returnToCompose: $returnToCompose")
        UnityDebugLogger.logUnityLifecycle("onCreate", "UnityBridgeActivity", mapOf(
            "sceneId" to (sceneId ?: "null"),
            "returnToCompose" to returnToCompose
        ))
        UnityDebugLogger.logUnityBridgeEvent("Bridge Created", "sceneId=$sceneId, returnToCompose=$returnToCompose")
        
        val resultIntent = Intent().apply {
            putExtra("unity_bridge_started", true)
            putExtra("unity_scene_id", sceneId)
            putExtra("unity_activity_type", "UnityBridgeActivity")
        }
        setResult(Activity.RESULT_OK, resultIntent)
        
        launchUnityWithBridge()
    }
    
    private fun launchUnityWithBridge() {
        try {
            val unityIntent = Intent().apply {
                setClassName(packageName, "com.unity3d.player.UnityPlayerActivity")
                
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    Log.d(TAG, "Passing scene ID to Unity: $sceneId")
                }

                putExtra(EXTRA_RETURN_TO_COMPOSE, returnToCompose)
                
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            
            Log.d(TAG, "Launching Unity with bridge support")
            startActivityForResult(unityIntent, 2001) // Use different request code
            unityLaunched = true
            
            Log.d(TAG, "Unity launched successfully with bridge")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Unity: ${e.message}")
            UnityDebugLogger.logUnityCrash("Unity Launch Failed", "Exception during Unity launch: ${e.message}", e)
            finish()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == 2001) {
            Log.d(TAG, "Unity returned with result code: $resultCode")
            UnityDebugLogger.logUnityLifecycle("onActivityResult", "UnityBridgeActivity", mapOf(
                "requestCode" to requestCode,
                "resultCode" to resultCode,
                "hasData" to (data != null)
            ))
            
            val resultIntent = Intent().apply {
                putExtra("unity_finished", true)
                putExtra("unity_result_code", resultCode)
                putExtra("unity_timestamp", System.currentTimeMillis())
                putExtra("unity_scene_id", sceneId)
                putExtra("unity_bridge_completed", true)
                putExtra("unity_success", true)
                
                data?.extras?.let { extras ->
                    putExtras(extras)
                }
            }
            
            Log.d(TAG, "Setting RESULT_OK with Unity completion data")
            UnityDebugLogger.logUnityBridgeEvent("Unity Return", "Result code: $resultCode, Scene: $sceneId")
            
            setResult(Activity.RESULT_OK, resultIntent)
            
            Log.d(TAG, "About to finish UnityBridgeActivity to return to Compose")
            
            Log.d(TAG, "Finishing UnityBridgeActivity NOW - returning to Compose")
            UnityDebugLogger.logUnityLifecycle("finish", "UnityBridgeActivity", mapOf("reason" to "Unity returned"))
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "UnityBridgeActivity resumed")
        
        if (unityLaunched && !isFinishing) {
            val resultIntent = Intent().apply {
                putExtra("unity_bridge_active", true)
                putExtra("unity_scene_id", sceneId)
                putExtra("unity_timestamp", System.currentTimeMillis())
            }
            setResult(Activity.RESULT_OK, resultIntent)
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "UnityBridgeActivity paused")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "UnityBridgeActivity stopped")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "UnityBridgeActivity destroyed")
    }
    
    override fun onBackPressed() {
        Log.d(TAG, "Back button pressed in Unity Bridge Activity")
        UnityDebugLogger.logUnityBridgeEvent("Back Button Pressed", "UnityBridgeActivity")
        
        if (isFinishing) {
            Log.d(TAG, "Already finishing, ignoring back press")
            UnityDebugLogger.logUnityBridgeEvent("Back Button Ignored", "Already finishing")
            return
        }
        
        val resultIntent = Intent().apply {
            putExtra("unity_finished", true)
            putExtra("unity_back_pressed", true)
            putExtra("unity_scene_id", sceneId)
            putExtra("unity_timestamp", System.currentTimeMillis())
            putExtra("unity_result_code", -1)
            putExtra("unity_bridge_back", true)
            putExtra("unity_success", true)
        }
        
        Log.d(TAG, "Setting RESULT_OK for back navigation to Compose")
        UnityDebugLogger.logUnityBridgeEvent("Back Navigation", "Returning to Compose, Scene: $sceneId")
        
        setResult(Activity.RESULT_OK, resultIntent)
        
        isFinishing = true
        
        Log.d(TAG, "Finishing UnityBridgeActivity NOW - returning to Compose")
        UnityDebugLogger.logUnityLifecycle("finish", "UnityBridgeActivity", mapOf("reason" to "back button pressed"))
        finish()
    }
    
    override fun finish() {
        Log.d(TAG, "UnityBridgeActivity finish() called")
        
        val resultIntent = Intent().apply {
            putExtra("unity_finished", true)
            putExtra("unity_scene_id", sceneId)
            putExtra("unity_timestamp", System.currentTimeMillis())
            putExtra("unity_result_code", -1)
            putExtra("unity_bridge_finished", true)
            putExtra("unity_success", true)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        
        Log.d(TAG, "UnityBridgeActivity finish() - result set, calling super.finish()")
        super.finish()
    }
    
    override fun onLowMemory() {
        Log.w(TAG, "Low memory warning in Unity Bridge")
        UnityDebugLogger.logUnityBridgeEvent("Low Memory Warning", "Unity Bridge - forcing GC")
        super.onLowMemory()
        
        System.gc()
    }
    
    override fun onTrimMemory(level: Int) {
        Log.w(TAG, "Memory trim requested, level: $level")
        UnityDebugLogger.logUnityBridgeEvent("Memory Trim", "Level: $level")
        super.onTrimMemory(level)
        
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                Log.w(TAG, "Memory pressure detected, forcing GC")
                UnityDebugLogger.logUnityBridgeEvent("Memory Pressure", "Critical level: $level - forcing GC")
                System.gc()
            }
        }
    }
    
    /**
     * Called by Back_Button_Handler script to return to Android
     */
    fun returnToAndroid() {
        Log.d(TAG, "returnToAndroid() called by Unity script")
        UnityDebugLogger.logUnityCommunication("Unity->Android", "returnToAndroid", mapOf("sceneId" to (sceneId ?: "null")))
        onBackPressed()
    }
    
    /**
     * Called by Back_Button_Handler script to return to home
     */
    fun returnToAndroidHome() {
        Log.d(TAG, "returnToAndroidHome() called by Unity script")
        UnityDebugLogger.logUnityCommunication("Unity->Android", "returnToAndroidHome", mapOf("sceneId" to (sceneId ?: "null")))
        
        try {
            moveTaskToBack(true)
            Log.d(TAG, "Moved to background (home screen)")
            UnityDebugLogger.logUnityBridgeEvent("Moved to Background", "Unity script requested home screen")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move to background: ${e.message}")
            UnityDebugLogger.logUnityCrash("Background Move Failed", "Failed to move to background: ${e.message}", e)
            onBackPressed()
        }
    }
}