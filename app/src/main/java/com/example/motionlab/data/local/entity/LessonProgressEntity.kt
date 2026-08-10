package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "lesson_progress",
    primaryKeys = ["username", "lessonId"], // Composite primary key
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["username"],
            childColumns = ["username"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["lessonId"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("username"), Index("lessonId")]
)
data class LessonProgressEntity(
    val username: String,
    val lessonId: Int,
    val preTestTaken: Boolean = false, // flag for first pre-test taken
    val preTestScore: Int? = null,     // first pre-test score only
    val postTestScore: Int? = null,    // highest post-test score
    val postTestTime: Long? = null,    // time for best post-test score
    // NEW FIELDS: First-time values for leaderboards (never updated)
    val firstPostTestScore: Int? = null,    // first post-test score (for leaderboards)
    val firstPostTestTime: Long? = null,    // first post-test time (for leaderboards)
    val isLessonFullyCompleted: Boolean = false,
    val isUnlocked: Boolean = false, // per-user lesson unlock state
    val isSyncedToFirebase: Boolean = false
)

