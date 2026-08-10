package com.example.motionlab.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isMedal: Boolean
)

@Entity(
    tableName = "user_achievements",
    primaryKeys = ["username", "achievementId"],
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["username"],
            childColumns = ["username"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AchievementEntity::class,
            parentColumns = ["id"],
            childColumns = ["achievementId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("username"), Index("achievementId")]
)
data class UserAchievementEntity(
    val username: String,
    val achievementId: Int,
    val isUnlocked: Boolean
)
