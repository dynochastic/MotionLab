package com.example.motionlab.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.R
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.presentation.lesson.SubtopicUiState
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun SubtopicsCarousel(
    subtopics: List<SubtopicUiState>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onSubtopicClick: (SubtopicUiState) -> Unit = {}
) {
    if (subtopics.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No subtopics available.",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = poppinsFontFamily,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            val current = subtopics.getOrNull(currentIndex) ?: subtopics.first()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BlueButtonColor)
                    .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
                    .clickable(enabled = current.isUnlocked) {
                        onSubtopicClick(current)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                ) {
                    // Prev button
                    Image(
                        painter = painterResource(R.drawable.lessthan_button),
                        contentDescription = "Previous",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                onIndexChange(if (currentIndex > 0) currentIndex - 1 else subtopics.lastIndex)
                            }
                    )

                    // Title + Icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = current.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontFamily = poppinsFontFamily,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Image(
                            painter = painterResource(id = current.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    // Next button
                    Image(
                        painter = painterResource(R.drawable.greaterthan_button),
                        contentDescription = "Next",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                onIndexChange(if (currentIndex < subtopics.lastIndex) currentIndex + 1 else 0)
                            }
                    )
                }

                // Lock overlay
                if (!current.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.75f))
                            .align(Alignment.Center)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.locked),
                            contentDescription = "Locked",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                        )
                    }
                }
            }

            // Header pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-16).dp)
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, Color.Black, RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Subtopics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ThirdBlue,
                    fontFamily = poppinsFontFamily
                )
            }
        }

        // Dots indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            repeat(subtopics.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (index == currentIndex) Color.White else Color.Gray)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubtopicsCarouselPreview() {
    val sampleSubtopics = listOf(
        SubtopicUiState(
            subtopicId = 1,
            title = "Uniformly Accelerated Motion",
            iconRes = R.drawable.uam,
            content = "This is the content for UAM",
            videoCompleted = false,
            problemCompleted = false,
            simulationCompleted = false,
            isUnlocked = true
        ),
        SubtopicUiState(
            subtopicId = 2,
            title = "Projectile Motion",
            iconRes = R.drawable.video_lesson,
            content = "This is the content for PM",
            isUnlocked = false
        )
    )
    var carouselIndex by remember { mutableStateOf(0) }
    Column {
        Text("Subtopics Carousel", color = Color.White)
        SubtopicsCarousel(
            subtopics = sampleSubtopics,
            currentIndex = carouselIndex,
            onIndexChange = { carouselIndex = it }
        )
    }
}
