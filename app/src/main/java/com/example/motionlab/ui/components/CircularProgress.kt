package com.example.motionlab.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.theme.MainBlueBg

@Composable
fun CircularProgress(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 110.dp,
    strokeWidth: Dp = 15.dp,
    backgroundColor: Color = MainBlueBg,
    progressColor: Color = Color(0xff5ce1e6) //Turquoise Blue
) {
    Column (horizontalAlignment = Alignment.CenterHorizontally){
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.size(size)
        ) {
            Canvas(modifier = Modifier.size(size)) {
                //  circle
                drawCircle(
                    color = backgroundColor,
                    style = Stroke(width = strokeWidth.toPx())
                )
                // fill progress circle
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            //  Percentage
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontFamily = poppinsFontFamily

            )
        }
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier
                .padding(top = 15.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            fontFamily = poppinsFontFamily
        )
    }

}
@Preview(showBackground = false)
@Composable
fun Previews(){
    Row (horizontalArrangement = Arrangement.SpaceEvenly) {
        CircularProgress(progress = .50f, "Mechanics",)
        CircularProgress(1f,"Newton's Laws \nof Motion")
        CircularProgress(progress = .50f, "Work, Power, & Energy",)


    }
}