package com.adgpt.app.data.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

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
class ApiKeyStore @Inject constructor() {
    private val _keys = MutableStateFlow<List<SavedApiKey>>(emptyList())
    val keys: StateFlow<List<SavedApiKey>> = _keys

    private val _activeKeyId = MutableStateFlow<String?>(null)
    val activeKeyId: StateFlow<String?> = _activeKeyId

    fun addOrUpdate(key: SavedApiKey) {
        _keys.update { keys -> keys.filterNot { it.id == key.id } + key }
        _activeKeyId.value = key.id
    }

    fun remove(id: String) {
        _keys.update { keys -> keys.filterNot { it.id == id } }
        if (_activeKeyId.value == id) {
            _activeKeyId.value = _keys.value.firstOrNull { it.enabled }?.id
        }
    }

    fun select(id: String) {
        if (_keys.value.any { it.id == id && it.enabled }) _activeKeyId.value = id
    }

    fun rename(id: String, label: String) {
        _keys.update { keys ->
            keys.map { if (it.id == id) it.copy(label = label.ifBlank { it.providerName }) else it }
        }
    }

    fun setEnabled(id: String, enabled: Boolean) {
        _keys.update { keys -> keys.map { if (it.id == id) it.copy(enabled = enabled) else it } }
        if (!enabled && _activeKeyId.value == id) {
            _activeKeyId.value = _keys.value.firstOrNull { it.enabled }?.id
        }
    }

    fun activeKey(): SavedApiKey? = _keys.value.firstOrNull { it.id == _activeKeyId.value && it.enabled }
}
