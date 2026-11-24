package com.refillfinder.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.refillfinder.app.ui.screens.ai.AIChatScreen
import com.refillfinder.app.ui.screens.auth.LoginScreen
import com.refillfinder.app.ui.screens.community.CommunityScreen
import com.refillfinder.app.ui.screens.home.HomeScreen
import com.refillfinder.app.ui.screens.loading.LoadingScreen
import com.refillfinder.app.ui.screens.map.CampusMapScreen
import com.refillfinder.app.ui.screens.profile.ProfileScreen
import com.refillfinder.app.ui.screens.splash.SplashScreen
import com.refillfinder.app.viewmodel.AuthViewModel
import com.refillfinder.app.viewmodel.HomeViewModel

/**
 * NavGraph — demonstrates:
 *  - Jetpack Compose Navigation
 *  - Type-safe route arguments (coolerId)
 *  - Loading screen route between Login and Home
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // ── Splash ───────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // ── Login ────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // After login → show loading screen for 2-3s
                    navController.navigate(Screen.Loading.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // ── Post-Login Loading (2-3 seconds) ─────────────────────
        composable(Screen.Loading.route) {
            LoadingScreen(
                onLoadingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home (Explore) ───────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToCommunity = { coolerId ->
                    navController.navigate(Screen.Community.createRoute(coolerId))
                },
                onNavigateToAI = { navController.navigate(Screen.AIChat.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                viewModel = homeViewModel
            )
        }

        // ── Campus Map ───────────────────────────────────────────
        composable(Screen.Map.route) {
            CampusMapScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCommunity = { coolerId ->
                    navController.navigate(Screen.Community.createRoute(coolerId))
                }
            )
        }

        // ── Community ────────────────────────────────────────────
        composable(
            route = Screen.Community.route,
            arguments = listOf(navArgument("coolerId") { type = NavType.StringType })
        ) { backStack ->
            val coolerId = backStack.arguments?.getString("coolerId") ?: "block1_floor1"
            CommunityScreen(
                coolerId = coolerId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── AI Chat ──────────────────────────────────────────────
        composable(Screen.AIChat.route) {
            AIChatScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── Profile ──────────────────────────────────────────────
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                authViewModel = authViewModel
            )
        }
    }
}
