import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.theme.ThirdBlue

@Composable
fun ChoicePill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.White else Color.Transparent,
            contentColor = if (isSelected) ThirdBlue else Color.White
        ),
        border = BorderStroke(2.dp, Color.Black),
        contentPadding = PaddingValues(horizontal = 50.dp, vertical = 10.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            fontFamily = poppinsFontFamily
        )
    }
}

@Preview(showBackground = false)
@Composable
fun headerpillPreview(){
    ChoicePill(
        text = "DSJHDSDS",
        isSelected = false,
        onClick = {}
    )
}