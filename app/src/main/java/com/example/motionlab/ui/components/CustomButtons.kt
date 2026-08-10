package com.example.motionlab.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
fun CardButtonSection(
    label: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit = {},
    expandable: Boolean = false,
    expandedContent: (@Composable () -> Unit)? = null,
    expanded: Boolean? = null,
    onExpandChanged: ((Boolean) -> Unit)? = null
) {
    val isControlled = expanded != null && onExpandChanged != null
    var internalExpanded by remember { mutableStateOf(false) }
    val isExpanded = expanded ?: internalExpanded

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(30.dp))
            .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(30.dp))
            .background(Color(0xff006cca))
            .clickable {
                if (expandable) {
                    if (isControlled) {
                        onExpandChanged?.invoke(!isExpanded)
                    } else {
                        internalExpanded = !internalExpanded
                    }
                } else {
                    onClick()
                }
            }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = if (isExpanded) 16.dp else 12.dp),
        contentAlignment = if (!isExpanded) Alignment.Center else Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(35.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (expandable && isExpanded && expandedContent != null) {
                Spacer(modifier = Modifier.height(16.dp))
                expandedContent()
            }
        }
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier){
    Image(
        painter = painterResource(id = R.drawable.back_button),
        contentDescription = "Back",
        modifier = modifier
            .sizeIn(maxHeight = 30.dp, maxWidth = 30.dp)
            .clickable { onClick() }
    )
}


@Composable
fun ProceedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
           onClick()
        },
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .border(
                width = 2.dp,
                color = Color(0xFFB9D9F6),
                shape = RoundedCornerShape(50)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF003366)
        ),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {
        Text(
            text = "Proceed",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = poppinsFontFamily,
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun NextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 120.dp)
            .clip(RoundedCornerShape(50))
            .border(
                width = 2.dp,
                color = Color(0xFFB9D9F6),
                shape = RoundedCornerShape(50)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF003366)
        ),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = poppinsFontFamily,
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun ShortcutButton(
    painter: Painter,
    contentDescription: String? = null,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    onLockedClick: () -> Unit //
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
            .clickable {
                if (isLocked) {
                    onLockedClick()
                } else {
                    onClick()
                }
            }
    ) {
        // Background image fills entire box
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        if (isLocked) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.4f)), // semi-transparent overlay
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.locked),
                    contentDescription = "Locked",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ShortcutButtonPreview() {
    Column {
        ShortcutButton(
            painter = painterResource(id = R.drawable.third_law_tb),
            contentDescription = "Unlocked",
            isLocked = false,
            onClick = { println("Unlocked clicked!") },
            onLockedClick = { println("Locked, should show Toast") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ShortcutButton(
            painter = painterResource(id = R.drawable.first_law_tb),
            contentDescription = "Locked",
            isLocked = true,
            onClick = { println("Unlocked clicked!") },
            onLockedClick = { println("Show toast: Finish subtopic to unlock") }
        )
    }
}
