package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motionlab.debug.UnityDebugLogger
import com.example.motionlab.debug.UnityCrashFixer

/**
 * Compose + Unity Bridge Activity
 * Handles proper navigation between Compose and Unity library
 * Fixes back button issues and ensures smooth transitions
 */
class ComposeUnityBridge : ComponentActivity() {

    companion object {
        private const val TAG = "ComposeUnityBridge"
        private const val UNITY_REQUEST_CODE = 1001
    }

    private var unityLaunched = false
    private var isReturningFromUnity = false

    private val unityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Unity returned with result code: ${result.resultCode}")
        UnityDebugLogger.logUnityBridgeEvent("Returned from Unity", "Unity library finished, result code: ${result.resultCode}")

        val mainActivity = this as? com.example.motionlab.MainActivity
        mainActivity?.setUnityRunning(false)

        unityLaunched = false
        isReturningFromUnity = true

        result.data?.extras?.let { extras ->
            Log.d(TAG, "Unity returned data: $extras")
            UnityDebugLogger.logUnityCommunication("Unity to Compose", "Data returned", extras.keySet().associateWith { extras.get(it) as Any })
        } ?: run {
            Log.w(TAG, "No data returned from Unity")
        }

        handleUnityResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UnityDebugLogger.initializeFileLogging(this)
        UnityDebugLogger.logSystemInfo(this)

        UnityCrashFixer.applyAllFixes(this)
        UnityCrashFixer.applyDeviceSpecificFixes(this)

        Log.d(TAG, "ComposeUnityBridge onCreate")
        UnityDebugLogger.logUnityBridgeEvent("Compose Unity Bridge Created", "Bridge initialized for Compose + Unity navigation")

        setContent {
            ComposeUnityScreen(
                onLaunchUnity = { launchUnity() },
                onBackPressed = { handleBackPress() }
            )
        }
    }

    private fun launchUnity() {
        if (unityLaunched) {
            Log.w(TAG, "Unity already launched, ignoring duplicate request")
            return
        }

        try {
            Log.d(TAG, "Launching Unity library from Compose")
            UnityDebugLogger.logUnityBridgeEvent("Launching Unity", "Starting Unity library from Compose app")

            val mainActivity = this as? com.example.motionlab.MainActivity
            mainActivity?.setUnityRunning(true)

            val intent = Intent(this, SimpleUnityActivity::class.java).apply {
                putExtra("launched_from_compose", true)
                putExtra("compose_activity", "ComposeUnityBridge")
            }

            unityLauncher.launch(intent)
            unityLaunched = true

            Log.d(TAG, "Unity library launched successfully")
            UnityDebugLogger.logUnityBridgeEvent("Unity Launched", "Unity library started from Compose")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Unity: ${e.message}")
            UnityDebugLogger.logUnityCrash("Unity Launch Failed", "Exception launching Unity from Compose: ${e.message}", e)
        }
    }

    // Removed deprecated onActivityResult - using modern Activity Result API instead

    private fun handleUnityResult(resultCode: Int, @Suppress("UNUSED_PARAMETER") data: Intent?) {
        when (resultCode) {
            RESULT_OK -> {
                Log.d(TAG, "Unity completed successfully")
                UnityDebugLogger.logUnityBridgeEvent("Unity Success", "Unity library completed successfully")
            }
            RESULT_CANCELED -> {
                Log.d(TAG, "Unity was cancelled")
                UnityDebugLogger.logUnityBridgeEvent("Unity Cancelled", "Unity library was cancelled by user")
            }
            else -> {
                Log.w(TAG, "Unity returned unknown result code: $resultCode")
                UnityDebugLogger.logUnityBridgeEvent("Unity Unknown Result", "Unity returned result code: $resultCode")
            }
        }
    }

    private fun handleBackPress() {
        Log.d(TAG, "Back button pressed in Compose")
        UnityDebugLogger.logUnityBridgeEvent("Compose Back Press", "Back button pressed in Compose app")

        if (unityLaunched) {
            Log.d(TAG, "Unity is still running, cannot go back")
            return
        }
    }

    @Deprecated("Deprecated in Android API 33+", ReplaceWith("OnBackPressedDispatcher"))
    override fun onBackPressed() {
        handleBackPress()
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "ComposeUnityBridge resumed")

        if (isReturningFromUnity) {
            Log.d(TAG, "Returned from Unity, updating UI")
            UnityDebugLogger.logUnityBridgeEvent("Returned to Compose", "Successfully returned from Unity to Compose")
            isReturningFromUnity = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ComposeUnityBridge destroyed")
        UnityDebugLogger.logUnityBridgeEvent("Compose Bridge Destroyed", "Compose Unity Bridge activity destroyed")
    }
}

@Composable
fun ComposeUnityScreen(
    onLaunchUnity: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onBackPressed: () -> Unit
) {
    var showUnityButton by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Compose + Unity Integration",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (showUnityButton) {
            Button(
                onClick = {
                    onLaunchUnity()
                    showUnityButton = false
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Launch Unity Simulation")
            }
        } else {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Unity is Running",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Use the back button in Unity to return to Compose",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "This demonstrates Compose + Unity library integration",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
