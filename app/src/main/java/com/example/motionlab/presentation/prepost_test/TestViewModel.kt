package com.example.motionlab.presentation.prepost_test

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.data.local.entity.QuestionEntity
import com.example.motionlab.repository.AchievementRepository
import com.example.motionlab.repository.AuthRepository
import com.example.motionlab.repository.LessonRepository
import com.example.motionlab.repository.TestRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: TestRepository,
    private val lessonRepository: LessonRepository,
    private val achievementRepository: AchievementRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val lessonId: Int = savedStateHandle["lessonId"] ?: 0
    val isPreTest: Boolean = savedStateHandle["isPreTest"] ?: true
    val username: String = savedStateHandle["username"] ?: ""

    private val _questions = mutableStateOf<List<QuestionEntity>>(emptyList())
    val questions: State<List<QuestionEntity>> = _questions

    private val _selectedAnswers = mutableStateListOf<String?>()
    val selectedAnswers: SnapshotStateList<String?> get() = _selectedAnswers

    private val _currentQuestionIndex = mutableStateOf(0) // ✅ used everywhere
    val currentQuestionIndex: State<Int> get() = _currentQuestionIndex

    var timeRemaining = mutableStateOf(
        if (isPreTest) 30 * 60 * 1000L else 25 * 60 * 1000L
    )

    private var timerJob: Job? = null

    init {
        Log.d("TestViewModel", "init: lessonId=$lessonId, isPreTest=$isPreTest, username=$username, hashCode=${this.hashCode()}, [TestViewModel CREATED]")
        // Restore timer and answers if present in SavedStateHandle
        savedStateHandle.get<Long>("timeRemaining")?.let {
            timeRemaining.value = it
            Log.d("TestViewModel", "Restored timeRemaining from SavedStateHandle: $it")
        }
        savedStateHandle.get<List<String>>("selectedAnswers")?.let {
            selectedAnswers.clear()
            selectedAnswers.addAll(it)
            Log.d("TestViewModel", "Restored selectedAnswers from SavedStateHandle: $it")
        }
        loadQuestions()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        Log.d("TestViewModel", "onCleared: timerJob cancelled, hashCode=${this.hashCode()}")
    }

    fun loadQuestions() {
        if (_questions.value.isNotEmpty()) return

        viewModelScope.launch {
            val data = repository.getQuestions(lessonId)
            
            // Randomize answer choices for each question
            val randomizedQuestions = data.map { question ->
                val shuffledChoices = question.choices.shuffled()
                question.copy(choices = shuffledChoices)
            }

            _questions.value = randomizedQuestions

            if (_selectedAnswers.isEmpty()) {
                _selectedAnswers.addAll(List(data.size) { null })
            } else if (_selectedAnswers.size < data.size) {
                val diff = data.size - _selectedAnswers.size
                repeat(diff) { _selectedAnswers.add(null) }
            }
        }
    }

    fun jumpToQuestion(index: Int) {
        _currentQuestionIndex.value = index
    }

    fun selectAnswer(index: Int, answer: String) {
        if (index < selectedAnswers.size) {
            selectedAnswers[index] = answer
        } else {
            while (selectedAnswers.size < index) selectedAnswers.add("")
            selectedAnswers.add(answer)
        }
        savedStateHandle["selectedAnswers"] = ArrayList(selectedAnswers)
        Log.d("TestViewModel", "selectAnswer: index=$index, answer=$answer, selectedAnswers=$selectedAnswers")
    }

    fun goToNextQuestion() {
        if (_currentQuestionIndex.value < _questions.value.lastIndex) {
            _currentQuestionIndex.value++
        }
    }

    fun goToPreviousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value--
        }
    }

    fun startTimer(onTimeout: () -> Unit) {
        Log.d("TestViewModel", "startTimer: timeRemaining=${timeRemaining.value}")
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeRemaining.value > 0) {
                delay(100L) // Run every 100 milliseconds for precision
                timeRemaining.value -= 100L
                savedStateHandle["timeRemaining"] = timeRemaining.value
            }
            onTimeout()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        Log.d("TestViewModel", "stopTimer: timerJob cancelled, hashCode=${this.hashCode()}")
    }

    fun submit(onDone: () -> Unit) {
        Log.d("TestViewModel", "submit: isPreTest=$isPreTest, timeRemaining=${timeRemaining.value}, timeTaken=${(if (isPreTest) 30 * 60 * 1000L else 25 * 60 * 1000L) - timeRemaining.value}, lessonId=$lessonId, username=$username, hashCode=${this.hashCode()}")
        stopTimer()

        val correctAnswers = questions.value.map { it.answer }
        val score = selectedAnswers.zip(correctAnswers).count { (selected, correct) -> selected == correct }
        val timeTaken = (if (isPreTest) 30 * 60 * 1000L else 25 * 60 * 1000L) - timeRemaining.value
        Log.d("TestViewModel", "submit: isPreTest=$isPreTest, timeRemaining=${timeRemaining.value}, timeTaken=$timeTaken, lessonId=$lessonId, username=$username")

        viewModelScope.launch {
            lessonRepository.updateLessonProgressAfterTest(
                username = username,
                lessonId = lessonId,
                isPreTest = isPreTest,
                score = score,
                timeTaken = timeTaken
            )

            // Unlock achievements as before
            if (isPreTest) {
                when (lessonId) {
                    1 -> if (score == 15) achievementRepository.unlockAchievementIfNeeded(username, "Quick Thinker")
                    2 -> if (score == 15) achievementRepository.unlockAchievementIfNeeded(username, "Law Learner")
                    3 -> if (score == 15) achievementRepository.unlockAchievementIfNeeded(username, "Power Prepper")
                }
            } else {
                when (lessonId) {
                    1 -> if (score == 15) achievementRepository.unlockAchievementIfNeeded(username, "Motion Master")
                    2 -> if (score == 15) achievementRepository.unlockAchievementIfNeeded(username, "Law Breaker")
                    3 -> if (score == 15) achievementRepository.unlockAchievementIfNeeded(username, "Energy Overload")
                }
            }

            // Update Firestore leaderboard
            updateLeaderboardInFirestore(username)

            onDone()
        }
    }

    private suspend fun updateLeaderboardInFirestore(username: String) {
        // Fetch all lessons to ensure all are included
        val allLessons = lessonRepository.getLessonsWithProgress(username)
        val lessonEntities = lessonRepository.getAllLessons() // get all lessons
        val allProgress = allLessons.mapNotNull { it.progress }

        // NEW LOGIC: Use first-time values for leaderboards
        // For pretest: Only use first pretest score/time (never update)
        // For posttest: Use first posttest time, but allow each score to be recorded
        val overallScore = allProgress.mapNotNull { it.firstPostTestScore }.sum()
        val overallTime = allProgress.mapNotNull { it.firstPostTestTime }.sum()
        
        // DEBUG: Log the values being used for leaderboards
        Log.d("FirebaseLeaderboard", "Overall Score: $overallScore, Overall Time: $overallTime")
        Log.d("FirebaseLeaderboard", "Total progress entries: ${allProgress.size}")
        allProgress.forEach { progress ->
            Log.d("FirebaseLeaderboard", "Lesson ${progress.lessonId}: firstPostTestScore=${progress.firstPostTestScore}, firstPostTestTime=${progress.firstPostTestTime}")
            Log.d("FirebaseLeaderboard", "Lesson ${progress.lessonId}: postTestScore=${progress.postTestScore}, postTestTime=${progress.postTestTime}")
        }

        // Build perLessonScores with all lessons, using defaults if missing
        val perLessonScores = mutableMapOf<String, Map<String, Any>>()
        val currentScores = mutableMapOf<String, Map<String, Any>>()
        
        for (lesson in lessonEntities) {
            val progress = allLessons.find { it.lessonId == lesson.lessonId }?.progress
            val lessonKey = when (lesson.lessonId) {
                1 -> "mechanics"
                2 -> "newtonsLaw"
                3 -> "wpe"
                else -> "lesson${lesson.lessonId}"
            }
            val attempts = if (progress != null) repository.getPostTestAttemptCount(lesson.lessonId, username) else 0
            
            // For leaderboards, use first-time values only
            val leaderboardScore = progress?.firstPostTestScore ?: 0
            val leaderboardTime = progress?.firstPostTestTime ?: 0L
            
            // For current scores, use current/best post-test values
            val currentScore = progress?.postTestScore ?: 0
            val currentTime = progress?.postTestTime ?: 0L
            
            // DEBUG: Log per-lesson values
            Log.d("FirebaseLeaderboard", "Lesson $lessonKey: leaderboardScore=$leaderboardScore, leaderboardTime=$leaderboardTime, attempts=$attempts")
            Log.d("FirebaseLeaderboard", "Lesson $lessonKey: currentScore=$currentScore, currentTime=$currentTime")
            
            perLessonScores[lessonKey] = mapOf(
                "score" to leaderboardScore,
                "time" to leaderboardTime,
                "attempts" to attempts
            )
            
            currentScores[lessonKey] = mapOf(
                "score" to currentScore,
                "time" to currentTime,
                "attempts" to attempts
            )
        }

        // Fetch user info
        val account = authRepository.getAccountByUsername(username)
        val firstName = account?.firstname ?: ""
        val lastName = account?.lastname ?: ""
        val profileImageUrl = account?.profilePictureUri ?: ""

        val leaderboardData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "overallScore" to overallScore,
            "overallTime" to overallTime,
            "perLessonScores" to perLessonScores,
            "currentScores" to currentScores,
            "profileImageUrl" to profileImageUrl,
            "userId" to username
        )

        FirebaseFirestore.getInstance()
            .collection("leaderboards")
            .document(username)
            .set(leaderboardData)
    }
}
