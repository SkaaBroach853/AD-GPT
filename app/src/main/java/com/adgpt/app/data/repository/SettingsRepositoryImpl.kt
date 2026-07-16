package com.adgpt.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.adgpt.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private val reduceMotionKey = booleanPreferencesKey("reduce_motion")

    override val reduceMotion: Flow<Boolean> =
        context.settingsDataStore.data.map { preferences -> preferences[reduceMotionKey] ?: false }

    override suspend fun setReduceMotion(enabled: Boolean) {
        context.settingsDataStore.edit { preferences -> preferences[reduceMotionKey] = enabled }
    }
}
