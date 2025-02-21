package com.touzalab.cryptotexto.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.touzalab.cryptotexto.R
import com.touzalab.cryptotexto.components.BiometricHelper
import com.touzalab.cryptotexto.components.CryptoActionButtons
import com.touzalab.cryptotexto.components.CryptoInputFields
import com.touzalab.cryptotexto.components.CryptoOperation
import com.touzalab.cryptotexto.components.CryptoResultCard
import com.touzalab.cryptotexto.components.CryptoTopBar
import com.touzalab.cryptotexto.components.CryptoViewModel
import com.touzalab.cryptotexto.components.CryptoViewModelFactory
import com.touzalab.cryptotexto.components.SavedKeysDialog
import com.touzalab.cryptotexto.components.SecretKeysDataStore
import com.touzalab.cryptotexto.components.areCoprimes
import com.touzalab.cryptotexto.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecryptionScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val dataStore = remember { SecretKeysDataStore(context) }
    val viewModel: CryptoViewModel = viewModel(
        factory = CryptoViewModelFactory(dataStore, context)
    )
    val state by viewModel.state.collectAsState()
    val algorithms = remember { listOf("Cesar", "Vigenère", "Affine", "Transposition") }
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val showLoadKeyDialog by viewModel.showLoadKeyDialog.collectAsState()
    val savedKeys by viewModel.savedKeys.collectAsState()

    val activity = remember {
        when (context) {
            is FragmentActivity -> context
            else -> throw IllegalStateException("L'activité doit être une FragmentActivity")
        }
    }
    val biometricHelper = remember { BiometricHelper(activity) }

    LaunchedEffect(state.showSnackbar) {
        if (state.showSnackbar) {
            snackbarHostState.showSnackbar(
                message = state.snackbarMessage,
                actionLabel = "OK",
                duration = SnackbarDuration.Short // 5 secondes
            )
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Décryptage") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    actionColor = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Champ message à décrypter
            /*
            OutlinedTextField(
                value = state.message,
                onValueChange = { viewModel.updateMessage(it) },
                label = { Text("Message à décrypter") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            */

            Spacer(modifier = Modifier.height(16.dp))

            CryptoInputFields(
                message = state.message,
                onMessageChange = { viewModel.updateMessage(it) },
                selectedAlgorithm = state.selectedAlgorithm,
                onAlgorithmChange = { viewModel.updateAlgorithm(it) },
                key = state.key,
                onKeyChange = { viewModel.updateKey(it) },
                algorithms = algorithms,
                affineA = state.affineA,
                onAffineAChange = { viewModel.updateAffineA(it) },
                affineB = state.affineB,
                onAffineBChange = { viewModel.updateAffineB(it) },
                onLoadSavedKey = {
                    biometricHelper.showBiometricPrompt(
                        onSuccess = {
                            viewModel.loadSavedKeys(state.selectedAlgorithm)
                            viewModel.showLoadKeyDialog()
                        },
                        onError = { code, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )

            // Bouton de décryptage
            Button(
                onClick = { viewModel.processCrypto(CryptoOperation.Decrypt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(5.dp),
                enabled = when (state.selectedAlgorithm) {
                    "Affine" -> !state.isLoading &&
                            state.message.isNotEmpty() &&
                            state.affineA != 0 &&
                            areCoprimes(state.affineA, 26)  // Vérifie que 'a' est valide pour Affine
                    "Cesar" -> !state.isLoading &&
                            state.message.isNotEmpty() &&
                            state.key.toIntOrNull() != null  // Vérifie que la clé est un nombre valide
                    else -> !state.isLoading &&
                            state.message.isNotEmpty() &&
                            state.key.isNotEmpty()  // Pour Vigenère, XOR et Transposition
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint=MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lancer le décryptage")
                    }
                }
            }

            // Affichage du résultat
            if (state.result.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Message décrypté",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.result,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Boutons d'action avec les snackbars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("decrypted_message", state.result)
                        clipboard.setPrimaryClip(clip)
                        viewModel.showSnackbar("Message copié dans le presse-papiers")
                    },
                    modifier = Modifier.size(48.dp),
                    enabled = state.result.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copier",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedIconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, state.result)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Partager via"))
                    },
                    modifier = Modifier.size(48.dp),
                    enabled = state.result.isNotEmpty()
                ) {
                    Image(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedIconButton(
                    onClick = {
                        viewModel.clearInputs()
                        viewModel.showSnackbar("Contenu effacé")
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Effacer le contenu",
                        tint=MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        // Snackbar pour les messages
        if (state.showSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.dismissSnackbar() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(state.snackbarMessage)
            }
        }

        if (showLoadKeyDialog) {
            SavedKeysDialog(
                keys = savedKeys,
                onDismiss = { viewModel.hideLoadKeyDialog() },
                onKeySelected = { viewModel.loadSelectedKey(it) }
            )
        }
    }
}