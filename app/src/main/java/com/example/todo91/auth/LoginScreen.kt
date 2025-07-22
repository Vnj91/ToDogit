package com.example.todo91.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.ui.theme.ToDo91Theme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onSignInSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by authViewModel.authError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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
            onSignInSuccess()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    authViewModel.signInWithGoogle(credential)
                }
            } catch (e: ApiException) {
                scope.launch {
                    snackbarHostState.showSnackbar("Google Sign-In failed: ${e.statusCode}")
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Google Sign-In cancelled or failed.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Login") })
        },
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
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.signInWithEmail(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login with Email")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToSignUp) {
                Text("Don't have an account? Sign Up")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val signInIntent = authViewModel.getGoogleSignInClient().signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In with Google")
            }
            // TODO: Add Phone Sign-In UI here later
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    ToDo91Theme {
        LoginScreen(onNavigateToSignUp = {}, onSignInSuccess = {})
    }
}
