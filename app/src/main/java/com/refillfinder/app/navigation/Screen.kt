package com.refillfinder.app.navigation

sealed class Screen(val route: String) {
    object Splash      : Screen("splash")
    object Login       : Screen("login")
    object Loading     : Screen("loading")        // Post-login 2-3s loading
    object Onboarding  : Screen("onboarding")
    object Home        : Screen("home")
    object Map         : Screen("map")
    object Community   : Screen("community/{coolerId}") {
        fun createRoute(coolerId: String) = "community/$coolerId"
    }
    object AIChat      : Screen("ai_chat")
    object Profile     : Screen("profile")
}
