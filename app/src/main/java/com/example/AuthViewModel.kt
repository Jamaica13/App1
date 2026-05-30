package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signUp(email: String, pass: String, selectedLanguage: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Format d'email invalide.")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("Le mot de passe doit contenir au moins 6 caractères.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Create User
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                
                if (user != null) {
                    // 2. Init Firestore Document
                    val userDoc = hashMapOf(
                        "uid" to user.uid,
                        "email" to email,
                        "language" to selectedLanguage,
                        "createdAt" to System.currentTimeMillis(),
                        "preferences" to emptyMap<String, Any>(),
                        "stats" to emptyMap<String, Any>()
                    )
                    
                    firestore.collection("users").document(user.uid).set(userDoc).await()
                    _authState.value = AuthState.Success("Compte créé avec succès !")
                } else {
                    _authState.value = AuthState.Error("Erreur lors de la création du compte.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Une erreur est survenue")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val error: String) : AuthState()
}
