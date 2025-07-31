package com.example.todo91.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val items = listOf(
        Screen.Home,
        Screen.Archive
    )
    val themeSetting by themeViewModel.themeSetting.collectAsState()
    var showThemeMenu by remember { mutableStateOf(false) }

    ModalDrawerSheet {
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

        // Theme Selector
        Box {
            NavigationDrawerItem(
                label = { Text("Theme") },
                icon = { Icon(Icons.Default.Contrast, contentDescription = "Theme") },
                badge = { Text(themeSetting.name) },
                selected = false,
                onClick = { showThemeMenu = true },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            DropdownMenu(
                expanded = showThemeMenu,
                onDismissRequest = { showThemeMenu = false }
            ) {
                ThemeSetting.values().forEach { setting ->
                    DropdownMenuItem(
                        text = { Text(setting.name) },
                        onClick = {
                            themeViewModel.updateThemeSetting(setting)
                            showThemeMenu = false
                        }
                    )
                }
            }
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
