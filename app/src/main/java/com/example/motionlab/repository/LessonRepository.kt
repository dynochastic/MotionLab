package com.example.motionlab.repository

import android.util.Log
import com.example.motionlab.data.local.dao.AccountDao
import com.example.motionlab.data.local.dao.LessonDao
import com.example.motionlab.data.local.dao.LessonProgressDao
import com.example.motionlab.data.local.dao.SubtopicDao
import com.example.motionlab.data.local.dao.SubtopicMaterialDao
import com.example.motionlab.data.local.dao.SubtopicProgressDao
import com.example.motionlab.data.local.dao.TestAttemptDao
import com.example.motionlab.data.local.entity.LessonEntity
import com.example.motionlab.data.local.entity.LessonProgressEntity
import com.example.motionlab.data.local.entity.SubtopicEntity
import com.example.motionlab.data.local.entity.SubtopicMaterialEntity
import com.example.motionlab.data.local.entity.SubtopicProgressEntity
import com.example.motionlab.presentation.lesson.LessonUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


class LessonRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val lessonDao: LessonDao,
    private val progressDao: LessonProgressDao,
     private val subtopicDao: SubtopicDao,
     private val subtopicProgressDao: SubtopicProgressDao,
    private val subtopicMaterialDao: SubtopicMaterialDao,
    private val testAttemptDao: TestAttemptDao
) {
    suspend fun getLessonsWithProgress(username: String): List<LessonUiState> {
        val lessons = lessonDao.getAllLessons()
        val progressList = progressDao.getProgressForUser(username)
        val progressMap = progressList.associateBy { it.lessonId }
        
        android.util.Log.d("GET_LESSONS", "Getting lessons for user: $username")
        android.util.Log.d("GET_LESSONS", "Progress list size: ${progressList.size}")
        progressList.forEach { progress ->
            android.util.Log.d("GET_LESSONS", "Progress: lessonId=${progress.lessonId}, isUnlocked=${progress.isUnlocked}")
        }

        return lessons.map { lesson ->
            val userProgress = progressMap[lesson.lessonId]
            val isUnlocked = userProgress?.isUnlocked ?: (lesson.lessonId == 1)
            android.util.Log.d("GET_LESSONS", "Lesson ${lesson.title}: isUnlocked=$isUnlocked, userProgress=$userProgress")
            LessonUiState(
                lessonId = lesson.lessonId,
                title = lesson.title,
                description = lesson.description,
                isUnlocked = isUnlocked, // Only first lesson unlocked by default
                isCompleted = userProgress?.isLessonFullyCompleted ?: false,
                iconRes = lesson.iconRes,
                progress = userProgress
            )
        }
    }

    // Save or update lesson-level progress
    suspend fun updateProgress(progress: LessonProgressEntity) {
        progressDao.insertOrUpdateProgress(progress)
    }
    suspend fun unlockNextLesson(lessonId: Int, username: String) {
        val nextId = lessonId + 1
        val nextLesson = lessonDao.getLessonById(nextId)
        android.util.Log.d("UNLOCK", "Attempting to unlock next lesson: user=$username, lessonId=$nextId")
        if (nextLesson != null) {
            android.util.Log.d("UNLOCK", "nextLesson exists: $nextLesson")
            // Update or create LessonProgressEntity for the next lesson and user
            val progress = progressDao.getLessonProgress(username, nextId)
            if (progress == null) {
                android.util.Log.d("UNLOCK", "No progress found for next lesson, creating new progress with isUnlocked=true")
                progressDao.insertOrUpdateProgress(
                    LessonProgressEntity(
                        username = username,
                        lessonId = nextId,
                        isUnlocked = true
                    )
                )
            } else if (!progress.isUnlocked) {
                android.util.Log.d("UNLOCK", "Progress found but not unlocked, updating isUnlocked=true")
                progressDao.insertOrUpdateProgress(progress.copy(isUnlocked = true))
            } else {
                android.util.Log.d("UNLOCK", "Progress already unlocked for user=$username, lessonId=$nextId")
            }
            // Log the value after update
            val updatedProgress = progressDao.getLessonProgress(username, nextId)
            android.util.Log.d("UNLOCK", "After update: user=$username, lessonId=$nextId, isUnlocked=${updatedProgress?.isUnlocked}")
            
            // Also ensure that the first subtopic of the unlocked lesson is available
            val subtopics = getSubtopics(nextId)
            if (subtopics.isNotEmpty()) {
                val firstSubtopic = subtopics.first()
                val subtopicProgress = subtopicProgressDao.getProgress(username, firstSubtopic.subtopicId)
                if (subtopicProgress == null) {
                    subtopicProgressDao.insertOrUpdateProgress(
                        SubtopicProgressEntity(
                            username = username,
                            subtopicId = firstSubtopic.subtopicId
                        )
                    )
                    android.util.Log.d("UNLOCK", "Created first subtopic progress for lesson $nextId: subtopicId=${firstSubtopic.subtopicId}")
                }
            }
        } else {
            android.util.Log.e("UNLOCK", "nextLesson is null for lessonId=$nextId. Cannot unlock.")
        }
    }

    // Get subtopics belonging to a specific lesson
    suspend fun getSubtopics(lessonId: Int): List<SubtopicEntity> {
        return subtopicDao.getSubtopicsForLesson(lessonId)

    }

    // Fetch all progress records for subtopics of this user
    suspend fun getSubtopicProgress(username: String): List<SubtopicProgressEntity> {
        val progress = subtopicProgressDao.getAllProgressForUser(username)
        android.util.Log.d("REPO_DEBUG", "Getting subtopic progress for user $username: ${progress.size} records")
        progress.forEach { prog ->
            android.util.Log.d("REPO_DEBUG", "  - Subtopic ${prog.subtopicId}: video=${prog.videoCompleted}, problem=${prog.problemCompleted}, simulation=${prog.simulationCompleted}")
        }
        return progress
    }

    // Save or update subtopic-level progress
    suspend fun saveOrUpdateSubtopicProgress(progress: SubtopicProgressEntity) {
        android.util.Log.d("REPO_DEBUG", "Saving/updating subtopic progress: $progress")
        subtopicProgressDao.insertOrUpdateProgress(progress)
        android.util.Log.d("REPO_DEBUG", "Subtopic progress saved/updated successfully")
    }
    suspend fun getSubtopicLessonId(subtopicId: Int): Int {
        return subtopicDao.getLessonIdForSubtopic(subtopicId)
    }

    suspend fun getMaterialsForSubtopic(subtopicId: Int): List<SubtopicMaterialEntity> {
        return subtopicMaterialDao.getMaterialsBySubtopicId(subtopicId)
    }
    suspend fun getSubtopicById(subtopicId: Int): SubtopicEntity {
        return subtopicDao.getSubtopicById(subtopicId)
    }

    suspend fun areAllSubtopicsCompleted(username: String, lessonId: Int): Boolean {
        val subtopics = getSubtopics(lessonId)
        val progresses = getSubtopicProgress(username)
        return subtopics.all { subtopic ->
            val progress = progresses.find { it.subtopicId == subtopic.subtopicId }
            progress?.videoCompleted == true && progress.problemCompleted == true && progress.simulationCompleted == true
        }
    }

    suspend fun updateLessonProgressAfterTest(
        username: String,
        lessonId: Int,
        isPreTest: Boolean,
        score: Int,
        timeTaken: Long
    ) {
        // 1. Insert the attempt
        testAttemptDao.insertAttempt(
            com.example.motionlab.data.local.entity.TestAttemptEntity(
                lessonId = lessonId,
                isPreTest = isPreTest,
                username = username,
                score = score,
                timeTakenMillis = timeTaken
            )
        )
        Log.d("LessonRepository", "Inserted test attempt: username=$username, lessonId=$lessonId, isPreTest=$isPreTest, score=$score, timeTaken=$timeTaken")

        val progress = progressDao.getLessonProgress(username, lessonId)
        if (isPreTest) {
            if (progress == null) {
                // First pre-test ever
                val newProgress = LessonProgressEntity(
                    username = username,
                    lessonId = lessonId,
                    preTestTaken = true,
                    preTestScore = score,
                    isUnlocked = true // Ensure lesson is unlocked when pre-test is taken
                )
                progressDao.insertOrUpdateProgress(newProgress)
                Log.d("LessonRepository", "Created new progress for pre-test: $newProgress")
            } else if (!progress.preTestTaken) {
                // First pre-test for this lesson
                val updated = progress.copy(
                    preTestTaken = true, 
                    preTestScore = score,
                    isUnlocked = true // Ensure lesson is unlocked when pre-test is taken
                )
                progressDao.updateProgress(updated)
                Log.d("LessonRepository", "Updated progress for first pre-test: $updated")
            } else {
                // Even if pre-test already taken, ensure lesson is unlocked
                if (!progress.isUnlocked) {
                    val updated = progress.copy(isUnlocked = true)
                    progressDao.updateProgress(updated)
                    Log.d("LessonRepository", "Updated lesson unlock status for existing pre-test: $updated")
                } else {
                    Log.d("LessonRepository", "Pre-test already taken, not updating score.")
                }
            }
        } else {
            // Post-test: NEW LOGIC - Track first-time values for leaderboards
            val bestScore = progress?.postTestScore
            val bestTime = progress?.postTestTime
            val firstPostTestScore = progress?.firstPostTestScore
            val firstPostTestTime = progress?.firstPostTestTime
            
            // Check if this is the first posttest attempt
            val isFirstPostTest = progress == null || firstPostTestScore == null
            
            // DEBUG: Log the detection logic
            Log.d("LessonRepository", "recordTestAttempt - progress: $progress")
            Log.d("LessonRepository", "recordTestAttempt - firstPostTestScore: $firstPostTestScore, firstPostTestTime: $firstPostTestTime")
            Log.d("LessonRepository", "recordTestAttempt - isFirstPostTest: $isFirstPostTest")
            Log.d("LessonRepository", "recordTestAttempt - score: $score, timeTaken: $timeTaken")
            
            if (progress == null || bestScore == null || score > bestScore) {
                val updated = (progress ?: LessonProgressEntity(username, lessonId, preTestTaken = false)).copy(
                    postTestScore = score,
                    postTestTime = timeTaken,
                    // NEW: Set first-time values only on first attempt
                    firstPostTestScore = if (isFirstPostTest) score else firstPostTestScore,
                    firstPostTestTime = if (isFirstPostTest) timeTaken else firstPostTestTime,
                    isUnlocked = true // Ensure current lesson is unlocked when post-test is taken
                )
                if (progress == null) progressDao.insertOrUpdateProgress(updated) else progressDao.updateProgress(updated)
                Log.d("LessonRepository", "Updated post-test: new best score/time: $updated")
                if (isFirstPostTest) {
                    Log.d("LessonRepository", "FIRST POSTTEST: Recorded first-time values for leaderboards")
                }
            } else if (score == bestScore && bestTime != null && timeTaken < bestTime) {
                val updated = progress.copy(
                    postTestTime = timeTaken,
                    isUnlocked = true // Ensure current lesson is unlocked when post-test is taken
                )
                progressDao.updateProgress(updated)
                Log.d("LessonRepository", "Updated post-test: tied best score, better time: $updated")
            } else {
                // Even if not updating score, ensure lesson is unlocked
                if (progress != null && !progress.isUnlocked) {
                    val updated = progress.copy(isUnlocked = true)
                    progressDao.updateProgress(updated)
                    Log.d("LessonRepository", "Updated lesson unlock status: $updated")
                } else {
                    Log.d("LessonRepository", "Post-test not a new best score/time, not updating.")
                }
            }
        }
    }

    // Reactive: Get lesson progress as Flow<List<LessonUiState>>
    fun getLessonsWithProgressUiStateFlow(username: String): Flow<List<LessonUiState>> {
        android.util.Log.d("FLOW_DEBUG", "Creating Flow for username: $username")
        return progressDao.getProgressForUserFlow(username).combine(kotlinx.coroutines.flow.flow {
            val lessons = lessonDao.getAllLessons()
            android.util.Log.d("FLOW_DEBUG", "Emitting lessons: ${lessons.size} lessons")
            emit(lessons)
        }) { progressList, lessons ->
            android.util.Log.d("FLOW_DEBUG", "Combining: progressList=${progressList.size}, lessons=${lessons.size}")
            val progressMap = progressList.associateBy { it.lessonId }
            progressList.forEach { progress ->
                android.util.Log.d("FLOW_DEBUG", "Progress: lessonId=${progress.lessonId}, isUnlocked=${progress.isUnlocked}")
            }
            lessons.map { lesson ->
                val userProgress = progressMap[lesson.lessonId]
                val isUnlocked = userProgress?.isUnlocked ?: (lesson.lessonId == 1)
                android.util.Log.d("FLOW_DEBUG", "Lesson ${lesson.title}: isUnlocked=$isUnlocked, userProgress=$userProgress")
                LessonUiState(
                    lessonId = lesson.lessonId,
                    title = lesson.title,
                    description = lesson.description,
                    isUnlocked = isUnlocked, // Only first lesson unlocked by default
                    isCompleted = userProgress?.isLessonFullyCompleted ?: false,
                    iconRes = lesson.iconRes,
                    progress = userProgress
                )
            }
        }
    }

    // Reactive: Get subtopic progress as Flow
    fun getSubtopicProgressFlow(username: String) = subtopicProgressDao.getAllProgressForUserFlow(username)

    // Add this function to expose all lessons
    suspend fun getAllLessons(): List<LessonEntity> {
        return lessonDao.getAllLessons()
    }
    
    // Ensure first lesson is unlocked for a user
    suspend fun ensureFirstLessonUnlocked(username: String) {
        // First check if the user exists in the Account table
        val account = accountDao.getAccountByUsername(username)
        if (account == null) {
            Log.e("LessonRepository", "User $username does not exist in Account table, cannot create lesson progress")
            return
        }
        
        // Check if lesson 1 exists
        val lesson1 = lessonDao.getLessonById(1)
        if (lesson1 == null) {
            Log.e("LessonRepository", "Lesson 1 does not exist in LessonEntity table, cannot create lesson progress")
            return
        }
        
        val firstLessonProgress = progressDao.getLessonProgress(username, 1)
        if (firstLessonProgress == null) {
            // Create progress for first lesson with unlocked status
            progressDao.insertOrUpdateProgress(
                LessonProgressEntity(
                    username = username,
                    lessonId = 1,
                    isUnlocked = true
                )
            )
            Log.d("LessonRepository", "Created first lesson progress with unlocked status for user: $username")
        } else if (!firstLessonProgress.isUnlocked) {
            // Update existing progress to unlock first lesson
            progressDao.insertOrUpdateProgress(firstLessonProgress.copy(isUnlocked = true))
            Log.d("LessonRepository", "Updated first lesson to unlocked for user: $username")
        }
        
        // Also ensure lesson 2 (Newton's Law) has progress entry if user has completed lesson 1
        val lesson1Completed = firstLessonProgress?.isLessonFullyCompleted == true
        if (lesson1Completed) {
            val lesson2Progress = progressDao.getLessonProgress(username, 2)
            if (lesson2Progress == null) {
                progressDao.insertOrUpdateProgress(
                    LessonProgressEntity(
                        username = username,
                        lessonId = 2,
                        isUnlocked = true
                    )
                )
                Log.d("LessonRepository", "Created lesson 2 progress with unlocked status for user: $username")
            } else if (!lesson2Progress.isUnlocked) {
                progressDao.insertOrUpdateProgress(lesson2Progress.copy(isUnlocked = true))
                Log.d("LessonRepository", "Updated lesson 2 to unlocked for user: $username")
            }
        }
    }
}

