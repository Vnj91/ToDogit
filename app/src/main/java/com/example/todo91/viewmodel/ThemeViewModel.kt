package com.example.todo91.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Enum remains the same
enum class ThemeSetting {
    System, Light, Dark
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val themeRepository = ThemeRepository(application)

    val themeSetting: StateFlow<ThemeSetting> = themeRepository.themeSettingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.System
        )

    fun updateThemeSetting(setting: ThemeSetting) {
        viewModelScope.launch {
            themeRepository.saveThemeSetting(setting)
        }
    }
}