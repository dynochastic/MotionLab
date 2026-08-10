package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button

/**
 * Custom Unity activity that handles resource loading issues
 */
class CustomUnityActivity : Activity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            val unityIntent = android.content.Intent().apply {
                setClassName(packageName, "com.unity3d.player.UnityPlayerActivity")
                addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val sceneId = getIntent().getStringExtra(UnityLauncher.EXTRA_SCENE_ID)
                if (!sceneId.isNullOrEmpty()) {
                    putExtra(UnityLauncher.EXTRA_SCENE_ID, sceneId)
                }
            }
            startActivity(unityIntent)
            finish()
        } catch (e: Exception) {
            setContentView(createFallbackView())
        }
    }
    
    private fun createFallbackView(): android.view.View {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(100, 100, 100, 100)
        }
        
        val titleText = TextView(context).apply {
            text = "🎮 Unity Simulation"
            textSize = 24f
            setPadding(0, 0, 0, 30)
        }
        
        val sceneText = TextView(context).apply {
            val sceneId = intent.getStringExtra(UnityLauncher.EXTRA_SCENE_ID)
            text = "Scene: $sceneId"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }
        
        val statusText = TextView(context).apply {
            text = "✅ Unity simulation loaded!\n\nThis is a fallback view for the Unity scene.\n\nThe actual Unity scene would load here in a fully working implementation."
            textSize = 16f
            setPadding(0, 0, 0, 30)
        }
        
        val backButton = Button(context).apply {
            text = "← Back to Lesson"
            setOnClickListener {
                finish()
            }
        }
        
        layout.addView(titleText)
        layout.addView(sceneText)
        layout.addView(statusText)
        layout.addView(backButton)
        
        return layout
    }
}
