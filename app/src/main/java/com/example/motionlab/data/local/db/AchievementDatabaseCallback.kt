package com.example.motionlab.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.motionlab.data.local.entity.AchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AchievementDatabaseCallback(
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        scope.launch {
            val achievementDao = AppDatabase.instance!!.achievementDao()
            val achievements = listOf(
                AchievementEntity(title = "Quick Thinker", description = "Get a perfect score on the Mechanics Pre-test", isMedal = true),
                AchievementEntity(title = "Motion Seeker", description = "Complete the topic on Mechanics", isMedal = false),
                AchievementEntity(title = "Motion Master", description = "Get a perfect score on the Mechanics Post-test", isMedal = true),

                AchievementEntity(title = "Law Learner", description = "Get a perfect score on the Newton’s Laws Pre-test", isMedal = true),
                AchievementEntity(title = "Force Follower", description = "Complete the topic on Newton’s Laws of Motion", isMedal = false),
                AchievementEntity(title = "Law Breaker", description = "Get a perfect score on the Newton’s Laws Post-test", isMedal = true),

                AchievementEntity(title = "Power Prepper", description = "Get a perfect score on the Work, Power, and Energy Pre-test", isMedal = true),
                AchievementEntity(title = "Energy Chaser", description = "Complete the topic on Work, Power, and Energy", isMedal = false),
                AchievementEntity(title = "Energy Overload", description = "Get a perfect score on the Work, Power, and Energy Post-test", isMedal = true),
            )
            achievements.forEach { achievementDao.insertAchievement(it) }
        }
    }
}