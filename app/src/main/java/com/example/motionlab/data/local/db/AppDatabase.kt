package com.example.motionlab.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.motionlab.data.local.dao.AccountDao
import com.example.motionlab.data.local.dao.AchievementDao
import com.example.motionlab.data.local.dao.LessonDao
import com.example.motionlab.data.local.dao.LessonProgressDao
import com.example.motionlab.data.local.dao.QuestionDao
import com.example.motionlab.data.local.dao.SubtopicDao
import com.example.motionlab.data.local.dao.SubtopicMaterialDao
import com.example.motionlab.data.local.dao.SubtopicProgressDao
import com.example.motionlab.data.local.dao.TestAttemptDao
import com.example.motionlab.data.local.entity.Account
import com.example.motionlab.data.local.entity.AchievementEntity
import com.example.motionlab.data.local.entity.LessonEntity
import com.example.motionlab.data.local.entity.LessonProgressEntity
import com.example.motionlab.data.local.entity.QuestionEntity
import com.example.motionlab.data.local.entity.SubtopicEntity
import com.example.motionlab.data.local.entity.SubtopicMaterialEntity
import com.example.motionlab.data.local.entity.SubtopicProgressEntity
import com.example.motionlab.data.local.entity.TestAttemptEntity
import com.example.motionlab.data.local.entity.UserAchievementEntity

@Database(
    entities = [
        Account::class,
        LessonEntity::class,
        LessonProgressEntity::class,
        QuestionEntity::class,
        SubtopicEntity::class,
        SubtopicMaterialEntity::class,
        SubtopicProgressEntity::class,
        TestAttemptEntity::class,
        AchievementEntity::class,
        UserAchievementEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun lessonDao(): LessonDao
    abstract fun questionDao(): QuestionDao
    abstract fun lessonProgressDao(): LessonProgressDao
    abstract fun subtopicDao(): SubtopicDao
    abstract fun subtopicProgressDao(): SubtopicProgressDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun subtopicMaterialDao(): SubtopicMaterialDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        var instance: AppDatabase? = null
    }
}

