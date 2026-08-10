package com.example.motionlab.domain.model.local

data class Achievement(
    val title: String,
    val description: String,
    val isMedal: Boolean,
    val isLocked: Boolean
)
