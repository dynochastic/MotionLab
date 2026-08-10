@file:Suppress("DEPRECATION")

package com.example.motionlab.ui.screens.settings


import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.motionlab.MainActivity
import com.example.motionlab.R
import com.example.motionlab.dmSansFontFamily
import com.example.motionlab.poppinsFontFamily
import com.example.motionlab.ui.LockPortrait
import com.example.motionlab.ui.components.BackButton
import com.example.motionlab.ui.components.BlueTextField
import com.example.motionlab.ui.components.CardButtonSection
import com.example.motionlab.ui.theme.SecondBlueBg
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.motionlab.presentation.profile.SettingsViewModel
import com.example.motionlab.presentation.profile.ChangePasswordState
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect

@Composable
fun Settings(navController: NavController, username: String) {
    LockPortrait()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)
    val viewModel: SettingsViewModel = hiltViewModel()
    val changePasswordState = viewModel.changePasswordState.collectAsState().value

    //Expanded Section to determine whether About or Setting will be closed
    var expandedSection by remember { mutableStateOf<String?>(null) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SecondBlueBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp))
                    .background(Color(0xff002340))
                    .heightIn(min = 250.dp)
                    .drawBehind {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.4f),
                            topLeft = Offset(0f, size.height - 20f),
                            size = Size(size.width, 30f)
                        )
                    }
            ) {

                BackButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 25.dp, top = 30.dp).size(50.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.logo_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(25.dp))

            var oldPassword by remember { mutableStateOf("") }
            var newPassword by remember { mutableStateOf("") }
            var confirmPassword by remember { mutableStateOf("") }

            // Expandable "Settings"
            CardButtonSection(
                label = "Settings",
                iconRes = R.drawable.gear,
                expandable = true,
                expanded = expandedSection == "settings",
                onExpandChanged = { expanded ->
                    expandedSection = if (expanded) "settings" else null
                },
                expandedContent = {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Change Password",
                            fontSize = 20.sp,
                            fontFamily = poppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(10.dp))

                        BlueTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = "ENTER CURRENT PASSWORD",
                            modifier = Modifier.fillMaxWidth(),
                            allowNumbers = true,
                            useBlackBorder = true,
                        )
                        Spacer(Modifier.height(10.dp))
                        BlueTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "ENTER NEW PASSWORD",
                            modifier = Modifier.fillMaxWidth(),
                            allowNumbers = true,
                            useBlackBorder = true,
                        )
                        Spacer(Modifier.height(10.dp))

                        BlueTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "CONFIRM NEW PASSWORD",
                            modifier = Modifier.fillMaxWidth(),
                            allowNumbers = true,
                            useBlackBorder = true
                        )
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                showChangePasswordDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 50.dp)
                            .border(2.dp, Color.Black, shape = RoundedCornerShape(50)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xff257dff),
                                contentColor = Color.White
                            ),
                        ) {
                            Text(
                                "CHANGE PASSWORD",
                                fontFamily = poppinsFontFamily,
                                fontSize = 15.sp
                            )
                        }
                        // Confirmation dialog
                        if (showChangePasswordDialog) {
                            AlertDialog(
                                onDismissRequest = { showChangePasswordDialog = false },
                                confirmButton = {
                                    Button(onClick = {
                                        showChangePasswordDialog = false
                                        viewModel.changePassword(username, oldPassword, newPassword, confirmPassword)
                                    }) {
                                        Text("Confirm", color = Color.White)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showChangePasswordDialog = false }) {
                                        Text("Cancel", color = Color.White)
                                    }
                                },
                                title = { Text("Change Password", color = Color.White, fontFamily = dmSansFontFamily ) },
                                text = { Text("Are you sure you want to change your password?", color = Color.White, fontFamily = dmSansFontFamily) },
                                containerColor = Color.Black.copy(.85f),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                        // Show feedback based on state
                        // Show Toast for error/success
                        LaunchedEffect(changePasswordState) {
                            when (changePasswordState) {
                                is ChangePasswordState.Error -> {
                                    Toast.makeText(context, (changePasswordState as ChangePasswordState.Error).message, Toast.LENGTH_LONG).show()
                                    viewModel.resetChangePasswordState()
                                }
                                is ChangePasswordState.Success -> {
                                    Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_LONG).show()
                                    viewModel.resetChangePasswordState()
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                }
                                else -> {}
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(25.dp))

            CardButtonSection(
                label = "About",
                iconRes = R.drawable.about,
                expandable = true,
                expanded = expandedSection == "about",
                onExpandChanged = { expanded ->
                    expandedSection = if (expanded) "about" else null
                },
                expandedContent = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("App Version: 1.0.0", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Made with ❤️ in MotionLab", color = Color.White)
                    }
                }
            )

            Spacer(Modifier.height(25.dp))

            // Non-expandable "Sign Out"
            CardButtonSection(
                label = "Sign out",
                iconRes = R.drawable.signout,
                onClick = {
                    showSignOutDialog = true
                },
                expandable = false
            )

            Spacer(Modifier.height(25.dp))

            if (showSignOutDialog) {
                AlertDialog(
                    onDismissRequest = { showSignOutDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? Activity)?.overridePendingTransition(
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right
                            )
                            (context as? Activity)?.finishAffinity()
                        }) {
                            Text(
                                "Sign out",
                                color = Color.White,
                                fontFamily = dmSansFontFamily,
                                fontSize = 20.sp
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSignOutDialog = false }) {
                            Text(
                                "Cancel",
                                color = Color.White,
                                fontFamily = dmSansFontFamily,
                                fontSize = 18.sp
                            )
                        }
                    },
                    title = {
                        Text(
                            "Sign Out",
                            color = Color.White,
                            fontFamily = dmSansFontFamily,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            "Are you sure you want to sign out?",
                            color = Color.White,
                            fontFamily = dmSansFontFamily,
                            fontSize = 18.sp
                        )
                    },
                    containerColor = Color.Black.copy(.85f),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun prev(){
    Settings(navController = rememberNavController(), username = "testuser")
}