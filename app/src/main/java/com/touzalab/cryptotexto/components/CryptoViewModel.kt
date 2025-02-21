package com.touzalab.cryptotexto.components

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CryptoState(
    val message: String = "",
    val selectedAlgorithm: String = "Cesar",
    val key: String = "",
    val result: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSnackbar: Boolean = false,
    val snackbarMessage: String = "",
    val affineA: Int = 0,
    val affineB: Int = 0
)

sealed class CryptoOperation {
    object Encrypt : CryptoOperation()
    object Decrypt : CryptoOperation()
}

class CryptoViewModel(
    private val dataStore: SecretKeysDataStore,
    private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(CryptoState())
    val state: StateFlow<CryptoState> = _state.asStateFlow()

    private val _showLoadKeyDialog = MutableStateFlow(false)
    val showLoadKeyDialog: StateFlow<Boolean> = _showLoadKeyDialog.asStateFlow()

    private val _savedKeys = MutableStateFlow<List<SecretKey>>(emptyList())
    val savedKeys: StateFlow<List<SecretKey>> = _savedKeys.asStateFlow()


    fun updateMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    fun updateAlgorithm(algorithm: String) {
        _state.update { it.copy(selectedAlgorithm = algorithm) }
    }

    fun updateKey(key: String) {
        _state.update { it.copy(key = key) }
    }

    fun updateAffineA(value: Int) {
        _state.update { it.copy(affineA = value) }
    }

    fun updateAffineB(value: Int) {
        _state.update { it.copy(affineB = value) }
    }

    fun clearInputs() {
        _state.update {
            it.copy(
                message = "",
                key = "",
                result = "",
                error = null,
                affineA = 0,
                affineB = 0
            )
        }
    }

    fun processCrypto(operation: CryptoOperation) {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.message.isBlank()) {
                showError("Veuillez saisir un message")
                return@launch
            }

            // Vérification spécifique pour chaque algorithme
            when (currentState.selectedAlgorithm) {
                "Cesar" -> if (currentState.key.toIntOrNull() == null) {
                    showError("La clé doit être un nombre pour César")
                    return@launch
                }
                "Affine" -> if (!areCoprimes(currentState.affineA, 26)) {
                    showError("Le paramètre 'a' doit être premier avec 26")
                    return@launch
                }
                else -> if (currentState.key.isBlank()) {
                    showError("Veuillez saisir une clé")
                    return@launch
                }
            }

            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = when (operation) {
                    is CryptoOperation.Encrypt -> encrypt(
                        currentState.message,
                        currentState.key,
                        currentState.selectedAlgorithm,
                        currentState.affineA,
                        currentState.affineB
                    )
                    is CryptoOperation.Decrypt -> decrypt(
                        currentState.message,
                        currentState.key,
                        currentState.selectedAlgorithm,
                        currentState.affineA,
                        currentState.affineB
                    )
                }
                _state.update {
                    it.copy(
                        result = result,
                        isLoading = false,
                        showSnackbar = true,
                        snackbarMessage = if (operation == CryptoOperation.Encrypt)
                            "Message crypté avec succès!"
                        else
                            "Message décrypté avec succès!"
                    )
                }
            } catch (e: Exception) {
                showError("Erreur: ${e.message}")
            }
        }
    }

    private fun encrypt(
        message: String,
        key: String,
        algorithm: String,
        affineA: Int,
        affineB: Int
    ): String {
        return when (algorithm) {
            "Cesar" -> cesarEncrypt(message, key.toIntOrNull()
                ?: throw IllegalArgumentException("La clé doit être un nombre"))
            "Vigenère" -> vigenereEncrypt(message, key)
            "XOR" -> xorEncrypt(message, key)
            "Affine" -> affineEncrypt(message, affineA, affineB)
            "Transposition" -> transpositionEncrypt(message, key)
            else -> throw IllegalArgumentException("Algorithme non supporté")
        }
    }

    private fun decrypt(
        message: String,
        key: String,
        algorithm: String,
        affineA: Int,
        affineB: Int
    ): String {
        return when (algorithm) {
            "Cesar" -> cesarDecrypt(message, key.toIntOrNull()
                ?: throw IllegalArgumentException("La clé doit être un nombre"))
            "Vigenère" -> vigenereDecrypt(message, key)
            "XOR" -> xorDecrypt(message, key)
            "Affine" -> affineDecrypt(message, affineA, affineB)
            "Transposition" -> transpositionDecrypt(message, key)
            else -> throw IllegalArgumentException("Algorithme non supporté")
        }
    }

    private fun showError(message: String) {
        _state.update {
            it.copy(
                error = message,
                isLoading = false,
                showSnackbar = true,
                snackbarMessage = message
            )
        }
    }

    fun dismissSnackbar() {
        _state.update { it.copy(showSnackbar = false) }
    }

    fun showSnackbar(message: String) {
        _state.update { it.copy(
            showSnackbar = true,
            snackbarMessage = message
        )}
    }

    //chargement de clé
    fun showLoadKeyDialog() {
        viewModelScope.launch {
            _showLoadKeyDialog.value = true
        }
    }

    fun hideLoadKeyDialog() {
        _showLoadKeyDialog.value = false
    }

    fun loadSavedKeys(algorithm: String) {
        viewModelScope.launch {
            val keys = dataStore.secretKeys.first()
            _savedKeys.value = keys.filter { it.algorithm == algorithm }
        }
    }

    fun loadSelectedKey(secretKey: SecretKey) {
        when (secretKey.algorithm) {
            "Affine" -> {
                val (a, b) = secretKey.key.split(":")
                updateAffineA(a.toInt())
                updateAffineB(b.toInt())
            }
            else -> updateKey(secretKey.key)
        }
        hideLoadKeyDialog()
    }
}

class CryptoViewModelFactory(
    private val dataStore: SecretKeysDataStore,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CryptoViewModel(dataStore, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}