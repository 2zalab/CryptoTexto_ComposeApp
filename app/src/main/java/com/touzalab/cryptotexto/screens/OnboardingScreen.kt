package com.touzalab.cryptotexto.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.touzalab.cryptotexto.R
import com.touzalab.cryptotexto.components.HorizontalPagerIndicator
import com.touzalab.cryptotexto.navigation.Screen
import com.touzalab.cryptotexto.ui.theme.Montserrat
import kotlinx.coroutines.launch
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.touzalab.cryptotexto.components.OnboardingPreferences


data class OnboardingPage(
    val title: String,
    val description: String,
    val animationRes: Int
)


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onboardingPreferences = remember { OnboardingPreferences(context) }

    val pages = listOf(
        OnboardingPage(
            "Bienvenue sur CryptoTexto",
            "L'application qui vous permet de sécuriser vos messages en toute simplicité",
            R.raw.welcome_animation// Replace with actual animation resource
        ),
        OnboardingPage(
            "Cryptage Sécurisé",
            "Utilisez des algorithmes de cryptage avancés pour protéger vos messages",
            R.raw.security_animation
        ),
        OnboardingPage(
            "Simple et Rapide",
            "Une interface intuitive pour crypter et décrypter vos messages en quelques clics",
            R.raw.fast_animation
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Use Box as the root container to allow fixed positioning at the bottom
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Content area that uses available space
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Indicator is below the pager content but above the fixed bottom buttons
            HorizontalPagerIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )

            // Space to ensure indicator isn't covered by the bottom buttons
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Fixed at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(onClick = {
                    scope.launch {
                        onboardingPreferences.setOnboardingCompleted()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }) {
                    Text("Passer", fontFamily = Montserrat)
                }
                Button(onClick = {
                    // Launch coroutine to animate to next page
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }) {
                    Text("Suivant", fontFamily = Montserrat)
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            onboardingPreferences.setOnboardingCompleted()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Commencer", fontFamily = Montserrat)
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(page.animationRes)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(200.dp),
            iterations = LottieConstants.IterateForever
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = Montserrat
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = Montserrat
        )
    }
}