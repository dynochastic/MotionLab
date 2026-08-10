package com.example.motionlab.presentation.lesson

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.data.local.entity.SubtopicMaterialEntity
import com.example.motionlab.data.local.entity.SubtopicProgressEntity
import com.example.motionlab.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubtopicViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private var currentUsername: String = ""
    private var currentLessonId: Int = -1

    private val _subtopics = MutableStateFlow<List<SubtopicUiState>>(emptyList())
    val subtopics: StateFlow<List<SubtopicUiState>> = _subtopics.asStateFlow()

    private val _subtopicDetails = MutableStateFlow<SubtopicWithMaterialsUiState?>(null)
    val subtopicDetails: StateFlow<SubtopicWithMaterialsUiState?> = _subtopicDetails.asStateFlow()

    private val _materials = MutableStateFlow<List<SubtopicMaterialEntity>>(emptyList())
    val materials: StateFlow<List<SubtopicMaterialEntity>> = _materials

    // Initialize with empty list to avoid UninitializedPropertyAccessException
    var subtopicProgress: StateFlow<List<SubtopicProgressEntity>> = MutableStateFlow(emptyList())
        private set

    fun loadMaterialsForSubtopic(subtopicId: Int) {
        viewModelScope.launch {
            _materials.value = lessonRepository.getMaterialsForSubtopic(subtopicId)
        }
    }


    /**
     * Loads subtopics for a given lesson and saves the lessonId for future updates.
     */
    fun loadSubtopics(lessonId: Int) {
        currentLessonId = lessonId
        viewModelScope.launch {
            val subtopics = lessonRepository.getSubtopics(lessonId)
            _subtopics.value = subtopics.map {
                SubtopicUiState(
                    subtopicId = it.subtopicId,
                    title = it.title,
                    iconRes = it.iconRes,
                    content = it.content,
                    videoUrl = it.videoUrl,
                    videoCompleted = false,
                    problemCompleted = false,
                    simulationCompleted = false,
                    isUnlocked = false
                )
            }
        }
    }
    fun loadSubtopicDetails(subtopicId: Int) {
        viewModelScope.launch {
            val subtopicEntity = lessonRepository.getSubtopicById(subtopicId)
            val materials = lessonRepository.getMaterialsForSubtopic(subtopicId)
            val progress = lessonRepository.getSubtopicProgress(currentUsername)
                .find { it.subtopicId == subtopicId }

            val subtopicUi = SubtopicUiState(
                subtopicId = subtopicEntity.subtopicId,
                title = subtopicEntity.title,
                iconRes = subtopicEntity.iconRes,
                content = subtopicEntity.content,
                videoUrl = subtopicEntity.videoUrl,
                videoCompleted = progress?.videoCompleted == true,
                problemCompleted = progress?.problemCompleted == true,
                simulationCompleted = progress?.simulationCompleted == true,
                isUnlocked = true
            )

            _subtopicDetails.value = SubtopicWithMaterialsUiState(subtopicUi, materials)
        }
    }

    /**
     * Loads subtopic progress and updates the UI state accordingly.
     */
    fun loadSubtopicProgress(username: String, preTestTaken: Boolean) {
        currentUsername = username
        viewModelScope.launch {
            val progresses = lessonRepository.getSubtopicProgress(username)
            val subtopics = lessonRepository.getSubtopics(currentLessonId)

            val updated = subtopics.mapIndexed { index, subtopic ->
                val progress = progresses.find { it.subtopicId == subtopic.subtopicId }

                val isUnlocked = when {
                    index == 0 -> preTestTaken // Only unlock first subtopic if pre-test is done
                    else -> progresses.getOrNull(index - 1)?.let {
                        it.videoCompleted && it.problemCompleted && it.simulationCompleted
                    } == true
                }

                SubtopicUiState(
                    subtopicId = subtopic.subtopicId,
                    title = subtopic.title,
                    iconRes = subtopic.iconRes,
                    content = subtopic.content,
                    videoUrl = subtopic.videoUrl,
                    videoCompleted = progress?.videoCompleted == true,
                    problemCompleted = progress?.problemCompleted == true,
                    simulationCompleted = progress?.simulationCompleted == true,
                    isUnlocked = isUnlocked
                )
            }

            _subtopics.value = updated
        }
    }
    fun initializeSubtopics(username: String, lessonId: Int, lessonPreTestTaken: Boolean) {
        currentUsername = username
        currentLessonId = lessonId

        viewModelScope.launch {
            val subtopics = lessonRepository.getSubtopics(lessonId).sortedBy { it.order }
            Log.d("DEBUG", "Repository.getSubtopics called for lessonId=$lessonId, result: $subtopics")
            val progresses = lessonRepository.getSubtopicProgress(username)

            // Only create progress entry for the first subtopic if lesson pre-test is completed
            if (lessonPreTestTaken && progresses.none { it.subtopicId == subtopics.firstOrNull()?.subtopicId }) {
                subtopics.firstOrNull()?.let { first ->
                    lessonRepository.saveOrUpdateSubtopicProgress(
                        SubtopicProgressEntity(username, first.subtopicId)
                    )
                }
            }

            val updated = subtopics.mapIndexed { index, subtopic ->
                val progress = progresses.find { it.subtopicId == subtopic.subtopicId }

                val isUnlocked = when {
                    index == 0 -> lessonPreTestTaken // Only unlock first subtopic if lesson pre-test is done
                    else -> {
                        val prev = subtopics.getOrNull(index - 1)
                        val prevProgress = progresses.find { it.subtopicId == prev?.subtopicId }
                        prevProgress?.videoCompleted == true && prevProgress.problemCompleted == true && prevProgress.simulationCompleted == true
                    }
                }
                SubtopicUiState(
                    subtopicId = subtopic.subtopicId,
                    title = subtopic.title,
                    iconRes = subtopic.iconRes,
                    content = subtopic.content,
                    videoUrl = subtopic.videoUrl,
                    videoCompleted = progress?.videoCompleted == true,
                    problemCompleted = progress?.problemCompleted == true,
                    simulationCompleted = progress?.simulationCompleted == true,
                    isUnlocked = isUnlocked
                )
            }

            _subtopics.value = updated
            Log.d("DEBUG", "Loaded subtopics in ViewModel: $updated")
        }
    }

    suspend fun areAllSubtopicsCompleted(username: String, lessonId: Int): Boolean {
        val subtopics = lessonRepository.getSubtopics(lessonId)
        val progresses = lessonRepository.getSubtopicProgress(username)
        return subtopics.all { subtopic ->
            val progress = progresses.find { it.subtopicId == subtopic.subtopicId }
            progress?.videoCompleted == true && progress.problemCompleted == true && progress.simulationCompleted == true
        }
    }

    /**
     * Updates progress for the given subtopic and unlocks the next one if fully complete.
     */
    fun updateSubtopicProgress(
        username: String,
        subtopicId: Int,
        videoDone: Boolean = false,
        problemDone: Boolean = false,
        simulationDone: Boolean = false
    ) {
        currentUsername = username
        Log.d("PROGRESS_DEBUG", "updateSubtopicProgress called: username=$username, subtopicId=$subtopicId, videoDone=$videoDone, problemDone=$problemDone, simulationDone=$simulationDone")
        viewModelScope.launch {
            val progresses = lessonRepository.getSubtopicProgress(username)
            val current = progresses.find { it.subtopicId == subtopicId }
            Log.d("PROGRESS_DEBUG", "Current progress for subtopic $subtopicId: $current")

            val updated = SubtopicProgressEntity(
                username = username,
                subtopicId = subtopicId,
                videoCompleted = if (videoDone) true else (current?.videoCompleted ?: false),
                problemCompleted = if (problemDone) true else (current?.problemCompleted ?: false),
                simulationCompleted = if (simulationDone) true else (current?.simulationCompleted ?: false)
            )
            
            Log.d("PROGRESS_DEBUG", "Updated progress: $updated")

            lessonRepository.saveOrUpdateSubtopicProgress(updated)

            // Refresh the subtopic details to update UI
            loadSubtopicDetails(subtopicId)

            val isCompleted = updated.videoCompleted && updated.problemCompleted && updated.simulationCompleted
            Log.d("PROGRESS_DEBUG", "Subtopic $subtopicId completed: $isCompleted")
            if (isCompleted) {
                val lessonId = lessonRepository.getSubtopicLessonId(subtopicId)
                val subtopics = lessonRepository.getSubtopics(lessonId)
                val currentIndex = subtopics.indexOfFirst { it.subtopicId == subtopicId }
                val nextSubtopic = subtopics.getOrNull(currentIndex + 1)

                if (nextSubtopic != null) {
                    val nextProgress = progresses.find { it.subtopicId == nextSubtopic.subtopicId }
                    if (nextProgress == null) {
                        lessonRepository.saveOrUpdateSubtopicProgress(
                            SubtopicProgressEntity(
                                username = username,
                                subtopicId = nextSubtopic.subtopicId
                            )
                        )
                    }
                }

                // Refresh the subtopics list to update unlocking status
                initializeSubtopics(username, lessonId, true)
            } else {
                // Even if not completed, refresh the progress to update UI
                loadSubtopicProgress(username, true)
            }
        }
    }

    fun unlockFirstSubtopic(username: String, lessonId: Int) {
        viewModelScope.launch {
            val subtopics = lessonRepository.getSubtopics(lessonId)
            val progresses = lessonRepository.getSubtopicProgress(username)
            if (subtopics.isNotEmpty() && progresses.none { it.subtopicId == subtopics.first().subtopicId }) {
                lessonRepository.saveOrUpdateSubtopicProgress(
                    SubtopicProgressEntity(username, subtopics.first().subtopicId)
                )
            }
        }
    }

    fun observeSubtopicProgress(username: String) {
        subtopicProgress = lessonRepository.getSubtopicProgressFlow(username)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    /**
     * Force refresh progress for a specific lesson
     */
    fun refreshProgressForLesson(username: String, lessonId: Int, preTestTaken: Boolean) {
        currentUsername = username
        currentLessonId = lessonId
        viewModelScope.launch {
            initializeSubtopics(username, lessonId, preTestTaken)
            loadSubtopicProgress(username, preTestTaken)
        }
    }
}
