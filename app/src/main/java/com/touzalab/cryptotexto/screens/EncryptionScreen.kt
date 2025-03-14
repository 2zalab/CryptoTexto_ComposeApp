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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.touzalab.cryptotexto.components.*
import com.touzalab.cryptotexto.ui.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
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

    // Contrôleurs pour gérer le clavier
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // État pour suivre si un champ est en focus
    var isAnyFieldFocused by remember { mutableStateOf(false) }

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
                onBackClick = {
                    // Masquer le clavier lors de la navigation
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    navController.navigateUp()
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    // Gestionnaire pour masquer le clavier lors du clic en dehors des champs
                    .onFocusChanged { focusState ->
                        if (!focusState.hasFocus && isAnyFieldFocused) {
                            keyboardController?.hide()
                            isAnyFieldFocused = false
                        }
                    }
            ) {
                // Composant d'entrée avec gestion du focus
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
                        // Masquer le clavier avant d'afficher le dialog biométrique
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        biometricHelper.showBiometricPrompt(
                            onSuccess = {
                                viewModel.loadSavedKeys(state.selectedAlgorithm)
                                viewModel.showLoadKeyDialog()
                            },
                            onError = { code, message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    isEncryption = true,
                    onFocusChange = { isFocused ->
                        isAnyFieldFocused = isFocused
                        if (isFocused) {
                            // Faire défiler pour rendre le champ visible quand il reçoit le focus
                            // (ceci sera adapté dans le composant CryptoInputFields)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Masquer le clavier lors du clic sur le bouton
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.processCrypto(CryptoOperation.Encrypt)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(5.dp),
                    enabled = when (state.selectedAlgorithm) {
                        "Affine" -> !state.isLoading &&
                                state.message.isNotEmpty() &&
                                state.affineA != 0 &&
                                areCoprimes(state.affineA, 26)
                        "Cesar" -> !state.isLoading &&
                                state.message.isNotEmpty() &&
                                state.key.toIntOrNull() != null
                        else -> !state.isLoading &&
                                state.message.isNotEmpty() &&
                                state.key.isNotEmpty()
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

                // Espace supplémentaire en bas pour assurer que tout le contenu est visible
                // lorsque le clavier est affiché
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        if (showLoadKeyDialog) {
            SavedKeysDialog(
                keys = savedKeys,
                selectedAlgorithm = state.selectedAlgorithm,
                onDismiss = { viewModel.hideLoadKeyDialog() },
                onKeySelected = { viewModel.loadSelectedKey(it) }
            )
        }
    }
}