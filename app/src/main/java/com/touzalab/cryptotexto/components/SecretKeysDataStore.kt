package com.touzalab.cryptotexto.components

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secret_keys")

class SecretKeysDataStore(private val context: Context) {
    private val gson = Gson()
    private val secretKeysKey = stringPreferencesKey("secret_keys")

    val secretKeys: Flow<List<SecretKey>> = context.dataStore.data.map { preferences ->
        val json = preferences[secretKeysKey] ?: "[]"
        val type = object : TypeToken<List<SecretKey>>() {}.type
        gson.fromJson(json, type)
    }

    suspend fun saveSecretKeys(secretKeys: List<SecretKey>) {
        context.dataStore.edit { preferences ->
            preferences[secretKeysKey] = gson.toJson(secretKeys)
        }
    }
}