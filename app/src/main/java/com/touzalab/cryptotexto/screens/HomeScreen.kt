package com.touzalab.cryptotexto.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.sharp.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer


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
                    onNavigateToSecretKeys = {
                        navController.navigate(Screen.SecretKeys.route)
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
                    // Fond avec gradient et overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    ) {
                        // Image de fond avec effet parallaxe
                        Image(
                            painter = painterResource(id = R.drawable.header_illustration),
                            contentDescription = "header image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(alpha = 0.85f)
                        )

                        // Overlay avec motif de points
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val pattern = Path().apply {
                                for (x in 0..size.width.toInt() step 20) {
                                    for (y in 0..size.height.toInt() step 20) {
                                        addOval(
                                            Rect(
                                                offset = Offset(x.toFloat(), y.toFloat()),
                                                size = Size(2f, 2f)
                                            )
                                        )
                                    }
                                }
                            }
                            drawPath(
                                path = pattern,
                                color = Color.White.copy(alpha = 0.1f)
                            )
                        }

                        // Contenu du header
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo animé
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_crypto),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Titre avec effet de brillance
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .padding(horizontal = 32.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "CryptoTexto",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        shadow = Shadow(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            offset = Offset(2f, 2f),
                                            blurRadius = 4f
                                        )
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // TopAppBar avec effet de verre
                    CenterAlignedTopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
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
                        text = "CHOISIR UNE OPERATION",
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
                        icon = Icons.Rounded.LockOpen,
                        onClick = { navController.navigate(Screen.Decryption.route) }
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {  navController.navigate(Screen.SecretKeys.route) },
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            tint =  Color.White,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Mes Clés Secrètes")

                    }

                }
            }

            // Texte "from 2zaLab" en bas
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    //.padding(bottom = 22.dp)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
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
    onNavigateToSecretKeys: () -> Unit,
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
            icon = Icons.Outlined.LockOpen,
            text = "Décrypter un message",
            onClick = onNavigateToDecryption
        )

        DrawerItem(
            icon = Icons.Default.Fingerprint,
            text = "Mes Clés secrètes",
            onClick = onNavigateToSecretKeys
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DrawerItem(
            icon = Icons.Default.Star,
            text = "Noter l'application",
            onClick = onRateApp
        )

        DrawerItem(
            icon = Icons.Default.Apps,
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
            icon = Icons.Default.InsertLink,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}