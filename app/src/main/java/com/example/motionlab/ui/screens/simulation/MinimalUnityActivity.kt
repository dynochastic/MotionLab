package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.content.Intent

/**
 * Minimal Unity activity that bypasses resource loading issues
 */
class MinimalUnityActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sceneId = intent.getStringExtra(UnityLauncher.EXTRA_SCENE_ID)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        val titleText = TextView(this).apply {
            text = "🎮 Unity Simulation"
            textSize = 24f
            setPadding(0, 0, 0, 30)
        }
        
        val sceneText = TextView(this).apply {
            text = "Scene: $sceneId"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }
        
        val statusText = TextView(this).apply {
            text = "✅ Unity simulation loaded!\n\nThis is a placeholder for the actual Unity scene.\n\nIn a real implementation, this would load the Unity scene: $sceneId"
            textSize = 16f
            setPadding(0, 0, 0, 30)
        }
        
        val backButton = Button(this).apply {
            text = "← Back to Lesson"
            setOnClickListener {
                finish()
            }
        }
        
        layout.addView(titleText)
        layout.addView(sceneText)
        layout.addView(statusText)
        layout.addView(backButton)
        
        setContentView(layout)
    }
}
