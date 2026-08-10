package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey (autoGenerate = true) val lessonId: Int = 0,
    val title: String,
    val description: String,
    val iconRes: Int
)
