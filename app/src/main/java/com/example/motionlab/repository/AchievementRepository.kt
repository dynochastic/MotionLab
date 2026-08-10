package com.example.motionlab.repository

import com.example.motionlab.data.local.dao.AchievementDao
import com.example.motionlab.data.local.entity.UserAchievementEntity
import com.example.motionlab.domain.model.local.Achievement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AchievementRepository(private val achievementDao: AchievementDao) {

    fun getAchievementsForUser(username: String): Flow<List<Achievement>> = flow {
        val dbAchievements = achievementDao.getAchievementsForUser(username)
        emit(
            dbAchievements.map {
                Achievement(
                    title = it.title,
                    description = it.description,
                    isMedal = it.isMedal,
                    isLocked = it.isUnlocked != true // true = unlocked, false/null = locked
                )
            }
        )
    }

    suspend fun unlockAchievementIfNeeded(username: String, achievementTitle: String) {
        val achievement = achievementDao.getAchievementByTitle(achievementTitle) ?: return
        val alreadyUnlocked = achievementDao.isAchievementUnlocked(username, achievement.id) == true
        if (!alreadyUnlocked) {
            achievementDao.unlockAchievement(UserAchievementEntity(username, achievement.id, true))
        }
    }
} 