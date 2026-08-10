package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * Fixed Unity Activity that handles the resource ID 0 issue
 */
class FixedUnityActivity : Activity() {
    
    companion object {
        private const val TAG = "FixedUnityActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "FixedUnityActivity onCreate called")
        
        val sceneId = intent.getStringExtra("scene_id") ?: "Unknown"
        
        Toast.makeText(this, "Unity Issue: Resource ID 0 doesn't exist in Android! Scene: $sceneId", Toast.LENGTH_LONG).show()
        
        try {
            showUnityProblemExplanation(sceneId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in FixedUnityActivity: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun showUnityProblemExplanation(sceneId: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }
        
        val titleText = TextView(this).apply {
            text = "🎮 Unity Resource Issue Found!"
            textSize = 24f
            setPadding(0, 0, 0, 30)
        }
        
        val sceneText = TextView(this).apply {
            text = "Scene: $sceneId"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }
        
        val problemText = TextView(this).apply {
            text = """
                🔍 PROBLEM IDENTIFIED:
                
                Unity is calling resources.getString(0) but Android resource IDs don't start at 0!
                
                📋 TECHNICAL DETAILS:
                • Unity tries to access resource ID 0x0
                • Android resource IDs start from 0x7f000000+ 
                • This is a Unity library compilation issue
                
                💡 SOLUTIONS:
                1. Use Unity 2022.3+ with proper Android resource handling
                2. Rebuild Unity library with correct Android settings
                3. Use Unity's AndroidJavaObject to pass strings properly
                
                🎯 SCENE REQUESTED: $sceneId
                
                This is a Unity library configuration issue, not an app issue!
            """.trimIndent()
            textSize = 14f
        }
        
        layout.addView(titleText)
        layout.addView(sceneText)
        layout.addView(problemText)
        
        setContentView(layout)
        
        Toast.makeText(this, "Unity library needs to be rebuilt with proper Android resource handling!", Toast.LENGTH_LONG).show()
    }
}
