package com.example.motionlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motionlab.data.local.entity.AchievementEntity
import com.example.motionlab.data.local.entity.UserAchievementEntity

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query(""" SELECT a.*, ua.isUnlocked
        FROM achievements a
        LEFT JOIN user_achievements ua
        ON a.id = ua.achievementId AND LOWER(ua.username) = LOWER(:username)
    """)
    suspend fun getAchievementsForUser(username: String): List<AchievementWithStatus>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlockAchievement(userAchievement: UserAchievementEntity)

    // New methods for achievement unlocking logic
    @Query("SELECT * FROM achievements WHERE title = :title LIMIT 1")
    suspend fun getAchievementByTitle(title: String): AchievementEntity?

    @Query("SELECT isUnlocked FROM user_achievements WHERE LOWER(username) = LOWER(:username) AND achievementId = :achievementId LIMIT 1")
    suspend fun isAchievementUnlocked(username: String, achievementId: Int): Boolean?
}

// Helper data class for joined result
data class AchievementWithStatus(
    val id: Int,
    val title: String,
    val description: String,
    val isMedal: Boolean,
    val isUnlocked: Boolean? // null if not yet unlocked
)