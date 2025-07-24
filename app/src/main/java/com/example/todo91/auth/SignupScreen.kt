package com.example.todo91.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val authError by authViewModel.authError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isLoading by authViewModel.isLoading.collectAsState()

    LaunchedEffect(authError) {
        authError?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                authViewModel.clearAuthError()
            }
        }
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onSignUpSuccess()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sign Up") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (password.length < 6) {
                        scope.launch { snackbarHostState.showSnackbar("Password must be at least 6 characters") }
                        return@Button
                    }
                    if (password == confirmPassword) {
                        authViewModel.signUpWithEmail(email, password)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign Up with Email")
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
                Text("Already have an account? Login")
            }
        }
    }
}