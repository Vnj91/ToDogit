package com.example.todo91.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Notes", Icons.Default.Home)
    object Archive : Screen("archive", "Archive", Icons.Default.Archive)

    // Use an optional query parameter for the task ID. This is a cleaner way to handle new vs. existing tasks.
    object TaskDetail : Screen("task_detail?taskId={taskId}", "Task Detail", Icons.Default.Home) {
        fun createRoute(taskId: String?): String {
            return if (taskId != null) {
                "task_detail?taskId=$taskId"
            } else {
                "task_detail" // Route for a new task
            }
        }
    }
}
