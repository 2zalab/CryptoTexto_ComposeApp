package com.touzalab.cryptotexto

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.touzalab.cryptotexto.components.OnboardingPreferences
import com.touzalab.cryptotexto.navigation.Screen
import com.touzalab.cryptotexto.screens.AboutScreen
import com.touzalab.cryptotexto.screens.DecryptionScreen
import com.touzalab.cryptotexto.screens.DeveloperScreen
import com.touzalab.cryptotexto.screens.EncryptionScreen
import com.touzalab.cryptotexto.screens.HomeScreen
import com.touzalab.cryptotexto.screens.OnboardingScreen
import com.touzalab.cryptotexto.screens.SplashScreen
import com.touzalab.cryptotexto.ui.theme.CryptoTextoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val onboardingPreferences = OnboardingPreferences(this)

        lifecycleScope.launch {
            val isCompleted = onboardingPreferences.isOnboardingCompleted.first()
            val startDestination = if (isCompleted) Screen.Home.route else Screen.Onboarding.route

            setContent {
                CryptoTextoTheme {
                    CryptoTextoApp(startDestination)
                }
            }
        }
    }
}

@Composable
fun CryptoTextoApp(startDestination: String) {
      val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Encryption.route) {
            EncryptionScreen(navController = navController)
        }
        composable(Screen.Decryption.route) {
            DecryptionScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.Developer.route) {
            DeveloperScreen(navController = navController)
        }
    }
}
