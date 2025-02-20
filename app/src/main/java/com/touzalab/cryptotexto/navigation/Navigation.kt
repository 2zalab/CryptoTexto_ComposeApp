package com.touzalab.cryptotexto.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Encryption : Screen("encryption")
    object Decryption : Screen("decryption")
}