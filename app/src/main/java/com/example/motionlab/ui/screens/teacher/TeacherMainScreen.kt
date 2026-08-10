package com.example.motionlab.ui.screens.teacher

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.motionlab.R
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BlueTextField
import com.example.motionlab.ui.navigation.Routes
import androidx.compose.material3.MaterialTheme
import com.example.motionlab.ui.theme.SecondBlueBg
import com.example.motionlab.ui.theme.BlueButtonColor
import com.google.firebase.firestore.FirebaseFirestore

data class LeaderboardRecord(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val school: String,
    val section: String,
    val currentScores: Map<String, Map<String, Any>>,
    val overallScore: Long,
    val overallTime: Long
)

private fun formatTime(timeValue: Long): String {
    val totalSeconds = if (timeValue > 1000) timeValue / 1000 else timeValue
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun TeacherMainScreen(navController: NavController) {
    LockPortrait()

    val context = LocalContext.current

    BackHandler {
        (context as? androidx.activity.ComponentActivity)?.finishAffinity()
    }

    val firestore = FirebaseFirestore.getInstance()
    val configuration = LocalConfiguration.current
    var nameQuery by remember { mutableStateOf("") }
    var schoolQuery by remember { mutableStateOf("") }
    var sectionQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<LeaderboardRecord>>(emptyList()) }
    var defaultResults by remember { mutableStateOf<List<LeaderboardRecord>>(emptyList()) }

    val screenHeight = configuration.screenHeightDp.dp
    val availableHeight = screenHeight * 0.52f

    LaunchedEffect(Unit) {
        try {
            loadDefaultRecords(firestore) { results ->
                defaultResults = results
            }
        } catch (e: Exception) {
        }
    }

    LaunchedEffect(nameQuery, schoolQuery, sectionQuery) {
        try {
            performSearch(firestore, nameQuery, schoolQuery, sectionQuery) { results ->
                searchResults = results
            }
        } catch (e: Exception) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        Text(
            "Search Student",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                fontSize = 32.sp,
                color = Color.Black
            )
        )

        Spacer(Modifier.height(30.dp))

        BlueTextField(
            value = nameQuery,
            onValueChange = { nameQuery = it },
            label = "Name or Username",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BlueTextField(
                value = sectionQuery,
                onValueChange = { sectionQuery = it },
                label = "Section",
                modifier = Modifier.weight(1f)
            )

            BlueTextField(
                value = schoolQuery,
                onValueChange = { schoolQuery = it },
                label = "School",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        val hasFilters = nameQuery.isNotBlank() || schoolQuery.isNotBlank() || sectionQuery.isNotBlank()
        val displayResults = if (hasFilters) {
            searchResults
        } else {
            defaultResults
        }
        val isSearching = hasFilters

        if (displayResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = availableHeight)
                    .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayResults) { record ->
                    LeaderboardCard(record = record)
                }
            }
        } else if (isSearching) {
            Text(
                "No results found",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            )
        }

        Spacer(Modifier.height(20.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSearching) {
                Button(
                    onClick = {
                        try {
                            loadDefaultRecords(firestore) { results ->
                                defaultResults = results
                            }
                        } catch (e: Exception) {
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueButtonColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("Refresh", fontSize = 20.sp)
                }
            }

            Button(
                onClick = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueButtonColor,
                    contentColor = Color.White
                )
            ) {
                Text("Sign Out", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun LeaderboardCard(record: LeaderboardRecord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp), clip = false)
            .border(0.5.dp, Color.Black, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${record.firstName} ${record.lastName}",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 18.sp,
                    color = Color.Black
                )
            )

            Text(
                text = "Username: ${record.userId}",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            )

            if (record.school.isNotBlank() || record.section.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buildString {
                        if (record.school.isNotBlank()) append(record.school)
                        if (record.school.isNotBlank() && record.section.isNotBlank()) append(" • ")
                        if (record.section.isNotBlank()) append(record.section)
                    },
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                        fontSize = 13.sp,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Current Scores:",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            )

            Spacer(Modifier.height(8.dp))

            val topics = listOf(
                "mechanics" to "Mechanics",
                "newtonsLaw" to "Laws of Motion",
                "wpe" to "WPE"
            )
            topics.forEach { (firestoreKey, displayName) ->
                val topicData = record.currentScores[firestoreKey] as? Map<String, Any>
                if (topicData != null) {
                    val score = topicData["score"] as? Long ?: 0L
                    val time = topicData["time"] as? Long ?: 0L
                    val attempts = topicData["attempts"] as? Long ?: 0L

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = displayName,
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        )
                        Text(
                            text = "Score: $score | Time: ${formatTime(time)} | Attempts: $attempts",
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                                fontSize = 12.sp,
                                color = Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

        }
    }
}

private fun performSearch(
    firestore: FirebaseFirestore,
    nameQuery: String,
    schoolQuery: String,
    sectionQuery: String,
    onResults: (List<LeaderboardRecord>) -> Unit
) {
    firestore.collection("leaderboards")
        .get()
        .addOnSuccessListener { result ->
            val records = result.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val userId = doc.id
                    val firstName = data["firstName"] as? String ?: ""
                    val lastName = data["lastName"] as? String ?: ""
                    val schoolValue = data["school"] as? String ?: ""
                    val sectionValue = data["section"] as? String ?: ""
                    val currentScores = data["currentScores"] as? Map<String, Map<String, Any>> ?: emptyMap()
                    val overallScore = (data["overallScore"] as? Long) ?: 0L
                    val overallTime = (data["overallTime"] as? Long) ?: 0L

                    val matchesName = nameQuery.isBlank() || 
                        firstName.contains(nameQuery, ignoreCase = true) ||
                        lastName.contains(nameQuery, ignoreCase = true) ||
                        userId.contains(nameQuery, ignoreCase = true)
                    
                    val matchesSchool = schoolQuery.isBlank() || 
                        schoolValue.contains(schoolQuery, ignoreCase = true)
                    
                    val matchesSection = sectionQuery.isBlank() || 
                        sectionValue.contains(sectionQuery, ignoreCase = true)

                    if (matchesName && matchesSchool && matchesSection) {
                        LeaderboardRecord(
                            userId = userId,
                            firstName = firstName,
                            lastName = lastName,
                            school = schoolValue,
                            section = sectionValue,
                            currentScores = currentScores,
                            overallScore = overallScore,
                            overallTime = overallTime
                        )
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
            onResults(records)
        }
        .addOnFailureListener {
            onResults(emptyList())
        }
}

private fun loadDefaultRecords(
    firestore: FirebaseFirestore,
    onResults: (List<LeaderboardRecord>) -> Unit
) {
    firestore.collection("leaderboards")
        .limit(10)
        .get()
        .addOnSuccessListener { result ->
            val records = result.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val userId = doc.id
                    val firstName = data["firstName"] as? String ?: ""
                    val lastName = data["lastName"] as? String ?: ""
                    val school = data["school"] as? String ?: ""
                    val section = data["section"] as? String ?: ""
                    val currentScores = data["currentScores"] as? Map<String, Map<String, Any>> ?: emptyMap()
                    val overallScore = (data["overallScore"] as? Long) ?: 0L
                    val overallTime = (data["overallTime"] as? Long) ?: 0L

                    LeaderboardRecord(
                        userId = userId,
                        firstName = firstName,
                        lastName = lastName,
                        school = school,
                        section = section,
                        currentScores = currentScores,
                        overallScore = overallScore,
                        overallTime = overallTime
                    )
                } catch (_: Exception) {
                    null
                }
            }
            onResults(records)
        }
        .addOnFailureListener {
            onResults(emptyList())
        }
}

