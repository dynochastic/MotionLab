package com.example.motionlab.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.motionlab.R

@Composable
fun HamburgerButton(modifier: Modifier = Modifier) {
    Box(modifier) {
        Image(painter = painterResource(id = R.drawable.hamburger_menu), contentDescription = null)
    }
}