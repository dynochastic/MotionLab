package com.example.motionlab.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.motionlab.data.local.dao.AccountDao
import com.example.motionlab.data.local.dao.AchievementDao

import com.example.motionlab.data.local.dao.LessonDao
import com.example.motionlab.data.local.dao.LessonProgressDao
import com.example.motionlab.data.local.dao.QuestionDao
import com.example.motionlab.data.local.dao.SubtopicDao
import com.example.motionlab.data.local.dao.SubtopicMaterialDao
import com.example.motionlab.data.local.dao.SubtopicProgressDao
import com.example.motionlab.data.local.dao.TestAttemptDao
import com.example.motionlab.data.local.db.AchievementDatabaseCallback
import com.example.motionlab.data.local.db.AppDatabase
import com.example.motionlab.data.local.db.LessonDatabaseCallback
import com.example.motionlab.repository.AchievementRepository
import com.example.motionlab.repository.AuthRepository
import com.example.motionlab.repository.LeaderboardRepository
import com.example.motionlab.repository.LessonRepository
import com.example.motionlab.repository.TestRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Migration from version 1 to 2: Add firstPostTestScore and firstPostTestTime columns
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase): Unit {
            // Add new columns to lesson_progress table
            database.execSQL("ALTER TABLE lesson_progress ADD COLUMN firstPostTestScore INTEGER")
            database.execSQL("ALTER TABLE lesson_progress ADD COLUMN firstPostTestTime INTEGER")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        val scope = CoroutineScope(Dispatchers.IO)

        val db = Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "motionlab"
        )
            .addCallback(LessonDatabaseCallback(scope, app))
            .addCallback(AchievementDatabaseCallback(scope)) //
            .addMigrations(MIGRATION_1_2)
            .build()

        AppDatabase.instance = db // store reference for callback
        return db
    }


    @Provides
    @Singleton
    fun provideDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    @Singleton
    fun provideRepository(dao: AccountDao): AuthRepository = AuthRepository(dao)

    @Provides
    @Singleton
    fun provideLessonDao(db: AppDatabase): LessonDao = db.lessonDao()

    @Provides
    @Singleton
    fun provideLessonProgressDao(db: AppDatabase): LessonProgressDao = db.lessonProgressDao()

    @Provides
    @Singleton
    fun provideSubtopicMaterialDao(db: AppDatabase): SubtopicMaterialDao = db.subtopicMaterialDao()


    @Provides
    @Singleton
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    @Singleton
    fun provideTestAttemptDao(db: AppDatabase): TestAttemptDao = db.testAttemptDao()




    @Provides
    @Singleton
    fun provideLessonRepository(
        accountDao: AccountDao,
        lessonDao: LessonDao,
        progressDao: LessonProgressDao,
        subtopicDao: SubtopicDao,
        subtopicProgressDao: SubtopicProgressDao,
        subtopicMaterialDao: SubtopicMaterialDao,
        testAttemptDao: TestAttemptDao
    ): LessonRepository = LessonRepository(
        accountDao = accountDao,
        lessonDao = lessonDao,
        progressDao = progressDao,
        subtopicDao = subtopicDao,
        subtopicProgressDao = subtopicProgressDao,
        subtopicMaterialDao = subtopicMaterialDao,
        testAttemptDao = testAttemptDao
    )


    @Provides
    fun provideSubtopicDao(db: AppDatabase): SubtopicDao = db.subtopicDao()

    @Provides
    fun provideSubtopicProgressDao(db: AppDatabase): SubtopicProgressDao = db.subtopicProgressDao()

    @Provides
    @Singleton
    fun provideTestRepository(
        questionDao: QuestionDao,
        attemptDao: TestAttemptDao
    ): TestRepository = TestRepository(questionDao, attemptDao)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideLeaderboardRepository(
        app: Application,
        firestore: FirebaseFirestore
    ): LeaderboardRepository = LeaderboardRepository(firestore, app.applicationContext)

    @Provides
    fun provideAchievementDao(db: AppDatabase): AchievementDao = db.achievementDao()

    @Provides
    @Singleton
    fun provideAchievementRepository(achievementDao: AchievementDao): AchievementRepository = AchievementRepository(achievementDao)


}


@HiltAndroidApp
class MotionLabApp : Application()
