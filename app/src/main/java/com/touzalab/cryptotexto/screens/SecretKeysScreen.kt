package com.touzalab.cryptotexto.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.touzalab.cryptotexto.R
import com.touzalab.cryptotexto.components.SecretKeysViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.touzalab.cryptotexto.components.SecretKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretKeysScreen(
    navController: NavController,
    viewModel: SecretKeysViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(

        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Clés Secrètes",
                        style = MaterialTheme.typography.headlineMedium
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
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.secretKeys.forEach { secretKey ->
                    item {
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
    }
}

@Composable
fun SecretKeyCard(
    secretKey: com.touzalab.cryptotexto.components.SecretKey,
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
                            painter = painterResource(id = R.drawable.baseline_content_copy_24),
                            contentDescription = "Copier",
                            colorFilter =tint(MaterialTheme.colorScheme.primary)
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

    // Validation des clés pour l'algorithme Affine
    val isAffineKeyValid = remember(keyA, keyB) {
        if (selectedAlgorithm == "Affine") {
            keyA.toIntOrNull()?.let { a ->
                keyB.toIntOrNull()?.let { b ->
                    // a doit être premier avec 26 et différent de 0
                    a != 0 && b in 0..25 && a.toBigInteger().gcd(26.toBigInteger()).toInt() == 1
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
                // Sélection de l'algorithme
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
                                    // Réinitialiser les champs de clé lors du changement d'algorithme
                                    key = ""
                                    keyA = ""
                                    keyB = ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Champs de clé adaptifs selon l'algorithme
                when (selectedAlgorithm) {
                    "Affine" -> {
                        // Deux champs pour les clés a et b
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

// Le composant de dialogue d'édition
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

    // Initialiser les valeurs en fonction du type d'algorithme
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

    // Validation pour Affine
    val isAffineKeyValid = remember(keyA, keyB) {
        if (secretKey.algorithm == "Affine") {
            keyA.toIntOrNull()?.let { a ->
                keyB.toIntOrNull()?.let { b ->
                    a != 0 && b in 0..25 && a.toBigInteger().gcd(26.toBigInteger()).toInt() == 1
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