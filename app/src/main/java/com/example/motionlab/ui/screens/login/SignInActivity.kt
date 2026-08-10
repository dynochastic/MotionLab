    package com.example.motionlab.ui.screens.login

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
    import com.example.motionlab.presentation.login.LoginState
    import com.example.motionlab.presentation.login.LoginViewModel
    import com.example.motionlab.ui.LockPortrait
    import com.example.motionlab.ui.components.BackTop
    import com.example.motionlab.ui.components.BlueTextField
    import com.example.motionlab.ui.navigation.Routes
    import com.example.motionlab.ui.theme.MainBlueBg

    @Composable
    fun SignInScreen(viewModel: LoginViewModel, navController: NavController) {
        LockPortrait()

        val context = LocalContext.current
        val loginState by viewModel.loginState.collectAsState()

        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        LaunchedEffect(loginState) {
            when (loginState) {
                is LoginState.Success -> {
                    val loggedInUsername = (loginState as LoginState.Success).account.username // ✅ the actual, validated username
                    android.util.Log.d("LOGIN_FLOW", "Login successful for user: $loggedInUsername, navigating to main app")
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                    navController.navigate(Routes.mainAppWithUsername(loggedInUsername)) {
                        popUpTo(Routes.SIGN_IN) { inclusive = true }
                    }
                    viewModel.resetState()
                }
                is LoginState.Error -> {
                    Toast.makeText(context, (loginState as LoginState.Error).message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
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
            Spacer(Modifier.height(60.dp))

            Text(
                "Sign in",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontSize = 40.sp,
                    color = Color.White
                )
            )

            Spacer(Modifier.height(50.dp))

            BlueTextField(
                value = username,
                onValueChange = { username = it },
                label = "USERNAME",
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            BlueTextField(
                value = password,
                onValueChange = { password = it },
                label = "PASSWORD",
                isPassword = true,
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 30.dp)
            )

            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier.size(width = 300.dp, height = 65.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff257dff),
                    contentColor = Color.White
                )
            ) {
                Text("Sign in", fontSize = 20.sp)
            }

            if (loginState is LoginState.Idle) {
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(Modifier.height(70.dp))

            Row(Modifier.padding(top = 10.dp)) {
                Text(
                    "Don't have an account?", fontSize = 15.sp,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                        color = Color.White
                    )
                )
                Spacer(Modifier.width(7.dp))
                Text(
                    "Sign Up",
                    textDecoration = TextDecoration.Underline,
                    fontSize = 15.sp,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.dm_sans_regular)),
                        color = Color(0xFF5f9abf)
                    ),
                    modifier = Modifier.clickable {
                        navController.navigate(Routes.SIGN_UP)
                    }
                )
            }
        }
    }

