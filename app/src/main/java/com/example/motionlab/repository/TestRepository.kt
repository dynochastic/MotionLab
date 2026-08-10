package com.example.motionlab.repository

import com.example.motionlab.data.local.dao.QuestionDao
import com.example.motionlab.data.local.dao.TestAttemptDao
import com.example.motionlab.data.local.entity.QuestionEntity
import com.example.motionlab.data.local.entity.TestAttemptEntity
import javax.inject.Inject

class TestRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val attemptDao: TestAttemptDao
) {
    suspend fun getQuestions(lessonId: Int): List<QuestionEntity> {
        return questionDao.getRandomQuestionsForLesson(lessonId)
    }

    suspend fun saveAttempt(attempt: TestAttemptEntity) {
        attemptDao.insertAttempt(attempt)
    }

    suspend fun getBestPostTestAttempt(lessonId: Int, username: String): TestAttemptEntity? {
        return attemptDao.getBestPostTestAttempt(lessonId, username)
    }

    suspend fun getBestPreTestTimeForScore(lessonId: Int, username: String, score: Int): Long? {
        return attemptDao.getBestPreTestTimeForScore(lessonId, username, score)
    }
    suspend fun getBestPostTestTimeForScore(lessonId: Int, username: String, score: Int): Long? {
        return attemptDao.getBestPostTestTimeForScore(lessonId, username, score)
    }

    suspend fun getPostTestAttemptCount(lessonId: Int, username: String): Int {
        return attemptDao.getPostTestAttemptCount(lessonId, username)
    }
}
