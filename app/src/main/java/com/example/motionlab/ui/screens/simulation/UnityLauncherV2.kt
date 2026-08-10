package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.navigation.NavController

/**
 * Enhanced Unity launcher that supports both Compose navigation and separate activities
 */
object UnityLauncherV2 {
    const val EXTRA_SCENE_ID = "scene_id"

    /**
     * Launch Unity simulation with multiple options:
     * 1. Compose navigation (embedded Unity in your app) - RECOMMENDED
     * 2. Separate Unity activity (fullscreen Unity) - FALLBACK
     */
    fun launchUnity(
        context: Context, 
        sceneId: String?, 
        navController: NavController? = null,
        useCompose: Boolean = true
    ) {
        println("🚀 UnityLauncherV2.launchUnity called with sceneId: $sceneId")
        println("🚀 UseCompose: $useCompose, NavController: ${navController != null}")
        
        if (sceneId.isNullOrEmpty()) {
            println("❌ No scene ID provided")
            return
        }
        
        if (useCompose && navController != null) {
            try {
                println("🎮 Navigating to Unity simulation in Compose: $sceneId")
                navController.navigate("unity_simulation/$sceneId")
                return
            } catch (e: Exception) {
                println("❌ Compose navigation failed: ${e.message}")
            }
        }
        
        launchUnityActivity(context, sceneId)
    }
    
    private fun launchUnityActivity(context: Context, sceneId: String) {
        println("🔧 Launching Unity activity as fallback...")
        
        try {
            val unityIntent = Intent().apply {
                setClassName(context.packageName, "com.unity3d.player.UnityPlayerActivity")
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                putExtra(EXTRA_SCENE_ID, sceneId)
                println("🎮 Passing scene ID as string: '$sceneId'")
            }
            println("🎮 Attempting to launch Unity activity directly")
            context.startActivity(unityIntent)
            println("✅ Unity activity launched successfully")
            return
        } catch (unityException: Exception) {
            println("❌ Unity activity failed: ${unityException.message}")
            println("❌ Unity exception details: ${unityException.stackTraceToString()}")
            println("❌ Unity launch failed: ${unityException.message}")
            unityException.printStackTrace()
        }
    }
}
