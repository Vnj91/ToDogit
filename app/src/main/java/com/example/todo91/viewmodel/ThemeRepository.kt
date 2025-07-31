package com.example.todo91.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Define the DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
    }

    val themeSettingFlow: Flow<ThemeSetting> = dataStore.data.map { preferences ->
        ThemeSetting.valueOf(
            preferences[PreferencesKeys.THEME_SETTING] ?: ThemeSetting.System.name
        )
    }

    suspend fun saveThemeSetting(themeSetting: ThemeSetting) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_SETTING] = themeSetting.name
        }
    }
}