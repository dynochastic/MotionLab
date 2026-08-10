package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Context
import android.content.Intent

/**
 * Helper to launch Unity with an optional scene identifier.
 * Unity can read extras from the starting intent via AndroidJava APIs.
 */
object UnityLauncher {

    const val EXTRA_SCENE_ID = "scene_id"

    fun launchUnity(context: Context, sceneId: String?) {
        println("🚀 UnityLauncher.launchUnity called with sceneId: $sceneId")
        println("🚀 SceneId type: ${sceneId?.javaClass?.simpleName}")
        println("🚀 SceneId length: ${sceneId?.length}")
        println("🚀 SceneId is null or empty: ${sceneId.isNullOrEmpty()}")

        try {
            val bridgeIntent = Intent().apply {
                setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.UnityBridgeActivity")
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    println("🎮 Passing scene ID to UnityBridgeActivity: '$sceneId'")
                }
                putExtra("return_to_compose", true)
            }
            println("🎮 Launching UnityBridgeActivity (best for Back_Button_Handler)")
            context.startActivity(bridgeIntent)
            println("✅ UnityBridgeActivity launched successfully")
            return
        } catch (bridgeException: Exception) {
            println("❌ UnityBridgeActivity failed: ${bridgeException.message}")
        }

        try {
            val simpleIntent = Intent().apply {
                setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.SimpleUnityActivity")
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    println("🎮 Passing scene ID to SimpleUnityActivity: '$sceneId'")
                }
                putExtra("return_to_compose", true)
            }
            println("🎮 Launching SimpleUnityActivity (fallback)")
            context.startActivity(simpleIntent)
            println("✅ SimpleUnityActivity launched successfully")
            return
        } catch (simpleException: Exception) {
            println("❌ SimpleUnityActivity failed: ${simpleException.message}")
        }

        try {
            val unityIntent = Intent().apply {
                setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.UnityWrapperActivity")
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    println("🎮 Passing scene ID to UnityWrapper: '$sceneId'")
                }
                putExtra("return_to_compose", true)
            }
            println("🎮 Attempting to launch Unity wrapper with back navigation")
            context.startActivity(unityIntent)
            println("✅ Unity wrapper with back navigation launched successfully")
            return
        } catch (unityException: Exception) {
            println("❌ Unity wrapper with back navigation failed: ${unityException.message}")
        }

        try {
            val unityIntent = Intent().apply {
                setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.SceneSpecificUnityActivity")
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    println("🎮 Passing scene ID as string: '$sceneId'")
                }
            }
            println("🎮 Attempting to launch scene-specific Unity activity")
            context.startActivity(unityIntent)
            println("✅ Scene-specific Unity activity launched successfully")
            return
        } catch (unityException: Exception) {
            println("❌ Scene-specific Unity activity failed: ${unityException.message}")
            println("❌ Unity exception details: ${unityException.stackTraceToString()}")
        }

        try {
            val unityIntent = Intent().apply {
                setClassName(context.packageName, "com.unity3d.player.UnityPlayerActivity")
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                    println("🎮 Passing scene ID as string: '$sceneId'")
                }
            }
            println("🎮 Attempting to launch standard Unity activity")
            context.startActivity(unityIntent)
            println("✅ Standard Unity activity launched successfully")
            return
        } catch (unityException: Exception) {
            println("❌ Standard Unity activity failed: ${unityException.message}")
            println("❌ Unity exception details: ${unityException.stackTraceToString()}")
        }

        try {
            val unityIntent = Intent().apply {
                setClassName(context.packageName, "com.unity3d.player.UnityPlayerActivity")
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(EXTRA_SCENE_ID, sceneId)
                }
            }
            println("🎮 Attempting to launch Unity activity with intent: $unityIntent")
            context.startActivity(unityIntent)
            println("✅ Unity activity launched successfully")
            return
        } catch (unityException: Exception) {
            println("❌ Unity activity failed: ${unityException.message}")
            try {
                val fallbackIntent = Intent().apply {
                    setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.MinimalUnityActivity")
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (!sceneId.isNullOrEmpty()) {
                        putExtra(EXTRA_SCENE_ID, sceneId)
                    }
                }
                println("🔄 Falling back to minimal Unity activity")
                context.startActivity(fallbackIntent)
                println("✅ Fallback activity started successfully")
            } catch (fallbackException: Exception) {
                println("❌ Error starting fallback activity: ${fallbackException.message}")
                fallbackException.printStackTrace()
            }
        }
    }
}


