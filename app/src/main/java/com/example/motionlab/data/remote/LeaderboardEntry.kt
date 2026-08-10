package com.example.motionlab.data.remote

data class LeaderboardEntry(
    val userId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profileImageUrl: String? = null,
    val overallScore: Int? = null,
    val overallTime: Long? = null,
    val perLessonScores: Map<String, LessonScore>? = null, // First post-test scores (for leaderboards)
    val currentScores: Map<String, LessonScore>? = null // Current/best post-test scores and times
) {
    // Helper properties to safely access values with defaults
    val safeUserId: String get() = userId ?: ""
    val safeFirstName: String get() = firstName ?: "Unknown"
    val safeLastName: String get() = lastName ?: "User"
    val safeProfileImageUrl: String get() = profileImageUrl ?: ""
    val safeOverallScore: Int get() = overallScore ?: 0
    val safeOverallTime: Long get() = overallTime ?: 0L
}

data class LessonScore(
    val score: Int = 0,
    val time: Long = 0L,
    val attempts: Int = 0
)