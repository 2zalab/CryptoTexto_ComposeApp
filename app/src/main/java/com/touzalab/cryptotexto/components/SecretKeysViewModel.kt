package com.touzalab.cryptotexto.components

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import org.apache.commons.io.IOUtils
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.touzalab.cryptotexto.R
import java.io.ByteArrayOutputStream

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

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }

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
                delay(4000)
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
        // Vérifier les permissions d'abord
        checkStoragePermission()
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


                // Créer un dossier dédié dans les Documents publics
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val appDir = File(documentsDir, "CryptoTexto")
                if (!appDir.exists()) {
                    appDir.mkdirs()
                }

                val fileName = "cles_secretes_${System.currentTimeMillis()}"
                val file = when (format) {
                    "pdf" -> createPdfFile(appDir, fileName, content)
                    "txt" -> createTextFile(appDir, fileName, content)
                    else -> throw IllegalArgumentException("Format non supporté")
                }

                // Assurez-vous que le fichier est scanné par le MediaScanner
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    null
                ) { path, uri ->
                    Log.d("MediaScanner", "Scanned $path: $uri")
                }

                // Partager le fichier créé
                shareFile(file, format)

                _exportState.value = ExportState.Success(file.absolutePath)
                _state.update { it.copy(
                    message = "Fichier exporté avec succès et enregistré dans Documents/CryptoTexto",
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

    private fun shareFile(file: File, format: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "com.touzalab.cryptotexto.fileprovider", // Utilisez exactement cette chaîne
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when (format) {
                    "pdf" -> "application/pdf"
                    "txt" -> "text/plain"
                    else -> "*/*"
                }
                putExtra(Intent.EXTRA_SUBJECT, "Mes Clés Secrètes CryptoTexto")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(intent, "Partager via")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            // Log l'erreur pour débogage
            Log.e("ShareFile", "Error sharing file: ${e.message}", e)

            // Mettre à jour l'état pour afficher l'erreur à l'utilisateur
            _state.update { it.copy(
                error = "Erreur lors du partage : ${e.message}",
                showMessage = true
            ) }
        }
    }

    private fun createPdfFile(directory: File, fileName: String, content: String): File {
        val file = File(directory, "$fileName.pdf")

        // Création du document avec marges personnalisées
        val document = Document(PageSize.A4, 36f, 36f, 54f, 36f) // Marges gauche, droite, haut, bas
        val writer = PdfWriter.getInstance(document, FileOutputStream(file))

        // Ajout d'un pied de page avec date
        writer.pageEvent = object : PdfPageEventHelper() {
            override fun onEndPage(writer: PdfWriter, document: Document) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val footerPhrase = Phrase("Document généré le $currentDate - CryptoTexto",
                    Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC, BaseColor.DARK_GRAY))

                // Position en bas au centre
                ColumnText.showTextAligned(
                    writer.directContent,
                    Element.ALIGN_CENTER,
                    footerPhrase,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 20,
                    0f
                )
            }
        }

        document.open()

        // Ajout du logo depuis drawable
        try {
            // Récupérer le logo depuis les drawable resources
            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.logo_crypto) // Remplacez "logo" par le nom de votre fichier
            val bitmap = (logoDrawable as BitmapDrawable).bitmap

            // Convertir le Bitmap en ByteArray pour iText
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val logoBytes = stream.toByteArray()

            // Créer l'image pour le PDF
            val logo = Image.getInstance(logoBytes)

            // Redimensionner le logo
            logo.scaleToFit(100f, 100f)
            logo.alignment = Element.ALIGN_CENTER

            document.add(logo)
            document.add(Paragraph(" ")) // Espace après le logo
        } catch (e: Exception) {
            Log.e("PDF Export", "Erreur lors du chargement du logo: ${e.message}")
            // Continuer sans logo si une erreur survient
        }

        // Ajout du logo
        /*
        try {
            val logoStream = context.assets.open("logo_crypto.png") // Assurez-vous que votre logo est dans les assets
            val logo = Image.getInstance(IOUtils.toByteArray(logoStream))

            // Redimensionner le logo
            logo.scaleToFit(100f, 100f)
            logo.alignment = Element.ALIGN_CENTER

            document.add(logo)
            document.add(Paragraph(" ")) // Espace après le logo
        } catch (e: Exception) {
            Log.e("PDF Export", "Erreur lors du chargement du logo: ${e.message}")
            // Continuer sans logo si une erreur survient
        }
        */

        // Ajout du titre
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(128, 0, 32)) // Rouge foncé
        val title = Paragraph("Mes Clés Secrètes", titleFont)
        title.alignment = Element.ALIGN_CENTER
        title.spacingAfter = 20f
        document.add(title)

        // Ajout d'une ligne de séparation
        val lineSeparator = LineSeparator(1f, 100f, BaseColor(200, 200, 200), Element.ALIGN_CENTER, -5f)
        document.add(Chunk(lineSeparator))
        document.add(Paragraph(" ")) // Espace après la ligne

        // Formatage du contenu
        val secretKeys = state.value.secretKeys

        secretKeys.forEachIndexed { index, key ->
            // Titre de la clé
            val keyTitleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor(128, 0, 32))
            val keyTitle = Paragraph("${index + 1}. ${key.algorithm}", keyTitleFont)
            keyTitle.spacingBefore = 15f
            document.add(keyTitle)

            // Table pour les détails
            val table = PdfPTable(2)
            table.widthPercentage = 100f
            table.spacingBefore = 10f
            table.spacingAfter = 15f

            // Définir les largeurs relatives des colonnes
            table.setWidths(floatArrayOf(1f, 3f))

            // Style pour les entêtes
            val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val cellFont = Font(Font.FontFamily.HELVETICA, 10f)

            // Ligne pour la description
            addTableRow(table, "Description:", key.description, headerFont, cellFont)

            // Ligne pour la clé
            val maskedKey = when (key.algorithm) {
                "Affine" -> {
                    val parts = key.key.split(":")
                    if (parts.size == 2) "a=${parts[0]}, b=${parts[1]}" else key.key
                }
                else -> key.key
            }
            addTableRow(table, "Clé:", maskedKey, headerFont, cellFont)

            document.add(table)

            // Ajouter une ligne de séparation si ce n'est pas la dernière clé
            if (index < secretKeys.size - 1) {
                document.add(Chunk(LineSeparator(0.5f, 80f, BaseColor(230, 230, 230), Element.ALIGN_CENTER, -5f)))
            }
        }

        document.close()
        return file
    }

    private fun addTableRow(table: PdfPTable, header: String, value: String, headerFont: Font, valueFont: Font) {
        val headerCell = PdfPCell(Phrase(header, headerFont))
        headerCell.backgroundColor = BaseColor(245, 245, 245)
        headerCell.paddingBottom = 8f
        headerCell.paddingTop = 8f
        headerCell.paddingLeft = 5f

        val valueCell = PdfPCell(Phrase(value, valueFont))
        valueCell.paddingBottom = 8f
        valueCell.paddingTop = 8f
        valueCell.paddingLeft = 5f

        table.addCell(headerCell)
        table.addCell(valueCell)
    }

    private fun createTextFile(directory: File, fileName: String, content: String): File {
        val file = File(directory, "$fileName.txt")
        file.writeText(content)
        return file
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Demander la permission
                // Cela doit être fait dans une activité
                val activity = context as? Activity
                activity?.let {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        STORAGE_PERMISSION_CODE
                    )
                }
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

