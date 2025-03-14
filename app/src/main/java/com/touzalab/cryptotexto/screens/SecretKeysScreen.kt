package com.touzalab.cryptotexto.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.touzalab.cryptotexto.R
import com.touzalab.cryptotexto.components.*
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretKeysScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val dataStore = remember { SecretKeysDataStore(context) }
    val viewModel: SecretKeysViewModel = viewModel(
        factory = SecretKeysViewModelFactory(dataStore, context)
    )
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isAuthenticated by remember { mutableStateOf(false) }
    val activity = remember {
        when (context) {
            is FragmentActivity -> context
            else -> throw IllegalStateException("L'activité doit être une FragmentActivity")
        }
    }
    val biometricHelper = remember { BiometricHelper(activity) }

    LaunchedEffect(Unit) {
        biometricHelper.showBiometricPrompt(
            onSuccess = {
                isAuthenticated = true
            },
            onError = { code, message ->
                when (code) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        navController.navigateUp()
                    }
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        navController.navigateUp()
                    }
                    else -> {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        navController.navigateUp()
                    }
                }
            }
        )
    }

    LaunchedEffect(state.showMessage) {
        if (state.showMessage) {
            state.message?.let {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Short
                )
            }
            state.error?.let {
                snackbarHostState.showSnackbar(
                    message = it,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond d'écran avec dégradé subtil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Contenu principal avec effet de flou si non authentifié
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isAuthenticated) {
                        Modifier.blur(radius = 20.dp)
                    } else {
                        Modifier
                    }
                )
        ) {
            MainContent(
                navController = navController,
                viewModel = viewModel,
                state = state,
                snackbarHostState = snackbarHostState
            )
        }

        // Overlay d'authentification
        if (!isAuthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                AuthenticationCard()
            }
        }
    }
}

@Composable
fun AuthenticationCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.85f)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icône animée de sécurité
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Icon(
                    imageVector = Icons.Filled.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(scale),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Authentification requise",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Veuillez vous authentifier pour accéder à vos clés secrètes",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    navController: NavController,
    viewModel: SecretKeysViewModel,
    state: SecretKeysState,
    snackbarHostState: SnackbarHostState
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    actionColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mes Clés Secrètes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.showAddKeyDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Ajouter une clé",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.secretKeys.isEmpty()) {
                EmptyKeysContent()
            } else {
                KeysListContent(
                    viewModel = viewModel,
                    state = state
                )
            }

            // Dialogs
            if (state.showAddDialog) {
                AddSecretKeyDialog(
                    onDismiss = { viewModel.hideAddKeyDialog() },
                    onConfirm = { algorithm, key, description ->
                        viewModel.addSecretKey(algorithm, key, description)
                    },
                    viewModel = viewModel
                )
            }

            if (state.showEditDialog && state.selectedKeyForEdit != null) {
                EditSecretKeyDialog(
                    secretKey = state.selectedKeyForEdit,
                    onDismiss = { viewModel.hideEditDialog() },
                    onConfirm = { secretKey, newKey, newDescription ->
                        viewModel.editKey(secretKey, newKey, newDescription)
                    },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun EmptyKeysContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Animation subtile de l'icône
            val infiniteTransition = rememberInfiniteTransition()
            val iconColor by infiniteTransition.animateColor(
                initialValue = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                targetValue = MaterialTheme.colorScheme.primary,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Icon(
                imageVector = Icons.Rounded.VpnKey,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aucune clé enregistrée",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ajoutez votre première clé en appuyant sur le bouton + en bas de l'écran",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun KeysListContent(
    viewModel: SecretKeysViewModel,
    state: SecretKeysState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // En-tête avec les boutons d'export
        ExportButtonsRow(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des clés
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Espace pour le FAB
        ) {
            items(
                items = state.secretKeys,
                key = { it.id }
            ) { secretKey ->
                SecretKeyCard(
                    secretKey = secretKey,
                    onCopy = { viewModel.copyKey(secretKey) },
                    onEdit = { viewModel.showEditDialog(secretKey) },
                    onDelete = { viewModel.deleteKey(secretKey) }
                )
            }
        }
    }
}

@Composable
fun ExportButtonsRow(viewModel: SecretKeysViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Exporter : ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalIconButton(
            onClick = { viewModel.exportKeys("pdf") },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "Exporter en PDF",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        FilledTonalIconButton(
            onClick = { viewModel.exportKeys("txt") },
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            ),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Exporter en TXT",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun SecretKeyCard(
    secretKey: SecretKey,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Déterminer l'icône en fonction de l'algorithme
    val algorithmIcon: ImageVector = when (secretKey.algorithm) {
        "Cesar" -> Icons.Outlined.Numbers
        "Vigenère" -> Icons.Outlined.Abc
        "Affine" -> Icons.Outlined.Functions
        "Transposition" -> Icons.Outlined.SwapVert
        else -> Icons.Outlined.Key
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône de l'algorithme
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = algorithmIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Informations sur la clé
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = secretKey.algorithm,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = secretKey.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSecretKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    viewModel: SecretKeysViewModel
) {
    var selectedAlgorithm by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    var keyA by remember { mutableStateOf("") }
    var keyB by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val isAffineKeyValid = remember(keyA, keyB) {
        if (selectedAlgorithm == "Affine") {
            keyA.toIntOrNull()?.let { a ->
                keyB.toIntOrNull()?.let { b ->
                    a != 0 && b in 0..25 && a.toBigInteger().gcd(BigInteger.valueOf(26)) == BigInteger.ONE
                }
            } ?: false
        } else true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouvelle clé secrète",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedAlgorithm,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Algorithme") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        viewModel.algorithms.forEach { algorithm ->
                            DropdownMenuItem(
                                text = { Text(algorithm) },
                                onClick = {
                                    selectedAlgorithm = algorithm
                                    key = ""
                                    keyA = ""
                                    keyB = ""
                                    expanded = false
                                },
                                leadingIcon = {
                                    val icon = when (algorithm) {
                                        "Cesar" -> Icons.Outlined.Numbers
                                        "Vigenère" -> Icons.Outlined.Abc
                                        "Affine" -> Icons.Outlined.Functions
                                        "Transposition" -> Icons.Outlined.SwapVert
                                        else -> Icons.Outlined.Key
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectedAlgorithm.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    when (selectedAlgorithm) {
                        "Affine" -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = keyA,
                                    onValueChange = {
                                        if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                                            keyA = it
                                        }
                                    },
                                    label = { Text("Clé a (nombre premier avec 26)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = keyA.isNotEmpty() && !isAffineKeyValid,
                                    supportingText = if (keyA.isNotEmpty() && !isAffineKeyValid) {
                                        { Text("La clé 'a' doit être première avec 26") }
                                    } else null,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        errorBorderColor = MaterialTheme.colorScheme.error,
                                        errorLabelColor = MaterialTheme.colorScheme.error
                                    )
                                )

                                OutlinedTextField(
                                    value = keyB,
                                    onValueChange = {
                                        if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                                            keyB = it
                                        }
                                    },
                                    label = { Text("Clé b (0-25)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = keyB.isNotEmpty() && keyB.toIntOrNull()?.let { it !in 0..25 } ?: false,
                                    supportingText = if (keyB.isNotEmpty() && keyB.toIntOrNull()?.let { it !in 0..25 } ?: false) {
                                        { Text("La clé 'b' doit être entre 0 et 25") }
                                    } else null,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                                        errorBorderColor = MaterialTheme.colorScheme.error,
                                        errorLabelColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }
                        "Vigenère" -> {
                            OutlinedTextField(
                                value = key,
                                onValueChange = { newKey ->
                                    if (newKey.all { it.isLetter() || it.isWhitespace() }) {
                                        key = newKey.uppercase()
                                    }
                                },
                                label = { Text("Clé (lettres uniquement)") },
                                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showKey = !showKey }) {
                                        Icon(
                                            imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showKey) "Masquer la clé" else "Afficher la clé",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        "Cesar" -> {
                            OutlinedTextField(
                                value = key,
                                onValueChange = { newKey ->
                                    if (newKey.isEmpty() || (newKey.toIntOrNull() != null && newKey.toInt() in 0..25)) {
                                        key = newKey
                                    }
                                },
                                label = { Text("Décalage (0-25)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        "Transposition" -> {
                            OutlinedTextField(
                                value = key,
                                onValueChange = { newKey ->
                                    if (newKey.isEmpty() || newKey.toIntOrNull() != null) {
                                        key = newKey
                                    }
                                },
                                label = { Text("Clé (nombre entier positif)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalKey = when (selectedAlgorithm) {
                        "Affine" -> "$keyA:$keyB"
                        else -> key
                    }
                    onConfirm(selectedAlgorithm, finalKey, description)
                    onDismiss()
                },
                enabled = when (selectedAlgorithm) {
                    "Affine" -> isAffineKeyValid && keyA.isNotEmpty() && keyB.isNotEmpty() && description.isNotEmpty()
                    else -> selectedAlgorithm.isNotBlank() && key.isNotBlank() && description.isNotEmpty()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSecretKeyDialog(
    secretKey: SecretKey,
    onDismiss: () -> Unit,
    onConfirm: (SecretKey, String, String) -> Unit,
    viewModel: SecretKeysViewModel
) {
    var key by remember { mutableStateOf("") }
    var keyA by remember { mutableStateOf("") }
    var keyB by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(secretKey.description) }
    var showKey by remember { mutableStateOf(false) }

    LaunchedEffect(secretKey) {
        when (secretKey.algorithm) {
            "Affine" -> {
                val (a, b) = secretKey.key.split(":")
                keyA = a
                keyB = b
            }
            else -> key = secretKey.key
        }
    }

    val isAffineKeyValid = remember(keyA, keyB) {
        if (secretKey.algorithm == "Affine") {
            keyA.toIntOrNull()?.let { a ->
                keyB.toIntOrNull()?.let { b ->
                    a != 0 && b in 0..25 && a.toBigInteger().gcd(BigInteger.valueOf(26)) == BigInteger.ONE
                }
            } ?: false
        } else true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modifier la clé secrète",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Affichage de l'algorithme (non modifiable)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (secretKey.algorithm) {
                            "Cesar" -> Icons.Outlined.Numbers
                            "Vigenère" -> Icons.Outlined.Abc
                            "Affine" -> Icons.Outlined.Functions
                            "Transposition" -> Icons.Outlined.SwapVert
                            else -> Icons.Outlined.Key
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Algorithme : ${secretKey.algorithm}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Champs de saisie selon l'algorithme
                when (secretKey.algorithm) {
                    "Affine" -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = keyA,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                                        keyA = it
                                    }
                                },
                                label = { Text("Clé a (nombre premier avec 26)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = keyA.isNotEmpty() && !isAffineKeyValid,
                                supportingText = if (keyA.isNotEmpty() && !isAffineKeyValid) {
                                    { Text("La clé 'a' doit être première avec 26") }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error
                                )
                            )

                            OutlinedTextField(
                                value = keyB,
                                onValueChange = {
                                    if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                                        keyB = it
                                    }
                                },
                                label = { Text("Clé b (0-25)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = keyB.isNotEmpty() && keyB.toIntOrNull()?.let { it !in 0..25 } ?: false,
                                supportingText = if (keyB.isNotEmpty() && keyB.toIntOrNull()?.let { it !in 0..25 } ?: false) {
                                    { Text("La clé 'b' doit être entre 0 et 25") }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    errorLabelColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                    "Vigenère" -> {
                        OutlinedTextField(
                            value = key,
                            onValueChange = { newKey ->
                                if (newKey.all { it.isLetter() || it.isWhitespace() }) {
                                    key = newKey.uppercase()
                                }
                            },
                            label = { Text("Clé (lettres uniquement)") },
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showKey) "Masquer la clé" else "Afficher la clé",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    "Cesar" -> {
                        OutlinedTextField(
                            value = key,
                            onValueChange = { newKey ->
                                if (newKey.isEmpty() || (newKey.toIntOrNull() != null && newKey.toInt() in 0..25)) {
                                    key = newKey
                                }
                            },
                            label = { Text("Décalage (0-25)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    "Transposition" -> {
                        OutlinedTextField(
                            value = key,
                            onValueChange = { newKey ->
                                if (newKey.isEmpty() || newKey.toIntOrNull() != null) {
                                    key = newKey
                                }
                            },
                            label = { Text("Clé (nombre entier positif)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalKey = when (secretKey.algorithm) {
                        "Affine" -> "$keyA:$keyB"
                        else -> key
                    }
                    onConfirm(secretKey, finalKey, description)
                    onDismiss()
                },
                enabled = when (secretKey.algorithm) {
                    "Affine" -> isAffineKeyValid && keyA.isNotEmpty() && keyB.isNotEmpty() && description.isNotEmpty()
                    else -> key.isNotBlank() && description.isNotEmpty()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Mettre à jour")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}