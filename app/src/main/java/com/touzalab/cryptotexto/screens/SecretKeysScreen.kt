package com.touzalab.cryptotexto.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
                        // L'utilisateur a cliqué sur "Annuler"
                        navController.navigateUp()
                    }
                    BiometricPrompt.ERROR_USER_CANCELED -> {
                        // L'utilisateur a annulé l'authentification
                        navController.navigateUp()
                    }
                    else -> {
                        // Autres erreurs : afficher un message et retourner
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

        if (!isAuthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Authentification requise",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Veuillez vous authentifier pour accéder à vos clés secrètes",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Clés Secrètes",
                        style = MaterialTheme.typography.headlineSmall
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
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddKeyDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, "Ajouter une clé")
                Spacer(Modifier.width(8.dp))
                Text("Nouvelle clé")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top
        ) {
            if (state.secretKeys.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucune clé secrète",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cliquez sur le bouton + pour ajouter une nouvelle clé",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                    .padding(top = 16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Première rangée avec les boutons d'export
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.exportKeys("pdf")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export PDF")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.exportKeys("txt")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export TXT")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // LazyColumn pour la liste des clés
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.secretKeys) { secretKey ->
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
fun SecretKeyCard(
    secretKey: SecretKey,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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

                Row {
                    IconButton(onClick = onCopy) {
                        Image(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copier",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
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
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            .menuAnchor()
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
                                }
                            )
                        }
                    }
                }

                when (selectedAlgorithm) {
                    "Affine" -> {
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
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                        contentDescription = if (showKey) "Masquer la clé" else "Afficher la clé"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
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
                    "Affine" -> isAffineKeyValid && keyA.isNotEmpty() && keyB.isNotEmpty()
                    else -> selectedAlgorithm.isNotBlank() && key.isNotBlank()
                }
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
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
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Algorithme : ${secretKey.algorithm}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when (secretKey.algorithm) {
                    "Affine" -> {
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
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
                        )
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
                                        contentDescription = if (showKey) "Masquer la clé" else "Afficher la clé"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
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
                    "Affine" -> isAffineKeyValid && keyA.isNotEmpty() && keyB.isNotEmpty()
                    else -> key.isNotBlank()
                }
            ) {
                Text("Modifier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}