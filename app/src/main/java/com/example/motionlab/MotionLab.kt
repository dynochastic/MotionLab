package com.example.motionlab

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.motionlab.ui.navigation.AppShellNavGraph
import com.example.motionlab.ui.navigation.BottomNavBar
import com.example.motionlab.ui.navigation.BottomNavItem
import com.example.motionlab.ui.navigation.Routes

@Composable
fun MotionLab(username: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    //  always start at the correct lesson route with username
    LaunchedEffect(username) {
        if (currentRoute == null) {
            navController.navigate("lesson/$username") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    android.util.Log.d("NAV_DEBUG", "MotionLab: username=$username, startDestination=lesson/$username")

    val showBottomBarRoutes = listOf(
        Routes.LESSON,
        Routes.SIMULATION,
        Routes.LEADERBOARDS,
        Routes.PROFILE
    )

    val bottomNavItems = listOf(
        BottomNavItem("Lessons", painterResource(R.drawable.lesson_nav), Routes.lessonWithUsername(username)),
        BottomNavItem("Simulation", painterResource(R.drawable.simulation_nav), Routes.simulationRoute(username)),
        BottomNavItem("Leaderboard", painterResource(R.drawable.leaderboards_nav), Routes.leaderboardWithUsername(username)),
        BottomNavItem("Profile", painterResource(R.drawable.profile_nav), Routes.profileWithUsername(username)),
    )
    Scaffold(
        contentWindowInsets = WindowInsets.systemBars, // ✅ handles status + nav bar
        bottomBar = {
            if (currentRoute != null && showBottomBarRoutes.any { currentRoute.startsWith(it.substringBefore("/{")) }) {
                BottomNavBar(
                    items = bottomNavItems,
                    selectedRoute = currentRoute ?: Routes.LESSON,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars), // ✅ adds space above 3-button nav
                    onItemSelected = { item ->
                        android.util.Log.d("NAV_DEBUG", "BottomNavBar: navigating to ${item.route}")
                        navController.navigate(item.route) {
                            popUpTo(Routes.lessonWithUsername(username)) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppShellNavGraph(
            navController = navController,
            username = username,
            modifier = Modifier.padding(innerPadding) // ✅ respects Scaffold padding
        )
    }

}
val poppinsFontFamily = FontFamily(
    Font(R.font.poppins_bold, weight = FontWeight.Bold),
    Font(R.font.poppins, weight = FontWeight.Normal)
)
val dmSansFontFamily = FontFamily(
    Font(R.font.dm_sans_bold, weight = FontWeight.Bold),
    Font(R.font.dm_sans_regular, weight = FontWeight.Normal)
)

