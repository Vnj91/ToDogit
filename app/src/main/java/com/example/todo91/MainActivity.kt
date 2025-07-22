package com.example.todo91

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todo91.auth.AuthViewModel // NEW: Import AuthViewModel
import com.example.todo91.auth.LoginScreen // NEW: Import LoginScreen
import com.example.todo91.auth.SignupScreen // NEW: Import SignupScreen
import com.example.todo91.home.HomeScreen
import com.example.todo91.navigation.Routes
import com.example.todo91.taskdetail.TaskDetailScreen
import com.example.todo91.ui.theme.ToDo91Theme
import com.google.firebase.auth.FirebaseAuth // NEW: Import FirebaseAuth for initial check

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDo91Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel() // Get AuthViewModel instance
                    val currentUser by authViewModel.currentUser.collectAsState() // Observe current user

                    // Determine start destination based on authentication status
                    // If currentUser is not null, go to HOME_SCREEN, otherwise to LOGIN_SCREEN
                    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
                        Routes.HOME_SCREEN
                    } else {
                        Routes.LOGIN_SCREEN // NEW: Define a LOGIN_SCREEN route in Routes.kt
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startDestination // Use dynamic start destination
                    ) {
                        // Login Screen
                        composable(Routes.LOGIN_SCREEN) {
                            LoginScreen(
                                onNavigateToSignUp = { navController.navigate(Routes.SIGNUP_SCREEN) }, // NEW: Navigate to Signup
                                onSignInSuccess = {
                                    // Pop up to the start destination (LoginScreen) and then navigate to Home
                                    navController.navigate(Routes.HOME_SCREEN) {
                                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true } // Clear auth screens from back stack
                                    }
                                },
                                authViewModel = authViewModel // Pass AuthViewModel
                            )
                        }

                        // Sign Up Screen
                        composable(Routes.SIGNUP_SCREEN) {
                            SignupScreen(
                                onNavigateToLogin = { navController.popBackStack() }, // Go back to Login
                                onSignUpSuccess = {
                                    // Pop up to the start destination (LoginScreen) and then navigate to Home
                                    navController.navigate(Routes.HOME_SCREEN) {
                                        popUpTo(Routes.LOGIN_SCREEN) { inclusive = true } // Clear auth screens from back stack
                                    }
                                },
                                authViewModel = authViewModel // Pass AuthViewModel
                            )
                        }

                        // Home Screen
                        composable(Routes.HOME_SCREEN) {
                            HomeScreen(
                                onNavigateToTaskDetail = { todoId ->
                                    navController.navigate(Routes.TASK_DETAIL_SCREEN_ARG + "/${todoId ?: "null"}")
                                },
                                // TODO: Add signOut functionality here later, e.g., via a menu item
                                // For now, if user signs out, they'll be redirected by authStateListener
                            )
                        }

                        // Task Detail Screen
                        composable(
                            route = Routes.TASK_DETAIL_SCREEN,
                            arguments = listOf(navArgument("taskId") {
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId")
                            TaskDetailScreen(
                                todoId = if (taskId == "null") null else taskId,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ToDo91Theme {
        // Preview for MainActivity now needs to handle the navigation state
        // This preview will show the HomeScreen by default.
        HomeScreen(onNavigateToTaskDetail = {})
    }
}