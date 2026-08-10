package com.example.motionlab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motionlab.poppinsFontFamily

@Composable
fun BlueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    allowNumbers: Boolean = true,
    useBlackBorder: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)

    val keyboardOptions = when {
        isPassword -> KeyboardOptions(keyboardType = KeyboardType.Password)
        !allowNumbers -> KeyboardOptions(keyboardType = KeyboardType.Text)
        else -> KeyboardOptions.Default
    }

    val visualTransformation = if (isPassword && !passwordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    val borderColor = if (useBlackBorder) Color.Black else Color(0xFF1E90FF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 3.dp, color = borderColor, shape = shape)
            .clip(shape)
            .background(Color(0xFF003D6E))
            .height(70.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (!allowNumbers) {
                    if (it.all { c -> !c.isDigit() }) {
                        onValueChange(it)
                    }
                } else {
                    onValueChange(it)
                }
            },
            placeholder = {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = poppinsFontFamily,
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f)
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}
