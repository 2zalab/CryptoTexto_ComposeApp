package com.touzalab.cryptotexto.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.touzalab.cryptotexto.R
import com.touzalab.cryptotexto.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    onNavigateToEncryption = {
                        navController.navigate(Screen.Encryption.route)
                        scope.launch { drawerState.close() }
                    },
                    onNavigateToDecryption = {
                        navController.navigate(Screen.Decryption.route)
                        scope.launch { drawerState.close() }
                    },
                    onRateApp = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("market://details?id=${context.packageName}")
                            setPackage("com.android.vending")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                        }
                        scope.launch { drawerState.close() }
                    },
                    onOtherApps = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("market://developer?id=2zaLab")
                            setPackage("com.android.vending")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/developer?id=2zaLab")))
                        }
                        scope.launch { drawerState.close() }
                    },
                    onShare = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT,
                                "Découvrez CryptoTexto pour crypter et décrypter vos messages : " +
                                        "https://play.google.com/store/apps/details?id=${context.packageName}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
                        scope.launch { drawerState.close() }
                    },
                    onGitHub = {
                        val intent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/2zalab/CryptoTexto_ComposeApp"))
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    onAbout = {
                        navController.navigate(Screen.About.route)
                        scope.launch { drawerState.close() }
                    },
                    onDeveloper = {
                        navController.navigate(Screen.Developer.route)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header avec image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.header_illustration),
                            contentDescription = "header image",
                            contentScale = ContentScale.Crop
                        )
                    }

                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "CryptoTexto",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }

                // Contenu scrollable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                ) {
                    // Sous-titre
                    Text(
                        text = "Crypter et décrypter vos messages avec Crypto Texto",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Titre des opérations
                    Text(
                        text = "CHOISIR L'OPERATION",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cartes des opérations
                    OperationCard(
                        title = "CRYPTER UN MESSAGE",
                        description = "Cette option vous permet de crypter votre texte ou votre message",
                        icon = Icons.Default.Lock,
                        onClick = { navController.navigate(Screen.Encryption.route) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OperationCard(
                        title = "DECRYPTER UN MESSAGE",
                        description = "Cette option vous permet de décrypter votre texte ou votre message",
                        icon = Icons.Outlined.Lock,
                        onClick = { navController.navigate(Screen.Decryption.route) }
                    )

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }

            // Texte "from 2zaLab" en bas
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp,0.dp,10.dp)
                    .height(50.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "from 2zaLab",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    onNavigateToEncryption: () -> Unit,
    onNavigateToDecryption: () -> Unit,
    onRateApp: () -> Unit,
    onOtherApps: () -> Unit,
    onShare: () -> Unit,
    onGitHub: () -> Unit,
    onDeveloper: () -> Unit,
    onAbout: () -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        Divider()

        DrawerItem(
            icon = Icons.Default.Lock,
            text = "Crypter un message",
            onClick = onNavigateToEncryption
        )

        DrawerItem(
            icon = Icons.Outlined.Lock,
            text = "Décrypter un message",
            onClick = onNavigateToDecryption
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(
            icon = Icons.Default.Star,
            text = "Noter l'application",
            onClick = onRateApp
        )

        DrawerItem(
            icon = Icons.Default.List,
            text = "Autres applications",
            onClick = onOtherApps
        )

        DrawerItem(
            icon = Icons.Default.Share,
            text = "Partager",
            onClick = onShare
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Nouvelles options
        DrawerItem(
            icon = Icons.Default.Info,
            text = "À propos",
            onClick = onAbout
        )

        DrawerItem(
            icon = Icons.Outlined.Settings,
            text = "GitHub",
            onClick = onGitHub
        )

        DrawerItem(
            icon = Icons.Default.Person,
            text = "Développeur",
            onClick = onDeveloper
        )

        DrawerItem(
            icon = Icons.Default.Build,
            text = "Version 1.0",
            onClick = {}
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer avec copyright
        Text(
            text = "© 2025 2zaLab\nTous droits réservés",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(32.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}