package com.example.motionlab.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.motionlab.domain.model.local.VideoTranscriptSegments
import com.google.gson.reflect.TypeToken
import android.net.Uri
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel() {
    // Transcript state
    var transcriptSegments = mutableStateOf<List<VideoTranscriptSegments>>(emptyList())
        private set

    // Video player state
    var isFullScreen = mutableStateOf(false)
    var videoUrlForFullscreen = mutableStateOf<String?>(null)
    var isVideoCompleted = mutableStateOf(false)
    var exoPlayer: ExoPlayer? = null
    var hasError = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    fun loadTranscriptFromAssets(context: Context, transcriptPath: String?) {
        if (transcriptPath.isNullOrBlank()) {
            transcriptSegments.value = emptyList()
            return
        }
        try {
            val json = context.assets.open(transcriptPath).bufferedReader().use { it.readText() }
            val gson = VideoTranscriptSegments.registerGson()
            val listType = com.google.gson.reflect.TypeToken.getParameterized(
                List::class.java, VideoTranscriptSegments::class.java
            ).type
            val segments: List<VideoTranscriptSegments> = gson.fromJson(json, listType)
            transcriptSegments.value = segments
        } catch (e: Exception) {
            transcriptSegments.value = emptyList()
        }
    }

    fun preparePlayer(context: Context, videoUrl: String?) {
        if (videoUrl.isNullOrBlank()) return
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        val player = exoPlayer ?: return
        hasError.value = false
        errorMessage.value = ""
        player.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
        player.prepare()
        player.playWhenReady = true
        // Attach error listener
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
        player.prepare()
        player.playWhenReady = true
        player.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                hasError.value = true
                errorMessage.value = error.message ?: "Unknown error"
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    isVideoCompleted.value = true
                }
            }
        })
    }

    fun setVideoCompleted(completed: Boolean) {
        isVideoCompleted.value = completed
    }

    fun enterFullScreen(url: String) {
        println("[DEBUG] VideoViewModel: Entering fullscreen with URL: $url")
        println("[DEBUG] VideoViewModel: Current player position: ${exoPlayer?.currentPosition}ms")
        println("[DEBUG] VideoViewModel: Current player state: ${exoPlayer?.playbackState}")
        
        // Store current playback state before switching
        val currentPosition = exoPlayer?.currentPosition ?: 0L
        val isPlaying = exoPlayer?.isPlaying ?: false
        
        // Set fullscreen state first
        videoUrlForFullscreen.value = url
        isFullScreen.value = true
        
        // Don't modify player state here - let the UI handle it
        // The player should continue playing from current position
        println("[DEBUG] VideoViewModel: Fullscreen state set, player should maintain position")
    }

    fun exitFullScreen() {
        println("[DEBUG] VideoViewModel: Exiting fullscreen")
        println("[DEBUG] VideoViewModel: Current player position: ${exoPlayer?.currentPosition}ms")
        println("[DEBUG] VideoViewModel: Current player state: ${exoPlayer?.playbackState}")
        
        // Set fullscreen state to false
        isFullScreen.value = false
        videoUrlForFullscreen.value = null
        
        // Don't modify player state here - let the UI handle it
        // The player should continue playing from current position
        println("[DEBUG] VideoViewModel: Fullscreen state cleared, player should maintain position")
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
        exoPlayer = null
    }
}