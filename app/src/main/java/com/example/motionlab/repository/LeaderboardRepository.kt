package com.example.motionlab.repository

import com.example.motionlab.data.remote.LeaderboardEntry
import com.example.motionlab.utils.FirebaseStorageUtils
import com.example.motionlab.utils.LeaderboardCache
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val appContext: Context
) {

    // Fetch leaderboard, ordered by score DESC, then time ASC
    suspend fun getLeaderboard(): List<LeaderboardEntry> {
        val snapshot = firestore.collection("leaderboards")
            .orderBy("overallScore", Query.Direction.DESCENDING)
            .orderBy("overallTime", Query.Direction.ASCENDING)
            .get()
            .await()
        val entries = snapshot.toObjects(LeaderboardEntry::class.java)
        
        // Fetch profile pictures from Firebase Storage for each entry
        val entriesWithProfilePictures = entries.map { entry ->
            try {
                val profilePictureUrl = FirebaseStorageUtils.getProfilePictureUrl(entry.safeUserId)
                entry.copy(profileImageUrl = profilePictureUrl ?: "default")
            } catch (e: Exception) {
                android.util.Log.e("LeaderboardRepository", "Error fetching profile picture for ${entry.safeUserId}", e)
                entry.copy(profileImageUrl = "default")
            }
        }
        // Cache for offline use
        try { 
            LeaderboardCache.save(appContext, entriesWithProfilePictures)
            android.util.Log.d("LEADERBOARD", "Cached ${entriesWithProfilePictures.size} entries")
        } catch (e: Exception) {
            android.util.Log.e("LEADERBOARD", "Error caching data", e)
        }

        android.util.Log.d("LEADERBOARD", "Fetched entries with profile pictures: $entriesWithProfilePictures")
        return entriesWithProfilePictures
    }

    // Load cached leaderboard (offline fallback)
    fun getCachedLeaderboard(): List<LeaderboardEntry> {
        return try { 
            val cached = LeaderboardCache.load(appContext)
            android.util.Log.d("LEADERBOARD", "Loading cached data: ${cached.size} entries")
            cached
        } catch (e: Exception) { 
            android.util.Log.e("LEADERBOARD", "Error loading cached data", e)
            emptyList() 
        }
    }

    /**
     * Listen to the leaderboard collection in Firestore in real-time.
     * Calls the provided callback with the latest list of LeaderboardEntry whenever data changes.
     * Returns a ListenerRegistration that you should keep and remove when no longer needed.
     */
    fun listenToLeaderboard(
        onUpdate: (List<LeaderboardEntry>) -> Unit,
        onError: (Exception?) -> Unit = {}
    ): com.google.firebase.firestore.ListenerRegistration {
        return firestore.collection("leaderboards")
            .orderBy("overallScore", Query.Direction.DESCENDING)
            .orderBy("overallTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val entries = snapshot?.toObjects(LeaderboardEntry::class.java) ?: emptyList()
                
                // For real-time updates, we'll use the profileImageUrl from the leaderboard entry
                // The profile pictures will be fetched when the leaderboard is loaded initially
                onUpdate(entries)
            }
    }

    // Update or create a leaderboard entry
    suspend fun updateLeaderboardEntry(entry: LeaderboardEntry) {
        firestore.collection("leaderboards")
            .document(entry.safeUserId)
            .set(entry)
            .await()
    }
}