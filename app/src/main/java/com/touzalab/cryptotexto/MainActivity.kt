package com.touzalab.cryptotexto
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.touzalab.cryptotexto.components.BiometricHelper
import com.touzalab.cryptotexto.components.OnboardingPreferences
import com.touzalab.cryptotexto.navigation.Screen
import com.touzalab.cryptotexto.screens.AboutScreen
import com.touzalab.cryptotexto.screens.DecryptionScreen
import com.touzalab.cryptotexto.screens.DeveloperScreen
import com.touzalab.cryptotexto.screens.EncryptionScreen
import com.touzalab.cryptotexto.screens.HomeScreen
import com.touzalab.cryptotexto.screens.OnboardingScreen
import com.touzalab.cryptotexto.screens.SecretKeysScreen
import com.touzalab.cryptotexto.screens.SplashScreen
import com.touzalab.cryptotexto.ui.theme.CryptoTextoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
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
      val context = LocalContext.current

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
        composable(Screen.SecretKeys.route) {
            val context = LocalContext.current
            val activity = remember {
                when (context) {
                    is FragmentActivity -> context
                    else -> throw IllegalStateException("L'activité doit être une FragmentActivity")
                }
            }

            // Créer le BiometricHelper avec l'activité
            val biometricHelper = remember { BiometricHelper(activity) }

            LaunchedEffect(Unit) {
                biometricHelper.showBiometricPrompt(
                    onSuccess = {
                        // L'authentification a réussi, ne rien faire car SecretKeysScreen gère déjà l'affichage
                    },
                    onError = { code, message ->
                        when (code) {
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_USER_CANCELED -> {
                                // L'utilisateur a annulé ou cliqué sur le bouton négatif
                                navController.navigateUp()
                            }
                            else -> {
                                // Autres erreurs
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                navController.navigateUp()
                            }
                        }
                    }
                )
            }

            SecretKeysScreen(navController)
        }
    }
}
