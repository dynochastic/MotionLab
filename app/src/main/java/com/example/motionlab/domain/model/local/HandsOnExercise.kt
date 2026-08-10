package com.example.motionlab.domain.model.local

data class HandsOnExercise(
    val id: Int,
    val title: String,
    val question: String,
    val template: List<String>,
    val answers: List<Answer>,
    val choices: List<String>,
    val commutative: Boolean = false
)

data class Answer(
    val blankIndex: Int,
    val value: String
)
