package com.example.motionlab.ui.screens.simulation

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitySimulationComposable(
    sceneId: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    var isUnityLaunched by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val unityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        println("UnityComposable: Unity returned with result code: ${result.resultCode}")
        
        isUnityLaunched = false
        
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                println("UnityComposable: Unity finished successfully")
                
                result.data?.let { data ->
                    val unityFinished = data.getBooleanExtra("unity_finished", false)
                    val unityResultCode = data.getIntExtra("unity_result_code", 0)
                    val unityTimestamp = data.getLongExtra("unity_timestamp", 0)
                    
                    println("UnityComposable: Unity completion data:")
                    println("  - unity_finished: $unityFinished")
                    println("  - unity_result_code: $unityResultCode")
                    println("  - unity_timestamp: $unityTimestamp")
                    
                    val customData = data.getStringExtra("unity_result_data")
                    if (customData != null) {
                        println("UnityComposable: Custom Unity data: $customData")
                    }
                }
                
                errorMessage = null
                
                println("UnityComposable: Successfully returned from Unity - Compose staying alive")
            }
            Activity.RESULT_CANCELED -> {
                println("UnityComposable: Unity was cancelled")
                errorMessage = null
            }
            else -> {
                println("UnityComposable: Unity finished with other result: ${result.resultCode}")
                errorMessage = null
            }
        }
        
        println("UnityComposable: Unity result handling complete - UI should update now")
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { 
                Text("Unity Simulation: $sceneId") 
            },
            navigationIcon = {
                TextButton(onClick = onClose) {
                    Text("Close")
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unity Launch Error",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onClose) {
                            Text("Go Back")
                        }
                    }
                }
            } else if (!isUnityLaunched) {
                // Show Unity launch interface
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎮 Unity Simulation Ready",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scene: $sceneId",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unity simulations will launch in a separate fullscreen view for the best experience.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent().apply {
                                        setClassName(context.packageName, "com.example.motionlab.ui.screens.simulation.SimpleUnityActivity")
                                        putExtra("scene_id", sceneId)
                                        putExtra("return_to_compose", true)
                                    }
                                    unityLauncher.launch(intent)
                                    isUnityLaunched = true
                                } catch (e: Exception) {
                                    try {
                                        val fallbackIntent = Intent().apply {
                                            setClassName(context.packageName, "com.unity3d.player.UnityPlayerActivity")
                                            putExtra("scene_id", sceneId)
                                        }
                                        unityLauncher.launch(fallbackIntent)
                                        isUnityLaunched = true
                                    } catch (fallbackException: Exception) {
                                        errorMessage = "Failed to launch Unity: ${fallbackException.message}"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🚀 Launch Unity Simulation")
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅ Unity Simulation Launched",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Unity is running in a separate window. You can return here when done.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to App")
                        }
                    }
                }
            }
        }
    }
}
