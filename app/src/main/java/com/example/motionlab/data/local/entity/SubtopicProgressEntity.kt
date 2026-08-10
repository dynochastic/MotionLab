package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "subtopic_progress",
    primaryKeys = ["username", "subtopicId"],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["username"],
            childColumns = ["username"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubtopicEntity::class,
            parentColumns = ["subtopicId"],
            childColumns = ["subtopicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("username"), Index("subtopicId")]
)
data class SubtopicProgressEntity(
    val username: String,
    val subtopicId: Int,
    val videoCompleted: Boolean = false,
    val problemCompleted: Boolean = false,
    val simulationCompleted: Boolean = false
)
