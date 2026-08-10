package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtopics",
    foreignKeys = [ForeignKey(
        entity = LessonEntity::class,
        parentColumns = ["lessonId"],
        childColumns = ["lessonId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lessonId")]
)
data class SubtopicEntity(
    @PrimaryKey(autoGenerate = true) val subtopicId: Int = 0,
    val lessonId: Int,
    val title: String,
    val iconRes: Int,
    val content: String, 
    val videoUrl: String? = null,
    val order: Int
)
