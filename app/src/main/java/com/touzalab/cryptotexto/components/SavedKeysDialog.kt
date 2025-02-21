package com.touzalab.cryptotexto.components

import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SavedKeysDialog(
    keys: List<SecretKey>,
    onDismiss: () -> Unit,
    onKeySelected: (SecretKey) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clés sauvegardées",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            LazyColumn {
                items(keys) { key ->
                    ListItem(
                        headlineContent = { Text(key.description) },
                        supportingContent = { Text("Clé: ${key.key}") },
                        modifier = Modifier.clickable { onKeySelected(key) }
                    )
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}