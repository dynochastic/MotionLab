package com.example.motionlab.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.theme.ProfileCardColor
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun PillHeader(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int
) {
    Box(
        modifier = modifier
            .shadow(8.dp, shape = RoundedCornerShape(50), clip = false) // ✨ Elevation
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, Color.Black, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = ThirdBlue,
            fontFamily = poppinsFontFamily,
        )
    }
}
@Composable
fun CollapsibleCard(
    pillTitle: String,
    pillFontSize: Int = 25,
    isExpandable: Boolean = true,
    modifier: Modifier = Modifier,
    collapsedContent: @Composable () -> Unit,
    expandedContent: (@Composable () -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
                .then(
                    if (isExpandable) Modifier.clickable { isExpanded = !isExpanded } else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = ProfileCardColor),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(horizontal = 20.dp, vertical = 35.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !isExpandable || !isExpanded -> collapsedContent()
                    expandedContent != null      -> expandedContent()
                    else                         -> collapsedContent()
                }
            }
        }

        // Pill Header overlaps card
        PillHeader(
            text = pillTitle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(min = 150.dp),
            fontSize = pillFontSize
        )
    }
}



@Preview(showBackground = true)
@Composable
fun CardPreview(){
    CollapsibleCard(
        pillTitle = "Dynamic Card",
        isExpandable = true,
        collapsedContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tap to Expand ↓", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        expandedContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Expanded View", color = Color.White)
                repeat(4) {
                    Text("Line ${it + 1}", color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {}) {
                    Text("Collapse")
                }
            }
        }
    )
}
