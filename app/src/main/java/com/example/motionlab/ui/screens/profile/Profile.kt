package com.example.motionlab.ui.screens.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import android.widget.Toast
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.motionlab.utils.PermissionUtils
import com.example.motionlab.utils.ImageUtils
import java.io.File
import com.example.motionlab.R
import com.example.motionlab.dmSansFontFamily
import com.example.motionlab.domain.model.local.Achievement
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.profile.ProfileViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.AchievementCard
import com.example.motionlab.ui.components.CircularProgress
import com.example.motionlab.ui.components.HamburgerButton
import com.example.motionlab.ui.components.MainBlueBackTop
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.components.CollapsibleCard as ProfileCard

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    username: String,
    firstname: String,
    lastname: String
) {
    LockPortrait()

    val achievements by viewModel.achievements.collectAsState()
    val lessonProgress by viewModel.lessonProgress.collectAsState()
    val account by viewModel.account.collectAsState()
    val lessonProgressPercent by viewModel.lessonProgressPercent.collectAsState()
    val context = LocalContext.current

    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    LaunchedEffect(username) {
        viewModel.loadAchievements(username)
        viewModel.loadLessonProgress(username)
        viewModel.loadAccountWithImage(username, context)
        viewModel.loadAggregatedLessonProgress(username)
    }

    val firstname = account?.firstname?.replaceFirstChar { it.uppercaseChar() } ?: ""
    val lastname = account?.lastname?.replaceFirstChar { it.uppercaseChar() } ?: ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SecondBlueBg)
    ) {
        Column(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                MainBlueBackTop(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 30.dp,
                            shape = RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp),
                            clip = false
                        )
                        .clip(RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                        .height(250.dp)
                        .background(MainBlueBg)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-30).dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .offset(y = 63.dp, x = (-26).dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        HamburgerButton(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    navController.navigate(
                                        Routes.settingsWithUsername(
                                            username
                                        )
                                    )
                                }
                        )
                    }
                    ProfilePicture(
                        account?.profilePictureUri ?: "default",
                        firstname,
                        lastname,
                        username,
                        viewModel
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 250.dp)
        ) {
            item {
                ProfileCard(
                    pillTitle = "Achievements",
                    isExpandable = true,

                    collapsedContent = {
                        val collapsedAchievement = achievements.take(3)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    12.dp,
                                    Alignment.CenterHorizontally
                                )
                            ) {
                                collapsedAchievement.forEach { achievement ->
                                    AchievementCard(
                                        title = achievement.title,
                                        description = achievement.description,
                                        isMedal = achievement.isMedal,
                                        isLocked = achievement.isLocked,
                                        onClick = { selectedAchievement = achievement },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    )
                                }
                                repeat(3 - collapsedAchievement.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    },

                    expandedContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            achievements.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { achievement ->
                                        AchievementCard(
                                            title = achievement.title,
                                            description = achievement.description,
                                            isMedal = achievement.isMedal,
                                            isLocked = achievement.isLocked,
                                            onClick = { selectedAchievement = achievement },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        )
                                    }
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(20.dp))
            }


            item {
                ProfileCard(
                    pillTitle = "Scores",
                    isExpandable = false,
                    collapsedContent = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header Row
                            Row(modifier = Modifier.fillMaxWidth()  ) {
                                Text(
                                    "Topic",
                                    modifier = Modifier.weight(1f),
                                    color = Color.White,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Pre-Test",
                                    modifier = Modifier.weight(1f),
                                    color = Color.White,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "Post-Test",
                                    modifier = Modifier.weight(1f),
                                    color = Color.White,
                                    fontFamily = poppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Map lesson titles to display order
                            val lessonOrder = listOf(
                                "Mechanics",
                                "Newton's Laws of Motion",
                                "Work, Power, & Energy"
                            )
                            val progressByTitle = lessonProgress.associateBy { it.lessonId }
                            // You may need to map lessonId to title using a static map or fetch lessons if needed
                            val lessonTitleMap = mapOf(
                                1 to "Mechanics",
                                2 to "Newton's Laws of Motion",
                                3 to "Work, Power, & Energy"
                            )
                            lessonOrder.forEach { topic ->
                                val progress = lessonProgress.find { lessonTitleMap[it.lessonId] == topic }
                                val preTestScore = progress?.preTestScore ?: 0
                                val postTestScore = progress?.postTestScore ?: 0
                                val maxScore = 15
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = topic,
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontFamily = poppinsFontFamily,
                                        fontSize = 15.sp,
                                    )
                                    Text(
                                        text = String.format("%02d/%d", preTestScore, maxScore),
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontFamily = poppinsFontFamily,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = String.format("%02d/%d", postTestScore, maxScore),
                                        modifier = Modifier.weight(1f),
                                        color = Color.White,
                                        fontFamily = poppinsFontFamily,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))
            }

            //PROGRESS
            item {
                ProfileCard(
                    pillTitle = "Progress",
                    isExpandable = false,
                    collapsedContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                            Row (horizontalArrangement = Arrangement.Center) {
                                val mechanicsProgress = lessonProgressPercent.find { it.lessonTitle == "Mechanics" }?.percent ?: 0f
                                val newtonsLawProgress = lessonProgressPercent.find {
                                    val normalizedTitle = it.lessonTitle.replace('’', '\'')
                                    normalizedTitle == "Newton's Laws of Motion"
                                }?.percent ?: 0f
                                val wpeProgress = lessonProgressPercent.find { it.lessonTitle == "Work, Power, & Energy" }?.percent ?: 0f

                                // Debug logging
                                android.util.Log.d("PROGRESS_DEBUG", "Mechanics progress: $mechanicsProgress")
                                android.util.Log.d("PROGRESS_DEBUG", "Newton's Law progress: $newtonsLawProgress")
                                android.util.Log.d("PROGRESS_DEBUG", "WPE progress: $wpeProgress")
                                android.util.Log.d("PROGRESS_DEBUG", "All progress: $lessonProgressPercent")

                                CircularProgress(mechanicsProgress, "Mechanics", modifier = Modifier.size(60.dp))
                                Spacer(Modifier.weight(1f))
                                CircularProgress(newtonsLawProgress, "Newtons Laws \nof Motion", modifier = Modifier.size(60.dp))
                                Spacer(Modifier.weight(1f))
                                CircularProgress(wpeProgress, "Work, Power,\n & Energy", modifier = Modifier.size(60.dp))
                            }
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        // DIALOG IF SELECTED
        if (selectedAchievement != null) {
            AlertDialog(
                onDismissRequest = { selectedAchievement = null },
                title = { Text("⭐ ${selectedAchievement!!.title}", color = Color.Black, fontFamily = dmSansFontFamily, fontSize = 22.sp) },
                text = { Text(selectedAchievement!!.description, color = Color.Black, fontFamily = dmSansFontFamily, fontSize = 18.sp) },
                confirmButton = {
                    TextButton(onClick = { selectedAchievement = null }) {
                        Text("OK", color = Color.Black, fontFamily = dmSansFontFamily, fontSize = 18.sp)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                containerColor = Color.White
            )
        }
    }
}
@Composable
fun ProfilePicture(
    profilePictureUri: String,
    firstname: String,
    lastname: String,
    username: String,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Image picker launcher - single image selection only
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel.updateProfilePicture(username, it.toString(), context)
            Toast.makeText(context, "Uploading profile picture...", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch image picker
            imagePickerLauncher.launch("image/*")
        } else {
            // Permission denied, show dialog
            showPermissionDialog = true
        }
    }
    
    // Function to handle camera button click
    fun onCameraButtonClick() {
        if (PermissionUtils.hasImagePermission(context)) {
            // Permission already granted, launch image picker
            imagePickerLauncher.launch("image/*")
        } else {
            // Request permission first
            permissionLauncher.launch(PermissionUtils.getRequiredImagePermission())
        }
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .size(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
            ) {
                // Display selected image or default
                if (profilePictureUri != "default" && profilePictureUri != "default_profile_picture_uri") {
                    // Check if it's a local file path, Firebase URL, or other URI
                    val imageData = when {
                        profilePictureUri.startsWith("/") -> {
                            // Local file path
                            File(profilePictureUri)
                        }
                        profilePictureUri.startsWith("https://firebasestorage.googleapis.com") -> {
                            // Firebase Storage URL
                            profilePictureUri
                        }
                        else -> {
                            // Other URI string
                            profilePictureUri
                        }
                    }
                    
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageData)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.default_profile_pic),
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            IconButton(
                onClick = { onCameraButtonClick() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp)
                    .size(20.dp)
                    .background(Color.White, shape = CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change Profile Picture",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row (Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
            Text(
                text = "$firstname ",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = poppinsFontFamily
            )
            Text(
                text = lastname,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = poppinsFontFamily
            )
        }
        Text(
            text = "@$username",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
    
    // Permission denied dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { 
                Text(
                    "Permission Required", 
                    color = Color.Black, 
                    fontFamily = dmSansFontFamily, 
                    fontSize = 20.sp
                ) 
            },
            text = { 
                Text(
                    "MotionLab needs permission to access your photos to set your profile picture. Please grant permission in Settings.",
                    color = Color.Black, 
                    fontFamily = dmSansFontFamily, 
                    fontSize = 16.sp
                ) 
            },
            confirmButton = {
                TextButton(onClick = { 
                    PermissionUtils.openAppSettings(context)
                    showPermissionDialog = false 
                }) {
                    Text("Go to Settings", color = Color.Black, fontFamily = dmSansFontFamily, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", color = Color.Black, fontFamily = dmSansFontFamily, fontSize = 16.sp)
                }
            },
            shape = RoundedCornerShape(10.dp),
            containerColor = Color.White
        )
    }
}