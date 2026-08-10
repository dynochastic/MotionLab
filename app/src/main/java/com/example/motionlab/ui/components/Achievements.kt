package com.example.motionlab.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.R
import com.example.motionlab.dmSansFontFamily
import com.example.motionlab.ui.theme.AchievementText

@Composable
fun AchievementCard(
    title: String,
    isMedal: Boolean,
    isLocked: Boolean,
    description: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isMedal) Color.White else Color(0xFFFFDE59)
    val imageRes = if (isMedal) R.drawable.medal else R.drawable.trophy

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
            .heightIn(180.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.wrapContentHeight().fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit
            )

            Text(
                text = title,
                color = AchievementText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = dmSansFontFamily,
                textAlign = TextAlign.Center,
                minLines = 1,
                modifier = Modifier
                    .fillMaxWidth(.95f)
            )
        }

        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.90f))
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AchievementCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AchievementCard(
            title = "Motion Seeker",
            isMedal = true,
            isLocked = false,
            description = "Complete the topic on Mechanics"
        )
        AchievementCard(
            title = "Quick Thinker",
            isMedal = false,
            isLocked = true,
            description = "Get a perfect score on the Mechanics Pre-test"
        )
    }
}

