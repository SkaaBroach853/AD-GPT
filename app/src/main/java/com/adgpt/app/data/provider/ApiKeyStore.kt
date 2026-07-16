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
    val maskedKey: String
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
        if (_activeKeyId.value == id) _activeKeyId.value = _keys.value.firstOrNull()?.id
    }

    fun select(id: String) {
        if (_keys.value.any { it.id == id }) _activeKeyId.value = id
    }

    fun activeKey(): SavedApiKey? = _keys.value.firstOrNull { it.id == _activeKeyId.value }
}
