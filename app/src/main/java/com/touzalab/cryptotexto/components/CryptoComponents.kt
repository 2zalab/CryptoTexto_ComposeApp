package com.touzalab.cryptotexto.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touzalab.cryptotexto.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoInputFields(
    message: String,
    onMessageChange: (String) -> Unit,
    selectedAlgorithm: String,
    onAlgorithmChange: (String) -> Unit,
    key: String,
    onKeyChange: (String) -> Unit,
    algorithms: List<String>,
    affineA: Int ,
    onAffineAChange: (Int) -> Unit,
    affineB: Int,
    onAffineBChange: (Int) -> Unit,
    onLoadSavedKey: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Message input
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            label = { Text("Message à traiter") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Algorithm dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedAlgorithm,
                onValueChange = {},
                readOnly = true,
                label = { Text("Algorithme") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
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
                algorithms.forEach { algorithm ->
                    DropdownMenuItem(
                        text = { Text(algorithm) },
                        onClick = {
                            onAlgorithmChange(algorithm)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key input dynamic selon l'algorithme
        CryptoKeyInput(
            selectedAlgorithm = selectedAlgorithm,
            key = key,
            onKeyChange = onKeyChange,
            affineA = affineA,
            onAffineAChange = onAffineAChange,
            affineB = affineB,
            onAffineBChange = onAffineBChange
        )
        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onLoadSavedKey,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Charger une clé sauvegardée")
            }
        }
    }
}


@Composable
fun CryptoKeyInput(
    selectedAlgorithm: String,
    key: String,
    onKeyChange: (String) -> Unit,
    affineA: Int,
    onAffineAChange: (Int) -> Unit,
    affineB: Int,
    onAffineBChange: (Int) -> Unit
) {
    when (selectedAlgorithm) {
        "Affine" -> {
            // Entrées pour le chiffrement affine
            Column {
                OutlinedTextField(
                    value = affineA.toString(),
                    onValueChange = {
                            newValue ->
                        // Permet la saisie vide temporaire pour faciliter l'édition
                        if (newValue.isEmpty()) {
                            onAffineAChange(0)
                        } else {
                            // Essaie de convertir en nombre
                            newValue.toIntOrNull()?.let { onAffineAChange(it) }
                        }
                    },
                    label = { Text("Paramètre a (doit être premier avec 26)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = affineB.toString(),
                    onValueChange = {
                            newValue ->
                        // Permet la saisie vide temporaire pour faciliter l'édition
                        if (newValue.isEmpty()) {
                            onAffineBChange(0)
                        } else {
                            // Essaie de convertir en nombre
                            newValue.toIntOrNull()?.let { onAffineBChange(it) }
                        }
                    },
                    label = { Text("Paramètre b") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        else -> {
            // Champ de clé standard pour les autres algorithmes
            var showPassword by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = key,
                onValueChange = onKeyChange,
                label = { Text(when (selectedAlgorithm) {
                    "Cesar" -> "Clé (nombre)"
                    "Vigenère" -> "Mot clé"
                    "XOR" -> "Clé"
                    "Transposition" -> "Mot clé pour la permutation"
                    else -> "Clé"
                }) },
                visualTransformation = if (showPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "Masquer la clé" else "Afficher la clé",
                            modifier = Modifier.size(24.dp)
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
    }
}

@Composable
fun CryptoResultCard(
    title: String,
    result: String
) {
    if (result.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 24.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun CryptoActionButtons(
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copy button
        OutlinedIconButton(
            onClick = onCopy,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copier",
                tint =MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Share button
        OutlinedIconButton(
            onClick = onShare,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Partager",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Clear button
        OutlinedIconButton(
            onClick = onClear,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Clear,
                contentDescription = "Effacer",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}