package com.example.motionlab.ui.screens.video_ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.motionlab.data.local.entity.MaterialType
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.lesson.SubtopicViewModel
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.ThirdBlue
import com.example.motionlab.ui.theme.videoBorderColor
import com.example.motionlab.domain.model.local.VideoTranscriptSegments
import com.example.motionlab.domain.model.local.TextStyles
import com.example.motionlab.presentation.VideoViewModel
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.os.Build
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun VideoLessonScreen(
    navController: NavController,
    username: String,
    lessonId: Int,
    subtopicId: Int,
    preTestTaken: Boolean,
    viewModel: SubtopicViewModel
) {
    val videoViewModel: VideoViewModel = viewModel()
    println("[DEBUG] VideoLessonScreen recomposed: username=$username, lessonId=$lessonId, subtopicId=$subtopicId, preTestTaken=$preTestTaken")
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val subtopics by viewModel.subtopics.collectAsState()
    val materials by viewModel.materials.collectAsState()
    val subtopicProgressList by viewModel.subtopicProgress.collectAsState()

    val subtopic = subtopics.find { it.subtopicId == subtopicId }
    val video = materials.find { it.type == MaterialType.VIDEO }
    val progress = subtopicProgressList.find { it.subtopicId == subtopicId }
    
    // State to prevent double-clicking the continue button
    var isNavigating by remember { mutableStateOf(false) }
    
    // State to force PlayerView refresh when transitioning between modes
    var forcePlayerViewRefresh by remember { mutableStateOf(0) }

    // Sync isVideoCompleted with progress
    LaunchedEffect(progress?.videoCompleted) {
        videoViewModel.isVideoCompleted.value = progress?.videoCompleted == true
    }

    // Create and set ExoPlayer if not already set
    val exoPlayer = remember {
        if (videoViewModel.exoPlayer == null) {
            val player = ExoPlayer.Builder(context).build()
            videoViewModel.exoPlayer = player
            player
        } else {
            videoViewModel.exoPlayer!!
        }
    }

    // Create a single shared PlayerView to prevent restarts/freezes
    val sharedPlayerView = remember {
        PlayerView(context).apply {
            player = exoPlayer
            useController = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setKeepContentOnPlayerReset(true)
            // Ensure proper surface handling
            setUseController(true)
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        println("🏁 Video ended")
                        videoViewModel.isVideoCompleted.value = true
                    }
                    Player.STATE_BUFFERING -> {
                        println("⏳ Video buffering")
                    }
                    Player.STATE_IDLE -> {
                        println("⏸️ Video idle")
                    }
                    Player.STATE_READY -> {
                        println("✅ Video ready")
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        
        // Add lifecycle observer for proper pause/resume
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (!videoViewModel.isFullScreen.value) {
                        exoPlayer.pause()
                        println("🎬 Video paused due to lifecycle")
                    }
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!videoViewModel.isFullScreen.value) {
                        exoPlayer.play()
                        println("🎬 Video resumed due to lifecycle")
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        
        onDispose {
            exoPlayer.removeListener(listener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

    println("[DEBUG] isFullScreen at start: ${videoViewModel.isFullScreen.value}, videoUrlForFullscreen: ${videoViewModel.videoUrlForFullscreen.value}")

    // Debug print for fullscreen state and handle surface synchronization
    LaunchedEffect(videoViewModel.isFullScreen.value) {
        println("[DEBUG] isFullScreen changed: ${videoViewModel.isFullScreen.value}")
        
        // Force surface refresh when transitioning between modes
        if (!videoViewModel.isFullScreen.value) {
            // When exiting fullscreen, force the normal PlayerView to refresh
            println("[DEBUG] Exiting fullscreen - forcing surface refresh")
            // Small delay to ensure proper surface attachment
            kotlinx.coroutines.delay(100)
            // Trigger PlayerView refresh
            forcePlayerViewRefresh++
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initializeSubtopics(username, lessonId, preTestTaken)
        viewModel.loadMaterialsForSubtopic(subtopicId)
        viewModel.observeSubtopicProgress(username)
    }

    LaunchedEffect(videoViewModel.isVideoCompleted.value) {
        if (videoViewModel.isVideoCompleted.value) {
            viewModel.updateSubtopicProgress(
                username = username,
                subtopicId = subtopicId,
                videoDone = true
            )
        }
    }

    // Load transcript from assets when video is available
    LaunchedEffect(video?.transcript) {
        videoViewModel.loadTranscriptFromAssets(context, video?.transcript)
    }

    // Load video when available
    LaunchedEffect(video?.contentPath) {
        video?.contentPath?.let { videoUrl ->
            println("🎬 Loading video: $videoUrl")
            val currentMediaItem = exoPlayer.currentMediaItem
            
            // Only set new media item if it's different
            if (currentMediaItem?.mediaId != videoUrl) {
                val mediaItem = MediaItem.Builder()
                    .setUri(videoUrl)
                    .setMediaId(videoUrl) // Use URL as stable ID
                    .build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } else {
                println("🎬 Video already loaded, maintaining position")
            }
        }
    }
    
    // Handle surface refresh when exiting fullscreen
    LaunchedEffect(forcePlayerViewRefresh) {
        if (forcePlayerViewRefresh > 0) {
            println("[DEBUG] Forcing PlayerView surface refresh")
            // Force a small delay to ensure proper surface attachment
            kotlinx.coroutines.delay(50)
        }
    }

    // Overlay fullscreen player if needed
    Box(Modifier.fillMaxSize()) {
        // Main UI
        if (subtopic == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading subtopic...", color = Color.White)
            }
        } else {
            Column(
                Modifier.fillMaxSize().background(MainBlueBg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BackButton(
                    onClick = {
                        navController.navigate(Routes.subtopicContentRoute(username, lessonId, subtopicId)) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(start = 25.dp, top = 50.dp).size(50.dp).fillMaxWidth().align(Alignment.Start)
                )
                Spacer(Modifier.height(10.dp))

                Text(
                    text = video?.title ?: "Video",
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily,
                    color = ThirdBlue,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                if (video != null) {
                    Column(
                        Modifier
                            .fillMaxHeight(.3f)
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Use shared PlayerView for normal mode
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 8f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(width = 2.dp, color = videoBorderColor, shape = RoundedCornerShape(12.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            println("[DEBUG] Double tap detected on video player")
                                            videoViewModel.enterFullScreen(video.contentPath)
                                        }
                                    )
                                }
                        ) {
                            AndroidView(
                                factory = { sharedPlayerView },
                                update = { playerView ->
                                    // Force re-attach player to ensure proper surface handling
                                    playerView.player = null
                                    playerView.player = exoPlayer
                                    
                                    // Force surface refresh
                                    playerView.invalidate()
                                    playerView.requestLayout()
                                    
                                    // Ensure proper buffering and surface settings
                                    playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                    playerView.setKeepContentOnPlayerReset(true)
                                    playerView.setUseController(true)
                                    
                                    // Force a surface update to prevent frame freeze
                                    playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                    
                                    println("🎬 Normal PlayerView updated with player state: ${exoPlayer.playbackState}")
                                    println("🎬 Normal PlayerView updated with position: ${exoPlayer.currentPosition}ms")
                                    println("🎬 Normal PlayerView isPlaying: ${exoPlayer.isPlaying}")
                                    println("🎬 Normal PlayerView refresh trigger: $forcePlayerViewRefresh")
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Fullscreen button overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    onClick = { 
                                        println("[DEBUG] Fullscreen button pressed (VideoPlayerScreen)")
                                        videoViewModel.enterFullScreen(video.contentPath)
                                        println("[DEBUG] isFullScreen set to true, videoUrlForFullscreen: ${videoViewModel.videoUrlForFullscreen.value}")
                                    },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxHeight(.3f)
                            .fillMaxWidth()
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No video available", color = Color.White)
                    }
                }
                Spacer(Modifier.height(15.dp))

                Column(
                    Modifier
                        .fillMaxWidth(.98f)
                        .fillMaxHeight()
                        .border(
                            width = 2.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp)
                        )
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                        .background(BlueButtonColor)
                ) {
                    //Transcript here
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = 25.dp, start = 25.dp, end = 25.dp)
                    ) {
                        VideoTranscript(transcript = videoViewModel.transcriptSegments.value, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(60.dp))
                    }
                    if (videoViewModel.isVideoCompleted.value) {
                        // Simple click throttle to prevent double/spam taps
                        val lastClickAtMs = remember { mutableStateOf(0L) }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 15.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Prevent action if already navigating or in fullscreen
                                    if (isNavigating || videoViewModel.isFullScreen.value) return@Button
                                    // Throttle very fast repeat taps (1s window)
                                    val now = SystemClock.elapsedRealtime()
                                    if (now - lastClickAtMs.value < 1000L) return@Button
                                    lastClickAtMs.value = now

                                    // Lock the button immediately
                                    isNavigating = true

                                    // Try to gracefully stop the player; never crash here
                                    runCatching {
                                        videoViewModel.exoPlayer?.let { player ->
                                            player.playWhenReady = false
                                            player.pause()
                                            player.stop()
                                        }
                                    }

                                    val handsOnMaterial = materials.find { it.type == MaterialType.HANDS_ON }
                                    if (handsOnMaterial != null && !handsOnMaterial.contentPath.isNullOrBlank()) {
                                        val route = Routes.handsOnRoute(
                                            username = username,
                                            lessonId = lessonId,
                                            subtopicId = subtopicId,
                                            contentPath = handsOnMaterial.contentPath
                                        )
                                        runCatching {
                                            navController.navigate(route) {
                                                launchSingleTop = true
                                                restoreState = true
                                                popUpTo(Routes.subtopicContentRoute(username, lessonId, subtopicId)) {
                                                    inclusive = false
                                                    saveState = true
                                                }
                                            }
                                        }.onFailure {
                                            // If navigation fails for any reason, re-enable button to retry
                                            isNavigating = false
                                        }
                                    } else {
                                        // No valid target; allow clicking again
                                        isNavigating = false
                                    }
                                },
                                enabled = !isNavigating && !videoViewModel.isFullScreen.value,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF003366)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(50))
                                    .border(2.dp, Color(0xFFB9D9F6), RoundedCornerShape(50))
                            ) {
                                Text(
                                    text = "Continue to Exercise",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overlay fullscreen player if needed
        if (videoViewModel.isFullScreen.value && videoViewModel.videoUrlForFullscreen.value != null) {
            println("[DEBUG] Showing FullScreenVideoPlayer overlay")
            FullScreenVideoPlayer(
                videoUrl = videoViewModel.videoUrlForFullscreen.value!!,
                exoPlayer = exoPlayer,
                onExitFullScreen = {
                    println("[DEBUG] Exiting fullscreen")
                    videoViewModel.exitFullScreen()
                },
                onVideoCompleted = { videoViewModel.isVideoCompleted.value = true }
            )
        }
    }

    // Handle device back button to go back to subtopic material content
    BackHandler {
        if (videoViewModel.isFullScreen.value) {
            println("[DEBUG] BackHandler: Exiting fullscreen")
            videoViewModel.exitFullScreen()
        } else {
            println("[DEBUG] BackHandler: Navigating to subtopic content")
            navController.navigate(Routes.subtopicContentRoute(username, lessonId, subtopicId)) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoPlayer(
    videoUrl: String,
    exoPlayer: ExoPlayer,
    onExitFullScreen: () -> Unit,
    onVideoCompleted: () -> Unit
) {
    println("[DEBUG] FullScreenVideoPlayer composable entered with videoUrl=$videoUrl")
    println("[DEBUG] Current player position: ${exoPlayer.currentPosition}ms")
    println("[DEBUG] Current player state: ${exoPlayer.playbackState}")
    println("[DEBUG] Is player playing: ${exoPlayer.isPlaying}")
    
    val context = LocalContext.current
    val activity = context as? Activity

    // Set orientation to landscape and hide system UI on enter, reset on exit
    LaunchedEffect(Unit) {
        activity?.let { act ->
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            hideSystemUI(act)
            println("[DEBUG] FullScreenVideoPlayer: Orientation set to landscape and system UI hidden")
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.let { act ->
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                showSystemUI(act)
                println("[DEBUG] FullScreenVideoPlayer: Orientation reset to portrait and system UI shown")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        println("[DEBUG] Double tap detected in fullscreen - exiting")
                        onExitFullScreen()
                    }
                )
            }
    ) {
        // Use direct PlayerView instead of CustomVideoPlayer to avoid restart
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    setKeepContentOnPlayerReset(true)
                    setUseController(true)
                }
            },
            update = { playerView ->
                // Force re-attach player to ensure proper surface handling
                playerView.player = null
                playerView.player = exoPlayer
                
                // Force surface refresh
                playerView.invalidate()
                playerView.requestLayout()
                
                // Ensure proper buffering and surface settings
                playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                playerView.setKeepContentOnPlayerReset(true)
                playerView.setUseController(true)
                
                // Force a surface update to prevent frame freeze
                playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                
                println("🎬 FullScreen PlayerView updated with player state: ${exoPlayer.playbackState}")
                println("🎬 FullScreen PlayerView updated with position: ${exoPlayer.currentPosition}ms")
                println("🎬 FullScreen PlayerView isPlaying: ${exoPlayer.isPlaying}")
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Exit Fullscreen button overlay - positioned at top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = {
                    println("[DEBUG] Exit Fullscreen button pressed (FullScreenVideoPlayer)")
                    onExitFullScreen()
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.FullscreenExit,
                    contentDescription = "Exit Fullscreen",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    exoPlayer: ExoPlayer,
    onFullScreen: () -> Unit,
    onVideoCompleted: () -> Unit
) {
    CustomVideoPlayer(
        videoUrl = videoUrl,
        exoPlayer = exoPlayer,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 8f),
        onFullScreen = onFullScreen,
        onVideoCompleted = onVideoCompleted
    )
}

@OptIn(UnstableApi::class)
@Composable
fun ImprovedVideoPlayer(
    videoUrl: String,
    exoPlayer: ExoPlayer,
    isFullScreen: Boolean,
    onFullScreenToggle: () -> Unit,
    onVideoCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Handle system UI and orientation changes
    LaunchedEffect(isFullScreen) {
        activity?.let { act ->
            if (isFullScreen) {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                hideSystemUI(act)
            } else {
                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                showSystemUI(act)
            }
        }
    }
    
    // Handle back button in fullscreen
    BackHandler(enabled = isFullScreen) {
        onFullScreenToggle()
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    setKeepContentOnPlayerReset(true)
                    // Set fullscreen button click listener
                    setFullscreenButtonClickListener { onFullScreenToggle() }
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.invalidate()
                playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                playerView.setKeepContentOnPlayerReset(true)
                println("🎬 Improved PlayerView updated with player state: ${exoPlayer.playbackState}")
                println("🎬 Improved PlayerView updated with position: ${exoPlayer.currentPosition}ms")
            },
            modifier = if (isFullScreen) {
                Modifier.fillMaxSize()
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
            }
        )
    }
}

// Helper function to set fullscreen button click listener
private fun PlayerView.setFullscreenButtonClickListener(onClick: () -> Unit) {
    try {
        val fullscreenButtonId = androidx.media3.ui.R.id.exo_fullscreen
        findViewById<View?>(fullscreenButtonId)?.setOnClickListener { onClick() }
    } catch (e: Exception) {
        // If we can't find the button, we'll rely on the double-tap gesture
        println("🎬 Could not set fullscreen button listener: ${e.message}")
    }
}

@OptIn(UnstableApi::class)
@Composable
fun CustomVideoPlayer(
    videoUrl: String,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
    onFullScreen: (() -> Unit)? = null,
    onVideoCompleted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    println("🎬 Attempting to load video: $videoUrl")

    // Track the actual video content to prevent unnecessary resets
    var lastVideoContent by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    var currentVideoUri by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(videoUrl) {
        val currentMediaItem = exoPlayer.currentMediaItem
        val newUri = videoUrl
        
        // Extract the actual video filename/content for comparison
        val newVideoContent = newUri.substringAfterLast("/")
        val currentVideoContent = currentMediaItem?.mediaId?.substringAfterLast("/")
        
        println("🎬 Video URL changed: $newUri")
        println("🎬 Current media item: ${currentMediaItem?.mediaId}")
        println("🎬 New video content: $newVideoContent")
        println("🎬 Current video content: $currentVideoContent")
        println("🎬 Last video content: $lastVideoContent")
        println("🎬 Current video URI: $currentVideoUri")
        println("🎬 Is initialized: $isInitialized")
        
        // Check if this is the same video content (even if URL format is different)
        val isSameVideoContent = newVideoContent == currentVideoContent || 
                                newVideoContent == lastVideoContent ||
                                (currentVideoUri != null && newUri == currentVideoUri)
        
        // Only set new media item if it's actually a different video OR if not initialized
        val isDifferentVideo = !isSameVideoContent && !isInitialized
        val shouldInitialize = !isInitialized && currentVideoUri == null
        
        // CRITICAL: Don't restart video if it's the same content and already playing
        if (isDifferentVideo || shouldInitialize) {
            println("🎬 Setting new media item: $newUri")
            exoPlayer.setMediaItem(MediaItem.fromUri(newUri))
            exoPlayer.prepare()
            lastVideoContent = newVideoContent
            currentVideoUri = newUri
            isInitialized = true
        } else {
            println("🎬 Same video content detected, maintaining current playback position")
            println("🎬 Current position: ${exoPlayer.currentPosition}ms")
            println("🎬 Current state: ${exoPlayer.playbackState}")
            println("🎬 Is same video content: $isSameVideoContent")
            // Don't call prepare() or setMediaItem() for same content
            return@LaunchedEffect
        }
        
        // Only start playing if not already playing
        if (!exoPlayer.isPlaying) {
            exoPlayer.playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                hasError = true
                errorMessage = error.message ?: "Unknown error"
                println("❌ Video playback error for '$videoUrl': ${error.message}")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        println("✅ Video ready to play: $videoUrl")
                    }
                    Player.STATE_BUFFERING -> {
                        println("⏳ Video buffering: $videoUrl")
                    }
                    Player.STATE_ENDED -> {
                        println("🏁 Video ended: $videoUrl")
                        onVideoCompleted?.invoke()
                    }
                    Player.STATE_IDLE -> {
                        println("�� Video idle: $videoUrl")
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            // Don't release the player here as it's shared between normal and fullscreen
        }
    }

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .aspectRatio(16 / 9f)
            .clip(shape)
            .border(width = 2.dp, color = videoBorderColor, shape = shape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        println("[DEBUG] Double tap detected on video player")
                        onFullScreen?.invoke()
                    }
                )
            }
    ) {
        if (hasError) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Video Not Available",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This video content is not yet available",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    if (errorMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Ensure proper video rendering
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        // Prevent surface issues
                        setKeepContentOnPlayerReset(true)
                    }
                },
                update = { playerView ->
                    // Force update the player view to sync with current player state
                    playerView.player = exoPlayer
                    // Force a surface update to prevent frame freeze
                    playerView.invalidate()
                    // Ensure the video surface is properly attached
                    playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    // Prevent surface issues
                    playerView.setKeepContentOnPlayerReset(true)
                    println("🎬 PlayerView updated with player state: ${exoPlayer.playbackState}")
                    println("🎬 PlayerView updated with position: ${exoPlayer.currentPosition}ms")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        // Add fullscreen button if not in fullscreen mode
        if (onFullScreen != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { 
                        println("[DEBUG] Fullscreen button pressed in CustomVideoPlayer")
                        onFullScreen() 
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoTranscript(transcript: List<VideoTranscriptSegments>, modifier: Modifier? = Modifier) {
    Column {
        transcript.forEach { segment ->
            when (segment) {
                is VideoTranscriptSegments.Text -> Text(
                    text = segment.content,
                    fontWeight = when (segment.style) {
                        TextStyles.HEADING, TextStyles.BOLD -> FontWeight.Bold
                        TextStyles.ITALIC -> FontWeight.Normal // or FontStyle.Italic
                        else -> FontWeight.Normal
                    },
                    textAlign = segment.align?: TextAlign.Justify,
                    fontSize = when (segment.style) {
                        TextStyles.HEADING -> 20.sp
                        else -> 18.sp
                    },
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                is VideoTranscriptSegments.Formula -> {
                    Text(
                        text = segment.latex,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                    )
                }
            }
        }
    }
}

// System UI control functions for better fullscreen experience
private fun hideSystemUI(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = activity.window.insetsController
        controller?.hide(WindowInsets.Type.systemBars())
        controller?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
}

private fun showSystemUI(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = activity.window.insetsController
        controller?.show(WindowInsets.Type.systemBars())
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
