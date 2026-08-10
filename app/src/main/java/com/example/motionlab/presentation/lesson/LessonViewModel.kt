package com.example.motionlab.presentation.lesson

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.repository.AchievementRepository
import com.example.motionlab.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val achievementRepository: AchievementRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val username = savedStateHandle.get<String>("username") ?: ""

    private val _lessons = MutableStateFlow<List<LessonUiState>>(emptyList())
    val lessons: StateFlow<List<LessonUiState>> = _lessons.asStateFlow()

    init {
        android.util.Log.d("VIEWMODEL", "LessonViewModel init with username: '$username'")
        // Ensure first lesson is unlocked for this user
        viewModelScope.launch {
            lessonRepository.ensureFirstLessonUnlocked(username)
            loadLessons(username)
        }
    }
    
    private suspend fun loadLessons() {
        android.util.Log.d("VIEWMODEL", "loadLessons called with username: '$username'")
        val lessonsWithProgress = lessonRepository.getLessonsWithProgress(username)
        _lessons.value = lessonsWithProgress
        android.util.Log.d("VIEWMODEL", "Loaded ${lessonsWithProgress.size} lessons")
        lessonsWithProgress.forEach { lesson ->
            android.util.Log.d("VIEWMODEL", "Lesson: ${lesson.title}, isUnlocked=${lesson.isUnlocked}")
        }
    }
    
    private suspend fun loadLessons(username: String) {
        android.util.Log.d("VIEWMODEL", "loadLessons called with passed username: '$username'")
        val lessonsWithProgress = lessonRepository.getLessonsWithProgress(username)
        _lessons.value = lessonsWithProgress
        android.util.Log.d("VIEWMODEL", "Loaded ${lessonsWithProgress.size} lessons")
        lessonsWithProgress.forEach { lesson ->
            android.util.Log.d("VIEWMODEL", "Lesson: ${lesson.title}, isUnlocked=${lesson.isUnlocked}")
        }
    }

    fun markPreTestTaken(username: String, lessonId: Int) {
        println("markPreTestTaken called for user=$username, lessonId=$lessonId")
        viewModelScope.launch {
            val progress = lessonRepository.getLessonsWithProgress(username)
                .find { it.lessonId == lessonId }?.progress
            println("Progress before update: $progress")
            if (progress != null && !progress.preTestTaken) {
                lessonRepository.updateProgress(progress.copy(preTestTaken = true))
                println("Progress updated: preTestTaken set to true for lessonId=$lessonId")
            } else if (progress == null) {
                // Create a new progress record if none exists
                lessonRepository.updateProgress(
                    com.example.motionlab.data.local.entity.LessonProgressEntity(
                        username = username,
                        lessonId = lessonId,
                        preTestTaken = true
                    )
                )
                println("Progress created: preTestTaken set to true for lessonId=$lessonId and user=$username")
            } else {
                println("Pre-test already taken, not updating.")
            }
        }
    }

    fun onPostTestPassed(username: String, lessonId: Int, score: Int, time: Long) {
        viewModelScope.launch {
            val progress = lessonRepository.getLessonsWithProgress(username)
                .find { it.lessonId == lessonId }?.progress
            if (progress != null) {
                val shouldUpdate = progress.postTestScore == null || score > progress.postTestScore || (score == progress.postTestScore && (progress.postTestTime == null || time < progress.postTestTime))
                val completed = score >= 8 // pass condition
                
                // DEBUG: Log current progress state
                Log.d("LessonViewModel", "onPostTestPassed - Current progress: $progress")
                Log.d("LessonViewModel", "onPostTestPassed - Score: $score, Time: $time")
                Log.d("LessonViewModel", "onPostTestPassed - firstPostTestScore: ${progress.firstPostTestScore}, firstPostTestTime: ${progress.firstPostTestTime}")
                
                // FIXED: Set first-time values if they don't exist yet
                val isFirstPostTest = progress.firstPostTestScore == null
                val finalFirstScore = if (isFirstPostTest) score else progress.firstPostTestScore
                val finalFirstTime = if (isFirstPostTest) time else progress.firstPostTestTime
                
                Log.d("LessonViewModel", "onPostTestPassed - isFirstPostTest: $isFirstPostTest")
                Log.d("LessonViewModel", "onPostTestPassed - finalFirstScore: $finalFirstScore, finalFirstTime: $finalFirstTime")
                
                lessonRepository.updateProgress(progress.copy(
                    postTestScore = if (shouldUpdate) score else progress.postTestScore,
                    postTestTime = if (shouldUpdate) time else progress.postTestTime,
                    // Set first-time values if this is the first attempt
                    firstPostTestScore = finalFirstScore,
                    firstPostTestTime = finalFirstTime,
                    isLessonFullyCompleted = completed
                ))
                if (completed) {
                    Log.d("UNLOCK", "Calling unlockNextLesson for user=$username, lessonId=$lessonId")
                    lessonRepository.unlockNextLesson(lessonId, username)
                    // Force a longer delay to ensure database update is processed
                    kotlinx.coroutines.delay(1000)
                    // Force refresh the lessons with the correct username
                    refreshLessonsInternal(username)
                }

                // --- Completion Achievement Logic ---
                val allSubtopicsDone = lessonRepository.areAllSubtopicsCompleted(username, lessonId)
                when (lessonId) {
                    1 -> if (allSubtopicsDone) achievementRepository.unlockAchievementIfNeeded(username, "Motion Seeker")
                    2 -> if (allSubtopicsDone) achievementRepository.unlockAchievementIfNeeded(username, "Force Follower")
                    3 -> if (allSubtopicsDone) achievementRepository.unlockAchievementIfNeeded(username, "Energy Chaser")
                }
                // --- End Completion Achievement Logic ---
            }
        }
    }
    
    private fun refreshLessonsInternal(username: String) {
        viewModelScope.launch {
            // Force a refresh by ensuring first lesson is unlocked again
            lessonRepository.ensureFirstLessonUnlocked(username)
            // Reload lessons from database
            loadLessons(username)
            Log.d("REFRESH", "Forced refresh of lessons for user: $username")
        }
    }
    
    fun refreshLessons() {
        viewModelScope.launch {
            android.util.Log.d("REFRESH", "refreshLessons called with username: '$username'")
            loadLessons(username)
            Log.d("REFRESH", "Manual refresh of lessons for user: $username")
        }
    }
    
    fun checkAndUnlockLessonAchievement(username: String, lessonId: Int) {
        viewModelScope.launch {
            val progress = lessonRepository.getLessonsWithProgress(username)
                .find { it.lessonId == lessonId }?.progress
            if (progress?.isLessonFullyCompleted == true) {
                when (lessonId) {
                    1 -> achievementRepository.unlockAchievementIfNeeded(username, "Motion Seeker")
                    2 -> achievementRepository.unlockAchievementIfNeeded(username, "Force Follower")
                    3 -> achievementRepository.unlockAchievementIfNeeded(username, "Energy Chaser")
                }
            }
        }
    }
    
    fun refreshLessons(username: String) {
        viewModelScope.launch {
            android.util.Log.d("REFRESH", "refreshLessons called with passed username: '$username'")
            val lessonsWithProgress = lessonRepository.getLessonsWithProgress(username)
            _lessons.value = lessonsWithProgress
            android.util.Log.d("VIEWMODEL", "Loaded ${lessonsWithProgress.size} lessons with passed username")
            lessonsWithProgress.forEach { lesson ->
                android.util.Log.d("VIEWMODEL", "Lesson: ${lesson.title}, isUnlocked=${lesson.isUnlocked}")
                // Check and unlock achievement if lesson is fully completed
                checkAndUnlockLessonAchievement(username, lesson.lessonId)
            }
            Log.d("REFRESH", "Manual refresh of lessons for user: $username")
        }
    }
}

