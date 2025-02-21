package com.touzalab.cryptotexto.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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
fun EncryptionScreen(
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
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        topBar = {
            CryptoTopBar(
                title = "Cryptage",
                onBackClick = { navController.navigateUp() }
            )
        },
        //snackbarHost = { SnackbarHost(snackbarHostState) }
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,  // Couleur de fond primary
                    contentColor = Color.White,  // Texte en blanc
                    actionColor = Color.White,   // Bouton "OK" en blanc
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

            Button(
                onClick = { viewModel.processCrypto(CryptoOperation.Encrypt) },
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
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Lancer le cryptage")
                }
            }

            CryptoResultCard(
                title = "Message crypté",
                result = state.result
            )

            CryptoActionButtons(
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("encrypted_message", state.result)
                    clipboard.setPrimaryClip(clip)
                    viewModel.showSnackbar("Message copié dans le presse-papiers")
                },
                onShare = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, state.result)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Partager via"))
                },
                onClear = {
                    viewModel.clearInputs()
                    viewModel.showSnackbar("Contenu effacé")
                }
            )
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