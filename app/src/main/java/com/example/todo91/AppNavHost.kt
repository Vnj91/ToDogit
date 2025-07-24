package com.example.todo91

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todo91.archive.ArchiveScreen
import com.example.todo91.home.HomeScreen
import com.example.todo91.navigation.Screen
import com.example.todo91.reminders.RemindersScreen
import com.example.todo91.taskdetail.TaskDetailScreen
import kotlinx.coroutines.CoroutineScope

@Composable
fun AppNavHost(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                drawerState = drawerState,
                scope = scope,
                onNavigateToTaskDetail = { todoId ->
                    navController.navigate(Screen.TaskDetail.createRoute(todoId))
                }
            )
        }
        composable(Screen.Archive.route) {
            ArchiveScreen(
                drawerState = drawerState,
                scope = scope,
                onNavigateToTaskDetail = { todoId ->
                    navController.navigate(Screen.TaskDetail.createRoute(todoId))
                }
            )
        }
        composable(Screen.Reminders.route) {
            RemindersScreen()
        }
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskDetailScreen(
                todoId = taskId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}