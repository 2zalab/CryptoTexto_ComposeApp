package com.touzalab.cryptotexto.components

import android.content.ClipData
import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SecretKey(
    val id: String = UUID.randomUUID().toString(),
    val algorithm: String,
    val key: String,
    val description: String
)

data class SecretKeysState(
    val secretKeys: List<SecretKey> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedKeyForEdit: SecretKey? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val showMessage: Boolean = false
)

class SecretKeysViewModel(
    private val dataStore: SecretKeysDataStore,
    private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SecretKeysState())
    val state: StateFlow<SecretKeysState> = _state.asStateFlow()
    val algorithms =  listOf("Cesar", "Vigenère", "Affine", "Transposition")

    init {
        loadSecretKeys()
    }

    private fun loadSecretKeys() {
        viewModelScope.launch {
            try {
                dataStore.secretKeys.collect { keys ->
                    _state.update { it.copy(secretKeys = keys, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun showAddKeyDialog() {
        _state.update { it.copy(showAddDialog = true) }
    }

    fun hideAddKeyDialog() {
        _state.update { it.copy(showAddDialog = false) }
    }

    fun showEditDialog(secretKey: SecretKey) {
        _state.update { it.copy(
            showEditDialog = true,
            selectedKeyForEdit = secretKey
        ) }
    }

    fun hideEditDialog() {
        _state.update { it.copy(
            showEditDialog = false,
            selectedKeyForEdit = null
        ) }
    }

    fun addSecretKey(algorithm: String, key: String, description: String) {
        viewModelScope.launch {
            try {
                val newKey = SecretKey(
                    algorithm = algorithm,
                    key = key,
                    description = description
                )
                val updatedKeys = _state.value.secretKeys + newKey
                dataStore.saveSecretKeys(updatedKeys)
                _state.update { it.copy(
                    secretKeys = updatedKeys,
                    showAddDialog = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun editKey(secretKey: SecretKey, newKey: String, newDescription: String) {
        viewModelScope.launch {
            try {
                val updatedKey = secretKey.copy(
                    key = newKey,
                    description = newDescription
                )
                val updatedKeys = _state.value.secretKeys.map {
                    if (it.id == secretKey.id) updatedKey else it
                }
                dataStore.saveSecretKeys(updatedKeys)
                _state.update { it.copy(secretKeys = updatedKeys) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteKey(secretKey: SecretKey) {
        viewModelScope.launch {
            try {
                val updatedKeys = _state.value.secretKeys - secretKey
                dataStore.saveSecretKeys(updatedKeys)
                _state.update { it.copy(secretKeys = updatedKeys) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun copyKey(secretKey: SecretKey) {
        viewModelScope.launch {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = ClipData.newPlainText("Secret Key", secretKey.key)
                clipboard.setPrimaryClip(clip)

                // Afficher un message de succès temporaire
                _state.update { it.copy(
                    message = "Clé copiée dans le presse-papiers",
                    showMessage = true
                ) }

                // Effacer le message après un délai
                delay(2000)
                _state.update { it.copy(
                    message = null,
                    showMessage = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Erreur lors de la copie : ${e.message}",
                    showMessage = true
                ) }
                delay(2000)
                _state.update { it.copy(
                    error = null,
                    showMessage = false
                ) }
            }
        }
    }
}

// Factory pour créer le ViewModel avec le DataStore
class SecretKeysViewModelFactory(
    private val dataStore: SecretKeysDataStore,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecretKeysViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SecretKeysViewModel(dataStore, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}