package com.example.todo91

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.todo91.auth.AuthViewModel
import com.example.todo91.auth.LoginScreen
import com.example.todo91.auth.SignupScreen
import com.example.todo91.navigation.AppDrawer
import com.example.todo91.navigation.NavigationManager
import com.example.todo91.ui.theme.ToDo91Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDo91Theme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.currentUser.collectAsState()

                if (authState == null && FirebaseAuth.getInstance().currentUser == null) {
                    AuthNavigation()
                } else {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun AuthNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onSignInSuccess = { /* Triggers recomposition in MainActivity */ }
            )
        }
        composable("signup") {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = { /* Triggers recomposition in MainActivity */ }
            )
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val navigationManager = NavigationManager(navController)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                navigateTo = { route ->
                    navigationManager.navigateTo(route)
                    scope.launch { drawerState.close() }
                },
                closeDrawer = { scope.launch { drawerState.close() } },
                scope = scope
            )
        }
    ) {
        AppNavHost(
            navController = navController,
            drawerState = drawerState,
            scope = scope
        )
    }
}