package com.example.motionlab.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Simple event bus for communication between screens
 */
object EventBus {
    
    private val _profilePictureUpdated = MutableSharedFlow<Unit>()
    val profilePictureUpdated: SharedFlow<Unit> = _profilePictureUpdated.asSharedFlow()
    
    fun notifyProfilePictureUpdated() {
        _profilePictureUpdated.tryEmit(Unit)
    }
}

