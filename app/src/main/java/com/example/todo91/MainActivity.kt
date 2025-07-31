package com.example.todo91

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.example.todo91.viewmodel.TodoViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val todoViewModel: TodoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        handleIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        // Handle deep link from shared note (placeholder)
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data
            if (uri?.scheme == "todo91app" && uri.host == "join") {
                val shareId = uri.getQueryParameter("id")
                if (!shareId.isNullOrBlank()) {
                    // todoViewModel.joinNoteViaShareId(shareId)
                }
            }
        }

        // Handle intent from notification tap
        if (intent.hasExtra("notification_todo_id")) {
            val todoId = intent.getStringExtra("notification_todo_id")
            if (!todoId.isNullOrBlank()) {
                todoViewModel.requestNavigationToTodo(todoId)
            }
            // Clear the extra to prevent re-handling on configuration changes
            intent.removeExtra("notification_todo_id")
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
