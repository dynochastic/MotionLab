// SignUpScreen.kt
package com.example.motionlab.ui.screens.signup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.motionlab.data.local.entity.Account
import com.example.motionlab.presentation.signup.SignUpState
import com.example.motionlab.presentation.signup.SignUpViewModel
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackTop
import com.example.motionlab.ui.components.BlueTextField
import com.example.motionlab.ui.navigation.Routes
import com.example.motionlab.ui.theme.MainBlueBg

@Composable
fun SignUpScreen(viewModel: SignUpViewModel, navController: NavController) {
    LockPortrait()
    val signUpState by viewModel.signUpState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(signUpState) {
        when (signUpState) {
            is SignUpState.Success -> {
                Toast.makeText(context, "Sign-up successful", Toast.LENGTH_SHORT).show()
                navController.navigate(Routes.SIGN_IN) {
                    popUpTo(Routes.SIGN_IN) { inclusive = true }
                }
            }

            is SignUpState.Error -> {
                Toast.makeText(
                    context,
                    (signUpState as SignUpState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MainBlueBg)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackTop()
        Spacer(Modifier.height(15.dp))

        Text(
            "Create an Account",
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

        Button(
            onClick = {
                viewModel.signUp(
                    Account(
                        username = username,
                        firstname = firstname,
                        lastname = lastname,
                        password = password,
                        profilePictureUri = "default"
                    )
                )
            },
            enabled = signUpState != SignUpState.Loading,
            modifier = Modifier.size(width = 300.dp, height = 65.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xff257dff),
                contentColor = Color.White
            ),
        ) {
            Text("Sign up", fontSize = 20.sp)
        }

        Spacer(Modifier.height(60.dp))

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
                modifier = Modifier.clickable(onClick = { navController.navigate(Routes.SIGN_IN) }),
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

