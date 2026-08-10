package com.example.motionlab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun PillHeaderCard(
    pillText: String,
    modifier: Modifier = Modifier,
    pillModifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier,
    pillColor: Color = Color.White,
    pillTextColor: Color = ThirdBlue,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        // Pill header floating above the card
        Box(
            modifier = pillModifier
                .offset(y = 0.dp)
                .zIndex(1f)
                .clip(RoundedCornerShape(50))
                .border(1.dp, Color.Black, RoundedCornerShape(50))
                .background(pillColor)
                .padding(horizontal = 60.dp, vertical = 10.dp)
        ) {
            Text(
                text = pillText,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 20.sp,
                color = pillTextColor,
                fontFamily = poppinsFontFamily
            )
        }
        // Card content below the pill
        Column(
            modifier = cardModifier
                .padding(top = 35.dp) // Space for the pill
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BlueButtonColor)
                .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
} 