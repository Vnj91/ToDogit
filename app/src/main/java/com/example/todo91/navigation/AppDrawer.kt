package com.example.todo91.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.viewmodel.AuthViewModel
import com.example.todo91.viewmodel.ThemeSetting
import com.example.todo91.viewmodel.ThemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    currentRoute: String?,
    navigateTo: (String) -> Unit,
    closeDrawer: () -> Unit,
    scope: CoroutineScope,
    authViewModel: AuthViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val items = listOf(Screen.Home, Screen.Archive)
    val themeSetting by themeViewModel.themeSetting.collectAsState()

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        items.forEach { screen ->
            NavigationDrawerItem(
                label = { Text(screen.title) },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navigateTo(screen.route)
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Dark Mode Toggle
        Row(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (themeSetting == ThemeSetting.Dark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = "Theme Toggle"
            )
            Spacer(Modifier.width(16.dp))
            Text("Dark Mode")
            Spacer(Modifier.weight(1f))
            Switch(
                checked = themeSetting == ThemeSetting.Dark,
                onCheckedChange = { isChecked ->
                    val newTheme = if (isChecked) ThemeSetting.Dark else ThemeSetting.Light
                    themeViewModel.updateThemeSetting(newTheme)
                }
            )
        }

        // Logout Button
        NavigationDrawerItem(
            label = { Text("Logout") },
            icon = { Icon(Icons.Default.Logout, contentDescription = "Logout") },
            selected = false,
            onClick = {
                scope.launch {
                    authViewModel.signOut()
                    closeDrawer()
                }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}