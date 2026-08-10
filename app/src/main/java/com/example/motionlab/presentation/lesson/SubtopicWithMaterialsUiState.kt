package com.example.motionlab.presentation.lesson

import com.example.motionlab.data.local.entity.SubtopicMaterialEntity

data class SubtopicWithMaterialsUiState(
    val subtopic: SubtopicUiState,
    val materials: List<SubtopicMaterialEntity>
)
