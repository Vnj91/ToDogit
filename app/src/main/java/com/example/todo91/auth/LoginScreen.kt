package com.example.todo91.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todo91.ui.theme.ToDo91Theme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
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
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    val authError by authViewModel.authError.collectAsState()
    val verificationId by authViewModel.verificationId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as Activity
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
        topBar = { TopAppBar(title = { Text("Login") }) },
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
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.signInWithEmail(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Login with Email")
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onNavigateToSignUp, enabled = !isLoading) {
                Text("Don't have an account? Sign Up")
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val signInIntent = authViewModel.getGoogleSignInClient().signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Sign In with Google")
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "OR",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = verificationId == null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (e.g., +919876543210)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (phoneNumber.isNotBlank()) {
                                authViewModel.startPhoneNumberVerification(activity, phoneNumber)
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Phone number cannot be empty") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Send Verification Code")
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = verificationId != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = { Text("Verification Code") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            verificationId?.let { id ->
                                val credential = PhoneAuthProvider.getCredential(id, verificationCode)
                                authViewModel.signInWithPhoneAuthCredential(credential)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && verificationCode.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Sign In with Code")
                        }
                    }
                }
            }
        }
    }
}