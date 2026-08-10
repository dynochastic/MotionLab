package com.example.motionlab.presentation.leaderboards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motionlab.data.remote.LeaderboardEntry
import com.example.motionlab.repository.LeaderboardRepository
import com.example.motionlab.utils.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.firestore.ListenerRegistration

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: LeaderboardRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val currentUserId: String = savedStateHandle.get<String>("username") ?: ""

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard

    private val _userRank = MutableStateFlow<Int?>(null)
    val userRank: StateFlow<Int?> = _userRank

    private var listenerRegistration: ListenerRegistration? = null

    init {
        // 1) Immediately show cached data (offline-first UX)
        loadCachedData()
        
        // 2) Then start real-time updates
        startRealTimeListener()
        listenToProfilePictureUpdates()
    }
    
    private fun loadCachedData() {
        val cached = repository.getCachedLeaderboard()
        if (cached.isNotEmpty()) {
            _leaderboard.value = cached
            _userRank.value = cached.indexOfFirst { it.safeUserId == (currentUserId ?: "") }
                .takeIf { it >= 0 }?.plus(1)
            android.util.Log.d("LEADERBOARD", "Loaded cached data: ${cached.size} entries")
        } else {
            android.util.Log.d("LEADERBOARD", "No cached data found")
        }
    }

    private fun startRealTimeListener() {
        listenerRegistration = repository.listenToLeaderboard(
            onUpdate = { entries ->
                // For real-time updates, we'll fetch profile pictures asynchronously
                viewModelScope.launch {
                    val entriesWithProfilePictures = entries.map { entry ->
                        try {
                            val profilePictureUrl = com.example.motionlab.utils.FirebaseStorageUtils.getProfilePictureUrl(entry.safeUserId)
                            entry.copy(profileImageUrl = profilePictureUrl ?: "default")
                        } catch (e: Exception) {
                            android.util.Log.e("LeaderboardViewModel", "Error fetching profile picture for ${entry.safeUserId}", e)
                            entry.copy(profileImageUrl = "default")
                        }
                    }
                    _leaderboard.value = entriesWithProfilePictures
                    _userRank.value = entriesWithProfilePictures.indexOfFirst { it.safeUserId == (currentUserId ?: "") }
                        .takeIf { it >= 0 }?.plus(1)
                }
            },
            onError = { error ->
                android.util.Log.e("LeaderboardViewModel", "Error listening to leaderboard", error)
            }
        )
    }
    
    private fun listenToProfilePictureUpdates() {
        EventBus.profilePictureUpdated
            .onEach {
                // Refresh leaderboard when profile picture is updated
                refreshLeaderboard()
            }
            .launchIn(viewModelScope)
    }
    
    fun loadLeaderboard() {
        viewModelScope.launch {
            val entries = repository.getLeaderboard()
            _leaderboard.value = entries
            _userRank.value = entries.indexOfFirst { it.safeUserId == (currentUserId ?: "") }
                .takeIf { it >= 0 }?.plus(1)
        }
    }
    
    fun refreshLeaderboard() {
        // Real-time listener will automatically update, but we can also force a refresh
        loadLeaderboard()
    }
    
    fun loadLeaderboardOnNavigation() {
        // When user navigates to leaderboard, first show cached data, then try to refresh
        loadCachedData()
        loadLeaderboard()
    }
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}