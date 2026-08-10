package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtopic_materials",
    foreignKeys = [
        ForeignKey(
            entity = SubtopicEntity::class,
            parentColumns = ["subtopicId"],
            childColumns = ["subtopicId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SubtopicMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subtopicId: Int,
    val type: MaterialType,
    val title: String,
    val contentPath: String, // for local video URI or file path
    val transcript: String? = null
)

enum class MaterialType {
    VIDEO, HANDS_ON, SIMULATION
}
