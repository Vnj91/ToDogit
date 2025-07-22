package com.example.todo91.auth

import android.app.Application
import android.app.Activity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private val _resendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    val resendToken: StateFlow<PhoneAuthProvider.ForceResendingToken?> = _resendToken.asStateFlow()

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("482697711588-m8928ecnjb6ecpqaqfvupdrg14n5mktl.apps.googleusercontent.com") // YOUR WEB CLIENT ID
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)

        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        _authError.value = null
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _authError.value = e.message
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        _authError.value = null
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _authError.value = e.message
            }
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return googleSignInClient
    }

    fun signInWithGoogle(credential: AuthCredential) {
        _authError.value = null
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _authError.value = e.message
            }
        }
    }

    fun startPhoneNumberVerification(activity: Activity, phoneNumber: String) {
        _authError.value = null
        _verificationId.value = null
        _resendToken.value = null

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authError.value = e.message
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _verificationId.value = verificationId
                _resendToken.value = token
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _authError.value = null
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _authError.value = e.message
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            googleSignInClient.signOut().await()
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }
}
