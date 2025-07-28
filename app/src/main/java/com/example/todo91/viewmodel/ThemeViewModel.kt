package com.example.todo91.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Enum to represent the available theme options
enum class ThemeSetting {
    System, Light, Dark
}

class ThemeViewModel : ViewModel() {
    private val _themeSetting = MutableStateFlow(ThemeSetting.System)
    val themeSetting: StateFlow<ThemeSetting> = _themeSetting.asStateFlow()

    fun updateThemeSetting(setting: ThemeSetting) {
        _themeSetting.value = setting
    }
}