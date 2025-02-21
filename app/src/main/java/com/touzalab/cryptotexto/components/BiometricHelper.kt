package com.touzalab.cryptotexto.components

import android.content.Context
//import android.hardware.biometrics.BiometricManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt

class BiometricHelper(private val activity: FragmentActivity) { // Utilisation directe de FragmentActivity

    private val biometricPrompt = BiometricPrompt(
        activity, // Plus besoin de cast
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Authentification réussie
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Erreur d'authentification
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Échec de l'authentification
            }
        }
    )

    fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentification requise")
                    .setSubtitle("Utilisez votre empreinte digitale pour accéder à vos clés secrètes")
                    .setNegativeButtonText("Annuler")
                    //setAllowedAuthenticators(BiometricPrompt.Authenticators.BIOMETRIC_STRONG)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(activity, "Aucun capteur biométrique", Toast.LENGTH_SHORT).show()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(activity, "Aucune empreinte enregistrée", Toast.LENGTH_SHORT).show()
            }
        }
    }

}