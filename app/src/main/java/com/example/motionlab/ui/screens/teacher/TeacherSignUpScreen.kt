package com.example.motionlab.ui.screens.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.motionlab.ui.components.BackTop
import com.example.motionlab.ui.components.BlueTextField
import com.example.motionlab.ui.navigation.Routes
import androidx.compose.material3.MaterialTheme
import com.example.motionlab.ui.theme.BlueButtonColor
import com.example.motionlab.utils.PasswordUtils
import com.example.motionlab.utils.PasswordValidator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.SharedPreferences

@Composable
fun TeacherSignUpScreen(navController: NavController) {
    LockPortrait()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val firestore = remember { FirebaseFirestore.getInstance() }

    BackHandler(enabled = true) {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        } else {
            navController.navigate(Routes.SIGN_IN) {
                popUpTo(Routes.SIGN_IN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackTop()
        Spacer(Modifier.height(15.dp))
        Text(
            "Create Teacher Account",
            fontSize = 22.sp,
            color = Color.White,
            style = TextStyle(fontFamily = FontFamily(Font(R.font.poppins_bold)))
        )

        Spacer(Modifier.height(25.dp))

        BlueTextField(
            value = username,
            onValueChange = { username = it },
            label = "USERNAME",
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(20.dp))

        BlueTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = "FIRST NAME",
            modifier = Modifier.padding(horizontal = 40.dp),
            allowNumbers = false
        )

        Spacer(Modifier.height(20.dp))

        BlueTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = "LAST NAME",
            modifier = Modifier.padding(horizontal = 40.dp),
            allowNumbers = false
        )

        Spacer(Modifier.height(20.dp))

        BlueTextField(
            value = password,
            onValueChange = { password = it },
            label = "PASSWORD",
            isPassword = true,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(20.dp))

        BlueTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "CONFIRM PASSWORD",
            isPassword = true,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (username.isBlank() || firstname.isBlank() || lastname.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val passwordError = PasswordValidator.getPasswordErrorMessage(password)
                if (passwordError != null) {
                    Toast.makeText(context, passwordError, Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val cleanUsername = username.trim().lowercase()
                val hashed = PasswordUtils.hashPassword(password)

                scope.launch {
                    try {
                        val existing = firestore.collection("teachersAccount")
                            .document(cleanUsername)
                            .get()
                            .await()
                        if (existing.exists()) {
                            Toast.makeText(context, "Username already taken", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val teacherData = mapOf(
                            "username" to cleanUsername,
                            "passwordHash" to hashed,
                            "firstname" to firstname.trim(),
                            "lastname" to lastname.trim()
                        )
                        firestore.collection("teachersAccount")
                            .document(cleanUsername)
                            .set(teacherData)
                            .await()
                        
                        val prefs = context.getSharedPreferences("account_type_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putString("last_account_type", "Teacher").apply()
                        
                        Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.SIGN_IN) {
                            popUpTo(Routes.SIGN_IN) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.size(width = 200.dp, height = 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BlueButtonColor,
                contentColor = Color.White
            ),
        ) {
            Text("Sign up", fontSize = 20.sp)
        }

        Spacer(Modifier.height(40.dp))

        Row(Modifier.padding(top = 10.dp)) {
            Text(
                "Already registered?",
                fontSize = 15.sp,
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                    color = Color.White
                )
            )
            Spacer(Modifier.width(7.dp))
            Text(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { 
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.SIGN_IN) {
                            popUpTo(Routes.SIGN_IN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                text = "Sign in",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                    color = Color(0xFF5f9abf),
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}


