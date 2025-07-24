package com.example.todo91.navigation

import androidx.navigation.NavHostController

class NavigationManager(private val navController: NavHostController) {
    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}