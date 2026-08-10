package com.example.motionlab.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.data.local.entity.Account
import com.example.motionlab.data.local.entity.LessonProgressEntity
import com.example.motionlab.data.local.entity.SubtopicProgressEntity
import com.example.motionlab.data.local.entity.SubtopicEntity
import com.example.motionlab.data.local.entity.LessonEntity
import com.example.motionlab.domain.model.local.Achievement
import com.example.motionlab.repository.AchievementRepository
import com.example.motionlab.repository.AuthRepository
import com.example.motionlab.repository.LessonRepository
import com.example.motionlab.repository.LeaderboardRepository
import com.example.motionlab.utils.ImageUtils
import com.example.motionlab.utils.FirebaseStorageUtils
import com.example.motionlab.utils.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data class for aggregated progress
data class LessonProgressWithPercent(
    val lessonId: Int,
    val lessonTitle: String,
    val percent: Float // 0.0 to 1.0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val lessonRepository: LessonRepository,
    private val authRepository: AuthRepository,
    private val leaderboardRepository: LeaderboardRepository
) : ViewModel() {
    

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _lessonProgress = MutableStateFlow<List<LessonProgressEntity>>(emptyList())
    val lessonProgress: StateFlow<List<LessonProgressEntity>> = _lessonProgress.asStateFlow()

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account.asStateFlow()

    private val _lessonProgressPercent = MutableStateFlow<List<LessonProgressWithPercent>>(emptyList())
    val lessonProgressPercent: StateFlow<List<LessonProgressWithPercent>> = _lessonProgressPercent.asStateFlow()

    fun loadAchievements(username: String) {
        viewModelScope.launch {
            achievementRepository.getAchievementsForUser(username).collect {
                _achievements.value = it
            }
        }
    }

    fun loadLessonProgress(username: String) {
        viewModelScope.launch {
            _lessonProgress.value = lessonRepository.getLessonsWithProgress(username)
                .mapNotNull { it.progress }
        }
    }

    fun loadAggregatedLessonProgress(username: String) {
        viewModelScope.launch {
            val lessons: List<LessonEntity> = lessonRepository.getAllLessons()
            val lessonProgresses = lessonRepository.getLessonsWithProgress(username).mapNotNull { it.progress }
            val subtopicProgresses = lessonRepository.getSubtopicProgress(username)
            
            android.util.Log.d("PROFILE_DEBUG", "Loading aggregated progress for user: $username")
            android.util.Log.d("PROFILE_DEBUG", "Lessons found: ${lessons.size}")
            android.util.Log.d("PROFILE_DEBUG", "Lesson progress records: ${lessonProgresses.size}")
            android.util.Log.d("PROFILE_DEBUG", "Subtopic progress records: ${subtopicProgresses.size}")
            
            // Debug: Log all subtopic progress records
            subtopicProgresses.forEach { prog ->
                android.util.Log.d("PROFILE_DEBUG", "Subtopic progress: subtopicId=${prog.subtopicId}, video=${prog.videoCompleted}, problem=${prog.problemCompleted}, simulation=${prog.simulationCompleted}")
            }
            
            // Debug: Log all lesson progress records
            lessonProgresses.forEach { prog ->
                android.util.Log.d("PROFILE_DEBUG", "Lesson progress: lessonId=${prog.lessonId}, preTestTaken=${prog.preTestTaken}, postTestScore=${prog.postTestScore}, isUnlocked=${prog.isUnlocked}")
            }
            
            // Debug: Log all lessons
            lessons.forEach { lesson ->
                android.util.Log.d("PROFILE_DEBUG", "Lesson: lessonId=${lesson.lessonId}, title='${lesson.title}'")
            }

            val result = lessons.map { lesson ->
                val lessonProgress = lessonProgresses.find { it.lessonId == lesson.lessonId }
                val subtopics = lessonRepository.getSubtopics(lesson.lessonId)
                var completed = 0
                val total = 2 + subtopics.size * 3 // 1 pre, 1 post, 3 per subtopic
                
                android.util.Log.d("PROFILE_DEBUG", "Lesson ${lesson.title} (ID: ${lesson.lessonId}):")
                android.util.Log.d("PROFILE_DEBUG", "  - Subtopics: ${subtopics.size}")
                android.util.Log.d("PROFILE_DEBUG", "  - Total possible: $total")
                android.util.Log.d("PROFILE_DEBUG", "  - Pre-test taken: ${lessonProgress?.preTestTaken}")
                android.util.Log.d("PROFILE_DEBUG", "  - Post-test score: ${lessonProgress?.postTestScore}")
                android.util.Log.d("PROFILE_DEBUG", "  - Lesson progress entity: $lessonProgress")
                
                // Debug: Log all subtopics for this lesson
                subtopics.forEach { subtopic ->
                    android.util.Log.d("PROFILE_DEBUG", "    - Subtopic: subtopicId=${subtopic.subtopicId}, title='${subtopic.title}', lessonId=${subtopic.lessonId}")
                }

                if (lessonProgress?.preTestTaken == true) completed++
                if (lessonProgress?.postTestScore != null && lessonProgress.postTestScore > 0) completed++

                for (subtopic in subtopics) {
                    val progress = subtopicProgresses.find { it.subtopicId == subtopic.subtopicId }
                    android.util.Log.d("PROFILE_DEBUG", "  - Subtopic ${subtopic.title} (ID: ${subtopic.subtopicId}):")
                    android.util.Log.d("PROFILE_DEBUG", "    * Video completed: ${progress?.videoCompleted}")
                    android.util.Log.d("PROFILE_DEBUG", "    * Problem completed: ${progress?.problemCompleted}")
                    android.util.Log.d("PROFILE_DEBUG", "    * Simulation completed: ${progress?.simulationCompleted}")
                    android.util.Log.d("PROFILE_DEBUG", "    * Progress entity: $progress")
                    
                    if (progress?.videoCompleted == true) completed++
                    if (progress?.problemCompleted == true) completed++
                    if (progress?.simulationCompleted == true) completed++
                }
                
                android.util.Log.d("PROFILE_DEBUG", "  - Total completed: $completed")
                android.util.Log.d("PROFILE_DEBUG", "  - Progress percentage: ${if (total > 0) completed.toFloat() / total else 0f}")

                val result = LessonProgressWithPercent(
                    lessonId = lesson.lessonId,
                    lessonTitle = lesson.title,
                    percent = if (total > 0) completed.toFloat() / total else 0f
                )
                
                android.util.Log.d("PROFILE_DEBUG", "  - Created result: $result")
                
                result
            }
            
            android.util.Log.d("PROFILE_DEBUG", "Final result list: $result")
            _lessonProgressPercent.value = result
        }
    }

    fun loadAccount(username: String) {
        viewModelScope.launch {
            _account.value = authRepository.getAccountByUsername(username)
        }
    }
    
    fun loadAccountWithImage(username: String, context: android.content.Context) {
        viewModelScope.launch {
            val account = authRepository.getAccountByUsername(username)
            
            // Check if there's a local profile image that's not in the database
            if (account?.profilePictureUri == "default" || account?.profilePictureUri == "default_profile_picture_uri") {
                val localImagePath = ImageUtils.getProfileImagePath(context, username)
                if (localImagePath != null) {
                    // Update the database with the local image path
                    authRepository.updateProfilePictureUri(username, localImagePath)
                    _account.value = authRepository.getAccountByUsername(username)
                } else {
                    _account.value = account
                }
            } else {
                _account.value = account
            }
        }
    }

    fun updateProfilePicture(username: String, pictureUri: String, context: android.content.Context) {
        viewModelScope.launch {
            try {
                val imageUri = android.net.Uri.parse(pictureUri)
                
                // Upload to Firebase Storage
                val firebaseUrl = FirebaseStorageUtils.uploadProfilePicture(username, imageUri)
                
                if (firebaseUrl != null) {
                    // Update database with Firebase Storage URL
                    authRepository.updateProfilePictureUri(username, firebaseUrl)
                    
                    // Clean up old Firebase images
                    FirebaseStorageUtils.cleanupOldProfilePictures(username)
                    
                    // Also save locally as backup
                    val localImagePath = ImageUtils.saveProfileImage(context, username, imageUri)
                    ImageUtils.cleanupOldProfileImages(context, username)
                    
                    // Reload account to get updated profile picture
                    _account.value = authRepository.getAccountByUsername(username)
                    
                    // Update Firebase leaderboard entry with new profile picture
                    updateLeaderboardProfilePicture(username, firebaseUrl)
                    
                    // Show success toast
                    android.util.Log.d("ProfileViewModel", "Profile picture uploaded successfully!")
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        Toast.makeText(context, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                    }
                    
                    // Notify that profile picture was updated
                    EventBus.notifyProfilePictureUpdated()
                } else {
                    // Fallback to local storage if Firebase upload fails
                    val localImagePath = ImageUtils.saveProfileImage(context, username, imageUri)
                    
                    if (localImagePath != null) {
                        authRepository.updateProfilePictureUri(username, localImagePath)
                        ImageUtils.cleanupOldProfileImages(context, username)
                        _account.value = authRepository.getAccountByUsername(username)
                        
                        // Update Firebase leaderboard entry with new profile picture (local fallback)
                        updateLeaderboardProfilePicture(username, localImagePath)
                        
                        // Show fallback success toast
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, "Profile picture saved locally", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Notify that profile picture was updated
                        EventBus.notifyProfilePictureUpdated()
                    } else {
                        // Complete failure
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Toast.makeText(context, "Failed to save profile picture", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Error updating profile picture", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, "Error saving profile picture", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateLeaderboardProfilePicture(username: String, profilePictureUrl: String) {
        viewModelScope.launch {
            try {
                // Get current leaderboard entry
                val leaderboard = leaderboardRepository.getLeaderboard()
                val userEntry = leaderboard.find { it.safeUserId == username }
                
                if (userEntry != null) {
                    // Update the profile picture URL in the leaderboard entry
                    val updatedEntry = userEntry.copy(profileImageUrl = profilePictureUrl)
                    leaderboardRepository.updateLeaderboardEntry(updatedEntry)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error silently - this is not critical for profile picture update
            }
        }
    }
}