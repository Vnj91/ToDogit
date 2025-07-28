package com.example.todo91

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.todo91.auth.LoginScreen
import com.example.todo91.auth.SignupScreen
import com.example.todo91.navigation.AppDrawer
import com.example.todo91.navigation.NavigationManager
import com.example.todo91.ui.theme.ToDo91Theme
import com.example.todo91.viewmodel.AuthViewModel
import com.example.todo91.viewmodel.ThemeSetting
import com.example.todo91.viewmodel.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeSetting by themeViewModel.themeSetting.collectAsState()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.System -> isSystemInDarkTheme()
                ThemeSetting.Light -> false
                ThemeSetting.Dark -> true
            }

            ToDo91Theme(darkTheme = useDarkTheme) {
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
                onSignInSuccess = { /* Triggers recomposition */ }
            )
        }
        composable("signup") {
            SignupScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onSignUpSuccess = { /* Triggers recomposition */ }
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