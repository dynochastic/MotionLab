package com.example.motionlab.ui.screens.leaderboards

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.motionlab.R
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.leaderboards.LeaderboardViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.MainBlueBackTop
import com.example.motionlab.ui.components.PillHeader
import com.example.motionlab.ui.formatMillisToMinSec
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.ui.theme.First
import com.example.motionlab.ui.theme.MainBlueBg
import com.example.motionlab.ui.theme.Second
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.theme.Third
import com.example.motionlab.ui.theme.ThirdBlue


@Composable
fun LeaderboardsScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    LockPortrait()
    
    // Load cached data when screen is composed
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadLeaderboardOnNavigation()
    }

    val leaderboard = viewModel.leaderboard.collectAsState().value
    val lastUpdatedMillis = com.example.motionlab.utils.LeaderboardCache.getUpdatedAt(
        (androidx.compose.ui.platform.LocalContext.current)
    )
    val userRank = viewModel.userRank.collectAsState().value
    val currentUserId = viewModel.currentUserId
    android.util.Log.d("LEADERBOARD", "currentUserId: '$currentUserId'")
    val user = leaderboard.firstOrNull { it.safeUserId == (currentUserId ?: "") }
    val firstName = user?.safeFirstName ?: "Name"
    val lastName = user?.safeLastName ?: "Here"
    val normalizedUserId = (currentUserId ?: "").trim().lowercase()
    val rank = userRank ?: leaderboard.indexOfFirst { it.safeUserId.trim().lowercase() == normalizedUserId }.takeIf { it >= 0 }?.plus(1)

    Column(
        Modifier
            .fillMaxSize()
            .background(SecondBlueBg),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP SECTION
        Box {
            Box(contentAlignment = Alignment.Center) {
                MainBlueBackTop(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(0.dp, 0.dp, 50.dp, 50.dp))
                        .background(MainBlueBg)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Leaderboards",
                        color = ThirdBlue,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (rank != null) "YOUR OVERALL RANK #$rank" else "Not ranked yet",
                        color = Color.White,
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
        // BADGES
        Column(
            modifier = Modifier.height(250.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {


                Row (modifier = Modifier.offset(y = 20.dp)) {
                    leaderboard.getOrNull(1)?.let { second ->
                        LeaderboardBadge(
                            profileImageUrl = second.safeProfileImageUrl,
                            firstName = second.safeFirstName,
                            lastName = second.safeLastName,
                            rank = 2,
                            borderColor = Second,
                            showCrown = false,
                        )
                    }
                    Row(Modifier.offset(y = (-60).dp))  {
                        leaderboard.getOrNull(0)?.let { first ->
                            LeaderboardBadge(
                                profileImageUrl = first.safeProfileImageUrl,
                                firstName = first.safeFirstName,
                                lastName = first.safeLastName,
                                rank = 1,
                                borderColor = First,
                                showCrown = true
                            )
                        }
                    }
                    leaderboard.getOrNull(2)?.let { third ->
                        LeaderboardBadge(
                            profileImageUrl = third.safeProfileImageUrl,
                            firstName = third.safeFirstName,
                            lastName = third.safeLastName,
                            rank = 3,
                            borderColor = Third,
                            showCrown = false
                        )
                    }
                }

            }
        }
        // STATS CONTAINER with HEADER PILLS
        Box(modifier = Modifier.weight(1f)) { // Use weight instead of fillMaxSize
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                    .background(BlueButtonColor)
            ) {
                // Header pills positioned at the top of the container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Rank column
                    PillHeader("Rank", fontSize = 14)
                    
                    PillHeader("Name", fontSize = 14)
                    
                    PillHeader("Score", fontSize = 14)
                    
                    // Time column
                    PillHeader("Time", fontSize = 14)
                }
                
                // Scrollable content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 15.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Displays first top five with proper spacing
                    leaderboard.take(5).forEachIndexed { index, entry ->
                        LeaderboardStatsRow(
                            rank = index + 1,
                            firstName = entry.safeFirstName,
                            lastName = entry.safeLastName,
                            score = "${entry.safeOverallScore}/45",
                            time = formatMillisToMinSec(entry.safeOverallTime),
                            profileImageUrl = entry.safeProfileImageUrl,
                            backgroundColor = when (index) {
                                0 -> First
                                1 -> Second
                                2 -> Third
                                else -> Color.White
                            }
                        )
                        
                        // Add spacing between rows (except for the last one)
                        if (index < 4) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardBadge(
    profileImageUrl: String?,
    firstName: String?,
    lastName: String?,
    rank: Int,
    borderColor: Color,
    showCrown: Boolean = true,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(100.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, borderColor, CircleShape)
            ) {
                AsyncImage(
                    model = profileImageUrl?.takeIf { it.isNotBlank() } ?: "",
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.default_profile_pic),
                    error = painterResource(R.drawable.default_profile_pic),
                    fallback = painterResource(R.drawable.default_profile_pic),
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Rank Circle
            Box(
                modifier = Modifier
                    .padding(top = 90.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            if (showCrown) {
                Image(
                    painter = painterResource(R.drawable.crown),
                    contentDescription = "Crown",
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.TopCenter)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${(firstName ?: "Username").replaceFirstChar { it.uppercase() }} ${(lastName ?: "Here").replaceFirstChar { it.uppercase() }}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun LeaderboardStatsRow(
    rank: Int,
    firstName: String?,
    lastName: String?,
    score: String,
    time: String,
    profileImageUrl: String?,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(backgroundColor)
            .border(2.dp, Color.Black, RoundedCornerShape(30.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
        , contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank Circle - fixed width to match header
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    color = Color.Black,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            // Profile Picture and Name column
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(140.dp)
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    if (!profileImageUrl.isNullOrEmpty() && profileImageUrl != "default" && profileImageUrl != "default_profile_picture_uri") {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.default_profile_pic),
                            contentDescription = "Default Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Name
                Text(
                    text = "${(firstName ?: "Username").replaceFirstChar { it.uppercase() }} ${(lastName ?: "Here").replaceFirstChar { it.uppercase() }}",
                    fontSize = 16.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Visible
                )
            }
            
            // Score column - aligned with header
            Text(
                text = score,
                fontSize = 16.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )
            
            // Time column - aligned with header
            Text(
                text = time,
                fontSize = 16.sp,
                fontFamily = poppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}