package com.touzalab.cryptotexto.components

import android.content.ClipData
import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
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

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()


    sealed class ExportState {
        object Idle : ExportState()
        object Loading : ExportState()
        data class Success(val filePath: String) : ExportState()
        data class Error(val message: String) : ExportState()
    }



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

    fun exportKeys(format: String) {
        viewModelScope.launch {
            try {
                _exportState.value = ExportState.Loading

                val content = buildString {
                    appendLine("Liste des clés secrètes")
                    appendLine("=====================")
                    appendLine()

                    state.value.secretKeys.forEach { key ->
                        appendLine("Algorithme: ${key.algorithm}")
                        appendLine("Description: ${key.description}")
                        appendLine("Clé: ${key.key}")
                        appendLine("---------------------")
                    }
                }

                val fileName = "cles_secretes_${System.currentTimeMillis()}"
                val file = when (format) {
                    "pdf" -> createPdfFile(fileName, content)
                    "txt" -> createTextFile(fileName, content)
                    else -> throw IllegalArgumentException("Format non supporté")
                }

                _exportState.value = ExportState.Success(file.absolutePath)
                _state.update { it.copy(
                    message = "Fichier exporté avec succès",
                    showMessage = true
                ) }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Erreur lors de l'export")
                _state.update { it.copy(
                    error = "Erreur lors de l'export : ${e.message}",
                    showMessage = true
                ) }
            }
        }
    }

    private fun createPdfFile(fileName: String, content: String): File {
        val document = Document()
        val file = File(context.getExternalFilesDir(null), "$fileName.pdf")
        PdfWriter.getInstance(document, FileOutputStream(file))

        document.open()
        document.add(Paragraph(content))
        document.close()

        return file
    }

    private fun createTextFile(fileName: String, content: String): File {
        val file = File(context.getExternalFilesDir(null), "$fileName.txt")
        file.writeText(content)
        return file
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

