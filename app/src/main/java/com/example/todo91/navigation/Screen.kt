package com.example.todo91.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Notes", Icons.Default.Home)
    object Archive : Screen("archive", "Archive", Icons.Default.Archive)
    object Reminders : Screen("reminders", "Reminders", Icons.Default.Notifications)

    object TaskDetail : Screen("task_detail/{taskId}", "Task Detail", Icons.Default.Home) {
        fun createRoute(taskId: String?) = "task_detail/${taskId ?: "null"}"
    }
}