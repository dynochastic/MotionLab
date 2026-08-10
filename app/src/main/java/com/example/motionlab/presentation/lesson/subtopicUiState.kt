package com.example.motionlab.presentation.lesson

data class SubtopicUiState(
    val subtopicId: Int,
    val title: String,
    val iconRes: Int,
    val content: String,
    val videoUrl: String? = null,

    val videoCompleted: Boolean = false,
    val problemCompleted: Boolean = false,
    val simulationCompleted: Boolean = false,

    val isUnlocked: Boolean = false
)
