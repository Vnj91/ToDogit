package com.example.todo91.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.todo91.auth.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppDrawer(
    currentRoute: String?,
    navigateTo: (String) -> Unit,
    closeDrawer: () -> Unit,
    scope: CoroutineScope,
    authViewModel: AuthViewModel = viewModel()
) {
    val items = listOf(
        Screen.Home,
        Screen.Archive,
        Screen.Reminders
    )
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
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Logout") },
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