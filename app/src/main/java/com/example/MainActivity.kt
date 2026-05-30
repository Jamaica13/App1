package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseApp.initializeApp(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        val viewModel: AuthViewModel = viewModel()
        val authState by viewModel.authState.collectAsStateWithLifecycle()
        
        if (authState is AuthState.Success) {
          HomeScreen()
        } else {
          Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            OnboardingScreen(modifier = Modifier.padding(innerPadding), viewModel = viewModel)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Français") }
    val languages = listOf("Français", "English", "Español", "日本語")

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val cosmicGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF31103F), Color(0xFF000000))
    )

    // Reset error when user types
    LaunchedEffect(email, password) {
        if (authState is AuthState.Error) {
            viewModel.resetState()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cosmicGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            
            Text(
                text = "MediaVerse",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Votre nouveau gestionnaire pop-culture.",
                fontSize = 16.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Language Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Langue / Language") },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = "Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFC084FC),
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                selectedLanguage = lang
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC084FC),
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                isError = authState is AuthState.Error,
                modifier = Modifier.fillMaxWidth() // Modifier.testTag("email_input") temporarily removed until i confirm import
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFC084FC),
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp),
                isError = authState is AuthState.Error,
                modifier = Modifier.fillMaxWidth()
            )

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            } else if (authState is AuthState.Success) {
                Text(
                    text = (authState as AuthState.Success).message,
                    color = Color.Green,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = { viewModel.signUp(email, password, selectedLanguage) },
                enabled = authState != AuthState.Loading && authState !is AuthState.Success,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC084FC)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Créer mon compte", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

