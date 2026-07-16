package com.adgpt.app.data.provider

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.apiKeyDataStore by preferencesDataStore(name = "api_keys")

@Serializable
data class SavedApiKey(
    val id: String,
    val providerId: String,
    val providerName: String,
    val model: String,
    val key: String,
    val maskedKey: String,
    val label: String = providerName,
    val enabled: Boolean = true
)

@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val savedKeysKey = stringPreferencesKey("saved_keys")
    private val activeKeyIdKey = stringPreferencesKey("active_key_id")

    private val _keys = MutableStateFlow<List<SavedApiKey>>(emptyList())
    val keys: StateFlow<List<SavedApiKey>> = _keys

    private val _activeKeyId = MutableStateFlow<String?>(null)
    val activeKeyId: StateFlow<String?> = _activeKeyId

    init {
        scope.launch {
            val preferences = context.apiKeyDataStore.data.first()
            val savedKeys = preferences[savedKeysKey]
                ?.let { runCatching { json.decodeFromString<List<SavedApiKey>>(it) }.getOrNull() }
                .orEmpty()
            _keys.value = savedKeys
            _activeKeyId.value = preferences[activeKeyIdKey]
                ?.takeIf { id -> savedKeys.any { it.id == id && it.enabled } }
                ?: savedKeys.firstOrNull { it.enabled }?.id
        }
    }

    fun addOrUpdate(key: SavedApiKey) {
        _keys.update { keys -> keys.filterNot { it.id == key.id } + key }
        _activeKeyId.value = key.id
        persist()
    }

    fun remove(id: String) {
        _keys.update { keys -> keys.filterNot { it.id == id } }
        if (_activeKeyId.value == id) {
            _activeKeyId.value = _keys.value.firstOrNull { it.enabled }?.id
        }
        persist()
    }

    fun select(id: String) {
        if (_keys.value.any { it.id == id && it.enabled }) {
            _activeKeyId.value = id
            persist()
        }
    }

    fun rename(id: String, label: String) {
        _keys.update { keys ->
            keys.map { if (it.id == id) it.copy(label = label.ifBlank { it.providerName }) else it }
        }
        persist()
    }

    fun setEnabled(id: String, enabled: Boolean) {
        _keys.update { keys -> keys.map { if (it.id == id) it.copy(enabled = enabled) else it } }
        if (!enabled && _activeKeyId.value == id) {
            _activeKeyId.value = _keys.value.firstOrNull { it.enabled }?.id
        }
        if (enabled && _activeKeyId.value == null) {
            _activeKeyId.value = id
        }
        persist()
    }

    fun activeKey(): SavedApiKey? = _keys.value.firstOrNull { it.id == _activeKeyId.value && it.enabled }

    private fun persist() {
        val keysSnapshot = _keys.value
        val activeKeyIdSnapshot = _activeKeyId.value
        scope.launch {
            context.apiKeyDataStore.edit { preferences ->
                preferences[savedKeysKey] = json.encodeToString(keysSnapshot)
                if (activeKeyIdSnapshot == null) {
                    preferences.remove(activeKeyIdKey)
                } else {
                    preferences[activeKeyIdKey] = activeKeyIdSnapshot
                }
            }
        }
    }
}
