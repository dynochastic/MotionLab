package com.example.motionlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motionlab.data.local.entity.QuestionEntity
import com.example.motionlab.data.local.entity.TestAttemptEntity

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE lessonId = :lessonId ORDER BY RANDOM() LIMIT 15")
    suspend fun getRandomQuestionsForLesson(lessonId: Int): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
}

@Dao
interface TestAttemptDao {
    @Insert
    suspend fun insertAttempt(attempt: TestAttemptEntity)

    @Query("SELECT * FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 0 AND LOWER(username) = LOWER(:username) ORDER BY timeTakenMillis ASC LIMIT 1")
    suspend fun getBestPostTestAttempt(lessonId: Int, username: String): TestAttemptEntity?

    // Get highest pretest score for a lesson and user
    @Query("SELECT MAX(score) FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 1 AND LOWER(username) = LOWER(:username)")
    suspend fun getHighestPreTestScore(lessonId: Int, username: String): Int?

    // Get highest posttest score for a lesson and user
    @Query("SELECT MAX(score) FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 0 AND LOWER(username) = LOWER(:username)")
    suspend fun getHighestPostTestScore(lessonId: Int, username: String): Int?

    // Get number of pretest attempts
    @Query("SELECT COUNT(*) FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 1 AND LOWER(username) = LOWER(:username)")
    suspend fun getPreTestAttemptCount(lessonId: Int, username: String): Int

    // Get number of posttest attempts
    @Query("SELECT COUNT(*) FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 0 AND LOWER(username) = LOWER(:username)")
    suspend fun getPostTestAttemptCount(lessonId: Int, username: String): Int

    // Get best pretest time for a given score
    @Query("SELECT MIN(timeTakenMillis) FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 1 AND LOWER(username) = LOWER(:username) AND score = :score")
    suspend fun getBestPreTestTimeForScore(lessonId: Int, username: String, score: Int): Long?

    // Get best posttest time for a given score
    @Query("SELECT MIN(timeTakenMillis) FROM test_attempts WHERE lessonId = :lessonId AND isPreTest = 0 AND LOWER(username) = LOWER(:username) AND score = :score")
    suspend fun getBestPostTestTimeForScore(lessonId: Int, username: String, score: Int): Long?
}
