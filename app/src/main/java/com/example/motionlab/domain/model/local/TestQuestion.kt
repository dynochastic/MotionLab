package com.example.motionlab.domain.model.local

data class QuestionJson(
    val id: Int,
    val lessonId: Int? = null,
    val question: String,
    val choices: List<String>,
    val answer: String,
    val solution: String? = null,
    val computation: String? = null
)
