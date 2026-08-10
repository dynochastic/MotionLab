package com.example.motionlab.ui.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.R
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.theme.BlueButtonColor

@Composable
fun LessonButton(
    lessonName: String,
    painter: Painter,
    description: String? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    isGreyedOut: Boolean = false,
    onClick: () -> Unit = {}
) {
    val clickableEnabled = if (isGreyedOut) false else !isLocked
    val overlayColor = if (!clickableEnabled && !isGreyedOut) Color.Black.copy(alpha = 0.85f) else Color.Transparent
    val buttonBackgroundColor = if (isGreyedOut) Color.Gray else BlueButtonColor
    
    Log.d("LESSON_BUTTON", "Lesson: $lessonName, isLocked=$isLocked, clickableEnabled=$clickableEnabled, overlayColor=$overlayColor")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
            .background(buttonBackgroundColor)
            .clickable(enabled = clickableEnabled) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = lessonName,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontFamily = poppinsFontFamily,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Image(
                painter = painter,
                contentDescription = description,
                modifier = Modifier
                    .size(90.dp)
                    .padding(end = 30.dp)
            )
        }

        if (!isGreyedOut && !clickableEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.locked),
                    contentDescription = "Locked",
                    modifier = Modifier.size(70.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LessonButtonPreview() {
    Column {
        Text("🔒 Normal Locked", color = Color.White)
        LessonButton(
            lessonName = "Work, Power, & Energy",
            description = "Locked",
            painter = painterResource(R.drawable.work_power_energy),
            isLocked = true
        )

        Spacer(modifier = Modifier.height(12.dp))



        Spacer(modifier = Modifier.height(12.dp))

        Text("🟪 Greyed Out (Taken)", color = Color.White)
        LessonButton(
            lessonName = "Pre-Test (Taken)",
            description = "Taken",
            painter = painterResource(R.drawable.pretest),
            isGreyedOut = true
        )
    }
}
