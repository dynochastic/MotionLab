package com.example.motionlab.presentation.lesson

import com.example.motionlab.data.local.entity.LessonProgressEntity

data class LessonUiState(
    val lessonId: Int,
    val title: String,
    val description: String,
    val iconRes: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val progress: LessonProgressEntity? = null
)
