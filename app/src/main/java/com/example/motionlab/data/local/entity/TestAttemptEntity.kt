package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "test_attempts",
    foreignKeys = [ForeignKey(
        entity = LessonEntity::class,
        parentColumns = ["lessonId"],
        childColumns = ["lessonId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("lessonId")]
)
data class  TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val attemptId: Int = 0,
    val lessonId: Int,
    val isPreTest: Boolean,
    val username: String,
    val score: Int,
    val timeTakenMillis: Long,
    val timestamp: Long = System.currentTimeMillis()
)
