package com.example.motionlab.ui.screens.simulation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Full-screen Unity simulation screen for Jetpack Compose navigation
 * Handles proper lifecycle management when returning from Unity
 */
@Composable
fun UnitySimulationScreen(
    navController: NavController,
    sceneId: String
) {
    UnitySimulationComposable(
        sceneId = sceneId,
        modifier = Modifier.fillMaxSize(),
        onClose = {
            try {
                navController.popBackStack()
            } catch (e: Exception) {
                println("Navigation error: ${e.message}")
            }
        }
    )
}
